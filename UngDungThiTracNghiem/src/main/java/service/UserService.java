package service;

import entity.User;

import java.rmi.RemoteException;

public interface UserService extends GenericService<User, String> {
    User selectByAccount(String username, String password) throws RemoteException;
}