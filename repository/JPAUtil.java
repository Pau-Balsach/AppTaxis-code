package repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class JPAUtil {

    private static final EntityManagerFactory emf;

    static {
        try {
            Properties config = ConfigLoader.get();
            Map<String, Object> props = new HashMap<>();
            props.put("jakarta.persistence.jdbc.driver",   "org.postgresql.Driver");
            props.put("jakarta.persistence.jdbc.url",      config.getProperty("db.url"));
            props.put("jakarta.persistence.jdbc.user",     config.getProperty("db.user"));
            props.put("jakarta.persistence.jdbc.password", config.getProperty("db.password"));
            props.put("hibernate.connection.driver_class", "org.postgresql.Driver");
            props.put("hibernate.connection.url",          config.getProperty("db.url"));
            props.put("hibernate.connection.username",     config.getProperty("db.user"));
            props.put("hibernate.connection.password",     config.getProperty("db.password"));
            props.put("hibernate.dialect",                 "org.hibernate.dialect.PostgreSQLDialect");
            props.put("hibernate.hbm2ddl.auto",            "none");
            props.put("hibernate.show_sql",                "false");
            emf = Persistence.createEntityManagerFactory("TaxisPU", props);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static EntityManagerFactory getEntityManagerFactory() { return emf; }
    public static EntityManager getEntityManager()               { return emf.createEntityManager(); }
}