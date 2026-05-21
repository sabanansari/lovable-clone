package com.ansari.projects.lovable_clone.services;

import com.ansari.projects.lovable_clone.dto.chat.StreamResponse;
import reactor.core.publisher.Flux;

public interface AIGenerationService {
    Flux<StreamResponse> streamResponse(String message, Long projectId);
}
