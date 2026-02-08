package com.ansari.projects.lovable_clone.services.impl;

import com.ansari.projects.lovable_clone.dto.subscription.SubscriptionResponse;
import com.ansari.projects.lovable_clone.entities.Plan;
import com.ansari.projects.lovable_clone.entities.Subscription;
import com.ansari.projects.lovable_clone.entities.User;
import com.ansari.projects.lovable_clone.enums.SubscriptionStatus;
import com.ansari.projects.lovable_clone.error.ResourceNotFoundException;
import com.ansari.projects.lovable_clone.mapper.SubscriptionMapper;
import com.ansari.projects.lovable_clone.repository.PlanRepository;
import com.ansari.projects.lovable_clone.repository.ProjectMemberRepository;
import com.ansari.projects.lovable_clone.repository.SubscriptionRepository;
import com.ansari.projects.lovable_clone.repository.UserRepository;
import com.ansari.projects.lovable_clone.security.AuthUtil;
import com.ansari.projects.lovable_clone.services.SubscriptionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {

    private final AuthUtil authUtil;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionMapper subscriptionMapper;
    private final PlanRepository planRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final Integer ALLOWED_FREE_TIER_PROJECTS = 1;

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
    @Transactional
    public void updateSubscription(String gatewaySubscriptionId, SubscriptionStatus status, Instant periodStart, Instant periodEnd, Boolean cancelAtPeriodEnd, Long planId) {
        Subscription subscription = getSubscription(gatewaySubscriptionId);

        boolean hasSubscriptionUpdated = false;

        if(status !=null && status != subscription.getStatus()) {
            log.info("Updating subscription status for gateway ID {} from {} to {}", gatewaySubscriptionId, subscription.getStatus(), status);
            subscription.setStatus(status);
            hasSubscriptionUpdated = true;
        }

        if(periodStart != null && !periodStart.equals(subscription.getCurrentPeriodStart())){
            subscription.setCurrentPeriodStart(periodStart);
            hasSubscriptionUpdated = true;
        }

        if(periodEnd != null && !periodEnd.equals(subscription.getCurrentPeriodEnd())){
            subscription.setCurrentPeriodEnd(periodEnd);
            hasSubscriptionUpdated = true;
        }

        if(cancelAtPeriodEnd != null && !cancelAtPeriodEnd.equals(subscription.getCancelAtPeriodEnd())){
            subscription.setCancelAtPeriodEnd(cancelAtPeriodEnd);
            hasSubscriptionUpdated = true;
        }

        if(planId != null && !planId.equals(subscription.getPlan().getId())){
            subscription.setPlan(getPlan(planId));
            hasSubscriptionUpdated = true;
        }

        if(hasSubscriptionUpdated){
            log.info("Subscription with gateway ID {} has been updated", gatewaySubscriptionId);
            subscriptionRepository.save(subscription);
        }


    }

    @Override
    public void cancelSubscription(String gatewaySubscriptionId) {

        Subscription subscription = getSubscription(gatewaySubscriptionId);
        subscription.setStatus(SubscriptionStatus.CANCELED);
        subscriptionRepository.save(subscription);
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
    public void markSubscriptionAsPastDue(String gatewaySubscriptionId) {

        Subscription subscription = getSubscription(gatewaySubscriptionId);

        if(subscription.getStatus()== SubscriptionStatus.PAST_DUE){
            log.debug("Subscription with gateway ID {} is already marked as PAST_DUE", gatewaySubscriptionId);
            return;
        }

        subscription.setStatus(SubscriptionStatus.PAST_DUE);
        subscriptionRepository.save(subscription);

        // Send email notification
    }

    @Override
    public boolean canCreateProject() {
        Long userId = authUtil.getCurrentUserId();

        SubscriptionResponse currentSubscription = getCurrentSubscription();

        int countOfOwnedProjects = projectMemberRepository.countProjectOwnedByUser(userId);

        if(currentSubscription.plan() == null){
            return countOfOwnedProjects < ALLOWED_FREE_TIER_PROJECTS;
        }

        return currentSubscription.plan().maxProjects() > countOfOwnedProjects;
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
