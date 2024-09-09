package net.taskwolf.proxy.distribution.event.database;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.taskwolf.core.database.transformation.DatabaseTransformationState;
import net.taskwolf.core.event.Event;
import net.taskwolf.proxy.distribution.client.ProxyClient;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class TableStateRequestEvent extends Event {
  private final ProxyClient client;
  private final String tableClass;
  private final DatabaseTransformationState state;
}