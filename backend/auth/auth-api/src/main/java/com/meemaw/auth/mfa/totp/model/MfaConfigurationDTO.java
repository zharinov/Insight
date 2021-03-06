package com.meemaw.auth.mfa.totp.model;

import com.meemaw.auth.mfa.MfaMethod;
import com.meemaw.auth.mfa.model.MfaConfiguration;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Value;

@Value
public class MfaConfigurationDTO implements MfaConfiguration {

  String secret;
  OffsetDateTime createdAt;
  UUID userId;

  @Override
  public MfaMethod getMethod() {
    return MfaMethod.TOTP;
  }
}
