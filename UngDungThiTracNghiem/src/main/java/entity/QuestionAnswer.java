package entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "question_answers")
public class QuestionAnswer implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_answer_id")
    private long questionAnswerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private Question question;

    @Column(name = "content")
    private String content;

    @Column(name = "is_correct")
    private boolean isCorrect;

    @OneToMany(mappedBy = "questionAnswer")
    private List<EnrollmentAnswer> enrollmentAnswers;

    public QuestionAnswer() {
    }

    public QuestionAnswer(Question question, String content, boolean isCorrect) {
        this.question = question;
        this.content = content;
        this.isCorrect = isCorrect;
    }

    public long getQuestionAnswerId() {
        return questionAnswerId;
    }

    public void setQuestionAnswerId(long questionAnswerId) {
        this.questionAnswerId = questionAnswerId;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }

    public List<EnrollmentAnswer> getEnrollmentAnswers() {
        return enrollmentAnswers;
    }

    public void setEnrollmentAnswers(List<EnrollmentAnswer> enrollmentAnswers) {
        this.enrollmentAnswers = enrollmentAnswers;
    }

    @Override
    public String toString() {
        return "QuestionAnswer{" +
                "questionAnswerId=" + questionAnswerId +
                ", question=" + (question != null ? question.getQuestionId() : null) +
                ", content='" + content + "'" +
                ", isCorrect=" + isCorrect +
                '}';
    }
}