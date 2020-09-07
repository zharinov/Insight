package com.meemaw.auth.sso.session.model;

import com.meemaw.auth.sso.model.SsoSession;
import javax.ws.rs.core.NewCookie;
import lombok.Value;

@Value
public class DirectLoginResult implements LoginResult<Boolean> {

  String sessionId;

  @Override
  public Boolean getData() {
    return true;
  }

  @Override
  public NewCookie cookie(String cookieDomain) {
    return SsoSession.cookie(sessionId, cookieDomain);
  }
}