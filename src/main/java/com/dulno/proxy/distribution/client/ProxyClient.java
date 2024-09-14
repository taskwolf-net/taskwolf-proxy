package com.dulno.proxy.distribution.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import com.dulno.core.event.EventExecutor;
import com.dulno.core.packet.PacketEventRepository;
import com.dulno.core.packet.PacketRegistry;
import com.dulno.core.worker.packet.outgoing.PacketOutgoing;
import com.dulno.proxy.distribution.channel.ChannelEquipment;
import com.dulno.proxy.distribution.event.node.NodeDisconnectEvent;

@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class ProxyClient {
  public static ProxyClient of(
    PacketRegistry packetRegistry, EventExecutor eventExecutor,
    ProxyClientRegistry clientRegistry,
    PacketEventRepository packetEventRepository, String hostname,
    Channel channel
  ) {
    return new ProxyClient(packetRegistry, eventExecutor, clientRegistry,
      packetEventRepository, hostname, channel);
  }

  private final PacketRegistry packetRegistry;
  private final EventExecutor eventExecutor;
  private final ProxyClientRegistry clientRegistry;
  private final PacketEventRepository packetEventRepository;
  @Getter
  private final String hostname;
  @Getter
  private ProxyClientState state = ProxyClientState.UNAUTHORIZED;
  @Getter
  private final ProxyClientCondition condition = ProxyClientCondition.create();
  @Getter
  private Channel channel;
  private EventLoopGroup group;

  private ProxyClient(
    PacketRegistry packetRegistry, EventExecutor eventExecutor,
    ProxyClientRegistry clientRegistry,
    PacketEventRepository packetEventRepository,
    String hostname, Channel channel
  ) {
    this.packetRegistry = packetRegistry;
    this.eventExecutor = eventExecutor;
    this.clientRegistry = clientRegistry;
    this.packetEventRepository = packetEventRepository;
    this.hostname = hostname;
    this.channel = channel;
  }

  /**
   * Sends a packet to the server
   * @param packet The packet that is to be send
   * @param <T> The generic type of the packet
   */
  public <T extends PacketOutgoing> void sendPacket(T packet) {
    if (channel == null) {
      return;
    }
    channel.writeAndFlush(packet);
  }

  /**
   * Disconnects the client from the server
   */
  public void disconnect() {
    group.shutdownGracefully();
    channel.close();
  }

  /**
   * Is used to change the state of the client to "authorized"
   */
  public void authorize() {
    state = ProxyClientState.AUTHORIZED;
  }
}

