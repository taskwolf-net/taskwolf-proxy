package net.taskwolf.proxy.distribution;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class DistributionExemptionRepository {
  private final List<DistributionExemption> exemptions = Lists.newArrayList();

  public void registerExemption(DistributionExemption exemption) {
    exemptions.add(exemption);
  }

  public void unregisterExemption(DistributionExemption exemption) {
    exemptions.remove(exemption);
  }

  public List<DistributionExemption> findAll() {
    return List.copyOf(exemptions);
  }
}
