package com.ansari.projects.lovable_clone.dto.subscription;

public record UsageTodayResponse(
        Integer tokenUsed,
        Integer tokensLimit,
        Integer previewsRunning,
        Integer previewsLimit
) {
}
