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


public class RoomResultSummary extends JFrame {
    private final User loginUser;
    private JPanel panelViewRoomResultSummary;
    private JTextField textfieldFindViewRoomResultSummary;
    private JTable tableViewRoomResultSummary;
    private JButton buttonBackViewRoomResultSummary;
    private JLabel labelFindViewRoomResultSummary;

    private DefaultTableModel columnModel;
    private DefaultTableModel rowModel;
    private TableRowSorter<TableModel> rowSorter;

    private EnrollmentServiceImpl enrollmentService;

    public RoomResultSummary(User loginUser) {
        this.loginUser = loginUser;
        try {
            this.enrollmentService = new EnrollmentServiceImpl();
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(this, "Error initializing enrollment service: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        this.setTitle("Tổng kết điểm");
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setContentPane(panelViewRoomResultSummary);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        initComponents();
        addActionEvent();
        fillDataToTable();
        makeTableSearchable();
    }

    public static void main(String[] args) {
        // var admin = new User("admin", "admin", "admin", true); // Old User constructor
        User admin = new User("admin", "Admin User", UserDAO.encryptPassword("admin"), true);
        EventQueue.invokeLater(() -> new RoomResultSummary(admin));
    }

    private void createUIComponents() {
    }

    private void initComponents() {
        tableViewRoomResultSummary.setDefaultEditor(Object.class, null);
        tableViewRoomResultSummary.getTableHeader().setReorderingAllowed(false);
        columnModel = new DefaultTableModel(
                new Object[][]{},
                new String[]{"UserID", "Mã phòng thi", "Điểm thi"}
        );
        tableViewRoomResultSummary.setModel(columnModel);
        rowModel = (DefaultTableModel) tableViewRoomResultSummary.getModel();
    }

    private void addActionEvent() {
        buttonBackViewRoomResultSummary.addActionListener(event -> {
            this.dispose();
            new RoomManagement(loginUser);
        });
    }

    private void fillDataToTable() {
        try {
            List<Enrollment> list = enrollmentService.getAll(); // Use service to get all enrollments
            rowModel.setRowCount(0);
            for (var enrollment : list) {
                rowModel.addRow(new Object[]{
                        (enrollment.getUser() != null ? enrollment.getUser().getUserId() : "N/A"), // Use getter
                        (enrollment.getRoom() != null ? enrollment.getRoom().getRoomId() : "N/A"), // Use getter
                        enrollment.getScore()
                });
            }
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải dữ liệu tổng kết điểm: " + e.getMessage(), "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void makeTableSearchable() {
        rowSorter = new TableRowSorter<>(rowModel);
        var i = 0;
        while (i < columnModel.getColumnCount()) {
            rowSorter.setSortable(i, false);
            ++i;
        }
        tableViewRoomResultSummary.setRowSorter(rowSorter);
        textfieldFindViewRoomResultSummary
                .getDocument()
                .addDocumentListener(new DocumentListener() {
                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        var text = textfieldFindViewRoomResultSummary.getText().strip();
                        if (text.length() != 0) {
                            rowSorter.setRowFilter(RowFilter.regexFilter(text));
                        } else {
                            rowSorter.setRowFilter(null);
                        }
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        var text = textfieldFindViewRoomResultSummary.getText().strip();
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
}