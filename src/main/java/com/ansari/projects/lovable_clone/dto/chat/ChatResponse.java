package com.ansari.projects.lovable_clone.dto.chat;

import com.ansari.projects.lovable_clone.enums.MessageRole;

import java.time.Instant;
import java.util.List;

public record ChatResponse(
        Long id,

        String content,

        MessageRole role,

        List<ChatEventResponse> events,

        Integer tokensUsed,

        Instant createdAt
) {


}
