package com.ansari.projects.lovable_clone.dto.chat;

import com.ansari.projects.lovable_clone.enums.ChatEventType;

public record ChatEventResponse(
        Long id,
        Integer sequenceOrder,
        String content,
        String filePath,
        ChatEventType type,
        String metadata
) {
}
