package com.dulno.proxy.distribution.packet.outgoing.node;


import com.dulno.core.packet.PacketBuffer;
import com.dulno.core.worker.packet.outgoing.PacketOutgoing;

public final class PacketOutgoingHandshakeResponse extends PacketOutgoing {
  private final boolean success;

  public PacketOutgoingHandshakeResponse(boolean success) {
    super(0x01);
    this.success = success;
  }

  @Override
  public void write(PacketBuffer buffer) throws Exception {
    buffer.raw().writeBoolean(success);
  }
}
