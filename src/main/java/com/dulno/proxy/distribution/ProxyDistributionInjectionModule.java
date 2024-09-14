package com.dulno.proxy.distribution;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import com.dulno.core.worker.WorkerConfiguration;

@RequiredArgsConstructor(staticName = "create")
public final class ProxyDistributionInjectionModule extends AbstractModule {
  @Provides
  @Singleton
  ProxyDistributionConfiguration provideProxyDistributionConfiguration() throws Exception {
    return ProxyDistributionConfiguration.createAndLoad();
  }
}
