package net.taskwolf.proxy;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.CoreApplication;
import net.taskwolf.proxy.log.ProxyLogInjectionModule;
import net.taskwolf.proxy.module.ProxyModuleInjectionModule;
import net.taskwolf.proxy.module.ProxyModuleLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.core.io.DefaultResourceLoader;

@RequiredArgsConstructor(staticName = "create")
public final class ProxyInjectionModule extends AbstractModule {
  @Override
  protected void configure() {
    install(ProxyLogInjectionModule.create());
    install(ProxyModuleInjectionModule.create());
  }

  @Provides
  @Singleton
  SpringApplication provideSpringApplication(ProxyModuleLoader moduleLoader) {
    var application = new SpringApplication(CoreApplication.class);
    var classLoader = moduleLoader.classLoader();
    application.setResourceLoader(new DefaultResourceLoader(classLoader));
    return application;
  }
}
