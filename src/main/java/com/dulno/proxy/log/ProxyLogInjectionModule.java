package com.dulno.proxy.log;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import com.dulno.core.log.Log;

@RequiredArgsConstructor(staticName = "create")
public final class ProxyLogInjectionModule extends AbstractModule {
  @Provides
  @Singleton
  Log provideProxyLog() throws Exception {
    return Log.create("Proxy", "/logs/");
  }
}
