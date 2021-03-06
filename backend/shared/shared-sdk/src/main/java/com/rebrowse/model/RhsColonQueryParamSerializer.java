package com.rebrowse.model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.rebrowse.api.query.QueryParam;
import java.io.IOException;

public class RhsColonQueryParamSerializer extends JsonSerializer<QueryParam<?>> {

  @Override
  public void serialize(
      QueryParam queryParam, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
      throws IOException {
    jsonGenerator.writeString(
        String.format("%s:%s", queryParam.getCondition().getKey(), queryParam.getValue()));
  }
}
