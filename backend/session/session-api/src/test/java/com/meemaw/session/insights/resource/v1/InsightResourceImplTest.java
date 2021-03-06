package com.meemaw.session.insights.resource.v1;

import static com.meemaw.shared.SharedConstants.REBROWSE_ORGANIZATION_ID;
import static com.meemaw.shared.rest.query.AbstractQueryParser.GROUP_BY_PARAM;
import static com.meemaw.shared.rest.query.AbstractQueryParser.LIMIT_PARAM;
import static com.meemaw.shared.rest.query.AbstractQueryParser.SORT_BY_PARAM;
import static com.meemaw.test.matchers.SameJSON.sameJson;
import static com.meemaw.test.setup.RestAssuredUtils.ssoBearerTokenTestCases;
import static com.meemaw.test.setup.RestAssuredUtils.ssoSessionCookieTestCases;
import static io.restassured.RestAssured.given;

import com.meemaw.auth.sso.session.model.SsoSession;
import com.meemaw.location.model.dto.LocationDTO;
import com.meemaw.session.sessions.datasource.SessionDatasource;
import com.meemaw.shared.sql.client.SqlPool;
import com.meemaw.test.setup.ExternalAuthApiProvidedTest;
import com.meemaw.test.testconainers.api.auth.AuthApiTestResource;
import com.meemaw.test.testconainers.pg.PostgresTestResource;
import com.meemaw.useragent.model.UserAgentDTO;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.Method;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.inject.Inject;
import javax.ws.rs.core.HttpHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@QuarkusTest
@Tag("integration")
@QuarkusTestResource(PostgresTestResource.class)
@QuarkusTestResource(AuthApiTestResource.class)
public class InsightResourceImplTest extends ExternalAuthApiProvidedTest {

  private static OffsetDateTime createdAt;
  private static final AtomicBoolean hasBeenSetup = new AtomicBoolean(false);
  private static final String DISTINCT_PATH = String.join("/", InsightsResource.PATH, "distinct");
  private static final String COUNT_PATH = String.join("/", InsightsResource.PATH, "count");

  @Inject SessionDatasource sessionDatasource;
  @Inject SqlPool sqlPool;

  @Test
  public void get_session_insights_count__should_throw__on_unauthorized() {
    ssoSessionCookieTestCases(Method.GET, COUNT_PATH);
    ssoBearerTokenTestCases(Method.GET, COUNT_PATH);
  }

