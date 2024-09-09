package net.taskwolf.proxy.distribution.server.database;

import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.kubernetes.client.openapi.ApiCallback;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.models.V1Deployment;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.database.transformation.DatabaseTransformationState;
import net.taskwolf.proxy.distribution.client.ProxyClient;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class TableStateChangeRepository {
  private final AppsV1Api kubernetesApi;
  private final Set<TableStateChange> changes = Collections.newSetFromMap(
    CacheBuilder.newBuilder().expireAfterWrite(Duration.ofMinutes(5))
      .<TableStateChange, Boolean>build().asMap());

  public CompletableFuture<TableStateChange> registerStateChange(
    String tableClass, DatabaseTransformationState state, ProxyClient client
  ) throws Exception {
    var change = findCoreReplicas().thenApply(replicas ->
      TableStateChange.create(tableClass, state, replicas, 0, client));
    change.thenAccept(changes::add);
    return change;
  }

  public Optional<TableStateChange> recogniseResponse(
    String tableClass, DatabaseTransformationState state
  ) {
    return findStateChange(tableClass, state)
      .flatMap(this::finishResponseRecognition);
  }

  private Optional<TableStateChange> finishResponseRecognition(
    TableStateChange change
  ) {
    change.addResponse();
    var ready = change.responses() == change.replicas();
    if (ready) {
      changes.remove(change);
      return Optional.of(change);
    }
    return Optional.empty();
  }

  private static final String CORE_DEPLOYMENT_NAME = "taskwolf-core-deployment";
  private static final String CORE_DEPLOYMENT_NAMESPACE = "default";

  private CompletableFuture<Integer> findCoreReplicas() throws Exception {
    var futureResponse = new CompletableFuture<Integer>();
    var deploymentRequest = kubernetesApi.readNamespacedDeployment(
      CORE_DEPLOYMENT_NAME, CORE_DEPLOYMENT_NAMESPACE);
    deploymentRequest.executeAsync(new ApiCallback<>() {
      @Override
      public void onFailure(
        ApiException exception, int statusCode, Map<String,
        List<String>> responseHeaders
      ) {
        exception.printStackTrace();
      }

      @Override
      public void onSuccess(
        V1Deployment result, int statusCode,
        Map<String, List<String>> responseHeaders
      ) {
        futureResponse.complete(result.getSpec().getReplicas());
      }

      @Override
      public void onUploadProgress(
        long bytesWritten, long contentLength, boolean done
      ) {
      }

      @Override
      public void onDownloadProgress(
        long bytesRead, long contentLength, boolean done
      ) {
      }
    });
    return futureResponse;
  }

  public Optional<TableStateChange> findStateChange(
    String tableClass, DatabaseTransformationState state
  ) {
    return changes.stream()
      .filter(change -> change.tableClass().equalsIgnoreCase(tableClass) ||
        change.state() == state)
      .findFirst();
  }
}
