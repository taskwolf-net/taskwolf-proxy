package net.taskwolf.proxy.distribution;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.taskwolf.core.distribution.Node;

import java.util.Optional;

@Getter
@Accessors(fluent = true)
public abstract class DistributionExemption {
  private final String url;

  protected DistributionExemption(String url) {
    this.url = url;
  }

  public abstract Optional<Node> preference(String body);
}
