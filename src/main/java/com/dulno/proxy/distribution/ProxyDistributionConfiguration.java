package com.dulno.proxy.distribution;

import lombok.Getter;
import lombok.experimental.Accessors;
import com.dulno.core.configuration.Configuration;
import org.json.JSONObject;

@Getter
@Accessors(fluent = true)
public final class ProxyDistributionConfiguration extends Configuration {
  private static final String CONFIGURATION_PATH = "/configurations/distribution/distribution.json";

  public static ProxyDistributionConfiguration createAndLoad() throws Exception {
    var configuration = new ProxyDistributionConfiguration(CONFIGURATION_PATH);
    configuration.load();
    return configuration;
  }

  private int distributionPort;
  private String distributionKey;

  private ProxyDistributionConfiguration(String path) {
    super(path);
  }

  @Override
  protected void deserialize(JSONObject json) {
    distributionPort = json.getInt("distributionPort");
    distributionKey = json.getString("distributionKey");
  }
}
