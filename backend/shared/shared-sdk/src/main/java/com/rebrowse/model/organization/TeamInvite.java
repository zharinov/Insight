package com.rebrowse.model.organization;

import com.rebrowse.model.user.UserRole;
import com.rebrowse.net.ApiResource;
import com.rebrowse.net.RequestMethod;
import com.rebrowse.net.RequestOptions;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class TeamInvite {

  UUID token;
  String email;
  String organizationId;
  UserRole role;
  UUID creator;
  OffsetDateTime createdAt;
  OffsetDateTime expiresAt;
  boolean valid;

  public static CompletionStage<TeamInvite> retrieve(UUID token) {
    return retrieve(token, null);
  }

  public static CompletionStage<TeamInvite> retrieve(UUID token, RequestOptions options) {
    String url = String.format("/v1/organization/invites/%s", token);
    return ApiResource.request(RequestMethod.GET, url, TeamInvite.class, options);
  }

  public static CompletionStage<TeamInvite> create(TeamInviteCreateParams params) {
    return create(params, null);
  }

  public static CompletionStage<TeamInvite> create(
      TeamInviteCreateParams params, RequestOptions options) {
    return ApiResource.request(
        RequestMethod.POST, "/v1/organization/invites", params, TeamInvite.class, options);
  }

  public static CompletionStage<Void> accept(UUID token, TeamInviteAcceptParams params) {
    return accept(token, params, null);
  }

  public static CompletionStage<Void> accept(
      UUID token, TeamInviteAcceptParams params, RequestOptions options) {
    String url = String.format("/v1/organization/invites/%s/accept", token);
    return ApiResource.request(RequestMethod.POST, url, params, Void.class, options);
  }

  public CompletionStage<Void> accept(TeamInviteAcceptParams params) {
    return accept(token, params);
  }

  public CompletionStage<Void> accept(TeamInviteAcceptParams params, RequestOptions options) {
    return accept(token, params, options);
  }
}
