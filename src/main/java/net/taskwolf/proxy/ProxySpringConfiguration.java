package net.taskwolf.proxy;

import jakarta.annotation.PostConstruct;
import net.taskwolf.core.distribution.DistributionConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;

@Configuration
public class ProxySpringConfiguration {
  private HttpClient httpClient;
  private String proxyToken;
  private DistributionConfiguration distributionConfiguration;

  @Bean
  HttpClient provideHttpClient() {
    return httpClient;
  }

  @Bean
  @Qualifier("proxyToken")
  String provideProxyToken() {
    return proxyToken;
  }

  @Bean
  DistributionConfiguration provideDistributionConfiguration() {
    return distributionConfiguration;
  }

  @PostConstruct
  private void initializeSecretKey() throws Exception {
    httpClient = HttpClient.newHttpClient();
    proxyToken = ProxyConfiguration.createAndLoad().proxyToken();
    distributionConfiguration = DistributionConfiguration.createAndLoad();
  }
}
