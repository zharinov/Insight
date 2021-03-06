package com.meemaw.shared.context;

import com.meemaw.shared.rest.exception.BoomException;
import com.meemaw.shared.rest.headers.MissingHttpHeaders;
import com.meemaw.shared.rest.response.Boom;
import io.vertx.core.http.HttpServerRequest;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

public final class RequestUtils {

  private static final int DOMAIN_MIN_PARTS = 2;

  private RequestUtils() {}

  /**
   * Extracts referer URL from http server request if present.
   *
   * @param request http server request
   * @return Optional URL
   * @throws BoomException if malformed URL
   */
  public static Optional<URL> sneakyParseRefererURL(HttpServerRequest request) {
    return Optional.ofNullable(request.getHeader("referer")).map(RequestUtils::sneakyURL);
  }

  /**
   * Extracts referer base URL from http server request if present.
   *
   * @param request http server request
   * @return Optional String base URL as string
   * @throws BoomException if malformed URL
   */
  public static Optional<URL> parseRefererBaseURL(HttpServerRequest request) {
    return sneakyParseRefererURL(request)
        .map(RequestUtils::parseBaseURL)
        .map(RequestUtils::sneakyURL);
  }

  public static Optional<URL> parseRefererURL(HttpServerRequest request) {
    return sneakyParseRefererURL(request);
  }

  /**
   * Parses URL with caught checked exception.
   *
   * @param url http request url
   * @return URL
   * @throws BoomException if malformed URL
   */
  public static URL sneakyURL(String url) {
    try {
      return new URL(url);
    } catch (MalformedURLException e) {
      throw Boom.badRequest().message(e.getMessage()).exception(e);
    }
  }

  public static URL sneakyURL(URI uri) {
    return sneakyURL(uri.toString());
  }

  /**
   * Parse base URL.
   *
   * @param url URL
   * @return String base url
   */
  public static String parseBaseURL(URL url) {
    String base = url.getProtocol() + "://" + url.getHost();
    if (url.getPort() == -1) {
      return base;
    }
    return base + ":" + url.getPort();
  }

  public static URI getServerBaseURI(UriInfo info, HttpServerRequest request) {
    String proto = request.getHeader(MissingHttpHeaders.X_FORWARDED_PROTO);
    String host = request.getHeader(MissingHttpHeaders.X_FORWARDED_HOST);
    return URI.create(getServerBaseURL(info, proto, host));
  }

  /**
   * Returns server base URL as seen from outer World. In cases when service is behind an Ingress,
   * X-Forwarded-* headers are used.
   *
   * @param info uri info
   * @param request http server request
   * @return server base URL
   */
  public static URL getServerBaseURL(UriInfo info, HttpServerRequest request) {
    return sneakyURL(getServerBaseURI(info, request));
  }

  /**
   * Returns server base URL as seen from outer World. In cases when service is behind an Ingress, *
   * X-Forwarded-* headers are used.
   *
   * @param info request uri info
   * @param forwardedProto X-Forwarded-Proto header value
   * @param forwardedHost X-Forwarded-Host header value
   * @return server base URL
   */
  public static String getServerBaseURL(
      UriInfo info, @Nullable String forwardedProto, @Nullable String forwardedHost) {
    if (forwardedProto != null && forwardedHost != null) {
      return forwardedProto + "://" + forwardedHost;
    }
    return info.getBaseUri().toString().replaceAll("/$", "");
  }

  /**
   * Parses top level domain of given URL.
   *
   * @param url associated with the http request
   * @return Optional String top level domain
   */
  public static Optional<String> parseTLD(String url) {
    try {
      String[] parts = new URL(url).getHost().split("\\.");
      if (parts.length < DOMAIN_MIN_PARTS) {
        return Optional.empty();
      }
      return Optional.of(String.join(".", parts[parts.length - 2], parts[parts.length - 1]));
    } catch (MalformedURLException e) {
      return Optional.empty();
    }
  }

  /**
   * Parses top level cookie domain of a given URL.
   *
   * @param url associated with the http request
   * @return String cookie domain if present else null
   */
  public static String parseCookieDomain(String url) {
    return parseTLD(url).orElse(null);
  }

  public static String parseCookieDomain(URI uri) {
    return parseCookieDomain(uri.toString());
  }

  public static String parseCookieDomain(URL url) {
    return parseCookieDomain(url.toString());
  }

  /**
   * Converts JAX-RS {@link MultivaluedMap} into a native java {@link Map}.
   *
   * @param map JAX-RS multivalued map
   * @return java Map
   */
  public static Map<String, List<String>> map(MultivaluedMap<String, String> map) {
    return map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Entry::getValue));
  }

  /**
   * Get remote IP address. In case application is behind a reverse proxy, X-Forwarded-For header is
   * checked first. Use in Vertx context via {@link HttpServerRequest}.
   *
   * @param request http server request
   * @return remote address
   */
  public static String getRemoteAddress(HttpServerRequest request) {
    return Optional.ofNullable(request.getHeader(MissingHttpHeaders.X_FORWARDED_FOR))
        .orElseGet(() -> request.remoteAddress().host());
  }

  /**
   * Get remote IP address. In case application is behind a reverse proxy, X-Forwarded-For header is
   * checked first. Use in Servlet context via {@link HttpServletRequest}.
   *
   * @param request http servlet request
   * @return remote address
   */
  public static String getRemoteAddress(HttpServletRequest request) {
    return Optional.ofNullable(request.getHeader(MissingHttpHeaders.X_FORWARDED_FOR))
        .orElseGet(request::getRemoteAddr);
  }

  public static Map<String, String> getQueryMap(String query) {
    if (query == null) {
      return Collections.emptyMap();
    }
    String[] params = query.split("&");
    Map<String, String> map = new HashMap<>(params.length);

    for (String param : params) {
      String name = param.split("=")[0];
      String value = param.split("=")[1];
      map.put(name, value);
    }
    return map;
  }
}
