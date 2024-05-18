package net.taskwolf.proxy.distribution.channel;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.event.EventExecutor;
import net.taskwolf.core.packet.PacketDecoder;
import net.taskwolf.core.packet.PacketEncoder;
import net.taskwolf.core.packet.PacketEventRepository;
import net.taskwolf.core.packet.PacketRegistry;
import net.taskwolf.proxy.distribution.client.ProxyClientRegistry;

@RequiredArgsConstructor(staticName = "create")
public final class ChannelEquipment extends ChannelInitializer<Channel> {
  private final PacketRegistry packetRegistry;
  private final EventExecutor eventExecutor;
  private final ProxyClientRegistry distributionClientRegistry;
  private final PacketEventRepository packetEventRepository;

  @Override
  protected void initChannel(Channel channel) {
    equipChannel(channel);
  }

  private void equipChannel(Channel channel) {
    channel.pipeline().addLast("channel-encoder", PacketEncoder.create());
    channel.pipeline().addLast("channel-decoder", PacketDecoder.create(
      packetRegistry));
    channel.pipeline().addLast("chanel-inbox", ChannelInbox.create(eventExecutor,
      distributionClientRegistry, packetEventRepository));
  }
}