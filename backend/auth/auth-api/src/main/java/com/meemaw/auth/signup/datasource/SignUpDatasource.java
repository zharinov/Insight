package com.meemaw.auth.signup.datasource;

import com.meemaw.auth.signup.model.SignUpRequest;
import com.meemaw.shared.sql.client.SqlTransaction;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public interface SignUpDatasource {

  CompletionStage<UUID> createSignUpRequest(
      SignUpRequest signUpRequest, SqlTransaction transaction);

  CompletionStage<Optional<SignUpRequest>> findSignUpRequest(UUID token);

  CompletionStage<Optional<SignUpRequest>> findSignUpRequest(
      UUID token, SqlTransaction transaction);

  CompletionStage<Boolean> deleteSignUpRequest(UUID token, SqlTransaction transaction);

  CompletionStage<Boolean> selectIsEmailTaken(String email, SqlTransaction transaction);
}
