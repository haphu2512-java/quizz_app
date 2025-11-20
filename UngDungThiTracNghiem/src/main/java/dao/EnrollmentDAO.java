package dao;

import entity.Enrollment;
import entity.EnrollmentAnswer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import java.util.List;

public class EnrollmentDAO extends Generic_DAO<Enrollment, Long> {
    public EnrollmentDAO(Class<Enrollment> clazz) {
        super(clazz);
    }

    public EnrollmentDAO(EntityManager em,Class<Enrollment> clazz) {
        super(em, clazz);
    }

    public List<Enrollment> selectByUserID(String userId) {
        return em.createQuery("SELECT e FROM Enrollment e WHERE e.user.userId = :userId", Enrollment.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    public long selectIDByModel(Enrollment enrollment) {
        try {
            return em.createQuery("SELECT e.enrollmentId FROM Enrollment e WHERE e.user = :user AND e.room = :room AND e.score = :score ORDER BY e.enrollmentId DESC", Long.class)
                    .setParameter("user", enrollment.getUser())
                    .setParameter("room", enrollment.getRoom())
                    .setParameter("score", enrollment.getScore())
                    .setMaxResults(1)
                    .getSingleResult();
        } catch (NoResultException e) {
            return -1;
        }
    }
}