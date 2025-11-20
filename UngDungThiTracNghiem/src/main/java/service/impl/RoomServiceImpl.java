package service.impl;

import dao.RoomDAO;
import entity.Room;
import service.RoomService;

import java.rmi.RemoteException;

public class RoomServiceImpl extends GenericServiceImpl<Room,Long> implements RoomService {
    private RoomDAO roomDAO;

    public RoomServiceImpl(RoomDAO roomDAO) throws RemoteException {
        super(roomDAO);
        this.roomDAO = roomDAO;
    }

    public Room selectVerifiedRoom(Long roomID, String password) {
        return roomDAO.selectVerifiedRoom(roomID, password);
    }
}
