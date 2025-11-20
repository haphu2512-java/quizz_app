package dao;

import entity.Exam;
import entity.Question;
import entity.QuestionAnswer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import util.EntityManagerFactoryUtil;

import java.util.Collections;
import java.util.List;

public class TakeExamDAO {

    private final EntityManager em;

    public TakeExamDAO() {
        this.em = EntityManagerFactoryUtil.getEntityManager();
    }

    public TakeExamDAO(EntityManager em) {
        this.em = em;
    }

    public boolean verifyUserAlreadyTakenExam(String userId, long roomId) {
        Long count = em.createQuery("SELECT COUNT(e) FROM Enrollment e WHERE e.user.userId = :userId AND e.room.roomId = :roomId", Long.class)
                .setParameter("userId", userId)
                .setParameter("roomId", roomId)
                .getSingleResult();
        return count > 0;
    }

    public Exam selectExamOfRoom(long roomId) {
        try {
            return em.createQuery("SELECT r.exam FROM Room r WHERE r.roomId = :roomId", Exam.class)
                .setParameter("roomId", roomId)
                .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<Question> selectQuestionOfExam(long examId) {
        return em.createQuery("SELECT q FROM Question q WHERE q.exam.examId = :examId ORDER BY q.level ASC", Question.class)
                .setParameter("examId", examId)
                .getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<QuestionAnswer> selectQuestionAnswerOfQuestion(long questionId) {
        // Using a native query for MySQL's RAND() function to maintain random order.
        List<QuestionAnswer> answers = em.createNativeQuery(
                        "SELECT * FROM question_answers WHERE question_id = ?",
                        QuestionAnswer.class)
                .setParameter(1, questionId)
                .getResultList();
        // Shuffle in memory to be database-agnostic, though the original used RAND().
        // For this refactoring, we will keep the spirit of randomization.
        Collections.shuffle(answers);
        return answers;
    }
}