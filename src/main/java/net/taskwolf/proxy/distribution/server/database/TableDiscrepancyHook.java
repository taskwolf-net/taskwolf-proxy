package net.taskwolf.proxy.distribution.server.database;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.taskwolf.core.event.EventHook;
import net.taskwolf.core.event.Hook;
import net.taskwolf.core.log.Log;
import net.taskwolf.proxy.distribution.client.ProxyClientRegistry;
import net.taskwolf.proxy.distribution.event.database.TableDiscrepancyEvent;
import net.taskwolf.proxy.distribution.packet.outgoing.database.PacketOutgoingTableTransform;

@Singleton
@RequiredArgsConstructor(access = AccessLevel.PRIVATE, onConstructor = @__({@Inject}))
public final class TableDiscrepancyHook implements Hook {
  private final Log log;
  private final ProxyClientRegistry clientRegistry;
  private final TableTransformationRepository transformationRepository;

  @EventHook
  private void tableDiscrepancy(TableDiscrepancyEvent event) throws Exception {
    var tableClass = event.tableClass();
    transformationRepository.recogniseDiscrepancy(tableClass)
      .thenAccept(ready -> processDiscrepancy(tableClass, ready));
  }

  private void processDiscrepancy(String tableClass, boolean ready) {
    if (!ready) {
      return;
    }
    clientRegistry.findAllClients().stream().findAny().get()
      .sendPacket(new PacketOutgoingTableTransform(tableClass));
    log.info("A discrepancy was found in the structure of the " + tableClass +
      " table. All pods have recognized this condition and reported it. " +
      "Therefore, the transformation is now started.");
  }
}

