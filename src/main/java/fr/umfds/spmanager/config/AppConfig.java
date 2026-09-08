package fr.umfds.spmanager.config;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Properties;

public class AppConfig {
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = AppConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input != null) {
                properties.load(input);
            } else {
                System.err.println("application.properties non trouvé dans le classpath.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getServerPort() {
        return Integer.parseInt(properties.getProperty("server.port", "7000"));
    }

    public static String getDbUrl() {
        return properties.getProperty("db.url", "jdbc:sqlite:project_agl.db");
    }

    public static String getJwtSecret() {
        return properties.getProperty("jwt.secret", "projet-agl-secret-key-for-jwt-signing-2026-2027-very-secure-key-32bytes");
    }

    public static LocalDateTime getPreferenceStartDate() {
        String val = properties.getProperty("preference.period.start", "2026-09-01T00:00:00");
        return LocalDateTime.parse(val);
    }

    public static LocalDateTime getPreferenceEndDate() {
        String val = properties.getProperty("preference.period.end", "2027-06-30T23:59:59");
        return LocalDateTime.parse(val);
    }
}
