package com.ansari.projects.lovable_clone.services.impl;

import com.ansari.projects.lovable_clone.dto.subscription.PlanResponse;
import com.ansari.projects.lovable_clone.dto.subscription.SubscriptionResponse;
import com.ansari.projects.lovable_clone.entities.UsageLog;
import com.ansari.projects.lovable_clone.repository.UsageLogRepository;
import com.ansari.projects.lovable_clone.security.AuthUtil;
import com.ansari.projects.lovable_clone.services.SubscriptionService;
import com.ansari.projects.lovable_clone.services.UsageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class UsageServiceImpl implements UsageService {

    private final UsageLogRepository usageLogRepository;
    private final AuthUtil authUtil;
    private final SubscriptionService subscriptionService;

    @Override
    public void recordTokenUsage(Long userId, int actualTokens) {
        LocalDate today = LocalDate.now();

        UsageLog todayLog = usageLogRepository.findByUserIdAndDate(userId, today)
                .orElseGet(() -> createNewDailyLog(userId, today));

        todayLog.setTokensUsed(todayLog.getTokensUsed() + actualTokens);
        usageLogRepository.save(todayLog);
    }

    @Override
    public void checkDailyTokensUsage() {

        Long userId = authUtil.getCurrentUserId();
        SubscriptionResponse subscriptionResponse = subscriptionService.getCurrentSubscription();
        PlanResponse plan = subscriptionResponse.plan();

        LocalDate today = LocalDate.now();

        UsageLog todayLog = usageLogRepository.findByUserIdAndDate(userId, today)
                .orElseGet(() -> createNewDailyLog(userId, today));

        int currentUsage = todayLog.getTokensUsed();
        int limit = plan.maxTokenPerDay();

        if(currentUsage >= limit){
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Daily token usage limit exceeded. Please upgrade your plan for more usage.");
        }

    }

    private UsageLog createNewDailyLog(Long userId, LocalDate date){
        UsageLog newLog = UsageLog.builder()
                .userId(userId)
                .date(date)
                .tokensUsed(0)
                .build();
        return usageLogRepository.save(newLog);

    }
}
