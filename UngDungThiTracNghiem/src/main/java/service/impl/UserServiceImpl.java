package service.impl;

import dao.UserDAO;
import entity.User;
import service.UserService;

import java.rmi.RemoteException;

public class UserServiceImpl extends GenericServiceImpl<User, String> implements UserService {
    private UserDAO userDAO;

    public UserServiceImpl(UserDAO userDAO) throws RemoteException {
        super(userDAO);
        this.userDAO = userDAO;
    }

    @Override
    public User selectByAccount(String username, String password) throws RemoteException {
        return userDAO.selectByAccount(username, password);
    }

}