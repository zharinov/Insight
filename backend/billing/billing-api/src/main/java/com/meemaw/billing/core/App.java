package com.meemaw.billing.core;

import javax.ws.rs.core.Application;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.servers.Server;

@OpenAPIDefinition(
    info = @Info(title = App.TITLE, version = App.VERSION),
    servers = @Server(url = "http://localhost:8083"))
public class App extends Application {

  public static final String TITLE = "Billing API";
  public static final String VERSION = "1.0.0";
}