package org.api.service;

import org.api.configuration.DBConfig;
import org.api.dto.ActionType;
import org.api.model.Column;
import org.api.model.Table;
import org.api.utils.QueryExecutorUtils;
import org.api.utils.ResultSetManager;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class QueryExecutor {

  public static <T> void createTable(Class<T> clazz) {
    Optional<Table> tableOpt = Optional.ofNullable(clazz.getAnnotation(Table.class));
    if (!tableOpt.isPresent()) return;

    String tableName = tableOpt.get().name();
    String columnsDef = QueryExecutorUtils.getColumnDefinitions(clazz)
            .collect(Collectors.joining(", "));

    String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " (" + columnsDef + ")";

    try (Connection conn = DBConfig.getConnection(); Statement stmt = conn.createStatement()) {
      stmt.execute(sql);
      LogExecutor.log(ActionType.CREATE, tableName, null);
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public static <T> void insert(T obj) {
    Class<?> clazz = obj.getClass();
    Table table = clazz.getAnnotation(Table.class);
    String tableName = table.name();

    List<Field> fields = Arrays.stream(clazz.getDeclaredFields())
            .filter(f -> f.isAnnotationPresent(Column.class) && !f.getAnnotation(Column.class).autoIncrement())
            .peek(f -> f.setAccessible(true))
            .collect(Collectors.toList());

    String columns = fields.stream().map(f -> f.getAnnotation(Column.class).name()).collect(Collectors.joining(", "));
    String placeholders = fields.stream().map(f -> "?").collect(Collectors.joining(", "));

    String sql = "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + placeholders + ")";

    try (Connection conn = DBConfig.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      for (int i = 0; i < fields.size(); i++) {
        stmt.setObject(i + 1, fields.get(i).get(obj));
      }

      int affectedRows = stmt.executeUpdate();
      if (affectedRows > 0) {
        try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
          if (generatedKeys.next()) {
            int generatedId = generatedKeys.getInt(1);
            LogExecutor.log(ActionType.CREATE, tableName, String.valueOf(generatedId));  // Log with generated ID
          }
        }
      }
    } catch (SQLException | IllegalAccessException e) {
      e.printStackTrace();
    }
  }

  public static <T> List<T> findAll(Class<T> clazz) {
    Table table = clazz.getAnnotation(Table.class);
    String sql = "SELECT * FROM " + table.name();

    try (Connection conn = DBConfig.getConnection(); Statement stmt = conn.createStatement()) {
      ResultSet rs = stmt.executeQuery(sql);
      return ResultSetManager.ResultSetMapper.mapResultSetToList(clazz, rs);
    } catch (Exception e) {
      e.printStackTrace();
      return Collections.emptyList();
    }
  }

  public static <T> T findById(Class<T> clazz, int id) {
    String tableName = getTableName(clazz);
    String pkColumn = getPrimaryKeyField(clazz)
            .map(f -> f.getAnnotation(Column.class).name())
            .orElseThrow(() -> new RuntimeException("No primary key defined"));

    String sql = "SELECT * FROM " + tableName + " WHERE " + pkColumn + " = ?";

    try (Connection conn = DBConfig.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setInt(1, id);
      ResultSet rs = stmt.executeQuery();

      List<T> results = ResultSetManager.ResultSetMapper.mapResultSetToList(clazz, rs);
      return results.isEmpty() ? null : results.get(0);

    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }


  public static <T> void update(T obj) {
    Class<?> clazz = obj.getClass();
    Table table = clazz.getAnnotation(Table.class);
    Field pkField = QueryExecutorUtils.getPrimaryKeyField(clazz)
            .orElseThrow(() -> new RuntimeException("No primary key found"));
    Column pkColumn = pkField.getAnnotation(Column.class);

    List<Field> fields = QueryExecutorUtils.getUpdatableFields(clazz)
            .peek(f -> f.setAccessible(true))
            .collect(Collectors.toList());

    String setClause = fields.stream()
            .map(f -> f.getAnnotation(Column.class).name() + " = ?")
            .collect(Collectors.joining(", "));

    String sql = "UPDATE " + table.name() + " SET " + setClause + " WHERE " + pkColumn.name() + " = ?";

    try (Connection conn = DBConfig.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
      for (int i = 0; i < fields.size(); i++) {
        stmt.setObject(i + 1, fields.get(i).get(obj));
      }
      pkField.setAccessible(true);
      stmt.setObject(fields.size() + 1, pkField.get(obj));
      stmt.executeUpdate();
      LogExecutor.log(ActionType.UPDATE, table.name(), QueryExecutorUtils.getPrimaryKeyValue(obj));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static <T> void deleteById(Class<T> clazz, int id) {
    Table table = clazz.getAnnotation(Table.class);
    Field pkField = QueryExecutorUtils.getPrimaryKeyField(clazz)
            .orElseThrow(() -> new RuntimeException("No primary key found"));
    Column pkColumn = pkField.getAnnotation(Column.class);

    String sql = "DELETE FROM " + table.name() + " WHERE " + pkColumn.name() + " = ?";

    try (Connection conn = DBConfig.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, id);
      stmt.executeUpdate();
      LogExecutor.log(ActionType.DELETE, table.name(), id);
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public static <T> Optional<Field> getPrimaryKeyField(Class<T> clazz) {
    return Arrays.stream(clazz.getDeclaredFields())
            .filter(f -> f.isAnnotationPresent(Column.class) && f.getAnnotation(Column.class).primaryKey())
            .findFirst();
  }

  public static <T> String getTableName(Class<T> clazz) {
    if (!clazz.isAnnotationPresent(Table.class))
      throw new IllegalArgumentException("Class missing @Table annotation");
    return clazz.getAnnotation(Table.class).name();
  }




}
