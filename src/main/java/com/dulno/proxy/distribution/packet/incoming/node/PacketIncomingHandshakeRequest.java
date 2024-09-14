package com.dulno.proxy.distribution.packet.incoming.node;

import lombok.Getter;
import lombok.experimental.Accessors;
import com.dulno.core.packet.PacketBuffer;
import com.dulno.core.worker.packet.incoming.PacketIncoming;

@Getter
@Accessors(fluent = true)
public final class PacketIncomingHandshakeRequest extends PacketIncoming {
  private String key;

  public PacketIncomingHandshakeRequest() {
    super(0x00);
  }

  @Override
  public void read(PacketBuffer buffer) throws Exception {
    key = buffer.readString();
  }
}
