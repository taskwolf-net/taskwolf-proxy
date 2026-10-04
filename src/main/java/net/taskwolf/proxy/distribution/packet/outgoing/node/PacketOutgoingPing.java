package net.taskwolf.proxy.distribution.packet.outgoing.node;


import net.taskwolf.core.packet.PacketBuffer;
import net.taskwolf.core.worker.packet.outgoing.PacketOutgoing;

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
