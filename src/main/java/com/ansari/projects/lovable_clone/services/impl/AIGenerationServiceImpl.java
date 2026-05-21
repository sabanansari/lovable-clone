package com.ansari.projects.lovable_clone.services.impl;

import com.ansari.projects.lovable_clone.dto.chat.StreamResponse;
import com.ansari.projects.lovable_clone.entities.*;
import com.ansari.projects.lovable_clone.enums.ChatEventType;
import com.ansari.projects.lovable_clone.enums.MessageRole;
import com.ansari.projects.lovable_clone.error.ResourceNotFoundException;
import com.ansari.projects.lovable_clone.llm.LlmResponseParser;
import com.ansari.projects.lovable_clone.llm.PromptUtils;
import com.ansari.projects.lovable_clone.llm.advisors.FileTreeContextAdvisor;
import com.ansari.projects.lovable_clone.llm.tools.CodeGenerationTools;
import com.ansari.projects.lovable_clone.repository.*;
import com.ansari.projects.lovable_clone.security.AuthUtil;
import com.ansari.projects.lovable_clone.services.AIGenerationService;
import com.ansari.projects.lovable_clone.services.ProjectFileService;
import com.ansari.projects.lovable_clone.services.UsageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIGenerationServiceImpl implements AIGenerationService {

    private final ChatClient chatClient;
    private final AuthUtil authUtil;
    private final ProjectFileService projectFileService;
    private final FileTreeContextAdvisor fileTreeContextAdvisor;
    private final ChatSessionRepository chatSessionRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final LlmResponseParser llmResponseParser;
    private final ChatEventRepository chatEventRepository;
    private final UsageService usageService;


    private static final Pattern FILE_TAG_PATTERN = Pattern.compile("<file path=\"([^\"]+)\">(.*?)</file>",Pattern.DOTALL);

    @Override
    @PreAuthorize("@security.canEditProject(#projectId)")
    public Flux<StreamResponse> streamResponse(String message, Long projectId) {
        log.info("Inside streamResponse for projectId: {}", projectId);
       // usageService.checkDailyTokensUsage();
        Long userId = authUtil.getCurrentUserId();


        ChatSession chatSession = createChatSessionIfNotExists(projectId, userId);


        Map<String,Object> advisorParams = Map.of("projectId",projectId,"userId",userId);

        StringBuilder fullResponseBuffer = new StringBuilder();

        CodeGenerationTools codeGenerationTools = new CodeGenerationTools(projectFileService,projectId);

        AtomicReference<Long> startTime = new AtomicReference<>(System.currentTimeMillis());
        AtomicReference<Long> endTime = new AtomicReference<>(0L);
        AtomicReference<Usage> usageRef = new AtomicReference<>();

        return chatClient.prompt()
                .system(PromptUtils.CODE_GENERATION_SYSTEM_PROMPT)
                .user(message)
                .tools(codeGenerationTools)
                .advisors(advisorSpec -> {
                           advisorSpec.params(advisorParams);
                           advisorSpec.advisors(fileTreeContextAdvisor);
                        }

                )
                .stream()
                .chatResponse()
                .doOnNext(response ->{

                    String content = response.getResult().getOutput().getText();

                    if(content != null && !content.isEmpty() && endTime.get() == 0){
                        endTime.set(System.currentTimeMillis());
                    }

                    if(response.getMetadata().getUsage() !=null){
                        log.info("Token usage so far: {}", response.getMetadata().getUsage().toString());
                        usageRef.set(response.getMetadata().getUsage());
                    }
                    fullResponseBuffer.append(content);
                })
                .doOnComplete(() -> {
                    Schedulers.boundedElastic().schedule(()->{
                    //    parseandSaveFiles(fullResponseBuffer.toString(),projectId);
                        long duration = (endTime.get() - startTime.get()) / 1000 ;
                        finalizeChats(message,chatSession,fullResponseBuffer.toString(),projectId,duration,usageRef.get(),userId);
                    });
                })
                .doOnError(error -> log.error("Error during streaming for projectId: {}", projectId, error))
                .map(
                        response -> {
                            String text = response.getResult().getOutput().getText();
                            return new StreamResponse(text != null ? text : "");

                        }
                );

    }

    private void finalizeChats(String userMessage, ChatSession chatSession, String fullText, Long projectId,Long duration, Usage usage,Long userId){

        if(usage !=null){
            int totalTokens = usage.getTotalTokens();
            usageService.recordTokenUsage(userId,totalTokens);

        }
        //Save the user message
        chatMessageRepository.save(
                ChatMessage.builder()
                        .chatSession(chatSession)
                        .role(MessageRole.USER)
                        .content(userMessage)
                        .tokensUsed(usage.getPromptTokens())
                        .build()
        );

        //Save the assistant message
        ChatMessage assistantChatMessage = ChatMessage.builder()
                .role(MessageRole.ASSISTANT)
                .content("Assistant message here...")
                .chatSession(chatSession)
                .tokensUsed(usage.getCompletionTokens())
                .build();

        assistantChatMessage = chatMessageRepository.save(assistantChatMessage);

        List<ChatEvent> chatEventList = llmResponseParser.parseChatEvents(fullText, assistantChatMessage);

        chatEventList.addFirst(ChatEvent.builder()
                        .type(ChatEventType.THOUGHT)
                        .content("Thought for "+duration+"s")
                        .chatMessage(assistantChatMessage)
                        .sequenceOrder(0)
                        .build());

        chatEventList.stream()
                .filter(e -> e.getType() == ChatEventType.FILE_EDIT)
                .forEach(e -> projectFileService.saveFile(projectId,e.getFilePath(),e.getContent()));

        chatEventRepository.saveAll(chatEventList);
    }

//    private void parseandSaveFiles(String fullResponse, Long projectId) {
////       String dummy =  """
////                <message> I am going to do something boy </message>
////                <file path = "src/App.jsx">
////                    import App from './App.jsx'
////                    ...
////                </file>
////                """;
//        Matcher matcher = FILE_TAG_PATTERN.matcher(fullResponse);
//        while (matcher.find()) {
//            String filePath = matcher.group(1);
//            String fileContent = matcher.group(2).trim();
//            // Save fileContent to database with projectId and filePath
//            log.info("Saving file for projectId: {}, filePath: {}", projectId, filePath);
//            projectFileService.saveFile(projectId,filePath,fileContent);
//        }
//    }

    private ChatSession createChatSessionIfNotExists(Long projectId, Long userId) {
        ChatSessionId chatSessionId = new ChatSessionId(projectId, userId);
        ChatSession chatSession = chatSessionRepository.findById(chatSessionId).orElse(null);
        if(chatSession == null){
           Project project = projectRepository.findById(projectId).orElseThrow(
                   () -> new ResourceNotFoundException("Project",projectId.toString())
           );

           User user = userRepository.findById(userId).orElseThrow(
                   () -> new ResourceNotFoundException("User",userId.toString())
           );

              chatSession = ChatSession.builder()
                     .id(chatSessionId)
                     .project(project)
                     .user(user)
                     .build();

            chatSession = chatSessionRepository.save(chatSession);
        }
        return chatSession;

    }
}
