package net.taskwolf.proxy.module;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.log.Log;

@RequiredArgsConstructor(staticName = "create")
public final class ProxyModuleInjectionModule extends AbstractModule {
  @Provides
  @Singleton
  ProxyModuleLoader provideModuleLoader(
    Log log, Injector injector
  ) {
    return ProxyModuleLoader.create(log, System.getProperty("user.dir") +
      "/modules/", injector);
  }
}
