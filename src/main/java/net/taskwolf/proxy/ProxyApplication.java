package net.taskwolf.proxy;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import net.taskwolf.core.CoreInjectionModule;
import net.taskwolf.core.command.CommandRegistry;
import net.taskwolf.core.command.CommandTask;
import net.taskwolf.core.command.implementation.ClearCommand;
import net.taskwolf.core.command.implementation.DistributionCommand;
import net.taskwolf.core.distribution.Distribution;
import net.taskwolf.core.distribution.DistributionConfiguration;
import net.taskwolf.core.distribution.server.node.NodePingSchedule;
import net.taskwolf.core.intro.Intro;
import net.taskwolf.core.log.Log;
import net.taskwolf.proxy.command.ExitCommand;
import net.taskwolf.proxy.command.HelpCommand;
import net.taskwolf.proxy.module.ProxyModuleLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Collections;

@SpringBootApplication
public class ProxyApplication {
  public static void main(String[] args) throws Exception {
    System.setProperty("jdk.httpclient.allowRestrictedHeaders",
      "host,connection,content-length");
    var injector = Guice.createInjector(Modules.override(CoreInjectionModule.create())
      .with(ProxyInjectionModule.create()));
    System.out.print("\033c");
    injector.getInstance(Intro.class).print();
    var log = injector.getInstance(Log.class);
    log.info("Initializing Taskwolf - Proxy");
    var application = new SpringApplication(ProxyApplication.class);
    var distributionConfiguration = injector.getInstance(
      DistributionConfiguration.class);
    setupDistribution(injector);
    var moduleLoader = injector.getInstance(ProxyModuleLoader.class);
    moduleLoader.loadModules();
    var commandRegistry = CommandRegistry.create();
    registerCommands(commandRegistry, injector);
    application.setDefaultProperties(Collections.singletonMap("server.port",
      distributionConfiguration.self().restPort()));
    application.run(args);
    new Thread(() -> CommandTask.create(log, commandRegistry).start()).start();
    log.info("Successfully booted Taskwolf - Proxy");
  }

  private static void setupDistribution(Injector injector) throws Exception {
    var distribution = injector.getInstance(Distribution.class);
    distribution.initialize();
    var nodePingScheduler = injector.getInstance(NodePingSchedule.class);
    nodePingScheduler.start();
  }

  private static void registerCommands(
    CommandRegistry registry, Injector injector
  ) {
    registry.register(injector.getInstance(ClearCommand.class));
    registry.register(injector.getInstance(HelpCommand.class));
    registry.register(injector.getInstance(DistributionCommand.class));
    registry.register(injector.getInstance(ExitCommand.class));
  }
}
