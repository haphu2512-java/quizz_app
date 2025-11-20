package rmi;

import dao.*;
import entity.*;
import service.*;
import service.impl.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.rmi.registry.LocateRegistry;

public class RMIServer {
    public static void main(String[] args) throws Exception{

        Context context = new InitialContext();
        LocateRegistry.createRegistry(1262);

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("AppTN");
        EntityManager em = emf.createEntityManager();

        //
        EnrollmentAnswerDAO enrollmentAnswerDAO = new EnrollmentAnswerDAO(EnrollmentAnswer.class);
        EnrollmentDAO enrollmentDAO = new EnrollmentDAO(Enrollment.class);
        ExamDAO examDao = new ExamDAO(Exam.class);
        QuestionAnswerDAO questionAnswerDAO = new QuestionAnswerDAO(QuestionAnswer.class);
        QuestionDAO questionDao = new QuestionDAO(Question.class);
        RoomDAO roomDao = new RoomDAO(Room.class);
        TakeExamDAO takeExamDAO = new TakeExamDAO();
        UserDAO userDAO = new UserDAO(User.class);


        //

        EnrollmentAnswerService enrollmentAnswerService = new EnrollmentAnswerServiceImpl(enrollmentAnswerDAO);
        EnrollmentService studentExamService = new EnrollmentServiceImpl(enrollmentDAO);
        ExamService examService = new ExamServiceImpl(examDao);
        QuestionAnswerService questionAnswerService = new QuestionAnswerServiceImpl(questionAnswerDAO);
        QuestionService questionService = new QuestionServiceImpl(questionDao);
        RoomService roomService = new RoomServiceImpl(roomDao);
        TakeExamService takeExamService = new TakeExamServiceImpl(takeExamDAO);
        UserService userService = new UserServiceImpl(userDAO);


        // Bind service

        context.bind("rmi://localhost:1262/enrollmentAnswerService", enrollmentAnswerService);
        context.bind("rmi://localhost:1262/studentExamService", studentExamService);
        context.bind("rmi://localhost:1262/examService", examService);
        context.bind("rmi://localhost:1262/questionAnswerService", questionAnswerService);
        context.bind("rmi://localhost:1262/questionService", questionService);
        context.bind("rmi://localhost:1262/roomService", roomService);
        context.bind("rmi://localhost:1262/takeExamService", takeExamService);
        context.bind("rmi://localhost:1262/userService", userService);


        System.out.println("RMI Server is running...");
    }
}