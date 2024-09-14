package com.dulno.proxy.distribution.server.node;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import com.dulno.core.event.EventHook;
import com.dulno.core.event.Hook;
import com.dulno.proxy.distribution.ProxyDistribution;
import com.dulno.proxy.distribution.event.node.NodeModulesLoadEvent;

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
