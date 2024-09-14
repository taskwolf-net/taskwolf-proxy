package com.dulno.proxy.command;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.dulno.core.command.Command;
import com.dulno.core.log.Log;
import com.dulno.proxy.distribution.ProxyDistribution;

@Singleton
public final class ExitCommand extends Command {
  private final ProxyDistribution distribution;

  @Inject
  private ExitCommand(Log log, ProxyDistribution distribution) {
    super(log, "exit", new String[] {"shutdown"}, new String[0]);
    this.distribution = distribution;
  }

  @Override
  public boolean execute(String[] arguments) {
    log().info("Ending Dulno - Proxy");
    distribution.disconnect();
    System.exit(0);
    return true;
  }
}
