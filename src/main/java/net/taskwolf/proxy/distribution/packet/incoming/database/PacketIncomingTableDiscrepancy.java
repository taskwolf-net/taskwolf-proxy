package net.taskwolf.proxy.distribution.packet.incoming.database;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.taskwolf.core.packet.PacketBuffer;
import net.taskwolf.core.worker.packet.incoming.PacketIncoming;

@Getter
@Accessors(fluent = true)
public final class PacketIncomingTableDiscrepancy extends PacketIncoming {
  private String tableClass;

  public PacketIncomingTableDiscrepancy() {
    super(0x34);
  }

  @Override
  public void read(PacketBuffer buffer) throws Exception {
    tableClass = buffer.readString();
  }
}
