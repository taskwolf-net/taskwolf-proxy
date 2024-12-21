package com.dulno.proxy.distribution;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import com.dulno.core.event.EventExecutor;
import com.dulno.core.event.HookRegistry;
import com.dulno.core.log.Log;
import com.dulno.core.organization.Organization;
import com.dulno.core.organization.OrganizationDatabaseTable;
import com.dulno.core.packet.PacketEventRepository;
import com.dulno.core.packet.PacketRegistry;
import com.dulno.core.user.User;
import com.dulno.core.user.UserDatabaseTable;
import com.dulno.core.worker.packet.outgoing.PacketOutgoing;
import com.dulno.proxy.distribution.client.ProxyClient;
import com.dulno.proxy.distribution.client.ProxyClientRegistry;
import com.dulno.proxy.distribution.event.database.TableDiscrepancyEvent;
import com.dulno.proxy.distribution.event.database.TableStateRequestEvent;
import com.dulno.proxy.distribution.event.database.TableStateResponseEvent;
import com.dulno.proxy.distribution.event.node.NodeDisconnectEvent;
import com.dulno.proxy.distribution.event.node.NodeModulesLoadEvent;
import com.dulno.proxy.distribution.event.node.NodeModulesUnloadEvent;
import com.dulno.proxy.distribution.event.node.NodePongEvent;
import com.dulno.proxy.distribution.packet.incoming.database.PacketIncomingTableDiscrepancy;
import com.dulno.proxy.distribution.packet.incoming.database.PacketIncomingTableStateRequest;
import com.dulno.proxy.distribution.packet.incoming.database.PacketIncomingTableStateResponse;
import com.dulno.proxy.distribution.packet.incoming.node.*;
import com.dulno.proxy.distribution.packet.outgoing.user.PacketOutgoingUsersReorganize;
import com.dulno.proxy.distribution.server.ProxyServer;
import com.dulno.proxy.distribution.server.database.TableDiscrepancyHook;
import com.dulno.proxy.distribution.server.database.TableStateRequestHook;
import com.dulno.proxy.distribution.server.database.TableStateResponseHook;
import com.dulno.proxy.distribution.server.node.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class ProxyDistribution {
  private final Injector injector;
  private final UserDatabaseTable userDatabaseTable;
  private final OrganizationDatabaseTable organizationDatabaseTable;
  private final ProxyDistributionConfiguration configuration;
  private final PacketRegistry packetRegistry;
  private final EventExecutor eventExecutor;
  private final HookRegistry hookRegistry;
  private final ProxyClientRegistry clientRegistry;
  private final PacketEventRepository packetEventRepository;
  private final Log log;
  private ProxyServer proxyServer;

  /**
   * Initializes node distribution (registers packets, hooks, events and
   * opens server)
   * @throws Exception
   */
  public void initialize() throws Exception {
    proxyServer = ProxyServer.create(packetRegistry, eventExecutor,
      clientRegistry, packetEventRepository,
      configuration.distributionPort());
    registerPackets();
    registerHooks();
    registerEvents();
    proxyServer.openAsync(() -> {});
  }

  private void registerPackets() throws Exception {
    packetRegistry.registerPacket(PacketIncomingHandshakeRequest.class);
    packetRegistry.registerPacket(PacketIncomingPong.class);
    packetRegistry.registerPacket(PacketIncomingDisconnect.class);
    packetRegistry.registerPacket(PacketIncomingModulesLoad.class);
    packetRegistry.registerPacket(PacketIncomingModulesUnload.class);
    packetRegistry.registerPacket(PacketIncomingTableDiscrepancy.class);
    packetRegistry.registerPacket(PacketIncomingTableStateRequest.class);
    packetRegistry.registerPacket(PacketIncomingTableStateResponse.class);
  }

  private void registerHooks() {
    hookRegistry.register(NodeHandshakeRequestHook.create(configuration,
      packetRegistry, eventExecutor, clientRegistry, packetEventRepository, log));
    hookRegistry.register(injector.getInstance(NodePongHook.class));
    hookRegistry.register(injector.getInstance(NodeModulesLoadHook.class));
    hookRegistry.register(injector.getInstance(NodeModulesUnloadHook.class));
    hookRegistry.register(NodeDisconnectHook.create(this, clientRegistry, log));
    hookRegistry.register(injector.getInstance(TableDiscrepancyHook.class));
    hookRegistry.register(injector.getInstance(TableStateRequestHook.class));
    hookRegistry.register(injector.getInstance(TableStateResponseHook.class));
  }

  private void registerEvents() {
    packetEventRepository.<ProxyClient, PacketIncomingPong>registerEvent(
      PacketIncomingPong.class, (client, packet) ->
        NodePongEvent.create(client, packet.value()));
    packetEventRepository.<ProxyClient, PacketIncomingDisconnect>registerEvent(
      PacketIncomingDisconnect.class, (client, packet) -> NodeDisconnectEvent.create(
        client, NodeDisconnectEvent.DisconnectReason.SHUTDOWN));
    packetEventRepository.<ProxyClient, PacketIncomingModulesLoad>registerEvent(
      PacketIncomingModulesLoad.class, (client, packet) ->
        NodeModulesLoadEvent.create(client, packet.modules()));
    packetEventRepository.<ProxyClient, PacketIncomingModulesUnload>registerEvent(
      PacketIncomingModulesUnload.class, (client, packet) ->
        NodeModulesUnloadEvent.create(client, packet.modules()));
    packetEventRepository.registerEvent(PacketIncomingTableDiscrepancy.class,
      (client, packet) -> TableDiscrepancyEvent.create(packet.tableClass()));
    packetEventRepository.<ProxyClient, PacketIncomingTableStateRequest>registerEvent(
      PacketIncomingTableStateRequest.class, (client, packet) ->
        TableStateRequestEvent.create(client, packet.tableClass(), packet.state()));
    packetEventRepository.registerEvent(PacketIncomingTableStateResponse.class,
      (client, packet) -> TableStateResponseEvent.create(packet.tableClass(),
        packet.state()));
  }

  /**
   * Reorganizes users that are assigned to every loaded module in the whole
   * distribution network (on all nodes) (enables constant and equal user distribution)
   */
  public void reorganizeUsers() {
    findAllPossibleUser().thenAccept(users -> findLoadedModules()
      .forEach(module -> reorganizeUsers(module, users)));
  }

  private void reorganizeUsers(String module, List<UUID> allUsers) {
    var nodes = clientRegistry.findAllClients().stream().filter(client ->
      client.condition().isModuleLoaded(module)).toList();
    var dividedUsers = divideUsers(allUsers, nodes.size());
    for (int i = 0; i < nodes.size(); i++) {
      nodes.get(i).sendPacket(new PacketOutgoingUsersReorganize(module,
        dividedUsers.get(i)));
    }
  }

  private List<List<UUID>> divideUsers(List<UUID> allUsers, long nodes) {
    var size = allUsers.size();
    var partSize = (int) (size / nodes);
    var remainder = (int) (size % nodes);
    var parts = Lists.<List<UUID>>newArrayList();
    for (int i = 0, start = 0; i < nodes; i++) {
      var end = start + partSize + (i < remainder ? 1 : 0);
      parts.add(Lists.newArrayList(allUsers.subList(start, end)));
      start = end;
    }
    return parts;
  }

  /**
   * Is used to find all user and organization ids
   * @return The list of ids
   */
  public CompletableFuture<List<UUID>> findAllPossibleUser() {
    var futureResponse = new CompletableFuture<List<UUID>>();
    new Thread(() -> userDatabaseTable.findAllUsers().thenAccept(users ->
      organizationDatabaseTable.findAllOrganization().thenAccept(organizations ->
        futureResponse.complete(Stream.concat(users.stream().map(User::id),
          organizations.stream().map(Organization::id)).collect(Collectors.toList())))))
      .start();
    return futureResponse;
  }

  /**
   * Is used to find all loaded modules
   * @return The list of loaded modules
   */
  public List<String> findLoadedModules() {
    var loadedModule = Lists.<String>newArrayList();
    for (var client : clientRegistry.findAllClients()) {
      loadedModule.addAll(client.condition().findLoadedModules());
    }
    return loadedModule.stream().distinct().toList();
  }

  /**
   * Sends an outgoing packet to all registered clients
   * @param packet The packet that will be sent
   */
  public void broadcastPacket(PacketOutgoing packet) {
    proxyServer.broadcastPacket(packet);
  }

  /**
   * Closes server
   */
  public void disconnect() {
    proxyServer.close();
  }
}
