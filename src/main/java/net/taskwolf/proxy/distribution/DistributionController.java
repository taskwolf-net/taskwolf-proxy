package net.taskwolf.proxy.distribution;

import jakarta.servlet.http.HttpServletRequest;
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
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

@RestController
public class DistributionController {
  private final HttpClient httpClient;
  private final String proxyToken;
  private final DistributionConfiguration distributionConfiguration;
  private final DistributionExemptionRepository exemptionRepository;
  private final Random random = new Random();

  private DistributionController(
    HttpClient httpClient, @Qualifier("proxyToken") String proxyToken,
    DistributionConfiguration distributionConfiguration,
    DistributionExemptionRepository exemptionRepository
  ) {
    this.httpClient = httpClient;
    this.proxyToken = proxyToken;
    this.distributionConfiguration = distributionConfiguration;
    this.exemptionRepository = exemptionRepository;
  }

  @RequestMapping(path = "/v1/")
  public String version() {
    return "Taskwolf API - Version V1";
  }

  @RequestMapping("/**")
  public CompletableFuture<ResponseEntity<byte[]>> processRequest(
    @RequestBody(required = false) String body, HttpMethod method,
    HttpServletRequest request
  ) throws Exception {
    var uri = createUri(request, body);
    var requestBuilder = HttpRequest.newBuilder().uri(uri)
      .method(method.name(), body == null ? HttpRequest.BodyPublishers.noBody() :
        HttpRequest.BodyPublishers.ofString(body));
    applyHeaders(requestBuilder, request);
    var httpRequest = requestBuilder.build();
    return httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofByteArray())
      .thenApply(this::createResponseEntity).exceptionally(throwable -> null);
  }

  private ResponseEntity<byte[]> createResponseEntity(
    HttpResponse<byte[]> httpResponse
  ) {
    var headers = new HttpHeaders();
    headers.add("Content-Type", "application/json");
    httpResponse.headers().firstValue("location").ifPresent(value ->
      headers.add("location", value));
    httpResponse.headers().allValues("Set-Cookie").forEach(value ->
      headers.add("Set-Cookie", value));
    return ResponseEntity.status(httpResponse.statusCode())
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

  private URI createUri(HttpServletRequest request, String body) throws Exception {
    var node = selectNode(request.getRequestURI(), body);
    var uri = new URI("http", null, node.hostname(), node.restPort(), null,
      null, null);
    return UriComponentsBuilder.fromUri(uri)
      .path(request.getRequestURI())
      .query(request.getQueryString())
      .build(true).toUri();
  }

  private Node selectNode(String url, String body) {
    var exemption = findResponsibleExemption(url);
    if (exemption.isPresent()) {
      var preference = exemption.get().preference(body);
      if (preference.isPresent()) {
        return preference.get();
      }
    }
    var nodes = distributionConfiguration.nodes().stream()
      .filter(node -> node.type().isWorker()).toList();
    return nodes.get(random.nextInt(nodes.size()));
  }

  private Optional<DistributionExemption> findResponsibleExemption(String url) {
    return exemptionRepository.findAll().stream()
      .filter(exemption -> url.contains(exemption.url()))
      .findFirst();
  }
}
