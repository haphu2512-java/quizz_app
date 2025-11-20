package entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "exams")
public class Exam implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exam_id")
    private long examId;

    @Column(name = "subject")
    private String subject;

    @Column(name = "total_question")
    private int totalQuestion;

    @Column(name = "total_score")
    private int totalScore;

    @Column(name = "score_per_question")
    private double scorePerQuestion;

    @OneToMany(mappedBy = "exam", fetch = FetchType.LAZY)
    private List<Question> questions;

    @OneToMany(mappedBy = "exam", fetch = FetchType.LAZY)
    private List<Room> rooms;

    public Exam() {
    }

    public Exam(String subject, int totalQuestion, int totalScore, double scorePerQuestion) {
        this.subject = subject;
        this.totalQuestion = totalQuestion;
        this.totalScore = totalScore;
        this.scorePerQuestion = scorePerQuestion;
    }

    public Exam(long examId, String subject, int totalQuestion, int totalScore, double scorePerQuestion) {
    }

    public long getExamId() {
        return examId;
    }

    public void setExamId(long examId) {
        this.examId = examId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public int getTotalQuestion() {
        return totalQuestion;
    }

    public void setTotalQuestion(int totalQuestion) {
        this.totalQuestion = totalQuestion;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public double getScorePerQuestion() {
        return scorePerQuestion;
    }

    public void setScorePerQuestion(double scorePerQuestion) {
        this.scorePerQuestion = scorePerQuestion;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public void setRooms(List<Room> rooms) {
        this.rooms = rooms;
    }

    @Override
    public String toString() {
        return "Exam{"
                + "examId=" + examId +
                ", subject='" + subject + "'"
                + ", totalQuestion=" + totalQuestion +
                ", totalScore=" + totalScore +
                ", scorePerQuestion=" + scorePerQuestion +
                '}';
    }
}