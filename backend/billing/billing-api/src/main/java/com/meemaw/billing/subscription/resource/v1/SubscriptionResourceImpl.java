package com.meemaw.billing.subscription.resource.v1;

import com.meemaw.auth.sso.session.model.InsightPrincipal;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.billing.service.BillingService;
import com.meemaw.billing.subscription.model.dto.CreateSubscriptionDTO;
import com.meemaw.shared.rest.response.Boom;
import com.meemaw.shared.rest.response.DataResponse;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import io.vertx.core.http.HttpServerRequest;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Slf4j
public class SubscriptionResourceImpl implements SubscriptionResource {

  @Inject InsightPrincipal insightPrincipal;
  @Inject BillingService billingService;
  @Context HttpServerRequest request;

  @ConfigProperty(name = "billing.stripe.webhook_secret")
  String stripeWebhookSecret;

  @Override
  public CompletionStage<Response> webhook(String body, String signature) {
    try {
      Event event = Webhook.constructEvent(body, signature, stripeWebhookSecret);
      return billingService.processEvent(event).thenApply(ignored -> Response.noContent().build());
    } catch (SignatureVerificationException ex) {
      throw Boom.badRequest().message(ex.getMessage()).exception(ex);
    }
  }

  @Override
  public CompletionStage<Response> create(CreateSubscriptionDTO body) {
    AuthUser user = insightPrincipal.user();
    return billingService.createSubscription(body, user).thenApply(DataResponse::created);
  }

  @Override
  public CompletionStage<Response> get() {
    String organizationId = insightPrincipal.user().getOrganizationId();
    return billingService.getSubscription(organizationId).thenApply(DataResponse::ok);
  }
}