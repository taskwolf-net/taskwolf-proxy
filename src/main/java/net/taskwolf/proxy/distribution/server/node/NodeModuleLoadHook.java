package net.taskwolf.proxy.distribution.server.node;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.event.EventHook;
import net.taskwolf.core.event.Hook;
import net.taskwolf.proxy.distribution.ProxyDistribution;
import net.taskwolf.proxy.distribution.event.node.NodeModuleLoadEvent;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class NodeModuleLoadHook implements Hook {
  private final ProxyDistribution distribution;

  @EventHook
  private void nodeModuleLoad(NodeModuleLoadEvent event) {
    event.client().condition().addModule(event.module());
    distribution.reorganizeUsers();
  }
}
