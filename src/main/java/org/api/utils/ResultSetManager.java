package org.api.utils;

import org.api.model.Column;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ResultSetManager {

  public class ResultSetMapper {
    public static <T> List<T> mapResultSetToList(Class<T> clazz, ResultSet rs) throws Exception {
      List<T> result = new ArrayList<>();
      while (rs.next()) {
        T instance = clazz.getDeclaredConstructor().newInstance();
        for (Field field : clazz.getDeclaredFields()) {
          if (!field.isAnnotationPresent(Column.class)) continue;
          Column col = field.getAnnotation(Column.class);
          field.setAccessible(true);
          field.set(instance, rs.getObject(col.name()));
        }
        result.add(instance);
      }
      return result;
    }
  }
}
