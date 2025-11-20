package service;

import entity.Exam;
import entity.Question;
import entity.QuestionAnswer;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface TakeExamService extends Remote {
    boolean verifyUserAlreadyTakenExam(String user_id, long room_id) throws RemoteException;
    Exam selectExamOfRoom(long room_id) throws RemoteException;
    List<Question> selectQuestionOfExam(long exam_id) throws RemoteException;
    List<QuestionAnswer> selectQuestionAnswerOfQuestion(long question_id) throws RemoteException;
}
