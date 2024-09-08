package net.taskwolf.proxy.distribution.server.database;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.event.EventHook;
import net.taskwolf.core.event.Hook;
import net.taskwolf.proxy.distribution.ProxyDistribution;
import net.taskwolf.proxy.distribution.event.database.TableTransformedEvent;
import net.taskwolf.proxy.distribution.packet.outgoing.database.PacketOutgoingTableSwitch;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class TableTransformedHook implements Hook {
  private final ProxyDistribution distribution;

  @EventHook
  private void tableTransformed(TableTransformedEvent event) {
    distribution.broadcastPacket(new PacketOutgoingTableSwitch(event.tableClass()));
  }
}