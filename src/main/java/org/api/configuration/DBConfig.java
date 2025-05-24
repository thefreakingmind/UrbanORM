package org.api.configuration;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

public class DBConfig {

  private static final String YAML_FILE_PATH = "/dbConfig.yaml";

  private static String primaryDbUrl;
  private static String secondaryDbUrl;
  private static String dbUser;
  private static String dbPassword;

  static {
    loadDatabaseConfig();
  }

  private static void loadDatabaseConfig() {
    Yaml yaml = new Yaml();
    try (InputStream inputStream = DBConfig.class.getResourceAsStream(YAML_FILE_PATH)) {
      Map<String, Object> obj = yaml.load(inputStream);
      Map<String, String> primary = (Map<String, String>) ((Map) obj.get("database")).get("primary");
      Map<String, String> secondary = (Map<String, String>) ((Map) obj.get("database")).get("secondary");
      primaryDbUrl = primary.get("url");
      secondaryDbUrl = secondary.get("url");
      dbUser = primary.get("username");
      dbPassword = primary.get("password");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static Connection getPrimaryConnection() throws SQLException {
    return DriverManager.getConnection(primaryDbUrl, dbUser, dbPassword);
  }

  public static Connection getSecondaryConnection() throws SQLException {
    return DriverManager.getConnection(secondaryDbUrl, dbUser, dbPassword);
  }
}
