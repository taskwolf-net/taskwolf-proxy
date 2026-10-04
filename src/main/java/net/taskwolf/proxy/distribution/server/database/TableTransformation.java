package net.taskwolf.proxy.distribution.server.database;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor(staticName = "create")
public final class TableTransformation {
  private final String tableClass;
  private final int replicas;
  private int discrepancies;

  public void addDiscrepancy() {
    discrepancies++;
  }
}
