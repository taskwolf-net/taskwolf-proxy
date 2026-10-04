package net.taskwolf.proxy.distribution.server.node;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.event.EventHook;
import net.taskwolf.core.event.Hook;
import net.taskwolf.proxy.distribution.ProxyDistribution;
import net.taskwolf.proxy.distribution.event.node.NodeModulesUnloadEvent;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class NodeModulesUnloadHook implements Hook {
  private final ProxyDistribution distribution;

  @EventHook
  private void nodeModulesUnload(NodeModulesUnloadEvent event) {
    event.client().condition().removeMultipleModules(event.modules());
    distribution.reorganizeUsers();
  }
}

