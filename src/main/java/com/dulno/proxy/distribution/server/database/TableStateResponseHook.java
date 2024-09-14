package com.dulno.proxy.distribution.server.database;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import com.dulno.core.event.EventHook;
import com.dulno.core.event.Hook;
import com.dulno.core.log.Log;
import com.dulno.proxy.distribution.event.database.TableStateResponseEvent;
import com.dulno.proxy.distribution.packet.outgoing.database.PacketOutgoingTableStateResponse;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class TableStateResponseHook implements Hook {
  private final Log log;
  private final TableStateChangeRepository changeRepository;

  @EventHook
  private void tableStateResponse(TableStateResponseEvent event) {
    var optionalChange = changeRepository.recogniseResponse(event.tableClass(),
      event.state());
    if (optionalChange.isEmpty()) {
      return;
    }
    var change = optionalChange.get();
    change.requester().sendPacket(new PacketOutgoingTableStateResponse(
      change.tableClass(), change.state()));
    log.info("All pods have reported back regarding the last state changes " +
      "in the transformation of the table " + event.tableClass() +
      ". The transformation is now continued");
  }
}
