package com.ansari.projects.lovable_clone.entities;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UsageLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    User user;
    Project project;

    String action;

    Integer tokensUsed;
    Integer durationMs;

    String metaData; //JSON of (model_used, prompt_used)

    @CreationTimestamp
    Instant createdAt;

}
