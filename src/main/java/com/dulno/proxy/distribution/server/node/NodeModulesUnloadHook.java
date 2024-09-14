package com.dulno.proxy.distribution.server.node;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import com.dulno.core.event.EventHook;
import com.dulno.core.event.Hook;
import com.dulno.proxy.distribution.ProxyDistribution;
import com.dulno.proxy.distribution.event.node.NodeModulesUnloadEvent;

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

