package service.impl;

import dao.TakeExamDAO;
import entity.Exam;
import entity.Question;
import entity.QuestionAnswer;
import service.TakeExamService;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class TakeExamServiceImpl extends UnicastRemoteObject implements TakeExamService {
    private TakeExamDAO takeExamDAO;

    public TakeExamServiceImpl(TakeExamDAO takeExamDAO) throws RemoteException {
        super();
        this.takeExamDAO = takeExamDAO;
    }
    @Override
    public boolean verifyUserAlreadyTakenExam(String user_id, long room_id) throws RemoteException {
        return takeExamDAO.verifyUserAlreadyTakenExam(user_id, room_id);
    }

    @Override
    public Exam selectExamOfRoom(long room_id) throws RemoteException {
        return takeExamDAO.selectExamOfRoom(room_id);
    }

    @Override
    public List<Question> selectQuestionOfExam(long exam_id) throws RemoteException {
        return takeExamDAO.selectQuestionOfExam(exam_id);
    }

    @Override
    public List<QuestionAnswer> selectQuestionAnswerOfQuestion(long question_id) throws RemoteException {
        return takeExamDAO.selectQuestionAnswerOfQuestion(question_id);
    }
}
