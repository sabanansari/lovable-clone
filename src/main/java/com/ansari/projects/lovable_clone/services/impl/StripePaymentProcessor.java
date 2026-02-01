package com.ansari.projects.lovable_clone.services.impl;

import com.ansari.projects.lovable_clone.dto.subscription.CheckoutRequest;
import com.ansari.projects.lovable_clone.dto.subscription.CheckoutResponse;
import com.ansari.projects.lovable_clone.dto.subscription.PortalResponse;
import com.ansari.projects.lovable_clone.entities.Plan;
import com.ansari.projects.lovable_clone.entities.User;
import com.ansari.projects.lovable_clone.error.ResourceNotFoundException;
import com.ansari.projects.lovable_clone.repository.PlanRepository;
import com.ansari.projects.lovable_clone.repository.UserRepository;
import com.ansari.projects.lovable_clone.security.AuthUtil;
import com.ansari.projects.lovable_clone.services.PaymentProcessor;
import com.stripe.exception.StripeException;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripePaymentProcessor implements PaymentProcessor {

    private final AuthUtil authUtil;
    private final PlanRepository planRepository;
    private final UserRepository userRepository;

    @Value("${client.url}")
    private String clientUrl;

    @Override
    public CheckoutResponse createCheckoutSessionUrl(CheckoutRequest request) {

        Plan plan = planRepository.findById(request.planId()).orElseThrow(
                () -> new ResourceNotFoundException("Plan", request.planId().toString()));

        Long userId = authUtil.getCurrentUserId();
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("User", userId.toString()));



        var params = SessionCreateParams.builder()
                .addLineItem(
                        SessionCreateParams.LineItem.builder().setPrice(
                          plan.getStripePriceId()
                        ).setQuantity(1L).build())
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .setSubscriptionData(
                        new SessionCreateParams.SubscriptionData.Builder()
                                .setBillingMode(SessionCreateParams.SubscriptionData.BillingMode.builder()
                                        .setType(SessionCreateParams.SubscriptionData.BillingMode.Type.FLEXIBLE)
                                        .build())
                                .build()
                )
                .setSuccessUrl(clientUrl + "/success.html?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(clientUrl + "/cancel.html")
                .putMetadata("user_id", userId.toString())
                .putMetadata("plan_id", request.planId().toString());

        try {
            String stripeCustomerId = user.getStripeCustomerId();

            if(stripeCustomerId == null || stripeCustomerId.isEmpty()){
                params.setCustomerEmail(user.getUsername());
            } else {
                params.setCustomer(stripeCustomerId);
            }
            Session session = Session.create(params.build());

            return new CheckoutResponse(session.getUrl());
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public PortalResponse openCustomerPortal() {
        return null;
    }

    @Override
    public void handleWebhookEvent(String type, StripeObject stripeObject, Map<String, String> metadata) {
        log.info("type:{}",type);

        switch(type){
            case "checkout.session.completed" -> handleCheckoutSessionCompleted(); //one time of checkout completed
            case "customer.subscription.updated" -> handleCustomerSubscriptionUpdated();
            case "customer.subscription.deleted" -> handleCustomerSubscriptionDeleted();
            case "invoice.paid" -> handleInvoicePaid();
            case "invoice.payment_failed" -> handleInvoicePaymentFailed();
            default -> log.debug("Ignoring event:{}",type);
        }
    }

    private void handleCheckoutSessionCompleted(){

    }

    private void handleCustomerSubscriptionUpdated(){

    }

    private void handleCustomerSubscriptionDeleted(){

    }

    private void handleInvoicePaid(){

    }

    private void handleInvoicePaymentFailed(){

    }
}
