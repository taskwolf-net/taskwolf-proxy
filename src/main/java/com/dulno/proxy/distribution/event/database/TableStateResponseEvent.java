package com.dulno.proxy.distribution.event.database;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import com.dulno.core.database.transformation.DatabaseTransformationState;
import com.dulno.core.event.Event;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor(staticName = "create")
public final class TableStateResponseEvent extends Event {
  private final String tableClass;
  private final DatabaseTransformationState state;
}