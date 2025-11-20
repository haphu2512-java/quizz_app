package dao;

import entity.Question;
import entity.QuestionAnswer;
import jakarta.persistence.EntityManager;

public class QuestionDAO extends Generic_DAO<Question, Long> {
    public QuestionDAO(Class<Question> clazz) {
        super(clazz);
    }

    public QuestionDAO(EntityManager em,Class<Question> clazz) {
        super(em, clazz);
    }
}