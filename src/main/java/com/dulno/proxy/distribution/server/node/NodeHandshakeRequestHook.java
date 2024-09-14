package com.dulno.proxy.distribution.server.node;

import lombok.RequiredArgsConstructor;
import com.dulno.core.event.EventExecutor;
import com.dulno.core.event.EventHook;
import com.dulno.core.event.Hook;
import com.dulno.core.log.Log;
import com.dulno.core.packet.PacketEventRepository;
import com.dulno.core.packet.PacketRegistry;
import com.dulno.proxy.distribution.ProxyDistributionConfiguration;
import com.dulno.proxy.distribution.client.ProxyClient;
import com.dulno.proxy.distribution.client.ProxyClientRegistry;
import com.dulno.proxy.distribution.event.node.NodeHandshakeRequestEvent;
import com.dulno.proxy.distribution.packet.outgoing.node.PacketOutgoingHandshakeResponse;

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