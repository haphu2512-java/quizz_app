package dao;

import entity.Question;
import entity.Room;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

public class RoomDAO extends Generic_DAO<Room, Long> {
    public RoomDAO(Class<Room> clazz) {
        super(clazz);
    }

    public RoomDAO(EntityManager em,Class<Room> clazz) {
        super(em, clazz);
    }

    public Room selectVerifiedRoom(long roomID, String password) {
        try {
            return em.createQuery("SELECT r FROM Room r WHERE r.roomId = :roomId AND r.password = :password", Room.class)
                    .setParameter("roomId", roomID)
                    .setParameter("password", password)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}