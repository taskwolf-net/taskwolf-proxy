package com.dulno.proxy;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.util.Config;
import lombok.RequiredArgsConstructor;
import com.dulno.proxy.distribution.ProxyDistributionInjectionModule;
import com.dulno.proxy.log.ProxyLogInjectionModule;
import com.dulno.proxy.module.ProxyModuleInjectionModule;

@RequiredArgsConstructor(staticName = "create")
public final class ProxyInjectionModule extends AbstractModule {
  @Override
  protected void configure() {
    install(ProxyLogInjectionModule.create());
    install(ProxyModuleInjectionModule.create());
    install(ProxyDistributionInjectionModule.create());
  }

  @Provides
  @Singleton
  AppsV1Api provideKubernetesApi() throws Exception {
    var client = Config.defaultClient();
    Configuration.setDefaultApiClient(client);
    return new AppsV1Api();
  }
}
