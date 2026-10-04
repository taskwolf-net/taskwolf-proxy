package net.taskwolf.proxy.distribution.event.node;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.taskwolf.core.worker.client.WorkerProxyClient;
import net.taskwolf.core.event.Event;
import net.taskwolf.proxy.distribution.client.ProxyClient;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class NodePongEvent extends Event {
  private final ProxyClient client;
  private final int value;
}
