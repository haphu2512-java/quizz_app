package service.impl;

import dao.EnrollmentDAO;
import entity.Enrollment;
import service.EnrollmentService;

import java.rmi.RemoteException;
import java.util.List;

public class EnrollmentServiceImpl extends GenericServiceImpl<Enrollment, Long> implements EnrollmentService {
    private EnrollmentDAO enrollmentDAO;

    public EnrollmentServiceImpl(EnrollmentDAO enrollmentDAO) throws RemoteException {
        super(enrollmentDAO);
        this.enrollmentDAO = enrollmentDAO;
    }

    @Override
    public List<Enrollment> selectByUserID(String userId) throws RemoteException {
        return enrollmentDAO.selectByUserID(userId);
    }

    @Override
    public long selectIDByModel(Enrollment enrollment) throws RemoteException {
        return enrollmentDAO.selectIDByModel(enrollment);
    }
}