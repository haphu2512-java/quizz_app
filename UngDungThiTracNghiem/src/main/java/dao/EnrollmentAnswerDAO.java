package dao;

import entity.Enrollment;
import entity.EnrollmentAnswer;
import jakarta.persistence.EntityManager;

public class EnrollmentAnswerDAO extends Generic_DAO<EnrollmentAnswer, Long> {
    public EnrollmentAnswerDAO(Class<EnrollmentAnswer> clazz) {
        super(clazz);
    }
    public EnrollmentAnswerDAO(EntityManager em,Class<EnrollmentAnswer> clazz) {
        super(em, clazz);
    }
}