package iuh.fit.se.gui;

import entity.User;
import service.impl.UserServiceImpl;

import javax.swing.*;
import java.awt.*;
import java.rmi.RemoteException;

public class Signup extends JFrame {
    private JTextField textfieldUserIDViewSignup;
    private JTextField textfieldFullnameViewSignup;
    private JPasswordField passwordfieldPasswordViewSignup;
    private JPasswordField passwordfieldPasswordAgainViewSignup;
    private JButton buttonSignupViewSignup;
    private JButton buttonBackViewSignup;
    private JLabel labelUserIDViewSignup;
    private JLabel labelFullnameViewSignup;
    private JLabel labelPasswordAgainViewSignup;
    private JPanel panelViewSignup;
    private JLabel labelPasswordViewSignup;

    private UserServiceImpl userService;

    public Signup() {
        try {
            this.userService = new UserServiceImpl();
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(this, "Error initializing user service: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        addActionEvent();
        this.setTitle("Đăng ký");
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(panelViewSignup);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(Signup::new);
    }

    private void addActionEvent() {
        buttonSignupViewSignup.addActionListener(event -> {
            var userID = textfieldUserIDViewSignup.getText().strip();
            var fullName = textfieldFullnameViewSignup.getText().strip();
            var password = String.valueOf(passwordfieldPasswordViewSignup.getPassword()).strip();
            var passwordAgain = String.valueOf(passwordfieldPasswordAgainViewSignup.getPassword()).strip();
            if (userID.isEmpty() || fullName.isEmpty() || password.isEmpty() || passwordAgain.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Các trường thông tin không được bỏ trống!",
                        "Cảnh Báo",
                        JOptionPane.WARNING_MESSAGE
                );
            } else if (!password.equals(passwordAgain)) {
                JOptionPane.showMessageDialog(
                        this,
                        "Mật khẩu nhập lại không khớp!",
                        "Cảnh Báo",
                        JOptionPane.WARNING_MESSAGE
                );
                passwordfieldPasswordViewSignup.setText("");
                passwordfieldPasswordAgainViewSignup.setText("");
            } else if (userID.equals("admin")) {
                JOptionPane.showMessageDialog(
                        this,
                        "Không thể tạo tài khoản với UserID này. Xin hãy sử dụng UserID khác!",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE
                );
                textfieldUserIDViewSignup.setText("");
                passwordfieldPasswordViewSignup.setText("");
                passwordfieldPasswordAgainViewSignup.setText("");
            } else {
                try {
                    if (verifyAccountNotExist(userID)) {
                        var user = new User(userID, fullName, password, false); // password will be encrypted by service
                        var isSuccess = userService.save(user);
                        if (isSuccess) {
                            JOptionPane.showMessageDialog(
                                    this,
                                    "Đăng ký tài khoản thành công.",
                                    "Thông Báo",
                                    JOptionPane.INFORMATION_MESSAGE
                            );
                        } else {
                            JOptionPane.showMessageDialog(
                                    this,
                                    "Đăng ký tài khoản thất bại. Xin hãy thử lại!",
                                    "Lỗi",
                                    JOptionPane.ERROR_MESSAGE
                            );
                        }
                        resetAll();
                    } else {
                        JOptionPane.showMessageDialog(
                                this,
                                "UserID đã tồn tại, thử lại với UserID khác!",
                                "Lỗi",
                                JOptionPane.ERROR_MESSAGE
                        );
                        passwordfieldPasswordViewSignup.setText("");
                        passwordfieldPasswordAgainViewSignup.setText("");
                    }
                } catch (RemoteException e) {
                    JOptionPane.showMessageDialog(this, "Lỗi khi đăng ký tài khoản: " + e.getMessage(), "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        buttonBackViewSignup.addActionListener(event -> {
            this.dispose();
            new Login();
        });
    }

    private boolean verifyAccountNotExist(String userID) throws RemoteException {
        var user = userService.findById(userID); // Use service to find by ID
        return user == null;
    }

    private void resetAll() {
        textfieldUserIDViewSignup.setText("");
        textfieldFullnameViewSignup.setText("");
        passwordfieldPasswordViewSignup.setText("");
        passwordfieldPasswordAgainViewSignup.setText("");
    }

    private void createUIComponents() {
    }
}