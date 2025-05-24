package org.api.service;

import org.api.dto.ActionType;

import java.time.LocalDateTime;

public class LogExecutor {

  public static void log(ActionType actionType, String tableName, Object recordId) {
    String timestamp = LocalDateTime.now().toString();
    System.out.printf(
            "[%s] Action: %s | Table: %s | Record ID: %s%n",
            timestamp,
            actionType,
            tableName,
            recordId != null ? recordId.toString() : "N/A"
    );
  }
}
