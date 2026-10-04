package net.taskwolf.proxy.distribution.packet.outgoing.node;


import net.taskwolf.core.packet.PacketBuffer;
import net.taskwolf.core.worker.packet.outgoing.PacketOutgoing;

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
