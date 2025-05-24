package org.api.configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConfig {

  private static final String PRIMARY_DB_URL = "jdbc:mysql://localhost:3306/urbanDB";
  private static final String SECONDARY_DB_URL = "jdbc:mysql://localhost:3306/urbanDBSlave";
  private static final String DB_USER = "root";
  private static final String DB_PASSWORD = "Mpasas1109123@";

  public static Connection getPrimaryConnection() throws SQLException {
    return DriverManager.getConnection(PRIMARY_DB_URL, DB_USER, DB_PASSWORD);
  }

  public static Connection getSecondaryConnection() throws SQLException {
    return DriverManager.getConnection(SECONDARY_DB_URL, DB_USER, DB_PASSWORD);
  }
}
