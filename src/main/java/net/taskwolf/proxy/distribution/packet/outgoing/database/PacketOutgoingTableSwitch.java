package net.taskwolf.proxy.distribution.packet.outgoing.database;

import net.taskwolf.core.packet.PacketBuffer;
import net.taskwolf.core.worker.packet.outgoing.PacketOutgoing;

public final class PacketOutgoingTableSwitch extends PacketOutgoing {
  private final String tableClass;

  public PacketOutgoingTableSwitch(String tableClass) {
    super(0x37);
    this.tableClass = tableClass;
  }

  @Override
  public void write(PacketBuffer buffer) throws Exception {
    buffer.writeString(tableClass);
  }
}
