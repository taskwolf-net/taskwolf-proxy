package net.taskwolf.proxy;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.taskwolf.core.distribution.DistributionConfiguration;
import net.taskwolf.core.distribution.Node;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

@RestController
public class ProxyController {
  private final HttpClient httpClient;
  private final String proxyToken;
  private final DistributionConfiguration distributionConfiguration;
  private final Random random = new Random();

  private ProxyController(
    HttpClient httpClient, @Qualifier("proxyToken") String proxyToken,
    DistributionConfiguration distributionConfiguration
  ) {
    this.httpClient = httpClient;
    this.proxyToken = proxyToken;
    this.distributionConfiguration = distributionConfiguration;
  }

  @RequestMapping("/**")
  public CompletableFuture<ResponseEntity<byte[]>> processRequest(
    @RequestBody(required = false) String body, HttpMethod method,
    HttpServletRequest request, HttpServletResponse response
  ) throws Exception {
    var uri = createUri(request);
    var requestBuilder = HttpRequest.newBuilder().uri(uri)
      .method(method.name(), body == null ? HttpRequest.BodyPublishers.noBody() :
        HttpRequest.BodyPublishers.ofString(body));
    applyHeaders(requestBuilder, request);
    var httpRequest = requestBuilder.build();
    return httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofByteArray())
      .thenApply(this::createResponseEntity);
  }

  private ResponseEntity<byte[]> createResponseEntity(HttpResponse<byte[]> httpResponse) {
    var headers = new HttpHeaders();
    for (var header : httpResponse.headers().map().entrySet()) {
      headers.add(header.getKey(), header.getValue().get(0));
    }
    return ResponseEntity.ok()
      .headers(headers)
      .body(httpResponse.body());
  }

  private void applyHeaders(
    HttpRequest.Builder requestBuilder, HttpServletRequest servletRequest
  ) {
    var headerNames = servletRequest.getHeaderNames();
    while (headerNames.hasMoreElements()) {
      var headerName = headerNames.nextElement();
      requestBuilder.setHeader(headerName, servletRequest.getHeader(headerName));
    }
    requestBuilder.setHeader("PROXYTOKEN", proxyToken);
  }

  private URI createUri(HttpServletRequest request) throws Exception {
    var node = selectNode();
    var uri = new URI("http", null, node.hostname(), node.restPort(), null, null, null);
    return UriComponentsBuilder.fromUri(uri)
      .path(request.getRequestURI())
      .query(request.getQueryString())
      .build(true).toUri();
  }

  private Node selectNode() {
    var nodes = distributionConfiguration.nodes();
    return nodes.get(random.nextInt(nodes.size()));
  }
}
