package iuh.fit.se.gui;

import dao.UserDAO;
import entity.User;

import javax.swing.*;
import java.awt.*;

public class MenuAttendee extends JFrame {
    private final User loginUser;
    private JButton buttonGoToRoomAttendeeViewMenuAttendee;
    private JButton buttonResultAttendeeViewMenuAttendee;
    private JButton buttonLogoutViewMenuAttendee;
    private JPanel panelViewMenuAttendee;

    public MenuAttendee(User user) {
        this.loginUser = user;
        addActionEvent();
        this.setTitle("Menu Attendee");
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(panelViewMenuAttendee);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    public static void main(String[] args) {
        // For testing purposes, create a dummy User entity
        User attendee = new User("attendee", "Attendee User", UserDAO.encryptPassword("attendee_pass"), false);
        EventQueue.invokeLater(() -> new MenuAttendee(attendee));
    }

    private void addActionEvent() {
        buttonGoToRoomAttendeeViewMenuAttendee.addActionListener(event -> {
            this.dispose();
            new GoToRoomAttendee(loginUser);
        });
        buttonResultAttendeeViewMenuAttendee.addActionListener(event -> {
            this.dispose();
            new ResultAttendee(loginUser);
        });
        buttonLogoutViewMenuAttendee.addActionListener(event -> {
            this.dispose();
            new Login();
        });
    }

    private void createUIComponents() {
    }
}