package net.taskwolf.proxy.distribution.server.node;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.event.EventHook;
import net.taskwolf.core.event.Hook;
import net.taskwolf.proxy.distribution.ProxyDistribution;
import net.taskwolf.proxy.distribution.event.node.NodeModulesLoadEvent;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class NodeModulesLoadHook implements Hook {
  private final ProxyDistribution distribution;

  @EventHook
  private void nodeModulesLoad(NodeModulesLoadEvent event) {
    event.client().condition().addMultipleModules(event.modules());
    distribution.reorganizeUsers();
  }
}
