package iuh.fit.se.gui;

import entity.User;
import iuh.fit.se.util.ServiceFactory;
import service.UserService;

import javax.swing.*;
import java.awt.*;
import java.rmi.RemoteException;

public class Login extends JFrame {
    public static final String username_admin = "admin";
    private static final String password_admin = "admin";

    private JPanel panelViewLogin;
    private JLabel labelUsernameViewLogin;
    private JLabel labelPasswordViewLogin;
    private JButton buttonLoginViewLogin;
    private JButton buttonQuitViewLogin;
    private JTextField textfieldUsernameViewLogin;
    private JPasswordField passwordfieldPasswordViewLogin;
    private JButton buttonSignupViewLogin;
    private JLabel labelSignupViewLogin;
    private JCheckBox checkboxShowPasswordViewLogin;

    private UserService userService;

    public Login() {
        try {
            this.userService = ServiceFactory.getUserService();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi kết nối service: " + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
            System.exit(1);
        }

        initComponents();
        addActionEvent();
        this.setTitle("Đăng nhập - Quiz App");
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(panelViewLogin);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private void initComponents() {
        // Initialize components if not using form designer
        if (panelViewLogin == null) {
            panelViewLogin = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            // Username label
            labelUsernameViewLogin = new JLabel("Tên đăng nhập:");
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            panelViewLogin.add(labelUsernameViewLogin, gbc);

            // Username field
            textfieldUsernameViewLogin = new JTextField(20);
            textfieldUsernameViewLogin.setFont(new Font("Arial", Font.BOLD, 16));
            gbc.gridx = 1;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            panelViewLogin.add(textfieldUsernameViewLogin, gbc);

            // Password label
            labelPasswordViewLogin = new JLabel("Mật khẩu:");
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.gridwidth = 1;
            panelViewLogin.add(labelPasswordViewLogin, gbc);

            // Password field
            passwordfieldPasswordViewLogin = new JPasswordField(20);
            passwordfieldPasswordViewLogin.setFont(new Font("Arial", Font.BOLD, 16));
            gbc.gridx = 1;
            gbc.gridy = 1;
            gbc.gridwidth = 2;
            panelViewLogin.add(passwordfieldPasswordViewLogin, gbc);

            // Show password checkbox
            checkboxShowPasswordViewLogin = new JCheckBox("Hiện mật khẩu");
            gbc.gridx = 1;
            gbc.gridy = 2;
            gbc.gridwidth = 2;
            gbc.anchor = GridBagConstraints.WEST;
            panelViewLogin.add(checkboxShowPasswordViewLogin, gbc);

            // Login button
            buttonLoginViewLogin = new JButton("Đăng nhập");
            gbc.gridx = 1;
            gbc.gridy = 3;
            gbc.gridwidth = 1;
            gbc.anchor = GridBagConstraints.CENTER;
            panelViewLogin.add(buttonLoginViewLogin, gbc);

            // Quit button
            buttonQuitViewLogin = new JButton("Thoát");
            gbc.gridx = 2;
            gbc.gridy = 3;
            panelViewLogin.add(buttonQuitViewLogin, gbc);

            // Signup label
            labelSignupViewLogin = new JLabel("Không có tài khoản?");
            gbc.gridx = 1;
            gbc.gridy = 4;
            panelViewLogin.add(labelSignupViewLogin, gbc);

            // Signup button
            buttonSignupViewLogin = new JButton("Đăng ký");
            gbc.gridx = 2;
            gbc.gridy = 4;
            panelViewLogin.add(buttonSignupViewLogin, gbc);

            panelViewLogin.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        }
    }

    private void addActionEvent() {
        buttonLoginViewLogin.addActionListener(event -> handleLogin());

        passwordfieldPasswordViewLogin.addActionListener(event -> handleLogin());

        buttonSignupViewLogin.addActionListener(event -> {
            this.dispose();
            new Signup();
        });

        checkboxShowPasswordViewLogin.addActionListener(event -> {
            if (checkboxShowPasswordViewLogin.isSelected()) {
                passwordfieldPasswordViewLogin.setEchoChar((char) 0);
            } else {
                passwordfieldPasswordViewLogin.setEchoChar('•');
            }
        });

        buttonQuitViewLogin.addActionListener(event -> {
            var selection = JOptionPane.showConfirmDialog(
                    this,
                    "Bạn thật sự muốn thoát?",
                    "Xác nhận thoát",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );
            if (selection == JOptionPane.YES_OPTION) {
                ServiceFactory.close();
                System.exit(0);
            }
        });
    }

    private void handleLogin() {
        String username = textfieldUsernameViewLogin.getText().strip();
        String password = String.valueOf(passwordfieldPasswordViewLogin.getPassword()).strip();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Tên đăng nhập hoặc mật khẩu không được bỏ trống!",
                    "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Show loading cursor
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        // Handle admin login
        if (username.equals(username_admin) && password.equals(password_admin)) {
            User admin = new User(username_admin, "Administrator", password_admin, true);
            setCursor(Cursor.getDefaultCursor());
            this.dispose();
            new MenuAdmin(admin);
            return;
        }

        // Handle normal user login via RMI
        try {
            User loginUser = userService.selectByAccount(username, password);
            setCursor(Cursor.getDefaultCursor());

            if (loginUser != null) {
                this.dispose();
                if (loginUser.isHost()) {
                    new MenuHost(loginUser);
                } else {
                    new MenuAttendee(loginUser);
                }
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Sai tên đăng nhập hoặc mật khẩu!",
                        "Đăng nhập thất bại",
                        JOptionPane.ERROR_MESSAGE
                );
                passwordfieldPasswordViewLogin.setText("");
                textfieldUsernameViewLogin.requestFocus();
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

    public static void main(String[] args) {
        EventQueue.invokeLater(Login::new);
    }
}