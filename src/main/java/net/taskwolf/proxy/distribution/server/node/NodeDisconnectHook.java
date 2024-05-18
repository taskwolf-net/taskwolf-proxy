package net.taskwolf.proxy.distribution.server.node;

import lombok.RequiredArgsConstructor;
import net.taskwolf.core.event.EventHook;
import net.taskwolf.core.event.Hook;
import net.taskwolf.core.log.Log;
import net.taskwolf.proxy.distribution.ProxyDistribution;
import net.taskwolf.proxy.distribution.client.ProxyClientRegistry;
import net.taskwolf.proxy.distribution.event.node.NodeDisconnectEvent;

@RequiredArgsConstructor(staticName = "create")
public final class NodeDisconnectHook implements Hook {
  private final ProxyDistribution distribution;
  private final ProxyClientRegistry clientRegistry;
  private final Log log;

  @EventHook
  private void nodeDisconnect(NodeDisconnectEvent event) {
    var client = event.client();
    clientRegistry.unregisterClient(client);
    distribution.reorganizeUsers();
    var reason = event.reason();
    if (reason.isConnectionFailed()) {
      return;
    }
    if (reason.isShutdown()) {
      log.info("The node " + event.client().hostname() + " has disconnected");
    } else {
      log.warning("The node " + event.client().hostname() + " has timed out");
    }
  }
}
