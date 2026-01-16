package com.ansari.projects.lovable_clone.services.impl;

import com.ansari.projects.lovable_clone.dto.subscription.PlanLimitResponse;
import com.ansari.projects.lovable_clone.dto.subscription.UsageTodayResponse;
import com.ansari.projects.lovable_clone.services.UsageService;
import org.springframework.stereotype.Service;

@Service
public class UsageServiceImpl implements UsageService {
    @Override
    public UsageTodayResponse getTodayUsageOfUser(Long userId) {
        return null;
    }

    @Override
    public PlanLimitResponse getCurrentSubscriptionLimitsOfUser(Long userId) {
        return null;
    }
}
