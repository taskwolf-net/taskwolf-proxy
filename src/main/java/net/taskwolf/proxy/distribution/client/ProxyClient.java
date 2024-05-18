package net.taskwolf.proxy.distribution.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.taskwolf.core.event.EventExecutor;
import net.taskwolf.core.packet.PacketEventRepository;
import net.taskwolf.core.packet.PacketRegistry;
import net.taskwolf.core.worker.packet.outgoing.PacketOutgoing;
import net.taskwolf.proxy.distribution.channel.ChannelEquipment;
import net.taskwolf.proxy.distribution.event.node.NodeDisconnectEvent;

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
   * Connects the client to the server in the background
   * @param callback A future that is called when the connection process is completed
   */
  public void connectAsync(Runnable callback) {
    new Thread(() -> connect(callback)).start();
  }

  private void connect(Runnable callback) {
    connect();
    callback.run();
  }

  /**
   * Connects the client to the server
   */
  public void connect() {
    try {
      group = new NioEventLoopGroup();
      channel = new Bootstrap()
        .group(group)
        .channel(NioSocketChannel.class)
        .handler(ChannelEquipment.create(packetRegistry, eventExecutor,
          clientRegistry, packetEventRepository))
        .connect(hostname, -1)
        .syncUninterruptibly().channel();
    } catch (Exception exception) {
      eventExecutor.execute(NodeDisconnectEvent.create(this,
        NodeDisconnectEvent.DisconnectReason.CONNECTION_FAILED));
    }
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

