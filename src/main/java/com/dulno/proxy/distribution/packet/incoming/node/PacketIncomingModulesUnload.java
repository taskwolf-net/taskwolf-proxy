package com.dulno.proxy.distribution.packet.incoming.node;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.experimental.Accessors;
import com.dulno.core.packet.PacketBuffer;
import com.dulno.core.worker.packet.incoming.PacketIncoming;

import java.util.List;

@Getter
@Accessors(fluent = true)
public final class PacketIncomingModulesUnload extends PacketIncoming {
  private List<String> modules;

  public PacketIncomingModulesUnload() {
    super(0x06);
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
