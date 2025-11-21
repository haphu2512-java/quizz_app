package iuh.fit.se.gui;

import entity.User;

import javax.swing.*;
import java.awt.*;

public class MenuAdmin extends JFrame {
    private final User loginUser;
    private JButton buttonUserManagementViewMenuAdmin;
    private JButton buttonLogoutViewMenuAdmin;
    private JButton buttonRoomManagementViewMenuAdmin;
    private JButton buttonQuestionManagementViewMenuAdmin;
    private JButton buttonExamManagementViewMenuAdmin;
    private JPanel panelViewMenuAdmin;

    public MenuAdmin(User user) {
        this.loginUser = user;
        
        initComponents();
        addActionEvent();

        this.setTitle("Menu Admin");
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(panelViewMenuAdmin);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private void initComponents() {
        panelViewMenuAdmin = new JPanel(new GridLayout(3, 2, 10, 10));
        panelViewMenuAdmin.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        buttonUserManagementViewMenuAdmin = new JButton("Quản lý người dùng");
        buttonRoomManagementViewMenuAdmin = new JButton("Quản lý phòng thi");
        buttonQuestionManagementViewMenuAdmin = new JButton("Quản lý câu hỏi");
        buttonExamManagementViewMenuAdmin = new JButton("Quản lý đề thi");
        buttonLogoutViewMenuAdmin = new JButton("Đăng xuất");

        panelViewMenuAdmin.add(buttonUserManagementViewMenuAdmin);
        panelViewMenuAdmin.add(buttonExamManagementViewMenuAdmin);
        panelViewMenuAdmin.add(buttonRoomManagementViewMenuAdmin);
        panelViewMenuAdmin.add(buttonQuestionManagementViewMenuAdmin);
        
        // Add a placeholder and the logout button to the last row for better alignment
        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        logoutPanel.add(buttonLogoutViewMenuAdmin);
        
        panelViewMenuAdmin.add(new JLabel()); // Placeholder
        panelViewMenuAdmin.add(logoutPanel);
    }

    private void addActionEvent() {
        buttonUserManagementViewMenuAdmin.addActionListener(event -> {
            this.dispose();
            new UserManagement(loginUser);
        });
        buttonExamManagementViewMenuAdmin.addActionListener(event -> {
            this.dispose();
            new ExamManagement(loginUser);
        });
        buttonQuestionManagementViewMenuAdmin.addActionListener(event -> {
            this.dispose();
            new QuestionManagement(loginUser);
        });
        buttonRoomManagementViewMenuAdmin.addActionListener(event -> {
            this.dispose();
            new RoomManagement(loginUser);
        });
        buttonLogoutViewMenuAdmin.addActionListener(event -> {
            this.dispose();
            new Login();
        });
    }
}