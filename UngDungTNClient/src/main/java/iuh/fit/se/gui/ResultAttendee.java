package iuh.fit.se.gui;

import dao.UserDAO;
import entity.Enrollment;
import entity.User;
import service.impl.EnrollmentServiceImpl;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.rmi.RemoteException;
import java.util.List;

public class ResultAttendee extends JFrame {
    private final User loginUser;
    private JTextField textfieldFindViewResultAttendee;
    private JTable tableViewResultAttendee;
    private JButton buttonBackViewResutlAttendee;
    private JLabel labelFindViewResultAttendee;
    private JPanel panelViewResultAttendee;
    private DefaultTableModel columnModel;
    private DefaultTableModel rowModel;
    private TableRowSorter<TableModel> rowSorter;

    private EnrollmentServiceImpl enrollmentService;

    public ResultAttendee(User loginUser) {
        this.loginUser = loginUser;
        try {
            this.enrollmentService = new EnrollmentServiceImpl();
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(this, "Error initializing enrollment service: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        this.setTitle("Xem điểm thi");
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setContentPane(panelViewResultAttendee);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        initComponents();
        addActionEvent();
        fillDataToTable();
        makeTableSearchable();
    }

    public static void main(String[] args) {
        // For testing purposes, create a dummy User entity
        User attendee = new User("attendee", "Attendee User", UserDAO.encryptPassword("attendee_pass"), false);
        EventQueue.invokeLater(() -> new ResultAttendee(attendee));
    }

    private void initComponents() {
        tableViewResultAttendee.setDefaultEditor(Object.class, null);
        tableViewResultAttendee.getTableHeader().setReorderingAllowed(false);
        columnModel = new DefaultTableModel(
                new Object[][]{},
                new String[]{"Mã phòng thi", "Điểm thi"}
        );
        tableViewResultAttendee.setModel(columnModel);
        rowModel = (DefaultTableModel) tableViewResultAttendee.getModel();
    }

    private void addActionEvent() {
        buttonBackViewResutlAttendee.addActionListener(event -> {
            this.dispose();
            new MenuAttendee(loginUser);
        });
    }

    private void fillDataToTable() {
        try {
            // Assuming selectByUserID is available and returns List<Enrollment>
            List<Enrollment> list = enrollmentService.selectByUserID(loginUser.getUserId());
            rowModel.setRowCount(0);
            for (var enrollment : list) {
                rowModel.addRow(new Object[]{
                        // enrollment.getRoom_id() -> enrollment.getRoom().getRoomId()
                        (enrollment.getRoom() != null ? enrollment.getRoom().getRoomId() : "N/A"),
                        enrollment.getScore()
                });
            }
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải dữ liệu điểm thi: " + e.getMessage(), "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void makeTableSearchable() {
        rowSorter = new TableRowSorter<>(rowModel);
        var i = 0;
        while (i < columnModel.getColumnCount()) {
            rowSorter.setSortable(i, false);
            ++i;
        }
        tableViewResultAttendee.setRowSorter(rowSorter);
        textfieldFindViewResultAttendee
                .getDocument()
                .addDocumentListener(new DocumentListener() {
                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        var text = textfieldFindViewResultAttendee.getText().strip();
                        if (text.length() != 0) {
                            rowSorter.setRowFilter(RowFilter.regexFilter(text));
                        } else {
                            rowSorter.setRowFilter(null);
                        }
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        var text = textfieldFindViewResultAttendee.getText().strip();
                        if (text.length() != 0) {
                            rowSorter.setRowFilter(RowFilter.regexFilter(text));
                        } else {
                            rowSorter.setRowFilter(null);
                        }
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                    }
                });
    }

    private void createUIComponents() {
    }
}