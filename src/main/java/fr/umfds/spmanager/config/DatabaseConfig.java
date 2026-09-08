package fr.umfds.spmanager.config;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConfig {

    private static String dbUrl = AppConfig.getDbUrl();

    public static void setDbUrl(String url) {
        dbUrl = url;
    }

    public static String getDbUrl() {
        return dbUrl;
    }

    public static Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(dbUrl);
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        return connection;
    }

    public static void initDatabase() {
        try (Connection conn = getConnection()) {
            boolean tablesExist = checkIfTableExists(conn, "users");
            if (!tablesExist) {
                System.out.println("Initialisation du schema et des donnees de la base SQLite...");
                executeSqlScript(conn, "schema.sql");
                executeSqlScript(conn, "data.sql");
                System.out.println("Base de donnees initialisee avec succes.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'initialisation de la base : " + e.getMessage(), e);
        }
    }

    private static boolean checkIfTableExists(Connection conn, String tableName) throws SQLException {
        String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name='" + tableName + "'";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next();
        }
    }

    public static void executeSqlScript(Connection conn, String resourceName) {
        try (InputStream in = DatabaseConfig.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (in == null) {
                System.err.println("Fichier SQL non trouvé : " + resourceName);
                return;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                StringBuilder sql = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("--")) {
                        continue;
                    }
                    sql.append(line).append(" ");
                    if (line.endsWith(";")) {
                        try (Statement stmt = conn.createStatement()) {
                            stmt.execute(sql.toString());
                        }
                        sql.setLength(0);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur d'exécution du script " + resourceName + " : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
