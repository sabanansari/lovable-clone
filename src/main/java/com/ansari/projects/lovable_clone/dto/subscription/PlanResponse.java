package com.ansari.projects.lovable_clone.dto.subscription;

public record PlanResponse(

        Long id,

        String name,

        Integer maxProjects,
        Integer maxTokenPerDay,
        Boolean unlimitedAi,
        String price

) {
}
