package net.taskwolf.proxy;

import com.google.inject.AbstractModule;
import com.google.inject.util.Modules;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.CoreInjectionModule;
import net.taskwolf.proxy.log.ProxyLogInjectionModule;

@RequiredArgsConstructor(staticName = "create")
public final class ProxyInjectionModule extends AbstractModule {
  @Override
  protected void configure() {
    install(Modules.override(CoreInjectionModule.create())
      .with(ProxyLogInjectionModule.create()));
  }
}
