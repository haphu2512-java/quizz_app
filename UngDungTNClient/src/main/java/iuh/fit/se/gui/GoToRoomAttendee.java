package iuh.fit.se.gui;

import dao.TakeExamDAO;
import dao.UserDAO;
import entity.Room;
import entity.User;
import service.impl.RoomServiceImpl;

import javax.swing.*;
import java.awt.*;
import java.rmi.RemoteException;

public class GoToRoomAttendee extends JFrame {
    private final User loginUser;
    private JTextField textfieldRoomIDViewGoToRoomAttendee;
    private JPasswordField passwordfieldPasswordViewGoToRoomAttendee;
    private JButton buttonGoToRoomViewGoToRoomAttendee;
    private JButton buttonBackViewGoToRoomAttendee;
    private JLabel labelRoomIDViewGoToRoomAttendee;
    private JLabel labelPasswordViewGoToRoomAttendee;
    private JPanel panelViewGoToRoomAttendee;
    private JCheckBox checkboxShowPasswordViewGoToRoomAttendee;

    private RoomServiceImpl roomService; // Though not used directly, good to have if more Room CRUD operations are added
    private TakeExamDAO takeExamDAO; // Using DAO directly for specific queries as it's not a full CRUD service

    public GoToRoomAttendee(User loginUser) {
        this.loginUser = loginUser;
        try {
            this.roomService = new RoomServiceImpl();
            this.takeExamDAO = new TakeExamDAO();
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(this, "Error initializing services: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        addActionEvent();
        this.setTitle("Vào Phòng Thi");
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(panelViewGoToRoomAttendee);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    public static void main(String[] args) {
        // For testing purposes, create a dummy User entity
        User attendee = new User("attendee", "Attendee User", UserDAO.encryptPassword("attendee_pass"), false);
        EventQueue.invokeLater(() -> new GoToRoomAttendee(attendee));
    }

    private void addActionEvent() {
        checkboxShowPasswordViewGoToRoomAttendee.addActionListener(event -> {
            if (checkboxShowPasswordViewGoToRoomAttendee.isSelected()) {
                passwordfieldPasswordViewGoToRoomAttendee.setEchoChar((char) 0);
            } else {
                passwordfieldPasswordViewGoToRoomAttendee.setEchoChar('*');
            }
        });

        buttonGoToRoomViewGoToRoomAttendee.addActionListener(event -> {
            var roomIDText = textfieldRoomIDViewGoToRoomAttendee.getText().strip();
            var password = String.valueOf(passwordfieldPasswordViewGoToRoomAttendee.getPassword()).strip();
            if (roomIDText.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Mã phòng thi hoặc mật khẩu phòng không được bỏ trống!",
                        "Cảnh Báo",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            try {
                long roomId = Long.parseLong(roomIDText);
                // Use TakeExamDAO's specific method for verifying room login
                Room room = takeExamDAO.selectVerifiedRoom(roomId, password); 
                if (room == null) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Sai mã phòng hoặc mật khẩu phòng!",
                            "Cảnh Báo",
                            JOptionPane.WARNING_MESSAGE
                    );
                    passwordfieldPasswordViewGoToRoomAttendee.setText("");
                    return;
                }
                if (!room.isAvailable()) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Phòng thi đã bị đóng!",
                            "Cảnh Báo",
                            JOptionPane.WARNING_MESSAGE
                    );
                    passwordfieldPasswordViewGoToRoomAttendee.setText("");
                    return;
                }
                
                // Check if the Exam object is null or if its ID is invalid (e.g., 0)
                // This handles cases where exam_id might be SET NULL or not assigned
                if (room.getExam() == null || room.getExam().getExamId() == 0) { 
                    JOptionPane.showMessageDialog(
                            this,
                            "Không thể vào phòng. Đề thi đã bị lỗi hoặc chưa được gán!",
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE
                    );
                    passwordfieldPasswordViewGoToRoomAttendee.setText("");
                    return;
                }

                // Verify if user has already taken the exam in this room
                boolean verifyUserAlreadyTakenExam = takeExamDAO.verifyUserAlreadyTakenExam(
                        loginUser.getUserId(), // Use getter from entity.User
                        room.getRoomId() // Use getter from entity.Room
                );
                if (verifyUserAlreadyTakenExam) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Không thể vào phòng. Bạn đã làm bài thi trong phòng này!",
                            "Cảnh Báo",
                            JOptionPane.WARNING_MESSAGE
                    );
                    passwordfieldPasswordViewGoToRoomAttendee.setText("");
                    return;
                }
                var room_id_text = "Mã phòng thi: " + room.getRoomId();
                var room_title_text = "Tiêu đề: " + room.getTitle();
                var room_timelimit_text = "Thời gian: " + room.getTimeLimit() + " phút"; // Use getter
                var confirm = "Bạn muốn vào phòng thi ngay bây giờ?";
                var selection = JOptionPane.showConfirmDialog(
                        this,
                        new Object[]{room_id_text, room_title_text, room_timelimit_text, confirm},
                        "Xác nhận vào phòng thi",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                if (selection == JOptionPane.OK_OPTION) {
                    this.dispose();
                    new TakeExamAttendee(loginUser, room);
                }
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this, "Mã phòng thi không hợp lệ.", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
            } catch (RemoteException e) {
                JOptionPane.showMessageDialog(this, "Lỗi khi kiểm tra phòng thi: " + e.getMessage(), "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
            }
        });

        buttonBackViewGoToRoomAttendee.addActionListener(event -> {
            this.dispose();
            new MenuAttendee(loginUser);
        });
    }

    private void createUIComponents() {
    }
}