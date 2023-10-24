package net.taskwolf.proxy.command;

import net.taskwolf.core.command.Command;
import net.taskwolf.core.distribution.DistributionConfiguration;
import net.taskwolf.core.iterator.AsyncIterator;
import net.taskwolf.core.log.Log;
import org.redisson.api.RedissonClient;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class StatusCommand extends Command {
  public static StatusCommand create(
    Log log, DistributionConfiguration distributionConfiguration,
    RedissonClient redisson
  ) {
    return new StatusCommand(log, distributionConfiguration, redisson);
  }

  private final DistributionConfiguration distributionConfiguration;
  private final RedissonClient redisson;

  private StatusCommand(
    Log log, DistributionConfiguration distributionConfiguration,
    RedissonClient redisson
  ) {
    super(log, "status", new String[] {"info"}, new String[0]);
    this.distributionConfiguration = distributionConfiguration;
    this.redisson = redisson;
  }

  @Override
  public boolean execute(String[] arguments) {
    findConnectedNodes().thenAccept(this::printStatus);
    return true;
  }

  private static final String COLOR_RESET = "\u001b[32m";
  private static final String COLOR_RED = "\u001b[31m";
  private static final String COLOR_GREEN = "\u001b[32m";

  private void printStatus(List<String> connectedNodes) {
    var self = distributionConfiguration.self();
    log().info("Proxy (" + self.hostname()  + ":" + self.redisPort() + "): " +
      COLOR_GREEN + "CONNECTED" + COLOR_RESET);
    log().info("Nodes: ");
    for (var node : distributionConfiguration.nodes()) {
      var address = node.hostname() + ":" + node.redisPort();
      log().info(" - " + address + " " + (connectedNodes.contains(address) ?
        COLOR_GREEN + "CONNECTED" : COLOR_RED + "DISCONNECTED") + COLOR_RESET);
    }
  }

  private CompletableFuture<List<String>> findConnectedNodes() {
    var futureResponse = new CompletableFuture<List<String>>();
    findRedisList("taskwolf-nodes").thenAccept(nodes -> AsyncIterator.execute(nodes,
      node -> redisson.<String>getBucket("taskwolf-" + node + "-hostname")
        .getAsync().toCompletableFuture(), nodes.size(), futureResponse::complete));
    return futureResponse;
  }

  private CompletableFuture<List<String>> findRedisList(String key) {
    var futureResponse = new CompletableFuture<List<String>>();
    var list = redisson.<String>getList(key);
    list.sizeAsync().thenAccept(size -> list.rangeAsync(0, size)
      .thenAccept(futureResponse::complete));
    return futureResponse;
  }
}
