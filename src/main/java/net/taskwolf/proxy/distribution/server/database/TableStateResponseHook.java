package net.taskwolf.proxy.distribution.server.database;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.event.EventHook;
import net.taskwolf.core.event.Hook;
import net.taskwolf.proxy.distribution.event.database.TableStateRequestEvent;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class TableStateResponseHook implements Hook {
  private final TableTransformationRepository transformationRepository;

  @EventHook
  private void tableStateResponse(TableStateRequestEvent event) {

  }
}
