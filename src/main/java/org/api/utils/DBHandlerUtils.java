package org.api.utils;

import org.api.configuration.DBConfig;
import org.api.dto.ActionType;
import org.api.model.Table;
import org.api.service.LogExecutor;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

public class DBHandlerUtils {

    public static void executeInBothDB(String sql, boolean isDualWriteEnabled, ActionType action, String tableName, String id, Object... params) {
        try (Connection primaryConn = DBConfig.getPrimaryConnection();
             PreparedStatement primaryStmt = primaryConn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                primaryStmt.setObject(i + 1, params[i]);
            }
            primaryStmt.executeUpdate();
            LogExecutor.log(action, tableName, id);

            if (isDualWriteEnabled) {
                try (Connection secondaryConn = DBConfig.getSecondaryConnection();
                     PreparedStatement secondaryStmt = secondaryConn.prepareStatement(sql)) {
                    for (int i = 0; i < params.length; i++) {
                        secondaryStmt.setObject(i + 1, params[i]);
                    }
                    secondaryStmt.executeUpdate();
                    LogExecutor.log(action, tableName, id);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static <T> void executeUpdateWithPrimaryKey(T obj, boolean isDualWriteEnabled, String sql, List<Field> fields, Field pkField, ActionType action) {
        try (Connection primaryConn = DBConfig.getPrimaryConnection();
             PreparedStatement primaryStmt = primaryConn.prepareStatement(sql)) {
            for (int i = 0; i < fields.size(); i++) {
                primaryStmt.setObject(i + 1, fields.get(i).get(obj));
            }
            pkField.setAccessible(true);
            Object id = pkField.get(obj);
            primaryStmt.setObject(fields.size() + 1, id);
            primaryStmt.executeUpdate();
            LogExecutor.log(action, obj.getClass().getAnnotation(Table.class).name(), id.toString());

            if (isDualWriteEnabled) {
                try (Connection secondaryConn = DBConfig.getSecondaryConnection();
                     PreparedStatement secondaryStmt = secondaryConn.prepareStatement(sql)) {
                    for (int i = 0; i < fields.size(); i++) {
                        secondaryStmt.setObject(i + 1, fields.get(i).get(obj));
                    }
                    secondaryStmt.setObject(fields.size() + 1, id);
                    secondaryStmt.executeUpdate();
                    LogExecutor.log(action, obj.getClass().getAnnotation(Table.class).name(), id.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
