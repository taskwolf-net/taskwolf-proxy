package net.taskwolf.proxy.distribution.client;

/**
 * This enum is used to determine if a client authorized itself and
 * if it is trustworthy
 */
public enum ProxyClientState {
  UNAUTHORIZED,
  AUTHORIZED;

  public boolean isUnauthorized() {
    return this == UNAUTHORIZED;
  }

  public boolean isAuthorized() {
    return this == AUTHORIZED;
  }
}

