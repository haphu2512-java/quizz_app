package dao;

import entity.EnrollmentAnswer;
import entity.Exam;
import jakarta.persistence.EntityManager;

public class ExamDAO extends Generic_DAO<Exam, Long> {
    public ExamDAO(Class<Exam> clazz) {
        super(clazz);
    }

    public ExamDAO(EntityManager em,Class<Exam> clazz) {
        super(em, clazz);
    }
}