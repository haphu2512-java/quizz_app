package service;

import entity.Enrollment;

import java.rmi.RemoteException;
import java.util.List;

public interface EnrollmentService extends GenericService<Enrollment, Long> {
    List<Enrollment> selectByUserID(String userId) throws RemoteException;
    long selectIDByModel(Enrollment enrollment) throws RemoteException;
}