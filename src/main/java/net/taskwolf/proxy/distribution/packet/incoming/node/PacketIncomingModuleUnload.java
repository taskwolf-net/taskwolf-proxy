package net.taskwolf.proxy.distribution.packet.incoming.node;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.taskwolf.core.packet.PacketBuffer;
import net.taskwolf.core.worker.packet.incoming.PacketIncoming;

@Getter
@Accessors(fluent = true)
public final class PacketIncomingModuleUnload extends PacketIncoming {
  private String module;

  public PacketIncomingModuleUnload() {
    super(0x06);
  }

  @Override
  public void read(PacketBuffer buffer) throws Exception {
    module = buffer.readString();
  }
}
