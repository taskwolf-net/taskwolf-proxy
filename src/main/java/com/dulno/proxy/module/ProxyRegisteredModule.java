package com.dulno.proxy.module;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.io.File;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class ProxyRegisteredModule {
  private final ProxyModule module;
  private final String name;
  private final String version;
  private final ProxyModuleLoadPriority priority;
  private final File file;
}
