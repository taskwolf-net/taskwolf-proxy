package com.dulno.proxy;

import com.dulno.core.database.DatabaseConnection;
import com.dulno.core.error.ErrorRepository;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.dulno.core.CoreInjectionModule;
import com.dulno.core.command.CommandRegistry;
import com.dulno.core.command.CommandTask;
import com.dulno.core.command.implementation.ClearCommand;
import com.dulno.core.intro.Intro;
import com.dulno.core.log.Log;
import com.dulno.proxy.command.ExitCommand;
import com.dulno.proxy.command.HelpCommand;
import com.dulno.proxy.distribution.ProxyDistribution;
import com.dulno.proxy.distribution.server.node.NodePingSchedule;
import com.dulno.proxy.module.ProxyModuleLoader;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProxyApplication {
  public static void main(String[] args) throws Exception {
    System.setProperty("jdk.httpclient.allowRestrictedHeaders",
      "host,connection,content-length");
    var injector = Guice.createInjector(Modules.override(CoreInjectionModule.create())
      .with(ProxyInjectionModule.create()));
    var errorRepository = injector.getInstance(ErrorRepository.class);
    Thread.setDefaultUncaughtExceptionHandler((thread, throwable) ->
      errorRepository.processError(throwable));
    injector.getInstance(DatabaseConnection.class).errorRepository(errorRepository);
    injector.getInstance(Intro.class).print();
    var log = injector.getInstance(Log.class);
    log.info("Initializing Dulno - Proxy");
    setupDistribution(injector);
    var moduleLoader = injector.getInstance(ProxyModuleLoader.class);
    moduleLoader.loadModules();
    var commandRegistry = CommandRegistry.create();
    registerCommands(commandRegistry, injector);
    new Thread(() -> CommandTask.create(log, errorRepository, commandRegistry)
      .start()).start();
    log.info("Successfully booted Dulno - Proxy");
  }

  private static void setupDistribution(Injector injector) throws Exception {
    var distribution = injector.getInstance(ProxyDistribution.class);
    distribution.initialize();
    var nodePingScheduler = injector.getInstance(NodePingSchedule.class);
    nodePingScheduler.start();
  }

  private static void registerCommands(
    CommandRegistry registry, Injector injector
  ) {
    registry.register(injector.getInstance(ClearCommand.class));
    registry.register(injector.getInstance(HelpCommand.class));
    registry.register(injector.getInstance(ExitCommand.class));
  }
}
