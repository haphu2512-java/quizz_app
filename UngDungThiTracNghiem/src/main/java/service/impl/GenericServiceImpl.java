package service.impl;


import dao.Generic_DAO;
import service.GenericService;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public abstract class GenericServiceImpl<T, ID> extends UnicastRemoteObject implements GenericService<T, ID> {

    protected Generic_DAO<T, ID> genericDAO;

    public GenericServiceImpl(Generic_DAO<T, ID> genericDAO) throws RemoteException {
        this.genericDAO = genericDAO;
    }

    @Override
    public T findById(ID id) throws RemoteException {

        return genericDAO.findById(id);
    }

    @Override
    public boolean save(T t) throws RemoteException {
        return genericDAO.save(t);
    }

    @Override
    public boolean update(T t) throws RemoteException {
        return genericDAO.update(t);
    }

    @Override
    public boolean delete(ID id) throws RemoteException {
        return genericDAO.delete(id);
    }

    @Override
    public List<T> getAll() throws RemoteException {
        return genericDAO.getAll();
    }

    @Override
    public T mergeIfDetached(T entity) throws RemoteException {
        return genericDAO.mergeIfDetached(entity);
    }
}