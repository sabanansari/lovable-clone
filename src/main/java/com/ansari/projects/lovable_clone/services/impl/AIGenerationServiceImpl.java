package com.ansari.projects.lovable_clone.services.impl;

import com.ansari.projects.lovable_clone.llm.PromptUtils;
import com.ansari.projects.lovable_clone.security.AuthUtil;
import com.ansari.projects.lovable_clone.services.AIGenerationService;
import com.ansari.projects.lovable_clone.services.ProjectFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIGenerationServiceImpl implements AIGenerationService {

    private final ChatClient chatClient;
    private final AuthUtil authUtil;
    private final ProjectFileService projectFileService;

    private static final Pattern FILE_TAG_PATTERN = Pattern.compile("<file path=\"([^\"]+)\">(.*?)</file>",Pattern.DOTALL);

    @Override
    @PreAuthorize("@security.canEditProject(#projectId)")
    public Flux<String> streamResponse(String message, Long projectId) {
        Long userId = authUtil.getCurrentUserId();


        createChatSessionIfNotExists(projectId, userId);


        Map<String,Object> advisorParams = Map.of("projectId",projectId,"userId",userId);

        StringBuilder fullResponseBuffer = new StringBuilder();

        return chatClient.prompt()
                .system(PromptUtils.CODE_GENERATION_SYSTEM_PROMPT)
                .user(message)
                .advisors(advisorSpec -> {
                           advisorSpec.params(advisorParams);
                        }

                )
                .stream()
                .chatResponse()
                .doOnNext(response ->{

                    String content = response.getResult().getOutput().getText();
                    log.info("Content recieved: {}", content);
                    fullResponseBuffer.append(content);
                })
                .doOnComplete(() -> {
                    Schedulers.boundedElastic().schedule(()->{
                        parseandSaveFiles(fullResponseBuffer.toString(),projectId);
                    });
                })
                .doOnError(error -> log.error("Error during streaming for projectId: {}", projectId, error))
                .map(response -> Objects.requireNonNull(response.getResult().getOutput().getText()));

    }

    private void parseandSaveFiles(String fullResponse, Long projectId) {
//       String dummy =  """
//                <message> I am going to do something boy </message>
//                <file path = "src/App.jsx">
//                    import App from './App.jsx'
//                    ...
//                </file>
//                """;
        Matcher matcher = FILE_TAG_PATTERN.matcher(fullResponse);
        while (matcher.find()) {
            String filePath = matcher.group(1);
            String fileContent = matcher.group(2).trim();
            // Save fileContent to database with projectId and filePath
            log.info("Saving file for projectId: {}, filePath: {}", projectId, filePath);
            projectFileService.saveFile(projectId,filePath,fileContent);
        }
    }

    private void createChatSessionIfNotExists(Long projectId, Long userId) {
    }
}
