package com.meemaw.auth.sso.setup.datasource.sql;

import static com.meemaw.auth.sso.setup.datasource.sql.SqlSsoSetupTable.CREATED_AT;
import static com.meemaw.auth.sso.setup.datasource.sql.SqlSsoSetupTable.DOMAIN;
import static com.meemaw.auth.sso.setup.datasource.sql.SqlSsoSetupTable.FIELDS;
import static com.meemaw.auth.sso.setup.datasource.sql.SqlSsoSetupTable.INSERT_FIELDS;
import static com.meemaw.auth.sso.setup.datasource.sql.SqlSsoSetupTable.METHOD;
import static com.meemaw.auth.sso.setup.datasource.sql.SqlSsoSetupTable.ORGANIZATION_ID;
import static com.meemaw.auth.sso.setup.datasource.sql.SqlSsoSetupTable.SAML;
import static com.meemaw.auth.sso.setup.datasource.sql.SqlSsoSetupTable.TABLE;

import com.meemaw.auth.sso.setup.datasource.SsoSetupDatasource;
import com.meemaw.auth.sso.setup.model.CreateSsoSetup;
import com.meemaw.auth.sso.setup.model.SsoMethod;
import com.meemaw.auth.sso.setup.model.dto.SamlConfiguration;
import com.meemaw.auth.sso.setup.model.dto.SsoSetup;
import com.meemaw.shared.sql.client.SqlPool;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.jooq.Query;

@ApplicationScoped
public class SqlSsoSetupDatasource implements SsoSetupDatasource {

  @Inject SqlPool sqlPool;

  @Override
  public CompletionStage<SsoSetup> create(CreateSsoSetup payload) {
    Query query =
        sqlPool
            .getContext()
            .insertInto(TABLE)
            .columns(INSERT_FIELDS)
            .values(
                payload.getOrganizationId(),
                payload.getDomain(),
                payload.getMethod().getKey(),
                JsonObject.mapFrom(payload.getSamlConfiguration()))
            .returning(FIELDS);

    return sqlPool.execute(query).thenApply(pgRowSet -> mapSsoSetup(pgRowSet.iterator().next()));
  }

  @Override
  public CompletionStage<Boolean> delete(String organizationId) {
    Query query =
        sqlPool
            .getContext()
            .deleteFrom(TABLE)
            .where(ORGANIZATION_ID.eq(organizationId))
            .returning(ORGANIZATION_ID);

    return sqlPool.execute(query).thenApply(pgRowSet -> pgRowSet.iterator().hasNext());
  }

  @Override
  public CompletionStage<Optional<SsoSetup>> get(String organizationId) {
    Query query = sqlPool.getContext().selectFrom(TABLE).where(ORGANIZATION_ID.eq(organizationId));
    return sqlPool.execute(query).thenApply(SqlSsoSetupDatasource::onRetrieveSsoSetup);
  }

  @Override
  public CompletionStage<Optional<SsoSetup>> getByDomain(String domain) {
    Query query = sqlPool.getContext().selectFrom(TABLE).where(DOMAIN.eq(domain));
    return sqlPool.execute(query).thenApply(SqlSsoSetupDatasource::onRetrieveSsoSetup);
  }

  public static Optional<SsoSetup> onRetrieveSsoSetup(RowSet<Row> rows) {
    if (!rows.iterator().hasNext()) {
      return Optional.empty();
    }
    return Optional.of(mapSsoSetup(rows.iterator().next()));
  }

  public static SsoSetup mapSsoSetup(Row row) {
    JsonObject saml = (JsonObject) row.getValue(SAML.getName());

    return new SsoSetup(
        row.getString(ORGANIZATION_ID.getName()),
        row.getString(DOMAIN.getName()),
        SsoMethod.fromString(row.getString(METHOD.getName())),
        Optional.ofNullable(saml).map(p -> p.mapTo(SamlConfiguration.class)).orElse(null),
        row.getOffsetDateTime(CREATED_AT.getName()));
  }
}
