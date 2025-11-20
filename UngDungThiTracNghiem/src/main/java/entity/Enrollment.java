package entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "enrollments")
public class Enrollment implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "enrollment_id")
    private long enrollmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    @Column(name = "score")
    private double score;

    @OneToMany(mappedBy = "enrollment", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<EnrollmentAnswer> enrollmentAnswers;

    public Enrollment() {
    }

    public Enrollment(User user, Room room, double score) {
        this.user = user;
        this.room = room;
        this.score = score;
    }

    public long getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(long enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public List<EnrollmentAnswer> getEnrollmentAnswers() {
        return enrollmentAnswers;
    }

    public void setEnrollmentAnswers(List<EnrollmentAnswer> enrollmentAnswers) {
        this.enrollmentAnswers = enrollmentAnswers;
    }

    @Override
    public String toString() {
        return "Enrollment{" +
                "enrollmentId=" + enrollmentId +
                ", user=" + (user != null ? user.getUserId() : null) +
                ", room=" + (room != null ? room.getRoomId() : null) +
                ", score=" + score +
                '}';
    }
}