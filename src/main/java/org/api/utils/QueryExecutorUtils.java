package org.api.utils;

import org.api.configuration.DBConfig;
import org.api.model.Column;
import org.api.model.Table;
import org.api.service.QueryExecutor;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;
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
}
