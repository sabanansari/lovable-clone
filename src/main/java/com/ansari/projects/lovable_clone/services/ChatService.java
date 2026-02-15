package com.ansari.projects.lovable_clone.services;

import com.ansari.projects.lovable_clone.dto.chat.ChatResponse;

import java.util.List;

public interface ChatService {

    List<ChatResponse> getProjectChatHistory(Long projectId);


}
