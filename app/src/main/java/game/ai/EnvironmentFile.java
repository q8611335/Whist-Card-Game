package game.ai;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/** Loads an AI credential from the process environment or local .env file. */
public final class EnvironmentFile {
    private EnvironmentFile() {
    }

    public static String get(String key) {
        String value = System.getenv(key);
        if (value != null && !value.isBlank()) return value.trim();
        try (InputStream input = EnvironmentFile.class.getClassLoader().getResourceAsStream(".env")) {
            if (input == null) return null;
            Properties values = new Properties();
            values.load(new InputStreamReader(input, StandardCharsets.UTF_8));
            value = values.getProperty(key);
            return value == null || value.isBlank() ? null : value.trim();
        } catch (IOException ignored) {
            return null;
        }
    }
}
