package net.taskwolf.proxy;

import com.google.inject.AbstractModule;
import lombok.RequiredArgsConstructor;
import net.taskwolf.proxy.distribution.ProxyDistributionInjectionModule;
import net.taskwolf.proxy.log.ProxyLogInjectionModule;
import net.taskwolf.proxy.module.ProxyModuleInjectionModule;

@RequiredArgsConstructor(staticName = "create")
public final class ProxyInjectionModule extends AbstractModule {
  @Override
  protected void configure() {
    install(ProxyLogInjectionModule.create());
    install(ProxyModuleInjectionModule.create());
    install(ProxyDistributionInjectionModule.create());
  }
}
