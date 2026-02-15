package com.ansari.projects.lovable_clone.services.impl;

import com.ansari.projects.lovable_clone.dto.chat.ChatResponse;
import com.ansari.projects.lovable_clone.entities.ChatMessage;
import com.ansari.projects.lovable_clone.entities.ChatSession;
import com.ansari.projects.lovable_clone.entities.ChatSessionId;
import com.ansari.projects.lovable_clone.mapper.ChatMapper;
import com.ansari.projects.lovable_clone.repository.ChatMessageRepository;
import com.ansari.projects.lovable_clone.repository.ChatSessionRepository;
import com.ansari.projects.lovable_clone.security.AuthUtil;
import com.ansari.projects.lovable_clone.services.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final AuthUtil authUtil;
    private final ChatMapper chatMapper;

    @Override
    public List<ChatResponse> getProjectChatHistory(Long projectId) {

        Long userId = authUtil.getCurrentUserId();
        ChatSession chatSession = chatSessionRepository.getReferenceById(new ChatSessionId(projectId, userId));

        List<ChatMessage> chatMessages = chatMessageRepository.findByChatSession(chatSession);
        return chatMapper.fromListOfChatMessage(chatMessages);
    }
}
