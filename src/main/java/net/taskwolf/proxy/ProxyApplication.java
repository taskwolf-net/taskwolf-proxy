package net.taskwolf.proxy;

import net.taskwolf.core.command.CommandRegistry;
import net.taskwolf.core.command.CommandTask;
import net.taskwolf.core.command.implementation.ExitCommand;
import net.taskwolf.core.distribution.DistributionConfiguration;
import net.taskwolf.core.distribution.Node;
import net.taskwolf.core.intro.Intro;
import net.taskwolf.core.log.Log;
import net.taskwolf.proxy.command.HelpCommand;
import net.taskwolf.proxy.command.StatusCommand;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProxyApplication {
  public static void main(String[] args) throws Exception {
    System.setProperty("jdk.httpclient.allowRestrictedHeaders",
      "host,connection,content-length");
    Intro.create("1.0.0").print();
    var log = Log.create("Proxy", "/logs/");
    SpringApplication.run(ProxyApplication.class);
    var distributionConfiguration = DistributionConfiguration.createAndLoad();
    var redisson = initializeRedisson(distributionConfiguration);
    var commandRegistry = CommandRegistry.create();
    registerCommands(log, commandRegistry, distributionConfiguration, redisson);
    new Thread(() -> CommandTask.create(log, commandRegistry).start()).start();
    log.info("Successfully booted Proxy");
  }

  private static RedissonClient initializeRedisson(
    DistributionConfiguration distributionConfiguration
  ) {
    var config = new Config();
    var clusterConfig = config.useClusterServers();
    clusterConfig.addNodeAddress(createNodeAddress(distributionConfiguration.self()));
    for (var node : distributionConfiguration.nodes()) {
      clusterConfig.addNodeAddress(createNodeAddress(node));
    }
    return Redisson.create(config);
  }

  private static String createNodeAddress(Node node) {
    return "redis://" + node.hostname() + ":" + node.redisPort();
  }

  private static void registerCommands(
    Log log, CommandRegistry registry,
    DistributionConfiguration distributionConfiguration, RedissonClient redisson
  ) {
    registry.register(HelpCommand.create(log));
    registry.register(StatusCommand.create(log, distributionConfiguration,
      redisson));
    registry.register(ExitCommand.create(log));
  }
}
