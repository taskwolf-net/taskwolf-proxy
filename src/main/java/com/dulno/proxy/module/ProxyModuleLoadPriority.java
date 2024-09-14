package com.dulno.proxy.module;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum ProxyModuleLoadPriority {
  FIRST(2),
  HIGH(1),
  NEUTRAL(0),
  LOW(-1),
  LAST(-2);

  private final int value;
}
