package iuh.fit.se.gui;

import entity.*;
import iuh.fit.se.util.ServiceFactory;
import service.EnrollmentService;
import service.TakeExamService;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

/**
 * Form làm bài thi - Hoàn toàn không dùng .form file
 * Tạo GUI 100% bằng Java Swing code
 */
public class TakeExamAttendee extends JFrame {
    private final User loginUser;
    private final Room room;
    private  TakeExamService takeExamService;
    private  EnrollmentService enrollmentService;

    // UI Components
    private JPanel mainPanel;
    private JTabbedPane tabbedPane;
    private JLabel labelRoomTitle;
    private JLabel labelRoomID;
    private JLabel labelTimeLimit;
    private JLabel labelTotalQuestion;
    private JLabel labelCountDownTimer;
    private JButton buttonSubmit;

    // Data
    private Exam exam;
    private List<Question> questionList;
    private List<List<QuestionAnswer>> listOfQuestionAnswerList;
    private List<QuestionAndAnswerForm> questionAndAnswerFormList;
    private int prevQAAFormIndex = 0;

    private Timer timer;
    private List<String> correctAnswerList;
    private List<String> chosenAnswerList;
    private List<String> resultList;
    private int totalCorrect;
    private double score;

    public TakeExamAttendee(User loginUser, Room room) {
        this.loginUser = loginUser;
        this.room = room;

        try {
            this.takeExamService = ServiceFactory.getTakeExamService();
            this.enrollmentService = ServiceFactory.getEnrollmentService();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi kết nối service: " + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
            this.dispose();
            new MenuAttendee(loginUser);
            return;
        }

        // Tạo UI
        createUIComponents();

        // Load dữ liệu
        initComponents();

        // Setup window
        this.setTitle("Làm bài thi - " + room.getTitle());
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setContentPane(mainPanel);
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setLocationRelativeTo(null);

        // Add event listeners
        addActionEvent();

        // Start timer
        setTimer();

        this.setVisible(true);
    }

    /**
     * Tạo toàn bộ UI components bằng code
     */
    private void createUIComponents() {
        // Main panel với BorderLayout
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(240, 240, 240));

