package com.dulno.proxy.distribution.event.node;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import com.dulno.core.worker.client.WorkerProxyClient;
import com.dulno.core.event.Event;
import com.dulno.proxy.distribution.client.ProxyClient;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class NodePongEvent extends Event {
  private final ProxyClient client;
  private final int value;
}
