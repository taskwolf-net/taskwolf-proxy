package com.dulno.proxy.distribution.packet.outgoing.node;


import com.dulno.core.packet.PacketBuffer;
import com.dulno.core.worker.packet.outgoing.PacketOutgoing;

public final class PacketOutgoingPing extends PacketOutgoing {
  private final int value;

  public PacketOutgoingPing(int value) {
    super(0x02);
    this.value = value;
  }

  @Override
  public void write(PacketBuffer buffer) throws Exception {
    buffer.writeVarInt(value);
  }
}