        // ============= TOP PANEL - Room Info =============
        JPanel topPanel = createTopPanel();
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // ============= CENTER - Questions Tabs =============
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 14));
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        // ============= RIGHT PANEL - Timer & Submit =============
        JPanel rightPanel = createRightPanel();
        mainPanel.add(rightPanel, BorderLayout.EAST);
    }

    /**
     * Tạo panel thông tin phòng thi (TOP)
     */
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new TitledBorder(BorderFactory.createLineBorder(new Color(100, 149, 237), 2),
                        "Thông tin phòng thi",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        new Font("Arial", Font.BOLD, 16),
                        new Color(100, 149, 237)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Room title - Lớn, nổi bật
        labelRoomTitle = new JLabel(room.getTitle(), SwingConstants.CENTER);
        labelRoomTitle.setFont(new Font("Arial", Font.BOLD, 20));
        labelRoomTitle.setForeground(new Color(25, 25, 112));
        panel.add(labelRoomTitle, BorderLayout.NORTH);

        // Info grid
        JPanel infoPanel = new JPanel(new GridLayout(1, 3, 20, 10));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        // Room ID
        infoPanel.add(createInfoItem("Mã phòng:", String.valueOf(room.getRoomId())));

        // Time limit
        infoPanel.add(createInfoItem("Thời gian:", room.getTimeLimit() + " phút"));

        // Total questions (will be set later)
        labelTotalQuestion = new JLabel("Loading..."); // Initialize the label
        infoPanel.add(createInfoItem("Số câu:", labelTotalQuestion));

        panel.add(infoPanel, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Tạo một info item (label + value) cho các mục thông thường
     */
    private JPanel createInfoItem(String label, String value) {
        JLabel valueLabel = new JLabel(value);
        return createInfoItem(label, valueLabel);
    }

    /**
     * Tạo một info item (label + value) và cho phép truy cập vào value label
     */
    private JPanel createInfoItem(String label, JLabel valueLabelOut) {
        JPanel panel = new JPanel(new GridLayout(2, 1, 0, 5));
        panel.setBackground(Color.WHITE);

        JLabel lblLabel = new JLabel(label, SwingConstants.CENTER);
        lblLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        lblLabel.setForeground(Color.GRAY);

        valueLabelOut.setFont(new Font("Arial", Font.BOLD, 16));
        valueLabelOut.setForeground(new Color(0, 102, 204));
        valueLabelOut.setHorizontalAlignment(SwingConstants.CENTER);

        panel.add(lblLabel);
        panel.add(valueLabelOut);

        return panel;
    }

    /**
     * Tạo panel timer và nút nộp bài (RIGHT)
     */
    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setPreferredSize(new Dimension(250, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        // ===== Timer Panel =====
        JPanel timerPanel = new JPanel(new BorderLayout());
        timerPanel.setBackground(Color.WHITE);
        timerPanel.setBorder(BorderFactory.createCompoundBorder(
                new TitledBorder(BorderFactory.createLineBorder(new Color(220, 20, 60), 2),
                        "Thời gian còn lại",
                        TitledBorder.CENTER,
                        TitledBorder.TOP,
                        new Font("Arial", Font.BOLD, 14),
                        new Color(220, 20, 60)),
                BorderFactory.createEmptyBorder(20, 10, 20, 10)
        ));

        labelCountDownTimer = new JLabel("00:00:00", SwingConstants.CENTER);
        labelCountDownTimer.setFont(new Font("Monospaced", Font.BOLD, 32));
        labelCountDownTimer.setForeground(new Color(220, 20, 60));
        timerPanel.add(labelCountDownTimer, BorderLayout.CENTER);

        panel.add(timerPanel, BorderLayout.NORTH);

        // ===== Instructions Panel =====
        JPanel instructionPanel = new JPanel(new BorderLayout());
        instructionPanel.setBackground(new Color(255, 255, 224));
        instructionPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 165, 0)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JTextArea instructions = new JTextArea(
                "Hướng dẫn:\n\n" +
                        "• Chọn đáp án cho mỗi câu hỏi\n\n" +
                        "• Tab màu XANH: Đã chọn đáp án\n\n" +
                        "• Click 'Bỏ chọn' để bỏ chọn\n\n" +
                        "• Nhấn 'Nộp bài' khi hoàn thành\n\n" +
                        "⚠ Hết giờ sẽ tự động nộp bài!"
        );
        instructions.setEditable(false);
        instructions.setLineWrap(true);
        instructions.setWrapStyleWord(true);
        instructions.setBackground(new Color(255, 255, 224));
        instructions.setFont(new Font("Arial", Font.PLAIN, 12));
        instructionPanel.add(instructions, BorderLayout.CENTER);

        panel.add(instructionPanel, BorderLayout.CENTER);

        // ===== Submit Button =====
        buttonSubmit = new JButton("📝 Nộp bài");
        buttonSubmit.setFont(new Font("Arial", Font.BOLD, 16));
        buttonSubmit.setPreferredSize(new Dimension(200, 50));
        buttonSubmit.setBackground(new Color(34, 139, 34));
        buttonSubmit.setForeground(Color.WHITE);
        buttonSubmit.setFocusPainted(false);
        buttonSubmit.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 100, 0), 2),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        buttonSubmit.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover effect
        buttonSubmit.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                buttonSubmit.setBackground(new Color(50, 205, 50));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                buttonSubmit.setBackground(new Color(34, 139, 34));
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(buttonSubmit);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Load dữ liệu đề thi và câu hỏi
     */
    private void initComponents() {
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            // Load exam
            exam = takeExamService.selectExamOfRoom(room.getRoomId());

            if (exam == null) {
                setCursor(Cursor.getDefaultCursor());
                JOptionPane.showMessageDialog(
                        this,
                        "Đề thi đã bị lỗi!\nBạn sẽ tự động thoát khỏi phòng.",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE
                );
                this.dispose();
                new MenuAttendee(loginUser);
                return;
            }

            // Load questions
            questionList = takeExamService.selectQuestionOfExam(exam.getExamId());

            if (questionList.isEmpty()) {
                setCursor(Cursor.getDefaultCursor());
                JOptionPane.showMessageDialog(
                        this,
                        "Đề thi không có câu hỏi!\nBạn sẽ tự động thoát khỏi phòng.",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE
                );
                this.dispose();
                new MenuAttendee(loginUser);
                return;
            }

            // Update total question label
            labelTotalQuestion.setText(String.valueOf(questionList.size()));

            // Initialize lists
            listOfQuestionAnswerList = new ArrayList<>();
            correctAnswerList = new ArrayList<>();
            chosenAnswerList = new ArrayList<>();
            questionAndAnswerFormList = new ArrayList<>();
            resultList = new ArrayList<>();
            totalCorrect = 0;

            // Load questions and answers
            fillDataToQAAForm();

            setCursor(Cursor.getDefaultCursor());

        } catch (RemoteException e) {
            setCursor(Cursor.getDefaultCursor());
            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi tải dữ liệu đề thi:\n" + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
            this.dispose();
            new MenuAttendee(loginUser);
            e.printStackTrace();
        }
    }

    /**
     * Load câu hỏi và đáp án vào tabs
     */
    private void fillDataToQAAForm() throws RemoteException {
        int index = 1;
        for (Question question : questionList) {
            List<QuestionAnswer> questionAnswerList = takeExamService.selectQuestionAnswerOfQuestion(
                    question.getQuestionId()
            );

            QuestionAndAnswerForm form = new QuestionAndAnswerForm(question, questionAnswerList);
            tabbedPane.addTab("Câu " + index++, form);

            // Find correct answer
            String correctAns = null;
            for (QuestionAnswer answer : questionAnswerList) {
                if (answer.isCorrect()) {
                    correctAns = answer.getContent().strip();
                    break;
                }
            }
            correctAnswerList.add(correctAns); // Always add something (the answer or null)

            questionAndAnswerFormList.add(form);
            listOfQuestionAnswerList.add(questionAnswerList);
        }
    }

    /**
     * Add event listeners
     */
    private void addActionEvent() {
        // Window closing event
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int selection = JOptionPane.showConfirmDialog(
                        TakeExamAttendee.this,
                        "Kết quả làm bài của bạn vẫn sẽ được tính nếu bạn thoát ngay!\n\n" +
                                "Bạn có chắc chắn muốn thoát?",
                        "Xác nhận thoát",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );

                if (selection == JOptionPane.OK_OPTION) {
                    timer.stop();
                    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    executeAndShowResult();
                }
            }
        });

        // Tab change event - mark answered tabs
        tabbedPane.addChangeListener(event -> {
            QuestionAndAnswerForm prevForm = questionAndAnswerFormList.get(prevQAAFormIndex);

            if (prevForm.getButtonGroup().getSelection() != null) {
                tabbedPane.setForegroundAt(prevQAAFormIndex, Color.WHITE);
                tabbedPane.setBackgroundAt(prevQAAFormIndex, new Color(34, 139, 34));
            } else {
                tabbedPane.setForegroundAt(prevQAAFormIndex, Color.BLACK);
                tabbedPane.setBackgroundAt(prevQAAFormIndex, null);
            }

            prevQAAFormIndex = tabbedPane.getSelectedIndex();
        });

        // Submit button
        buttonSubmit.addActionListener(event -> {
            int selection = JOptionPane.showConfirmDialog(
                    this,
                    "Bạn thật sự muốn nộp bài?",
                    "Xác nhận nộp bài",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (selection == JOptionPane.OK_OPTION) {
                timer.stop();
                this.dispose();
                executeAndShowResult();
            }
        });
    }

    /**
     * Setup countdown timer
     */
    private void setTimer() {
        timer = new Timer(1000, event -> {
            int[] timeLeft = calculateTimeLeft();
            int hours = timeLeft[0];
            int minutes = timeLeft[1];
            int seconds = timeLeft[2];

            if (hours == 0 && minutes == 0 && seconds == 0) {
                timer.stop();
                dispose();
                JOptionPane.showMessageDialog(
                        null,
                        "Hết giờ làm bài thi!",
                        "Thông báo",
                        JOptionPane.INFORMATION_MESSAGE
                );
                executeAndShowResult();
                return;
            }

            String timeFormat = String.format("%02d:%02d:%02d", hours, minutes, seconds);
            labelCountDownTimer.setText(timeFormat);

            // Change color when time is running out
            if (hours == 0 && minutes < 5) {
                labelCountDownTimer.setForeground(Color.RED);
            }
        });
        timer.start();
    }

    private int totalSecondsLeft = -1;

    private int[] calculateTimeLeft() {
        if (totalSecondsLeft == -1) {
            totalSecondsLeft = room.getTimeLimit() * 60;
        }

        totalSecondsLeft--;

        int hours = totalSecondsLeft / 3600;
        int minutes = (totalSecondsLeft % 3600) / 60;
        int seconds = totalSecondsLeft % 60;

        return new int[]{hours, minutes, seconds};
    }

    /**
     * Execute và hiển thị kết quả
     */
    private void executeAndShowResult() {
        executeExamResult();
        saveExamResultToDatabase();
        new ResultForm(loginUser, questionList, chosenAnswerList,
                correctAnswerList, resultList, totalCorrect, score);
    }

    /**
     * Tính điểm
     */
    private void executeExamResult() {
        // Collect chosen answers
        for (QuestionAndAnswerForm form : questionAndAnswerFormList) {
            String chosenAnswer;
            try {
                chosenAnswer = form.getButtonGroup().getSelection().getActionCommand();
            } catch (Exception e) {
                chosenAnswer = "Chưa chọn";
            }
            chosenAnswerList.add(chosenAnswer);
        }

        // Compare with correct answers
        for (int i = 0; i < chosenAnswerList.size(); i++) {
            String chosenAnswer = chosenAnswerList.get(i);
            String correctAnswer = correctAnswerList.get(i);

            if (correctAnswer != null && correctAnswer.equals(chosenAnswer)) {
                resultList.add("Đúng");
                totalCorrect++;
            } else {
                resultList.add("Sai");
            }
        }

        score = totalCorrect * exam.getScorePerQuestion();
    }

    /**
     * Lưu kết quả vào database
     */
    private void saveExamResultToDatabase() {
        try {
            Enrollment enrollment = new Enrollment(loginUser, room, score);
            boolean success = enrollmentService.save(enrollment);

            if (!success) {
                JOptionPane.showMessageDialog(
                        this,
                        "Lưu kết quả dự thi thất bại!",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi lưu kết quả:\n" + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        }
    }
}