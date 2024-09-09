package net.taskwolf.proxy.distribution.server.database;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.event.EventHook;
import net.taskwolf.core.event.Hook;
import net.taskwolf.proxy.distribution.client.ProxyClientRegistry;
import net.taskwolf.proxy.distribution.event.database.TableStateRequestEvent;
import net.taskwolf.proxy.distribution.packet.outgoing.database.PacketOutgoingTableStateRequest;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class TableStateRequestHook implements Hook {
  private final ProxyClientRegistry clientRegistry;
  private final TableTransformationRepository transformationRepository;

  @EventHook
  private void tableStateRequest(TableStateRequestEvent event) {
    //TODO: REGISTER STATE CHANGE
    for (var client : clientRegistry.findAllClients()) {
      client.sendPacket(new PacketOutgoingTableStateRequest(event.tableClass(),
        event.state()));
    }
  }
}
