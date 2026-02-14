package com.ansari.projects.lovable_clone.services;

import reactor.core.publisher.Flux;

public interface AIGenerationService {
    Flux<String> streamResponse(String message, Long projectId);
}
