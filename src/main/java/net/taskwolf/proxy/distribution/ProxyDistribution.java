package net.taskwolf.proxy.distribution;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.event.EventExecutor;
import net.taskwolf.core.event.HookRegistry;
import net.taskwolf.core.log.Log;
import net.taskwolf.core.organization.Organization;
import net.taskwolf.core.organization.OrganizationDatabaseTable;
import net.taskwolf.core.packet.PacketEventRepository;
import net.taskwolf.core.packet.PacketRegistry;
import net.taskwolf.core.user.User;
import net.taskwolf.core.user.UserDatabaseTable;
import net.taskwolf.core.worker.packet.outgoing.PacketOutgoing;
import net.taskwolf.proxy.distribution.client.ProxyClient;
import net.taskwolf.proxy.distribution.client.ProxyClientRegistry;
import net.taskwolf.proxy.distribution.event.database.TableDiscrepancyEvent;
import net.taskwolf.proxy.distribution.event.database.TableStateRequestEvent;
import net.taskwolf.proxy.distribution.event.database.TableStateResponseEvent;
import net.taskwolf.proxy.distribution.event.node.NodeDisconnectEvent;
import net.taskwolf.proxy.distribution.event.node.NodeModuleLoadEvent;
import net.taskwolf.proxy.distribution.event.node.NodeModuleUnloadEvent;
import net.taskwolf.proxy.distribution.event.node.NodePongEvent;
import net.taskwolf.proxy.distribution.packet.incoming.database.PacketIncomingTableDiscrepancy;
import net.taskwolf.proxy.distribution.packet.incoming.database.PacketIncomingTableStateRequest;
import net.taskwolf.proxy.distribution.packet.incoming.database.PacketIncomingTableStateResponse;
import net.taskwolf.proxy.distribution.packet.incoming.node.*;
import net.taskwolf.proxy.distribution.packet.outgoing.user.PacketOutgoingUsersReorganize;
import net.taskwolf.proxy.distribution.server.ProxyServer;
import net.taskwolf.proxy.distribution.server.database.TableDiscrepancyHook;
import net.taskwolf.proxy.distribution.server.database.TableStateRequestHook;
import net.taskwolf.proxy.distribution.server.database.TableStateResponseHook;
import net.taskwolf.proxy.distribution.server.node.*;

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
    packetRegistry.registerPacket(PacketIncomingModuleLoad.class);
    packetRegistry.registerPacket(PacketIncomingModuleUnload.class);
    packetRegistry.registerPacket(PacketIncomingTableDiscrepancy.class);
    packetRegistry.registerPacket(PacketIncomingTableStateRequest.class);
    packetRegistry.registerPacket(PacketIncomingTableStateResponse.class);
  }

  private void registerHooks() {
    hookRegistry.register(NodeHandshakeRequestHook.create(configuration,
      packetRegistry, eventExecutor, clientRegistry, packetEventRepository, log));
    hookRegistry.register(injector.getInstance(NodePongHook.class));
    hookRegistry.register(injector.getInstance(NodeModuleLoadHook.class));
    hookRegistry.register(injector.getInstance(NodeModuleUnloadHook.class));
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
    packetEventRepository.<ProxyClient, PacketIncomingModuleLoad>registerEvent(
      PacketIncomingModuleLoad.class, (client, packet) ->
        NodeModuleLoadEvent.create(client, packet.module()));
    packetEventRepository.<ProxyClient, PacketIncomingModuleUnload>registerEvent(
      PacketIncomingModuleUnload.class, (client, packet) ->
        NodeModuleUnloadEvent.create(client, packet.module()));
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
    findLoadedModules().forEach(this::reorganizeUsers);
  }

  /**
   * Reorganizes users that are assigned to a module in the whole distribution
   * network (on all nodes) (enables constant and equal user distribution)
   * @param module The module that will be reorganized
   */
  public void reorganizeUsers(String module) {
    findAllPossibleUser().thenAccept(users -> reorganizeUsers(module, users));
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
    var result = Lists.<List<UUID>>newArrayList();
    int size = (int) Math.floor((double) allUsers.size() / nodes);
    for (var start = 0; start < allUsers.size(); start += size) {
      var end = Math.min(start + size, allUsers.size());
      result.add(allUsers.subList(start, end));
    }
    return result;
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
