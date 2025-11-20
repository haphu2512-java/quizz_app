package iuh.fit.se.util;



import service.*;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

public class ServiceFactory {
    private static final String RMI_URL = "rmi://localhost:1262/";
    private static Context context;

    static {
        try {
            Properties props = new Properties();
            props.setProperty(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.rmi.registry.RegistryContextFactory");
            props.setProperty(Context.PROVIDER_URL, "rmi://localhost:1262");
            context = new InitialContext(props);
        } catch (NamingException e) {
            throw new RuntimeException("Không thể kết nối tới RMI Server: " + e.getMessage(), e);
        }
    }

    public static UserService getUserService() {
        try {
            return (UserService) context.lookup(RMI_URL + "userService");
        } catch (NamingException e) {
            throw new RuntimeException("Không thể lấy UserService: " + e.getMessage(), e);
        }
    }

    public static ExamService getExamService() {
        try {
            return (ExamService) context.lookup(RMI_URL + "examService");
        } catch (NamingException e) {
            throw new RuntimeException("Không thể lấy ExamService: " + e.getMessage(), e);
        }
    }

    public static QuestionService getQuestionService() {
        try {
            return (QuestionService) context.lookup(RMI_URL + "questionService");
        } catch (NamingException e) {
            throw new RuntimeException("Không thể lấy QuestionService: " + e.getMessage(), e);
        }
    }

    public static QuestionAnswerService getQuestionAnswerService() {
        try {
            return (QuestionAnswerService) context.lookup(RMI_URL + "questionAnswerService");
        } catch (NamingException e) {
            throw new RuntimeException("Không thể lấy QuestionAnswerService: " + e.getMessage(), e);
        }
    }

    public static RoomService getRoomService() {
        try {
            return (RoomService) context.lookup(RMI_URL + "roomService");
        } catch (NamingException e) {
            throw new RuntimeException("Không thể lấy RoomService: " + e.getMessage(), e);
        }
    }

    public static EnrollmentService getEnrollmentService() {
        try {
            return (EnrollmentService) context.lookup(RMI_URL + "studentExamService");
        } catch (NamingException e) {
            throw new RuntimeException("Không thể lấy EnrollmentService: " + e.getMessage(), e);
        }
    }

    public static EnrollmentAnswerService getEnrollmentAnswerService() {
        try {
            return (EnrollmentAnswerService) context.lookup(RMI_URL + "enrollmentAnswerService");
        } catch (NamingException e) {
            throw new RuntimeException("Không thể lấy EnrollmentAnswerService: " + e.getMessage(), e);
        }
    }

    public static TakeExamService getTakeExamService() {
        try {
            return (TakeExamService) context.lookup(RMI_URL + "takeExamService");
        } catch (NamingException e) {
            throw new RuntimeException("Không thể lấy TakeExamService: " + e.getMessage(), e);
        }
    }

    public static boolean testConnection() {
        try {
            getUserService();
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    public static void close() {
        try {
            if (context != null) {
                context.close();
            }
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }
}