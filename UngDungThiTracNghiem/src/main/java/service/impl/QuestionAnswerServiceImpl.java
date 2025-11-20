package service.impl;

import dao.Generic_DAO;
import dao.QuestionAnswerDAO;
import dao.QuestionDAO;
import entity.Question;
import entity.QuestionAnswer;
import service.QuestionAnswerService;
import service.QuestionService;

import java.rmi.RemoteException;

public class QuestionAnswerServiceImpl extends GenericServiceImpl<QuestionAnswer, Long> implements QuestionAnswerService {
    private QuestionAnswerDAO questionAnswerDAO;

    public QuestionAnswerServiceImpl(QuestionAnswerDAO questionAnswerDAO) throws RemoteException {
        super(questionAnswerDAO);
        this.questionAnswerDAO = questionAnswerDAO;
    }
}