package com.ansari.projects.lovable_clone.entities;

import com.ansari.projects.lovable_clone.enums.MessageRole;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Entity
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatMessage {

    Long id;
    ChatSession chatSession;

    String content;

    MessageRole role;

    String toolCalls; //JSON Array of Tools Called

    Integer tokensUsed;

    Instant createdAt;
}
