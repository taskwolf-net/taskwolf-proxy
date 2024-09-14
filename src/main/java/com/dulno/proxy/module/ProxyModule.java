package com.dulno.proxy.module;

import com.google.inject.Injector;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import com.dulno.core.command.Command;
import com.dulno.core.command.CommandRegistry;
import com.dulno.core.event.Hook;
import com.dulno.core.event.HookRegistry;

@Getter(AccessLevel.PROTECTED)
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class ProxyModule {
  private final Injector injector;

  public abstract void enable() throws Exception;

  public abstract void disable() throws Exception;

  public void registerCommand(Command command) {
    injector.getInstance(CommandRegistry.class).register(command);
  }

  public void unregisterCommand(Command command) {
    injector.getInstance(CommandRegistry.class).unregister(command);
  }

  public void registerHook(Hook hook) {
    injector.getInstance(HookRegistry.class).register(hook);
  }

  public void unregisterHook(Hook hook) {
    injector.getInstance(HookRegistry.class).unregister(hook);
  }
}
