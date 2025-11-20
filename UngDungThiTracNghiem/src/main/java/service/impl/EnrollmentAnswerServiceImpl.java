package service.impl;

import dao.EnrollmentAnswerDAO;
import entity.EnrollmentAnswer;
import service.EnrollmentAnswerService;

import java.rmi.RemoteException;

public class EnrollmentAnswerServiceImpl extends GenericServiceImpl<EnrollmentAnswer, Long> implements EnrollmentAnswerService {
    private EnrollmentAnswerDAO enrollmentAnswerDAO;

    public EnrollmentAnswerServiceImpl(EnrollmentAnswerDAO enrollmentAnswerDAO) throws RemoteException {
        super(enrollmentAnswerDAO);
        this.enrollmentAnswerDAO = enrollmentAnswerDAO;
    }
}