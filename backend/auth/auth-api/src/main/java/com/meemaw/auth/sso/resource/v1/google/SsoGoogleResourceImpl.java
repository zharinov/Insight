package com.meemaw.auth.sso.resource.v1.google;

import com.meemaw.auth.sso.resource.v1.AbstractSsoOAuthResource;
import com.meemaw.auth.sso.service.google.SsoGoogleService;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

public class SsoGoogleResourceImpl extends AbstractSsoOAuthResource implements SsoGoogleResource {

  @Inject SsoGoogleService oauthService;

  @Override
  public Response signIn(String destinationPath) {
    return signIn(oauthService, destinationPath);
  }

  @Override
  public CompletionStage<Response> oauth2callback(String state, String code, String sessionState) {
    return oauth2callback(oauthService, state, sessionState, code);
  }
}
