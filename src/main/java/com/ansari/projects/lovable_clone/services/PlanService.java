package com.ansari.projects.lovable_clone.services;

import com.ansari.projects.lovable_clone.dto.subscription.PlanResponse;

import java.util.List;

public interface PlanService {
     List<PlanResponse> getAllActivePlans();
}
