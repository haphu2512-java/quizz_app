package iuh.fit.se.gui;

import entity.Exam;
import entity.Question;
import entity.Room;
import entity.User;
import iuh.fit.se.util.ServiceFactory;
import service.RoomService;
import service.TakeExamService;

import javax.swing.*;
import java.awt.*;
import java.rmi.RemoteException;
import java.util.List;

public class GoToRoomAttendee extends JFrame {
    private final User loginUser;
    private RoomService roomService;
    private TakeExamService takeExamService;

    private JPanel panelViewGoToRoomAttendee;
    private JTextField textfieldRoomIDViewGoToRoomAttendee;
    private JPasswordField passwordfieldPasswordViewGoToRoomAttendee;
    private JButton buttonGoToRoomViewGoToRoomAttendee;
    private JButton buttonBackViewGoToRoomAttendee;
    private JCheckBox checkboxShowPasswordViewGoToRoomAttendee;

    public GoToRoomAttendee(User loginUser) {
        this.loginUser = loginUser;

        try {
            this.roomService = ServiceFactory.getRoomService();
            this.takeExamService = ServiceFactory.getTakeExamService();
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

        initComponents();
        addActionEvent();

        this.setTitle("Vào Phòng Thi");
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(panelViewGoToRoomAttendee);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private void initComponents() {
        if (panelViewGoToRoomAttendee == null) {
            panelViewGoToRoomAttendee = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            // Room ID Label
            JLabel labelRoomID = new JLabel("Nhập mã phòng:");
            gbc.gridx = 0;
            gbc.gridy = 0;
            panelViewGoToRoomAttendee.add(labelRoomID, gbc);

            // Room ID Field
            textfieldRoomIDViewGoToRoomAttendee = new JTextField(20);
            textfieldRoomIDViewGoToRoomAttendee.setFont(new Font("Arial", Font.BOLD, 16));
            gbc.gridx = 1;
            gbc.gridy = 0;
            panelViewGoToRoomAttendee.add(textfieldRoomIDViewGoToRoomAttendee, gbc);

            // Password Label
            JLabel labelPassword = new JLabel("Mật khẩu:");
            gbc.gridx = 0;
            gbc.gridy = 1;
            panelViewGoToRoomAttendee.add(labelPassword, gbc);

            // Password Field
            passwordfieldPasswordViewGoToRoomAttendee = new JPasswordField(20);
            passwordfieldPasswordViewGoToRoomAttendee.setFont(new Font("Arial", Font.BOLD, 16));
            gbc.gridx = 1;
            gbc.gridy = 1;
            panelViewGoToRoomAttendee.add(passwordfieldPasswordViewGoToRoomAttendee, gbc);

            // Show Password Checkbox
            checkboxShowPasswordViewGoToRoomAttendee = new JCheckBox("Hiện mật khẩu");
            gbc.gridx = 1;
            gbc.gridy = 2;
            gbc.anchor = GridBagConstraints.WEST;
            panelViewGoToRoomAttendee.add(checkboxShowPasswordViewGoToRoomAttendee, gbc);

            // Button Panel
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
            buttonGoToRoomViewGoToRoomAttendee = new JButton("Vào phòng");
            buttonBackViewGoToRoomAttendee = new JButton("Quay lại");
            buttonPanel.add(buttonGoToRoomViewGoToRoomAttendee);
            buttonPanel.add(buttonBackViewGoToRoomAttendee);

            gbc.gridx = 0;
            gbc.gridy = 3;
            gbc.gridwidth = 2;
            gbc.anchor = GridBagConstraints.CENTER;
            panelViewGoToRoomAttendee.add(buttonPanel, gbc);

            panelViewGoToRoomAttendee.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        }
    }

    private void addActionEvent() {
        if (checkboxShowPasswordViewGoToRoomAttendee != null) {
            checkboxShowPasswordViewGoToRoomAttendee.addActionListener(event -> {
                if (checkboxShowPasswordViewGoToRoomAttendee.isSelected()) {
                    passwordfieldPasswordViewGoToRoomAttendee.setEchoChar((char) 0);
                } else {
                    passwordfieldPasswordViewGoToRoomAttendee.setEchoChar('•');
                }
            });
        }

        if (buttonGoToRoomViewGoToRoomAttendee != null) {
            buttonGoToRoomViewGoToRoomAttendee.addActionListener(event -> handleGoToRoom());
        }

        if (buttonBackViewGoToRoomAttendee != null) {
            buttonBackViewGoToRoomAttendee.addActionListener(event -> {
                this.dispose();
                new MenuAttendee(loginUser);
            });
        }
    }

    private void handleGoToRoom() {
        String roomIDText = textfieldRoomIDViewGoToRoomAttendee.getText().strip();
        String password = String.valueOf(passwordfieldPasswordViewGoToRoomAttendee.getPassword()).strip();

        if (roomIDText.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Mã phòng thi và mật khẩu không được bỏ trống!",
                    "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        try {
            long roomId = Long.parseLong(roomIDText);

            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            // Verify room and password
            Room room = roomService.selectVerifiedRoom(roomId, password);

            if (room == null) {
                setCursor(Cursor.getDefaultCursor());
                JOptionPane.showMessageDialog(
                        this,
                        "Sai mã phòng hoặc mật khẩu!",
                        "Lỗi đăng nhập",
                        JOptionPane.ERROR_MESSAGE
                );
                passwordfieldPasswordViewGoToRoomAttendee.setText("");
                return;
            }

            // Check if room is available
            if (!room.isAvailable()) {
                setCursor(Cursor.getDefaultCursor());
                JOptionPane.showMessageDialog(
                        this,
                        "Phòng thi đã bị đóng!",
                        "Cảnh báo",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            // Check if exam is assigned
            if (room.getExam() == null) {
                setCursor(Cursor.getDefaultCursor());
                JOptionPane.showMessageDialog(
                        this,
                        "Phòng thi chưa có đề thi!\nVui lòng liên hệ giáo viên.",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            // Check if user already took the exam
            boolean alreadyTaken = takeExamService.verifyUserAlreadyTakenExam(
                    loginUser.getUserId(),
                    room.getRoomId()
            );

            if (alreadyTaken) {
                setCursor(Cursor.getDefaultCursor());
                JOptionPane.showMessageDialog(
                        this,
                        "Bạn đã làm bài thi trong phòng này rồi!",
                        "Cảnh báo",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            // Fetch the full exam object to avoid LazyInitializationException
            Exam exam = takeExamService.selectExamOfRoom(room.getRoomId());
            if (exam == null) {
                setCursor(Cursor.getDefaultCursor());
                JOptionPane.showMessageDialog(
                        this,
                        "Không thể tải được thông tin đề thi!\nVui lòng thử lại.",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }
            
            // Fetch questions to get the total count, avoiding the lazy-loaded property
            List<Question> questions = takeExamService.selectQuestionOfExam(exam.getExamId());
            int totalQuestion = questions.size();

            setCursor(Cursor.getDefaultCursor());
            
            // Show confirmation dialog
            String[] roomInfo = {
                    "Mã phòng thi: " + room.getRoomId(),
                    "Tiêu đề: " + room.getTitle(),
                    "Thời gian: " + room.getTimeLimit() + " phút",
                    "Số câu hỏi: " + totalQuestion,
                    "",
                    "Bạn muốn vào phòng thi ngay bây giờ?"
            };

            int selection = JOptionPane.showConfirmDialog(
                    this,
                    roomInfo,
                    "Xác nhận vào phòng thi",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (selection == JOptionPane.OK_OPTION) {
                this.dispose();
                new TakeExamAttendee(loginUser, room);
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Mã phòng thi phải là số!",
                    "Lỗi nhập liệu",
                    JOptionPane.ERROR_MESSAGE
            );
        } catch (RemoteException e) {
            setCursor(Cursor.getDefaultCursor());
            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi kết nối server:\n" + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // For testing
        User testUser = new User("test", "Test User", "test", false);
        EventQueue.invokeLater(() -> new GoToRoomAttendee(testUser));
    }
}