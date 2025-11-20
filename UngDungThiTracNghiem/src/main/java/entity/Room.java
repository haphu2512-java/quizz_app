package entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "rooms")
public class Room implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private long roomId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id")
    private Exam exam;

    @Column(name = "title")
    private String title;

    @Column(name = "time_limit")
    private int timeLimit;

    @Column(name = "password")
    private String password;

    @Column(name = "is_available")
    private boolean isAvailable;

    @OneToMany(mappedBy = "room", fetch = FetchType.LAZY)
    private List<Enrollment> enrollments;

    public Room() {
    }

    public Room(Exam exam, String title, int timeLimit, String password, boolean isAvailable) {
        this.exam = exam;
        this.title = title;
        this.timeLimit = timeLimit;
        this.password = password;
        this.isAvailable = isAvailable;
    }

    public long getRoomId() {
        return roomId;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }

    public Exam getExam() {
        return exam;
    }

    public void setExam(Exam exam) {
        this.exam = exam;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public List<Enrollment> getEnrollments() {
        return enrollments;
    }

    public void setEnrollments(List<Enrollment> enrollments) {
        this.enrollments = enrollments;
    }

    @Override
    public String toString() {
        return "Room{" +
                "roomId=" + roomId +
                ", exam=" + (exam != null ? exam.getExamId() : null) +
                ", title='" + title + "'" +
                ", timeLimit=" + timeLimit +
                ", password='" + password + "'" +
                ", isAvailable=" + isAvailable +
                '}';
    }
}