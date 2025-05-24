package org.api.builder;

import org.api.model.Column;
import org.api.model.Table;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SQLBuilder {

    public static <T> String buildCreateTableSQL(Class<T> clazz) {
        Table table = clazz.getAnnotation(Table.class);
        String tableName = table.name();

        String columnsDef = Arrays.stream(clazz.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(Column.class))
                .map(SQLBuilder::columnDefinition)
                .collect(Collectors.joining(", "));

        return "CREATE TABLE IF NOT EXISTS " + tableName + " (" + columnsDef + ")";
    }

    public static <T> String buildInsertSQL(Class<T> clazz, List<Field> fields) {
        String tableName = clazz.getAnnotation(Table.class).name();
        String columns = fields.stream()
                .map(f -> f.getAnnotation(Column.class).name())
                .collect(Collectors.joining(", "));
        String placeholders = fields.stream().map(f -> "?").collect(Collectors.joining(", "));
        return "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + placeholders + ")";
    }

    public static <T> String buildSelectAllSQL(Class<T> clazz) {
        return "SELECT * FROM " + clazz.getAnnotation(Table.class).name();
    }

    public static <T> String buildSelectByIdSQL(Class<T> clazz, Field pkField) {
        Column column = pkField.getAnnotation(Column.class);
        return "SELECT * FROM " + clazz.getAnnotation(Table.class).name() +
                " WHERE " + column.name() + " = ?";
    }

    public static <T> String buildUpdateSQL(Class<T> clazz, List<Field> fields, Field pkField) {
        String tableName = clazz.getAnnotation(Table.class).name();
        String setClause = fields.stream()
                .map(f -> f.getAnnotation(Column.class).name() + " = ?")
                .collect(Collectors.joining(", "));
        String pkColumnName = pkField.getAnnotation(Column.class).name();
        return "UPDATE " + tableName + " SET " + setClause + " WHERE " + pkColumnName + " = ?";
    }

    public static <T> String buildDeleteSQL(Class<T> clazz, Field pkField) {
        String tableName = clazz.getAnnotation(Table.class).name();
        String pkColumnName = pkField.getAnnotation(Column.class).name();
        return "DELETE FROM " + tableName + " WHERE " + pkColumnName + " = ?";
    }

    private static String columnDefinition(Field field) {
        Column column = field.getAnnotation(Column.class);
        String type = field.getType().equals(int.class) ? "INT" :
                field.getType().equals(String.class) ? "VARCHAR(255)" : "TEXT";
        String constraints = (column.primaryKey() ? " PRIMARY KEY" : "") +
                (column.autoIncrement() ? " AUTO_INCREMENT" : "");
        return column.name() + " " + type + constraints;
    }
}

