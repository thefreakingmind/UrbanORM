package org.api.service;

import org.api.builder.SQLBuilder;
import org.api.configuration.DBConfig;
import org.api.dto.ActionType;
import org.api.model.Column;
import org.api.model.Table;
import org.api.utils.QueryExecutorUtils;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

import static org.api.utils.DBHandlerUtils.executeInBothDB;
import static org.api.utils.DBHandlerUtils.executeUpdateWithPrimaryKey;
import static org.api.utils.QueryExecutorUtils.executeWithParams;

public class QueryExcutionManager {

    public static boolean isDualWriteEnabled = true;

    public static void setDualWriteEnabled(boolean enabled) {
        isDualWriteEnabled = enabled;
    }

    public static <T> void createTable(Class<T> clazz) {
        if (!clazz.isAnnotationPresent(Table.class)) return;
        String sql = SQLBuilder.buildCreateTableSQL(clazz);
        executeInBothDB(sql, isDualWriteEnabled, ActionType.CREATE, clazz.getAnnotation(Table.class).name(), "N/A");
    }

    public static <T> void insert(T obj) {
        Class<?> clazz = obj.getClass();
        List<Field> fields = getNonAutoIncrementFields(clazz);
        String sql = SQLBuilder.buildInsertSQL(clazz, fields);
        executeWithParams(obj, sql, fields, ActionType.CREATE, isDualWriteEnabled);
    }

    public static <T> List<T> findAll(Class<T> clazz) {
        String sql = SQLBuilder.buildSelectAllSQL(clazz);
        try (Connection conn = DBConfig.getPrimaryConnection();
            Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            List<T> result = new ArrayList<>();
            while (rs.next()) {
                result.add(QueryExecutorUtils.populateFromResultSet(clazz, rs));
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public static <T> T findById(Class<T> clazz, int id) {
        Field pkField = getPrimaryKeyField(clazz).orElseThrow(() -> new RuntimeException("No primary key found"));
        String sql = SQLBuilder.buildSelectByIdSQL(clazz, pkField);
        try (Connection conn = DBConfig.getPrimaryConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? QueryExecutorUtils.populateFromResultSet(clazz, rs) : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T> void update(T obj) {
        Class<?> clazz = obj.getClass();
        Field pkField = getPrimaryKeyField(clazz).orElseThrow();
        List<Field> fields = getUpdatableFields(clazz);
        String sql = SQLBuilder.buildUpdateSQL(clazz, fields, pkField);
        executeUpdateWithPrimaryKey(obj, isDualWriteEnabled, sql, fields, pkField, ActionType.UPDATE);
    }

    public static <T> void deleteById(Class<T> clazz, int id) {
        Field pkField = getPrimaryKeyField(clazz).orElseThrow();
        String sql = SQLBuilder.buildDeleteSQL(clazz, pkField);
        executeInBothDB(sql, isDualWriteEnabled, ActionType.DELETE, clazz.getAnnotation(Table.class).name(), String.valueOf(id), id);
    }

    private static <T> List<Field> getNonAutoIncrementFields(Class<T> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(Column.class) && !f.getAnnotation(Column.class).autoIncrement())
                .peek(f -> f.setAccessible(true))
                .collect(Collectors.toList());
    }

    private static <T> List<Field> getUpdatableFields(Class<T> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(Column.class) && !f.getAnnotation(Column.class).primaryKey())
                .peek(f -> f.setAccessible(true))
                .collect(Collectors.toList());
    }

    public static <T> Optional<Field> getPrimaryKeyField(Class<T> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(Column.class) && f.getAnnotation(Column.class).primaryKey())
                .findFirst();
    }

}
