package net.taskwolf.proxy.distribution.client;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class ProxyClientRegistry {
  private final List<ProxyClient> clients = Lists.newArrayList();

  /**
   * Registers a new distribution client
   * @param client The new distribution client
   */
  public void registerClient(ProxyClient client) {
    clients.add(client);
  }


  /**
   * Unregisters a distribution client
   * @param client The client that is to be unregistered
   */
  public void unregisterClient(ProxyClient client) {
    clients.remove(client);
  }

  /**
   * Is used to find all registered distribution clients
   * @return The list of all distribution clients
   */
  public List<ProxyClient> findAllClients() {
    return List.copyOf(clients);
  }
}

