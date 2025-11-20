package util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class EntityManagerFactoryUtil {

    private static final EntityManagerFactory entityManagerFactory;

    static {
        try {
            // Khởi tạo theo đúng persistence-unit mà bạn đã đặt trong persistence.xml
            entityManagerFactory = Persistence.createEntityManagerFactory("AppTN");
        } catch (Throwable ex) {
            System.err.println("❌ Initial EntityManagerFactory creation failed: " + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    // Lấy 1 EntityManager mới
    public static EntityManager getEntityManager() {
        return entityManagerFactory.createEntityManager();
    }

    // Đóng factory khi tắt server / chương trình
    public static void closeFactory() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
    }
}

