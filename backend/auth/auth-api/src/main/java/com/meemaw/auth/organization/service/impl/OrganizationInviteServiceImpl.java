package com.meemaw.auth.organization.service.impl;

import com.meemaw.auth.core.MailingConstants;
import com.meemaw.auth.organization.datasource.OrganizationDatasource;
import com.meemaw.auth.organization.datasource.OrganizationInviteDatasource;
import com.meemaw.auth.organization.model.Organization;
import com.meemaw.auth.organization.model.TeamInviteTemplateData;
import com.meemaw.auth.organization.model.dto.InviteAcceptDTO;
import com.meemaw.auth.organization.model.dto.InviteCreateDTO;
import com.meemaw.auth.organization.model.dto.TeamInviteDTO;
import com.meemaw.auth.organization.service.OrganizationInviteService;
import com.meemaw.auth.sso.model.InsightPrincipal;
import com.meemaw.auth.user.datasource.UserDatasource;
import com.meemaw.auth.user.model.AuthUser;
import com.meemaw.auth.user.model.UserRole;
import com.meemaw.shared.rest.response.Boom;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.reactive.ReactiveMailer;
import io.quarkus.qute.Template;
import io.quarkus.qute.api.ResourcePath;
import io.vertx.axle.pgclient.PgPool;
import io.vertx.axle.sqlclient.Transaction;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response.Status;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.microprofile.opentracing.Traced;

@ApplicationScoped
@Slf4j
public class OrganizationInviteServiceImpl implements OrganizationInviteService {

  @Inject ReactiveMailer mailer;
  @Inject PgPool pgPool;
  @Inject UserDatasource userDatasource;
  @Inject OrganizationDatasource datasource;
  @Inject OrganizationInviteDatasource inviteDatasource;

  @ResourcePath("organization/team_invite")
  Template teamInviteTemplate;

  @Override
  @Traced
  public CompletionStage<TeamInviteDTO> createTeamInvite(
      InviteCreateDTO invite, InsightPrincipal principal, String acceptInviteURL) {
    AuthUser authUser = principal.user();
    String organizationId = principal.user().getOrganizationId();
    log.info(
        "[AUTH]: Creating team invite for user: {} organizationId: {}",
        invite.getEmail(),
        organizationId);

    return pgPool
        .begin()
        .thenCompose(
            transaction ->
                datasource
                    .findOrganization(organizationId, transaction)
                    .thenCompose(
                        maybeOrganization -> {
                          Organization organization =
                              maybeOrganization.orElseThrow(() -> Boom.notFound().exception());

                          TeamInviteTemplateData teamInviteTemplateData =
                              new TeamInviteTemplateData(
                                  invite.getEmail(),
                                  invite.getRole(),
                                  authUser.getFullName(),
                                  organization.getName());

                          return createTeamInvite(
                              organizationId,
                              authUser.getId(),
                              teamInviteTemplateData,
                              acceptInviteURL,
                              transaction);
                        }));
  }

  private CompletionStage<TeamInviteDTO> createTeamInvite(
      String organizationId,
      UUID creatorId,
      TeamInviteTemplateData teamInviteTemplateData,
      String acceptInviteURL,
      Transaction transaction) {
    return inviteDatasource
        .create(organizationId, creatorId, teamInviteTemplateData, transaction)
        .thenCompose(
            teamInvite ->
                sendInviteEmail(teamInvite.getToken(), teamInviteTemplateData, acceptInviteURL)
                    .exceptionally(
                        throwable -> {
                          transaction.rollback();
                          log.error(
                              "[AUTH]: Failed to send team invite email to user: {} token: {}",
                              teamInvite.getEmail(),
                              teamInvite.getToken());
                          throw Boom.serverError()
                              .message("Failed to send invite email")
                              .exception();
                        })
                    .thenCompose(x -> transaction.commit())
                    .thenApply(x -> teamInvite));
  }

  @Override
  @Traced
  public CompletionStage<Boolean> acceptTeamInvite(UUID token, InviteAcceptDTO inviteAccept) {
    log.info("[AUTH]: Accepting team invite attempt for token: {}", token);
    return pgPool
        .begin()
        .thenCompose(transaction -> acceptTeamInvite(token, inviteAccept, transaction));
  }

