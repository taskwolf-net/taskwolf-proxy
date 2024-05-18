package net.taskwolf.proxy.distribution.server.node;

import lombok.RequiredArgsConstructor;
import net.taskwolf.core.event.EventExecutor;
import net.taskwolf.core.event.EventHook;
import net.taskwolf.core.event.Hook;
import net.taskwolf.core.log.Log;
import net.taskwolf.core.packet.PacketEventRepository;
import net.taskwolf.core.packet.PacketRegistry;
import net.taskwolf.proxy.distribution.ProxyDistributionConfiguration;
import net.taskwolf.proxy.distribution.client.ProxyClient;
import net.taskwolf.proxy.distribution.client.ProxyClientRegistry;
import net.taskwolf.proxy.distribution.event.node.NodeHandshakeRequestEvent;
import net.taskwolf.proxy.distribution.packet.outgoing.node.PacketOutgoingHandshakeResponse;

import java.net.InetSocketAddress;

@RequiredArgsConstructor(staticName = "create")
public final class NodeHandshakeRequestHook implements Hook {
  private final ProxyDistributionConfiguration configuration;
  private final PacketRegistry packetRegistry;
  private final EventExecutor eventExecutor;
  private final ProxyClientRegistry clientRegistry;
  private final PacketEventRepository packetEventRepository;
  private final Log log;

  @EventHook
  private void nodeHandshake(NodeHandshakeRequestEvent event) {
    if (!event.key().equals(configuration.distributionKey())) {
      event.channel().writeAndFlush(new PacketOutgoingHandshakeResponse(false));
      event.channel().close();
      return;
    }
    var channel = event.channel();
    var channelHost = ((InetSocketAddress) channel.remoteAddress())
      .getAddress().getHostAddress();
    var client = ProxyClient.of(packetRegistry,
      eventExecutor, clientRegistry, packetEventRepository, channelHost, channel);
    clientRegistry.registerClient(client);
    client.authorize();
    event.channel().writeAndFlush(new PacketOutgoingHandshakeResponse(true));
    log.info("Node " + channelHost + " has connected");
  }
}