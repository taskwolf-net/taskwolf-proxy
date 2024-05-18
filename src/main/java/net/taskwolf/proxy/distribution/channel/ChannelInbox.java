package net.taskwolf.proxy.distribution.channel;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.event.EventExecutor;
import net.taskwolf.core.packet.PacketEventRepository;
import net.taskwolf.core.worker.packet.incoming.PacketIncoming;
import net.taskwolf.proxy.distribution.client.ProxyClient;
import net.taskwolf.proxy.distribution.client.ProxyClientRegistry;
import net.taskwolf.proxy.distribution.event.node.NodeDisconnectEvent;
import net.taskwolf.proxy.distribution.event.node.NodeHandshakeRequestEvent;
import net.taskwolf.proxy.distribution.packet.incoming.node.PacketIncomingHandshakeRequest;

import java.util.Optional;

@RequiredArgsConstructor(staticName = "create")
public final class ChannelInbox extends SimpleChannelInboundHandler<PacketIncoming> {
  private final EventExecutor eventExecutor;
  private final ProxyClientRegistry clientRegistry;
  private final PacketEventRepository packetEventRepository;

  @Override
  protected void channelRead0(
    ChannelHandlerContext context, PacketIncoming incomingPacket
  ) {
    var client = findClientByContext(context);
    if (client.isEmpty() || client.get().state().isUnauthorized()) {
      processUnauthorizedChannel(context, incomingPacket);
      return;
    }
    processAuthorizedChannel(client.get(), incomingPacket);
  }

  private void processUnauthorizedChannel(
    ChannelHandlerContext context, PacketIncoming incomingPacket
  ) {
    if (incomingPacket instanceof PacketIncomingHandshakeRequest packet) {
      processHandshakeRequestPacket(context, packet);
    }
  }

  private void processHandshakeRequestPacket(
    ChannelHandlerContext context, PacketIncomingHandshakeRequest packet
  ) {
    eventExecutor.execute(NodeHandshakeRequestEvent.create(context.channel(),
      packet.key()));
  }

  private void processAuthorizedChannel(
    ProxyClient client, PacketIncoming incomingPacket
  ) {
    var event = packetEventRepository.findEvent(incomingPacket.getClass());
    if (event.isEmpty()) {
      return;
    }
    eventExecutor.execute(event.get().process(client, incomingPacket));
  }

  @Override
  public void channelInactive(ChannelHandlerContext context) {
    var client = findClientByContext(context);
    if (client.isEmpty()) {
      return;
    }
    eventExecutor.execute(NodeDisconnectEvent.create(client.get(),
      NodeDisconnectEvent.DisconnectReason.TIME_OUT));
  }

  private Optional<ProxyClient> findClientByContext(ChannelHandlerContext context) {
    var channel = context.channel();
    return clientRegistry.findAllClients().stream()
      .filter(client -> client.channel().equals(channel))
      .findFirst();
  }
}

