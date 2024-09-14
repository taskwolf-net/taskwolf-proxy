package com.dulno.proxy.distribution.packet.outgoing.database;

import com.dulno.core.database.transformation.DatabaseTransformationState;
import com.dulno.core.packet.PacketBuffer;
import com.dulno.core.worker.packet.outgoing.PacketOutgoing;

public final class PacketOutgoingTableStateRequest extends PacketOutgoing {
  private final String tableClass;
  private final DatabaseTransformationState state;

  public PacketOutgoingTableStateRequest(
    String tableClass, DatabaseTransformationState state
  ) {
    super(0x37);
    this.tableClass = tableClass;
    this.state = state;
  }

  @Override
  public void write(PacketBuffer buffer) throws Exception {
    buffer.writeString(tableClass);
    buffer.writeString(state.toString());
  }
}
