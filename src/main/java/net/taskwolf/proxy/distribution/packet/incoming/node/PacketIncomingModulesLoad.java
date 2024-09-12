package net.taskwolf.proxy.distribution.packet.incoming.node;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.taskwolf.core.packet.PacketBuffer;
import net.taskwolf.core.worker.packet.incoming.PacketIncoming;

import java.util.List;

@Getter
@Accessors(fluent = true)
public final class PacketIncomingModulesLoad extends PacketIncoming {
  private List<String> modules;

  public PacketIncomingModulesLoad() {
    super(0x05);
  }

  @Override
  public void read(PacketBuffer buffer) throws Exception {
    var length = buffer.readVarInt();
    modules = Lists.newArrayList();
    for (var i = 0; i < length; i++) {
      modules.add(buffer.readString());
    }
  }
}

