package iuh.fit.se.gui;

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
        initComponents();
        addActionEvent();
        
        this.setTitle("Menu Attendee");
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(panelViewMenuAttendee);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private void initComponents() {
        panelViewMenuAttendee = new JPanel(new GridLayout(3, 1, 10, 10));
        panelViewMenuAttendee.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
        panelViewMenuAttendee.setMinimumSize(new Dimension(300, 300));
        panelViewMenuAttendee.setPreferredSize(new Dimension(350, 300));

        buttonGoToRoomAttendeeViewMenuAttendee = new JButton("Vào phòng thi");
        buttonResultAttendeeViewMenuAttendee = new JButton("Xem điểm thi");
        buttonLogoutViewMenuAttendee = new JButton("Đăng xuất");
        
        panelViewMenuAttendee.add(buttonGoToRoomAttendeeViewMenuAttendee);
        panelViewMenuAttendee.add(buttonResultAttendeeViewMenuAttendee);
        panelViewMenuAttendee.add(buttonLogoutViewMenuAttendee);
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
}