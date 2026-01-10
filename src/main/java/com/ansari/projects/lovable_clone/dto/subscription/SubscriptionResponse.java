package com.ansari.projects.lovable_clone.dto.subscription;

public record SubscriptionResponse(
        PlanResponse plan,
        String status,
        Integer periodEnd,
        Long tokensUsedThisCycle
) {
}
