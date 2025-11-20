package service.impl;

import dao.QuestionDAO;
import entity.Question;
import service.QuestionService;

import java.rmi.RemoteException;

public class QuestionServiceImpl extends GenericServiceImpl<Question, Long> implements QuestionService {
    private QuestionDAO questionDAO;

    public QuestionServiceImpl(QuestionDAO questionDAO) throws RemoteException {
        super(questionDAO);
        this.questionDAO = questionDAO;
    }
}