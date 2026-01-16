package com.ansari.projects.lovable_clone.services.impl;

import com.ansari.projects.lovable_clone.dto.subscription.CheckoutRequest;
import com.ansari.projects.lovable_clone.dto.subscription.CheckoutResponse;
import com.ansari.projects.lovable_clone.dto.subscription.PortalResponse;
import com.ansari.projects.lovable_clone.dto.subscription.SubscriptionResponse;
import com.ansari.projects.lovable_clone.services.SubscriptionService;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {
    @Override
    public SubscriptionResponse getCurrentSubscription(Long userId) {
        return null;
    }

    @Override
    public CheckoutResponse createCheckoutSessionUrl(CheckoutRequest request, Long userId) {
        return null;
    }

    @Override
    public PortalResponse openCustomerPortal(Long userId) {
        return null;
    }
}
