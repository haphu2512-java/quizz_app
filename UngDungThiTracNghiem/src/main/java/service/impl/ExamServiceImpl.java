package service.impl;

import dao.ExamDAO;
import entity.Exam;
import service.ExamService;

import java.rmi.RemoteException;

public class ExamServiceImpl extends GenericServiceImpl<Exam, Long> implements ExamService {
    private ExamDAO examDAO;

    public ExamServiceImpl(ExamDAO examDAO) throws RemoteException {
        super(examDAO);
        this.examDAO = examDAO;
    }
}