package net.taskwolf.proxy.distribution.server.database;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.kubernetes.client.openapi.ApiCallback;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.models.V1Deployment;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.commons.compress.utils.Lists;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class TableTransformationRepository {
  private final AppsV1Api kubernetesApi;
  private final List<TableTransformation> transformations = Lists.newArrayList();

  public CompletableFuture<Boolean> recogniseDiscrepancy(
    String tableClass
  ) throws Exception {
    var transformation = findTransformation(tableClass);
    if (transformation.isPresent()) {
      return CompletableFuture.completedFuture(
        finishDiscrepancyRecognition(transformation.get()));
    }
    return findCoreReplicas()
      .thenApply(replicas -> TableTransformation.create(tableClass, replicas, 0))
      .thenApply(this::finishDiscrepancyRecognition);
  }

  private boolean finishDiscrepancyRecognition(TableTransformation transformation) {
    transformation.addDiscrepancy();
    return transformation.discrepancies() == transformation.replicas();
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

  public Optional<TableTransformation> findTransformation(String tableClass) {
    return transformations.stream()
      .filter(transformation ->
        transformation.tableClass().equalsIgnoreCase(tableClass))
      .findFirst();
  }
}
