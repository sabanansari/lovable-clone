package com.ansari.projects.lovable_clone.mapper;

import com.ansari.projects.lovable_clone.dto.subscription.PlanResponse;
import com.ansari.projects.lovable_clone.dto.subscription.SubscriptionResponse;
import com.ansari.projects.lovable_clone.entities.Plan;
import com.ansari.projects.lovable_clone.entities.Subscription;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper {

    SubscriptionResponse toSubscriptionResponse(Subscription subscription);

    PlanResponse toPlanResponse(Plan plan);
}
