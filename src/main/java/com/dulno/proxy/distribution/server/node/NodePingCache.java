package com.dulno.proxy.distribution.server.node;

import com.datastax.oss.driver.shaded.guava.common.collect.Maps;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import com.dulno.proxy.distribution.client.ProxyClient;

import java.util.List;
import java.util.Map;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class NodePingCache {
  private final Map<ProxyClient, Integer> pings = Maps.newHashMap();

  public void insertNodePing(ProxyClient client, int value) {
    pings.put(client, value);
  }

  public void removeNodePing(ProxyClient client) {
    pings.remove(client);
  }

  public void clear() {
    pings.clear();
  }

  public boolean pingExists(ProxyClient client) {
    return pings.containsKey(client);
  }

  public int findPingValue(ProxyClient client) {
    return pings.get(client);
  }

  public List<ProxyClient> findPendingPingClients() {
    return Lists.newArrayList(pings.keySet());
  }
}
