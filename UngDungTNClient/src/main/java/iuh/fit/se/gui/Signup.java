package iuh.fit.se.gui;

import entity.User;
import iuh.fit.se.util.ServiceFactory;
import service.UserService;

import javax.swing.*;
import java.awt.*;
import java.rmi.RemoteException;

public class Signup extends JFrame {
    private JPanel panelViewSignup;
    private JTextField textfieldUserIDViewSignup;
    private JTextField textfieldFullnameViewSignup;
    private JPasswordField passwordfieldPasswordViewSignup;
    private JPasswordField passwordfieldPasswordAgainViewSignup;
    private JButton buttonSignupViewSignup;
    private JButton buttonBackViewSignup;
    private JLabel labelUserIDViewSignup;
    private JLabel labelFullnameViewSignup;
    private JLabel labelPasswordViewSignup;
    private JLabel labelPasswordAgainViewSignup;

    private UserService userService;

    public Signup() {
        try {
            this.userService = ServiceFactory.getUserService();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi kết nối service: " + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
            this.dispose();
            new Login();
            return;
        }

        initComponents();
        addActionEvent();

        this.setTitle("Đăng ký tài khoản - Quiz App");
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(panelViewSignup);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private void initComponents() {
        if (panelViewSignup == null) {
            panelViewSignup = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(8, 8, 8, 8);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            // UserID Label
            labelUserIDViewSignup = new JLabel("Tên tài khoản (UserID):");
            labelUserIDViewSignup.setFont(new Font("Arial", Font.BOLD, 14));
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.WEST;
            panelViewSignup.add(labelUserIDViewSignup, gbc);

            // UserID Field
            textfieldUserIDViewSignup = new JTextField(20);
            textfieldUserIDViewSignup.setFont(new Font("Arial", Font.PLAIN, 16));
            gbc.gridx = 0;
            gbc.gridy = 1;
            panelViewSignup.add(textfieldUserIDViewSignup, gbc);

            // Fullname Label
            labelFullnameViewSignup = new JLabel("Họ và tên:");
            labelFullnameViewSignup.setFont(new Font("Arial", Font.BOLD, 14));
            gbc.gridx = 0;
            gbc.gridy = 2;
            panelViewSignup.add(labelFullnameViewSignup, gbc);

            // Fullname Field
            textfieldFullnameViewSignup = new JTextField(20);
            textfieldFullnameViewSignup.setFont(new Font("Arial", Font.PLAIN, 16));
            gbc.gridx = 0;
            gbc.gridy = 3;
            panelViewSignup.add(textfieldFullnameViewSignup, gbc);

            // Password Label
            labelPasswordViewSignup = new JLabel("Mật khẩu:");
            labelPasswordViewSignup.setFont(new Font("Arial", Font.BOLD, 14));
            gbc.gridx = 0;
            gbc.gridy = 4;
            panelViewSignup.add(labelPasswordViewSignup, gbc);

            // Password Field
            passwordfieldPasswordViewSignup = new JPasswordField(20);
            passwordfieldPasswordViewSignup.setFont(new Font("Arial", Font.PLAIN, 16));
            gbc.gridx = 0;
            gbc.gridy = 5;
            panelViewSignup.add(passwordfieldPasswordViewSignup, gbc);

            // Password Again Label
            labelPasswordAgainViewSignup = new JLabel("Nhập lại mật khẩu:");
            labelPasswordAgainViewSignup.setFont(new Font("Arial", Font.BOLD, 14));
            gbc.gridx = 0;
            gbc.gridy = 6;
            panelViewSignup.add(labelPasswordAgainViewSignup, gbc);

            // Password Again Field
            passwordfieldPasswordAgainViewSignup = new JPasswordField(20);
            passwordfieldPasswordAgainViewSignup.setFont(new Font("Arial", Font.PLAIN, 16));
            gbc.gridx = 0;
            gbc.gridy = 7;
            panelViewSignup.add(passwordfieldPasswordAgainViewSignup, gbc);

            // Button Panel
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
            buttonSignupViewSignup = new JButton("Đăng ký");
            buttonBackViewSignup = new JButton("Quay lại");
            buttonPanel.add(buttonSignupViewSignup);
            buttonPanel.add(buttonBackViewSignup);

            gbc.gridx = 0;
            gbc.gridy = 8;
            gbc.insets = new Insets(15, 8, 8, 8);
            panelViewSignup.add(buttonPanel, gbc);

            panelViewSignup.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        }
    }

    private void addActionEvent() {
        buttonSignupViewSignup.addActionListener(event -> handleSignup());

        buttonBackViewSignup.addActionListener(event -> {
            this.dispose();
            new Login();
        });
    }

    private void handleSignup() {
        String userID = textfieldUserIDViewSignup.getText().strip();
        String fullName = textfieldFullnameViewSignup.getText().strip();
        String password = String.valueOf(passwordfieldPasswordViewSignup.getPassword()).strip();
        String passwordAgain = String.valueOf(passwordfieldPasswordAgainViewSignup.getPassword()).strip();

        // Validate empty fields
        if (userID.isEmpty() || fullName.isEmpty() || password.isEmpty() || passwordAgain.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Các trường thông tin không được bỏ trống!",
                    "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Validate password match
        if (!password.equals(passwordAgain)) {
            JOptionPane.showMessageDialog(
                    this,
                    "Mật khẩu nhập lại không khớp!",
                    "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE
            );
            passwordfieldPasswordViewSignup.setText("");
            passwordfieldPasswordAgainViewSignup.setText("");
            return;
        }

        // Validate admin username
        if (userID.equalsIgnoreCase("admin")) {
            JOptionPane.showMessageDialog(
                    this,
                    "Không thể tạo tài khoản với UserID này!\nXin hãy sử dụng UserID khác.",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
            textfieldUserIDViewSignup.setText("");
            passwordfieldPasswordViewSignup.setText("");
            passwordfieldPasswordAgainViewSignup.setText("");
            return;
        }

        // Validate password length
        if (password.length() < 6) {
            JOptionPane.showMessageDialog(
                    this,
                    "Mật khẩu phải có ít nhất 6 ký tự!",
                    "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            // Check if user exists
            User existingUser = userService.findById(userID);
            if (existingUser != null) {
                setCursor(Cursor.getDefaultCursor());
                JOptionPane.showMessageDialog(
                        this,
                        "UserID đã tồn tại!\nVui lòng chọn UserID khác.",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE
                );
                passwordfieldPasswordViewSignup.setText("");
                passwordfieldPasswordAgainViewSignup.setText("");
                return;
            }

            // Create new user (password will be encrypted by service)
            User newUser = new User(userID, fullName, password, false);
            boolean success = userService.save(newUser);

            setCursor(Cursor.getDefaultCursor());

            if (success) {
                JOptionPane.showMessageDialog(
                        this,
                        "Đăng ký tài khoản thành công!\nBạn có thể đăng nhập ngay bây giờ.",
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE
                );
                this.dispose();
                new Login();
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Đăng ký tài khoản thất bại!\nVui lòng thử lại.",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE
                );
                resetFields();
            }

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

    private void resetFields() {
        textfieldUserIDViewSignup.setText("");
        textfieldFullnameViewSignup.setText("");
        passwordfieldPasswordViewSignup.setText("");
        passwordfieldPasswordAgainViewSignup.setText("");
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(Signup::new);
    }
}