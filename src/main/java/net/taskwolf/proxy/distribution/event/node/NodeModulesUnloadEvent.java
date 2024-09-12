package net.taskwolf.proxy.distribution.event.node;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.taskwolf.core.event.Event;
import net.taskwolf.proxy.distribution.client.ProxyClient;

import java.util.List;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class NodeModulesUnloadEvent extends Event {
  private final ProxyClient client;
  private final List<String> modules;
}

