package net.taskwolf.proxy;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.proxy.distribution.DistributionExemptionRepository;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class ProxyContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
  private final DistributionExemptionRepository exemptionRepository;

  @Override
  public void initialize(ConfigurableApplicationContext applicationContext) {
    var beanFactory = applicationContext.getBeanFactory();
    beanFactory.registerSingleton("exemptionRepository", exemptionRepository);
  }
}
