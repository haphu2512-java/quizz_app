package entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "users")
public class User implements Serializable {
    @Id
    @Column(name = "user_id")
    private String userId;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "is_host")
    private boolean isHost;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Enrollment> enrollments;

    public User() {
    }

    public User(String userId, String fullName, String passwordHash, boolean isHost) {
        this.userId = userId;
        this.fullName = fullName;
        this.passwordHash = passwordHash;
        this.isHost = isHost;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public boolean isHost() {
        return isHost;
    }

    public void setHost(boolean host) {
        isHost = host;
    }

    public List<Enrollment> getEnrollments() {
        return enrollments;
    }

    public void setEnrollments(List<Enrollment> enrollments) {
        this.enrollments = enrollments;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + "'" +
                ", fullName='" + fullName + "'" +
                ", passwordHash='" + passwordHash + "'" +
                ", isHost=" + isHost +
                '}';
    }
}