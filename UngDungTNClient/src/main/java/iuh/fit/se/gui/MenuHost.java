package iuh.fit.se.gui;

import entity.User;

import javax.swing.*;
import java.awt.*;

public class MenuHost extends JFrame {
    private final User loginUser;
    private JButton buttonLogoutViewMenuHost;
    private JButton buttonRoomManagementViewMenuHost;
    private JButton buttonExamManagementViewMenuHost;
    private JButton buttonQuestionManagementViewMenuHost;
    private JPanel panelViewMenuHost;

    public MenuHost(User user) {
        this.loginUser = user;
        
        initComponents();
        addActionEvent();
        
        this.setTitle("Menu Host");
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(panelViewMenuHost);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private void initComponents() {
        panelViewMenuHost = new JPanel(new GridLayout(2, 2, 15, 15));
        panelViewMenuHost.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        buttonExamManagementViewMenuHost = new JButton("Quản lý đề thi");
        buttonQuestionManagementViewMenuHost = new JButton("Quản lý câu hỏi");
        buttonRoomManagementViewMenuHost = new JButton("Quản lý phòng thi");
        buttonLogoutViewMenuHost = new JButton("Đăng xuất");

        panelViewMenuHost.add(buttonExamManagementViewMenuHost);
        panelViewMenuHost.add(buttonQuestionManagementViewMenuHost);
        panelViewMenuHost.add(buttonRoomManagementViewMenuHost);
        panelViewMenuHost.add(buttonLogoutViewMenuHost);
    }

    private void addActionEvent() {
        buttonExamManagementViewMenuHost.addActionListener(event -> {
            this.dispose();
            new ExamManagement(loginUser);
        });
        buttonQuestionManagementViewMenuHost.addActionListener(event -> {
            this.dispose();
            new QuestionManagement(loginUser);
        });
        buttonRoomManagementViewMenuHost.addActionListener(event -> {
            this.dispose();
            new RoomManagement(loginUser);
        });
        buttonLogoutViewMenuHost.addActionListener(event -> {
            this.dispose();
            new Login();
        });
    }
}