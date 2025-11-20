package dao;

import entity.QuestionAnswer;
import jakarta.persistence.EntityManager;

public class QuestionAnswerDAO extends Generic_DAO<QuestionAnswer, Long> {
    public QuestionAnswerDAO(Class<QuestionAnswer> clazz) {
        super(clazz);
    }

    public QuestionAnswerDAO(EntityManager em,Class<QuestionAnswer> clazz) {
        super(em, clazz);
    }
}