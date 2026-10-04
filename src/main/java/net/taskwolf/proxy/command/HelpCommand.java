package net.taskwolf.proxy.command;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.taskwolf.core.command.Command;
import net.taskwolf.core.log.Log;

@Singleton
public final class HelpCommand extends Command {
  @Inject
  private HelpCommand(Log log) {
    super(log, "help", new String[] {"commands"}, new String[0]);
  }

  @Override
  public boolean execute(String[] arguments) {
    log().info("Commands: ");
    log().info("- distribution");
    log().info("- exit");
    return true;
  }
}
