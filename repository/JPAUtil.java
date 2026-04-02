package repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class JPAUtil {

    private static final EntityManagerFactory emf;

    static {
        try {
            Properties props = new Properties();
            try (InputStream in = JPAUtil.class.getClassLoader().getResourceAsStream("config.properties")) {
                if (in == null) throw new RuntimeException("No se encontró config.properties");
                try (InputStreamReader isr = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                    props.load(isr);
                }
            }

            Map<String, String> dbProps = new HashMap<>();
            dbProps.put("jakarta.persistence.jdbc.url",      props.getProperty("db.url"));
            dbProps.put("jakarta.persistence.jdbc.user",     props.getProperty("db.user"));
            dbProps.put("jakarta.persistence.jdbc.password", props.getProperty("db.password"));
            dbProps.put("jakarta.persistence.jdbc.driver",   "org.postgresql.Driver");

            emf = Persistence.createEntityManagerFactory("TaxisPU", dbProps);

        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static EntityManagerFactory getEntityManagerFactory() {
        return emf;
    }

    public static EntityManager getEntityManager() {
        return emf.createEntityManager();
    }
}