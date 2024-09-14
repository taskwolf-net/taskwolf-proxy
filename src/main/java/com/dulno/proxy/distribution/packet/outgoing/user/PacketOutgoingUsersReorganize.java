package com.dulno.proxy.distribution.packet.outgoing.user;

import com.dulno.core.packet.PacketBuffer;
import com.dulno.core.worker.packet.outgoing.PacketOutgoing;

import java.util.List;
import java.util.UUID;

public final class PacketOutgoingUsersReorganize extends PacketOutgoing {
  private final String module;
  private final List<UUID> users;

  public PacketOutgoingUsersReorganize(String module, List<UUID> users) {
    super(0x07);
    this.module = module;
    this.users = users;
  }

  @Override
  public void write(PacketBuffer buffer) throws Exception {
    buffer.writeString(module);
    buffer.writeVarInt(users.size());
    for (var user : users) {
      buffer.writeUUID(user);
    }
  }
}
