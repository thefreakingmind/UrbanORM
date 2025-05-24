package org.api.utils;

import org.api.configuration.DBConfig;
import org.api.dto.ActionType;
import org.api.model.Column;
import org.api.model.Table;
import org.api.service.LogExecutor;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class QueryExecutorUtils {

  public static <T> Stream<Field> getInsertableFields(Class<T> clazz) {
    return Arrays.stream(clazz.getDeclaredFields())
            .filter(f -> f.isAnnotationPresent(Column.class) && !f.getAnnotation(Column.class).autoIncrement());
  }

  public static <T> Stream<Field> getUpdatableFields(Class<T> clazz) {
    return Arrays.stream(clazz.getDeclaredFields())
            .filter(f -> f.isAnnotationPresent(Column.class) && !f.getAnnotation(Column.class).primaryKey());
  }

  public static <T> Stream<String> getColumnDefinitions(Class<T> clazz) {
    return Arrays.stream(clazz.getDeclaredFields())
            .filter(f -> f.isAnnotationPresent(Column.class))
            .map(f -> {
              Column c = f.getAnnotation(Column.class);
              String type = f.getType().equals(int.class) ? "INT" :
                      f.getType().equals(String.class) ? "VARCHAR(255)" : "TEXT";
              String constraints = (c.primaryKey() ? " PRIMARY KEY" : "") +
                      (c.autoIncrement() ? " AUTO_INCREMENT" : "");
              return c.name() + " " + type + constraints;
            });
  }

  public static <T> List<Object> extractInsertValues(T obj) throws IllegalAccessException {
    List<Object> values = new ArrayList<>();
    for (Field field : getInsertableFields(obj.getClass()).toList()) {
      field.setAccessible(true);
      values.add(field.get(obj));
    }
    return values;
  }

  public static <T> List<Object> extractUpdateValues(T obj) throws IllegalAccessException {
    List<Object> values = new ArrayList<>();
    for (Field field : getUpdatableFields(obj.getClass()).toList()) {
      field.setAccessible(true);
      values.add(field.get(obj));
    }
    values.add(getPrimaryKeyValue(obj));
    return values;
  }

  public static <T> Optional<Field> getPrimaryKeyField(Class<T> clazz) {
    return Arrays.stream(clazz.getDeclaredFields())
            .filter(f -> f.isAnnotationPresent(Column.class) && f.getAnnotation(Column.class).primaryKey())
            .findFirst();
  }

  public static <T> Object getPrimaryKeyValue(T obj) throws IllegalAccessException {
    Optional<Field> pkFieldOpt = getPrimaryKeyField(obj.getClass());
    if (!pkFieldOpt.isPresent()) return null;
    Field pkField = pkFieldOpt.get();
    pkField.setAccessible(true);
    return pkField.get(obj);
  }

  public static <T> T populateFromResultSet(Class<T> clazz, ResultSet rs) throws Exception {
    T instance = clazz.getDeclaredConstructor().newInstance();
    for (Field field : clazz.getDeclaredFields()) {
      if (!field.isAnnotationPresent(Column.class)) continue;
      Column col = field.getAnnotation(Column.class);
      field.setAccessible(true);
      Object value = rs.getObject(col.name());
      field.set(instance, value);
    }
    return instance;
  }

  public static <T> void executeWithParams(T obj, String sql, List<Field> fields, ActionType actionType,
                                           boolean isDualWriteEnabled) {
    String tableName = obj.getClass().getAnnotation(Table.class).name();
    long generatedId = -1;
    try (Connection primaryConn = DBConfig.getPrimaryConnection();
         PreparedStatement primaryStmt = primaryConn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      setPreparedStatementFields(primaryStmt, fields, obj);
      primaryStmt.executeUpdate();
      try (ResultSet generatedKeys = primaryStmt.getGeneratedKeys()) {
        if (generatedKeys.next()) {
          generatedId = generatedKeys.getLong(1);
        }
      }
      LogExecutor.log(actionType, tableName, String.valueOf(generatedId));
      if (isDualWriteEnabled) {
        try (Connection secondaryConn = DBConfig.getSecondaryConnection();
             PreparedStatement secondaryStmt = secondaryConn.prepareStatement(sql)) {
          setPreparedStatementFields(secondaryStmt, fields, obj);
          secondaryStmt.executeUpdate();
          LogExecutor.log(actionType, tableName, "DualWrite_ID=" + generatedId);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static <T> void setPreparedStatementFields(PreparedStatement stmt, List<Field> fields, T obj) {
    IntStream.range(0, fields.size())
            .forEach(i -> {
              try {
                fields.get(i).setAccessible(true);
                stmt.setObject(i + 1, fields.get(i).get(obj));
              } catch (IllegalAccessException | SQLException e) {
                throw new RuntimeException(e);
              }
            });
  }


}
