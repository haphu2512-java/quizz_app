package entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "questions")
public class Question implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private long questionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id")
    private Exam exam;

    @Column(name = "level")
    private int level;

    @Column(name = "content")
    private String content;

    @OneToMany(mappedBy = "question", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<QuestionAnswer> answers;

    @OneToMany(mappedBy = "question")
    private List<EnrollmentAnswer> enrollmentAnswers;

    public Question() {
    }

    public Question(Exam exam, int level, String content) {
        this.exam = exam;
        this.level = level;
        this.content = content;
    }

    public long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(long questionId) {
        this.questionId = questionId;
    }

    public Exam getExam() {
        return exam;
    }

    public void setExam(Exam exam) {
        this.exam = exam;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<QuestionAnswer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<QuestionAnswer> answers) {
        this.answers = answers;
    }

    public List<EnrollmentAnswer> getEnrollmentAnswers() {
        return enrollmentAnswers;
    }

    public void setEnrollmentAnswers(List<EnrollmentAnswer> enrollmentAnswers) {
        this.enrollmentAnswers = enrollmentAnswers;
    }

    @Override
    public String toString() {
        return "Question{" +
                "questionId=" + questionId +
                ", exam=" + (exam != null ? exam.getExamId() : null) +
                ", level=" + level +
                ", content='" + content + "'" +
                '}';
    }
}