package entity;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "enrollment_answers")
public class EnrollmentAnswer implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "enrollment_answer_id")
    private long enrollmentAnswerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id")
    private Enrollment enrollment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_answer_id")
    private QuestionAnswer questionAnswer;

    public EnrollmentAnswer() {
    }

    public EnrollmentAnswer(Enrollment enrollment, Question question, QuestionAnswer questionAnswer) {
        this.enrollment = enrollment;
        this.question = question;
        this.questionAnswer = questionAnswer;
    }

    public long getEnrollmentAnswerId() {
        return enrollmentAnswerId;
    }

    public void setEnrollmentAnswerId(long enrollmentAnswerId) {
        this.enrollmentAnswerId = enrollmentAnswerId;
    }

    public Enrollment getEnrollment() {
        return enrollment;
    }

    public void setEnrollment(Enrollment enrollment) {
        this.enrollment = enrollment;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public QuestionAnswer getQuestionAnswer() {
        return questionAnswer;
    }

    public void setQuestionAnswer(QuestionAnswer questionAnswer) {
        this.questionAnswer = questionAnswer;
    }

    @Override
    public String toString() {
        return "EnrollmentAnswer{" +
                "enrollmentAnswerId=" + enrollmentAnswerId +
                ", enrollment=" + (enrollment != null ? enrollment.getEnrollmentId() : null) +
                ", question=" + (question != null ? question.getQuestionId() : null) +
                ", questionAnswer=" + (questionAnswer != null ? questionAnswer.getQuestionAnswerId() : null) +
                '}';
    }
}