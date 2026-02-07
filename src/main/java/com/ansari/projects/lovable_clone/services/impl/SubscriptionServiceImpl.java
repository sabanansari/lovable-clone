package com.ansari.projects.lovable_clone.services.impl;

import com.ansari.projects.lovable_clone.dto.subscription.SubscriptionResponse;
import com.ansari.projects.lovable_clone.entities.Plan;
import com.ansari.projects.lovable_clone.entities.Subscription;
import com.ansari.projects.lovable_clone.entities.User;
import com.ansari.projects.lovable_clone.enums.SubscriptionStatus;
import com.ansari.projects.lovable_clone.error.ResourceNotFoundException;
import com.ansari.projects.lovable_clone.mapper.SubscriptionMapper;
import com.ansari.projects.lovable_clone.repository.PlanRepository;
import com.ansari.projects.lovable_clone.repository.SubscriptionRepository;
import com.ansari.projects.lovable_clone.repository.UserRepository;
import com.ansari.projects.lovable_clone.security.AuthUtil;
import com.ansari.projects.lovable_clone.services.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final AuthUtil authUtil;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionMapper subscriptionMapper;
    private final PlanRepository planRepository;
    private final UserRepository userRepository;

    @Override
    public SubscriptionResponse getCurrentSubscription() {
        Long userId = authUtil.getCurrentUserId();
        var currentSub = subscriptionRepository.findByUserIdAndStatusIn(userId, Set.of(SubscriptionStatus.ACTIVE,
                SubscriptionStatus.PAST_DUE, SubscriptionStatus.TRIALING)).orElse(
                        new Subscription()
                );

        return subscriptionMapper.toSubscriptionResponse(currentSub);

    }

    @Override
    public void activateSubscription(Long userId, Long planId, String subscriptionId, String customerId) {

        boolean exists = subscriptionRepository.existsByStripeSubscriptionId(subscriptionId);
        if(exists) {
            return;
        }

        User user = getUser(userId);
        Plan plan = getPlan(planId);

        Subscription subscription = Subscription.builder()
                .user(user)
                .plan(plan)
                .status(SubscriptionStatus.INCOMPLETE)
                .stripeSubscriptionId(subscriptionId)
                .build();

        subscriptionRepository.save(subscription);


    }

    @Override
    public void updateSubscription(String id, SubscriptionStatus status, Instant periodStart, Instant periodEnd, Boolean cancelAtPeriodEnd, Long planId) {
        Subscription subscription = subscriptionRepository.findById(Long.parseLong(id)).orElseThrow(
                () -> new RuntimeException("Subscription not found with id: " + id)
        );
        subscription.setStatus(status);
        subscription.setCurrentPeriodStart(periodStart);
        subscription.setCurrentPeriodEnd(periodEnd);
        subscription.setCancelAtPeriodEnd(cancelAtPeriodEnd);
        subscription.setPlan(getPlan(planId));
        subscriptionRepository.save(subscription);
    }

    @Override
    public void cancelSubscription(String id) {

    }

    @Override
    public void renewSubscriptionPeriod(String gatewaySubscriptionId, Instant periodStart, Instant periodEnd) {

        Subscription subscription = getSubscription(gatewaySubscriptionId);

        Instant newStart = periodStart != null ? periodStart : subscription.getCurrentPeriodEnd();
        subscription.setCurrentPeriodStart(newStart);
        subscription.setCurrentPeriodEnd(periodEnd);

        if(subscription.getStatus() == SubscriptionStatus.PAST_DUE || subscription.getStatus() == SubscriptionStatus.INCOMPLETE) {
            subscription.setStatus(SubscriptionStatus.ACTIVE);
        }

        subscriptionRepository.save(subscription);
    }

    @Override
    public void markSubscriptionAsPastDue(String subId) {

    }

    private User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new RuntimeException("User not found with id: " + userId.toString())
        );
    }

    private Plan getPlan(Long planId) {
        return planRepository.findById(planId).orElseThrow(
                () -> new RuntimeException("Plan not found with id: " + planId.toString())
        );
    }

    private Subscription getSubscription(String subscriptionId) {
        return subscriptionRepository.findByStripeSubscriptionId(subscriptionId).orElseThrow(
                () -> new ResourceNotFoundException("Subscription",subscriptionId)
        );
    }
}