  private CompletionStage<Boolean> acceptTeamInvite(
      UUID token, InviteAcceptDTO inviteAccept, Transaction transaction) {

    return inviteDatasource
        .get(token, transaction)
        .thenApply(
            maybeTeamInvite -> {
              TeamInviteDTO teamInvite =
                  maybeTeamInvite.orElseThrow(
                      () -> Boom.badRequest().message("Team invite does not exist.").exception());

              log.info(
                  "[AUTH]: Accepting team invite attempt for user: {} organizationId: {}",
                  teamInvite.getEmail(),
                  teamInvite.getOrganizationId());

              if (teamInvite.hasExpired()) {
                log.info(
                    "[AUTH]: Team invite has expired for user: {} organizationId: {}",
                    teamInvite.getEmail(),
                    teamInvite.getOrganizationId());
                throw Boom.badRequest().message("Team invite expired").exception();
              }
              return teamInvite;
            })
        .thenCompose(
            teamInvite -> {
              UserRole role = teamInvite.getRole();
              return userDatasource.createUser(
                  teamInvite.getEmail(),
                  inviteAccept.getFullName(),
                  teamInvite.getOrganizationId(),
                  role,
                  transaction);
            })
        .thenCompose(
            user ->
                inviteDatasource.deleteAll(user.getEmail(), user.getOrganizationId(), transaction))
        .thenCompose(deleted -> transaction.commit().thenApply(x -> deleted));
  }

  @Override
  @Traced
  public CompletionStage<Void> sendTeamInvite(
      UUID token, InsightPrincipal principal, String acceptInviteURL) {
    String creatorFullName = principal.user().getFullName();

    return inviteDatasource
        .getWithOrganization(token)
        .thenCompose(
            maybeTeamInviteAndOrganization -> {
              Pair<TeamInviteDTO, Organization> teamInviteOrganization =
                  maybeTeamInviteAndOrganization.orElseThrow(() -> Boom.notFound().exception());
              Organization organization = teamInviteOrganization.getRight();
              TeamInviteDTO teamInvite = teamInviteOrganization.getLeft();

              return sendInviteEmail(
                  token,
                  new TeamInviteTemplateData(
                      teamInvite.getEmail(),
                      teamInvite.getRole(),
                      creatorFullName,
                      organization.getName()),
                  acceptInviteURL);
            });
  }

  @Override
  @Traced
  public CompletionStage<Boolean> deleteTeamInvite(UUID token, InsightPrincipal principal) {
    log.info("[AUTH]: Delete team invite attempt for token: {}", token);
    return inviteDatasource
        .delete(token)
        .thenApply(
            deleted -> {
              if (!deleted) {
                throw Boom.status(Status.NOT_FOUND).exception();
              }
              log.info("[AUTH]: Team invite deleted for token: {}", token);
              return true;
            });
  }

  @Override
  @Traced
  public CompletionStage<List<TeamInviteDTO>> listTeamInvites(InsightPrincipal principal) {
    String organizationId = principal.user().getOrganizationId();
    log.debug("[AUTH]: List team invites for organizationId: {}", organizationId);
    return inviteDatasource.find(organizationId);
  }

  private CompletionStage<Void> sendInviteEmail(
      UUID token, TeamInviteTemplateData templateData, String acceptInviteURL) {
    String subject = "You've been invited to Insight";
    log.info(
        "[AUTH]: Sending team invite email to user: {} token: {}",
        templateData.getRecipientEmail(),
        token);

    return teamInviteTemplate
        .data("creator_full_name", templateData.getCreatorFullName())
        .data("token", token)
        .data("role", templateData.getRecipientRole())
        .data("company", templateData.getOrganizationName())
        .data("acceptInviteURL", acceptInviteURL)
        .renderAsync()
        .thenCompose(
            html ->
                mailer
                    .send(
                        Mail.withHtml(templateData.getRecipientEmail(), subject, html)
                            .setFrom(MailingConstants.FROM_SUPPORT))
                    .subscribeAsCompletionStage());
  }
}