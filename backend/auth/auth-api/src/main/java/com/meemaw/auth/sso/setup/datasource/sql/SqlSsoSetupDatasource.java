package com.meemaw.auth.sso.setup.datasource.sql;

import static com.meemaw.auth.sso.setup.datasource.sql.SqlSsoSetupTable.CONFIGURATION_ENDPOINT;
import static com.meemaw.auth.sso.setup.datasource.sql.SqlSsoSetupTable.CREATED_AT;
import static com.meemaw.auth.sso.setup.datasource.sql.SqlSsoSetupTable.DOMAIN;
import static com.meemaw.auth.sso.setup.datasource.sql.SqlSsoSetupTable.FIELDS;
import static com.meemaw.auth.sso.setup.datasource.sql.SqlSsoSetupTable.INSERT_FIELDS;
import static com.meemaw.auth.sso.setup.datasource.sql.SqlSsoSetupTable.ORGANIZATION_ID;
import static com.meemaw.auth.sso.setup.datasource.sql.SqlSsoSetupTable.TABLE;
import static com.meemaw.auth.sso.setup.datasource.sql.SqlSsoSetupTable.TYPE;

import com.meemaw.auth.sso.setup.datasource.SsoSetupDatasource;
import com.meemaw.auth.sso.setup.model.CreateSsoSetupDTO;
import com.meemaw.auth.sso.setup.model.SsoSetupDTO;
import com.meemaw.shared.sql.client.SqlPool;
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
  public CompletionStage<SsoSetupDTO> create(CreateSsoSetupDTO payload) {
    Query query =
        sqlPool
            .getContext()
            .insertInto(TABLE)
            .columns(INSERT_FIELDS)
            .values(
                payload.getOrganizationId(),
                payload.getDomain(),
                payload.getType(),
                payload.getConfigurationEndpoint())
            .returning(FIELDS);

    return sqlPool.execute(query).thenApply(pgRowSet -> mapSsoSetup(pgRowSet.iterator().next()));
  }

  @Override
  public CompletionStage<Optional<SsoSetupDTO>> get(String organizationId) {
    Query query = sqlPool.getContext().selectFrom(TABLE).where(ORGANIZATION_ID.eq(organizationId));
    return sqlPool.execute(query).thenApply(SqlSsoSetupDatasource::onGetSsoSetup);
  }

  @Override
  public CompletionStage<Optional<SsoSetupDTO>> getByDomain(String domain) {
    Query query = sqlPool.getContext().selectFrom(TABLE).where(DOMAIN.eq(domain));
    return sqlPool.execute(query).thenApply(SqlSsoSetupDatasource::onGetSsoSetup);
  }

  public static Optional<SsoSetupDTO> onGetSsoSetup(RowSet<Row> rows) {
    if (!rows.iterator().hasNext()) {
      return Optional.empty();
    }
    return Optional.of(mapSsoSetup(rows.iterator().next()));
  }

  public static SsoSetupDTO mapSsoSetup(Row row) {
    return new SsoSetupDTO(
        row.getString(ORGANIZATION_ID.getName()),
        row.getString(DOMAIN.getName()),
        row.getString(TYPE.getName()),
        row.getString(CONFIGURATION_ENDPOINT.getName()),
        row.getOffsetDateTime(CREATED_AT.getName()));
  }
}