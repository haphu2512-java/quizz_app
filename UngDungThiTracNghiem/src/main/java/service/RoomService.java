package service;

import entity.Room;

import java.rmi.RemoteException;

public interface RoomService extends GenericService<Room,Long>{
    Room selectVerifiedRoom(Long roomID, String password) throws RemoteException;
}