  @Test
  public void get_session_insights_count__should_throw__on_unsupported_fields() {
    String sessionId = authApi().loginWithAdminUser();

    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .queryParam("random", "gte:aba")
        .queryParam("aba", "gtecaba")
        .queryParam(GROUP_BY_PARAM, "another")
        .queryParam(SORT_BY_PARAM, "hehe")
        .queryParam(LIMIT_PARAM, "not_string")
        .get(COUNT_PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"aba\":\"Unexpected field in search query\",\"random\":\"Unexpected field in search query\",\"limit\":\"Number expected\",\"group_by\":{\"another\":\"Unexpected field in group_by query\"},\"sort_by\":{\"hehe\":\"Unexpected field in sort_by query\"}}}}"));
  }

  @Test
  public void get_session_insights_count__should_return_count__on_empty_request() {
    String sessionId = authApi().loginWithAdminUser();
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .queryParam("created_at", String.format("gte:%s", createdAt))
        .get(COUNT_PATH)
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":{\"count\":5}}"));

    String apiKey = authApi().createApiKey(sessionId);
    given()
        .when()
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
        .queryParam("created_at", String.format("gte:%s", createdAt))
        .get(COUNT_PATH)
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":{\"count\":5}}"));
  }

  @Test
  public void get_session_insights_count__should_return_count__on_request_with_filters() {
    String sessionId = authApi().loginWithAdminUser();
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .queryParam("created_at", String.format("gte:%s", createdAt))
        .queryParam("location.city", "eq:Maribor")
        .get(COUNT_PATH)
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":{\"count\":1}}"));
  }

  @Test
  public void get_session_insights_count__should_return_counts__on_group_by_country() {
    String sessionId = authApi().loginWithAdminUser();
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .queryParam("group_by", "location.countryName")
        .queryParam("created_at", String.format("gte:%s", createdAt))
        .get(COUNT_PATH)
        .then()
        .statusCode(200)
        .body(
            sameJson(
                "{\"data\":[{\"count\":1,\"location.countryName\":\"Canada\"},{\"count\":1,\"location.countryName\":\"Croatia\"},{\"count\":2,\"location.countryName\":\"Slovenia\"},{\"count\":1,\"location.countryName\":\"United States\"}]}"));
  }

  @Test
  public void
      get_session_insights_count__should_return_counts__on_group_by_country_and_continent() {
    String sessionId = authApi().loginWithAdminUser();
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .queryParam("group_by", "location.countryName,location.continentName")
        .queryParam("created_at", String.format("gte:%s", createdAt))
        .get(COUNT_PATH)
        .then()
        .statusCode(200)
        .body(
            sameJson(
                "{\"data\":[{\"count\":1,\"location.countryName\":\"Canada\",\"location.continentName\":\"North America\"},{\"count\":1,\"location.countryName\":\"Croatia\",\"location.continentName\":\"Europe\"},{\"count\":2,\"location.countryName\":\"Slovenia\",\"location.continentName\":\"Europe\"},{\"count\":1,\"location.countryName\":\"United States\",\"location.continentName\":\"North America\"}]}"));
  }

  @Test
  public void get_session_insights_count__should_return_counts__on_group_by_device() {
    String sessionId = authApi().loginWithAdminUser();
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .queryParam("group_by", "user_agent.deviceClass")
        .queryParam("created_at", String.format("gte:%s", createdAt))
        .get(COUNT_PATH)
        .then()
        .statusCode(200)
        .body(
            sameJson(
                "{\"data\":[{\"count\":1,\"user_agent.deviceClass\":\"Desktop\"},{\"count\":4,\"user_agent.deviceClass\":\"Phone\"}]}"));
  }

  @Test
  public void get_session_insights_distinct__should_throw__on_unauthorized() {
    ssoSessionCookieTestCases(Method.GET, DISTINCT_PATH);
    ssoBearerTokenTestCases(Method.GET, DISTINCT_PATH);
  }

  @Test
  public void get_session_insights_distinct__should_throw__when_no_columns() {
    String sessionId = authApi().loginWithAdminUser();
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .get(DISTINCT_PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Validation Error\",\"errors\":{\"on\":\"Required\"}}}"));
  }

  @Test
  public void get_session_insights_distinct__should_return_cities() {
    String sessionId = authApi().loginWithAdminUser();
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, sessionId)
        .queryParam("on", "location.city")
        .queryParam("created_at", String.format("gte:%s", createdAt))
        .get(DISTINCT_PATH)
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":[null,\"Maribor\",\"New York\",\"Otawa\",\"Zagreb\"]}"));

    String apiKey = authApi().createApiKey(sessionId);
    given()
        .when()
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
        .queryParam("on", "location.city")
        .queryParam("created_at", String.format("gte:%s", createdAt))
        .get(DISTINCT_PATH)
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":[null,\"Maribor\",\"New York\",\"Otawa\",\"Zagreb\"]}"));
  }

  @Test
  public void get_session_insights_distinct__should_return_continents() {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, authApi().loginWithAdminUser())
        .queryParam("on", "location.continentName")
        .queryParam("created_at", String.format("gte:%s", createdAt))
        .get(DISTINCT_PATH)
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":[\"Europe\",\"North America\"]}"));
  }

  @Test
  public void get_session_insights_distinct__should_return_countries() {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, authApi().loginWithAdminUser())
        .queryParam("on", "location.countryName")
        .queryParam("created_at", String.format("gte:%s", createdAt))
        .get(DISTINCT_PATH)
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":[\"Canada\",\"Croatia\",\"Slovenia\",\"United States\"]}"));
  }

  @Test
  public void get_session_insights_distinct__should_return_regions() {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, authApi().loginWithAdminUser())
        .queryParam("on", "location.regionName")
        .queryParam("created_at", String.format("gte:%s", createdAt))
        .get(DISTINCT_PATH)
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":[null,\"Podravska\",\"Washington\"]}"));
  }

  @Test
  public void get_session_insights_distinct__should_return_browser_name() {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, authApi().loginWithAdminUser())
        .queryParam("on", "user_agent.browserName")
        .queryParam("created_at", String.format("gte:%s", createdAt))
        .get(DISTINCT_PATH)
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":[\"Chrome\"]}"));
  }

  @Test
  public void get_session_insights_distinct__should_return_operating_system_name() {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, authApi().loginWithAdminUser())
        .queryParam("on", "user_agent.operatingSystemName")
        .queryParam("created_at", String.format("gte:%s", createdAt))
        .get(DISTINCT_PATH)
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":[\"Mac OS X\"]}"));
  }

  @Test
  public void get_session_insights_distinct__should_return_device_class() {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, authApi().loginWithAdminUser())
        .queryParam("on", "user_agent.deviceClass")
        .queryParam("created_at", String.format("gte:%s", createdAt))
        .get(DISTINCT_PATH)
        .then()
        .statusCode(200)
        .body(sameJson("{\"data\":[\"Desktop\",\"Phone\"]}"));
  }

  @Test
  public void get_session_insights_distinct__should_throw__when_unexpected_fields() {
    given()
        .when()
        .cookie(SsoSession.COOKIE_NAME, authApi().loginWithAdminUser())
        .queryParam("on", "random")
        .queryParam("created_at", String.format("gte:%s", createdAt))
        .get(DISTINCT_PATH)
        .then()
        .statusCode(400)
        .body(
            sameJson(
                "{\"error\":{\"statusCode\":400,\"reason\":\"Bad Request\",\"message\":\"Bad Request\",\"errors\":{\"random\":\"Unexpected field\"}}}"));
  }

  @BeforeEach
  void init() {
    if (hasBeenSetup.getAndSet(true)) {
      return;
    }

    UUID firstSessionId = UUID.randomUUID();

    sqlPool
        .beginTransaction()
        .thenCompose(
            transaction ->
                CompletableFuture.allOf(
                        sessionDatasource
                            .createSession(
                                firstSessionId,
                                UUID.randomUUID(),
                                REBROWSE_ORGANIZATION_ID,
                                LocationDTO.builder()
                                    .city("New York")
                                    .countryName("United States")
                                    .continentName("North America")
                                    .regionName("Washington")
                                    .build(),
                                new UserAgentDTO("Desktop", "Mac OS X", "Chrome"),
                                transaction)
                            .toCompletableFuture(),
                        sessionDatasource
                            .createSession(
                                UUID.randomUUID(),
                                UUID.randomUUID(),
                                REBROWSE_ORGANIZATION_ID,
                                LocationDTO.builder()
                                    .city("Otawa")
                                    .countryName("Canada")
                                    .continentName("North America")
                                    .build(),
                                new UserAgentDTO("Phone", "Mac OS X", "Chrome"),
                                transaction)
                            .toCompletableFuture(),
                        sessionDatasource
                            .createSession(
                                UUID.randomUUID(),
                                UUID.randomUUID(),
                                REBROWSE_ORGANIZATION_ID,
                                LocationDTO.builder()
                                    .city("Maribor")
                                    .countryName("Slovenia")
                                    .continentName("Europe")
                                    .regionName("Podravska")
                                    .build(),
                                new UserAgentDTO("Phone", "Mac OS X", "Chrome"),
                                transaction)
                            .toCompletableFuture(),
                        sessionDatasource
                            .createSession(
                                UUID.randomUUID(),
                                UUID.randomUUID(),
                                REBROWSE_ORGANIZATION_ID,
                                LocationDTO.builder()
                                    .countryName("Slovenia")
                                    .continentName("Europe")
                                    .build(),
                                new UserAgentDTO("Phone", "Mac OS X", "Chrome"),
                                transaction)
                            .toCompletableFuture(),
                        sessionDatasource
                            .createSession(
                                UUID.randomUUID(),
                                UUID.randomUUID(),
                                REBROWSE_ORGANIZATION_ID,
                                LocationDTO.builder()
                                    .city("Zagreb")
                                    .countryName("Croatia")
                                    .continentName("Europe")
                                    .build(),
                                new UserAgentDTO("Phone", "Mac OS X", "Chrome"),
                                transaction)
                            .toCompletableFuture())
                    .thenCompose(ignored -> transaction.commit()))
        .toCompletableFuture()
        .join();

    createdAt =
        sessionDatasource
            .getSession(firstSessionId, REBROWSE_ORGANIZATION_ID)
            .toCompletableFuture()
            .join()
            .get()
            .getCreatedAt();
  }
}
