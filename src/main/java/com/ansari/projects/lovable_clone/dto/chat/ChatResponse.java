package com.ansari.projects.lovable_clone.dto.chat;

import com.ansari.projects.lovable_clone.entities.ChatEvent;
import com.ansari.projects.lovable_clone.entities.ChatSession;
import com.ansari.projects.lovable_clone.enums.MessageRole;

import java.time.Instant;
import java.util.List;

public record ChatResponse(
        Long id,

        ChatSession chatSession,

        String content,

        MessageRole role,

        List<ChatEvent> events,

        Integer tokensUsed,

        Instant createdAt
) {


}
