package com.meemaw.auth.sso.resource.v1.google;

import com.meemaw.auth.sso.resource.v1.SsoOAuthResource;
import com.meemaw.auth.sso.resource.v1.SsoResource;
import javax.ws.rs.Path;

@Path(SsoGoogleResource.PATH)
public interface SsoGoogleResource extends SsoOAuthResource {

  String PATH = SsoResource.PATH + "/google";

  @Override
  default String getBasePath() {
    return PATH;
  }
}
