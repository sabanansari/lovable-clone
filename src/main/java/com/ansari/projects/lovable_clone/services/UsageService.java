package com.ansari.projects.lovable_clone.services;

import com.ansari.projects.lovable_clone.dto.subscription.PlanLimitResponse;
import com.ansari.projects.lovable_clone.dto.subscription.UsageTodayResponse;

public interface UsageService {
     UsageTodayResponse getTodayUsageOfUser(Long userId);

     PlanLimitResponse getCurrentSubscriptionLimitsOfUser(Long userId);
}
