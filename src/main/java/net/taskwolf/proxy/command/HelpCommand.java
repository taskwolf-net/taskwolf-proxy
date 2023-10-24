package net.taskwolf.proxy.command;

import net.taskwolf.core.command.Command;
import net.taskwolf.core.log.Log;

public final class HelpCommand extends Command {
  public static HelpCommand create(Log log) {
    return new HelpCommand(log);
  }

  private HelpCommand(Log log) {
    super(log, "help", new String[] {"commands"}, new String[0]);
  }

  @Override
  public boolean execute(String[] arguments) {
    log().info("Commands: ");
    log().info("- status");
    log().info("- exit");
    return true;
  }
}
