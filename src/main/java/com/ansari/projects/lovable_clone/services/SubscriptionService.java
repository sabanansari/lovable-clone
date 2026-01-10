package com.ansari.projects.lovable_clone.services;

import com.ansari.projects.lovable_clone.dto.subscription.CheckoutRequest;
import com.ansari.projects.lovable_clone.dto.subscription.CheckoutResponse;
import com.ansari.projects.lovable_clone.dto.subscription.PortalResponse;
import com.ansari.projects.lovable_clone.dto.subscription.SubscriptionResponse;

public interface SubscriptionService {
    SubscriptionResponse getCurrentSubscription(Long userId);

    CheckoutResponse createCheckoutSessionUrl(CheckoutRequest request, Long userId);

    PortalResponse openCustomerPortal(Long userId);
}
