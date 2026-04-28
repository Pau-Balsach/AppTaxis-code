package repository;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Properties;

public class ConfigLoader {

    private static final String CONFIG_FILE = "config.properties";
    private static Properties cached = null;

    public static Properties get() {
        if (cached != null) return cached;

        Properties props = new Properties();

        String appDir = System.getProperty("app.dir");
        if (appDir != null && !appDir.isBlank()) {
            Path candidate = Paths.get(appDir, "config", CONFIG_FILE);
            if (Files.exists(candidate)) {
                try (InputStream in = new FileInputStream(candidate.toFile());
                     InputStreamReader isr = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                    props.load(isr);
                    cached = props;
                    return cached;
                } catch (IOException e) {
                    throw new RuntimeException("No se pudo leer config.properties en: " + candidate, e);
                }
            }
        }

        try (InputStream in = ConfigLoader.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (in == null) throw new RuntimeException(
                "config.properties no encontrado ni en app.dir ni en classpath.");
            props.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            cached = props;
            return cached;
        } catch (IOException e) {
            throw new RuntimeException("Error leyendo config.properties desde classpath.", e);
        }
    }
}