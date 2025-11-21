package iuh.fit.se.gui;

import entity.Enrollment;
import entity.User;
import iuh.fit.se.util.ServiceFactory;
import service.EnrollmentService;

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
    private EnrollmentService enrollmentService;

    private JPanel panelViewRoomResultSummary;
    private JTextField textfieldFindViewRoomResultSummary;
    private JTable tableViewRoomResultSummary;
    private JButton buttonBackViewRoomResultSummary;

    private DefaultTableModel columnModel;
    private DefaultTableModel rowModel;
    private TableRowSorter<TableModel> rowSorter;

    public RoomResultSummary(User loginUser) {
        this.loginUser = loginUser;

        try {
            this.enrollmentService = ServiceFactory.getEnrollmentService();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi kết nối service: " + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
            this.dispose();
            return;
        }

        initComponents();
        addActionEvent();

        this.setTitle("Tổng Kết Điểm");
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setContentPane(panelViewRoomResultSummary);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);

        loadResultData();
        makeTableSearchable();
    }

    private void initComponents() {
        panelViewRoomResultSummary = new JPanel(new BorderLayout(10, 10));
        panelViewRoomResultSummary.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panelViewRoomResultSummary.setPreferredSize(new Dimension(800, 600));

        // --- TABLE ---
        columnModel = new DefaultTableModel(
                new Object[][]{},
                new String[]{"UserID", "Họ tên", "Mã phòng", "Tiêu đề phòng", "Điểm"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableViewRoomResultSummary = new JTable(columnModel);
        tableViewRoomResultSummary.getTableHeader().setReorderingAllowed(false);
        rowModel = (DefaultTableModel) tableViewRoomResultSummary.getModel();
        JScrollPane tableScrollPane = new JScrollPane(tableViewRoomResultSummary);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("Bảng tổng kết điểm"));
        panelViewRoomResultSummary.add(tableScrollPane, BorderLayout.CENTER);


        // --- BUTTONS & SEARCH (SOUTH) ---
        JPanel southPanel = new JPanel(new BorderLayout(10, 10));
        
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Tìm kiếm:"));
        textfieldFindViewRoomResultSummary = new JTextField(20);
        searchPanel.add(textfieldFindViewRoomResultSummary);
        southPanel.add(searchPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonBackViewRoomResultSummary = new JButton("Quay lại");
        buttonPanel.add(buttonBackViewRoomResultSummary);
        southPanel.add(buttonPanel, BorderLayout.EAST);
        
        panelViewRoomResultSummary.add(southPanel, BorderLayout.SOUTH);
    }

    private void addActionEvent() {
        if (buttonBackViewRoomResultSummary != null) {
            buttonBackViewRoomResultSummary.addActionListener(event -> {
                this.dispose();
                new RoomManagement(loginUser);
            });
        }
    }

    private void loadResultData() {
        if (rowModel == null) return;
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            List<Enrollment> enrollments = enrollmentService.getAll();
            rowModel.setRowCount(0);

            for (Enrollment enrollment : enrollments) {
                String userId = enrollment.getUser() != null ?
                        enrollment.getUser().getUserId() : "N/A";
                String userName = enrollment.getUser() != null ?
                        enrollment.getUser().getFullName() : "N/A";
                String roomId = enrollment.getRoom() != null ?
                        String.valueOf(enrollment.getRoom().getRoomId()) : "N/A";
                String roomTitle = enrollment.getRoom() != null ?
                        enrollment.getRoom().getTitle() : "N/A";

                rowModel.addRow(new Object[]{
                        userId,
                        userName,
                        roomId,
                        roomTitle,
                        String.format("%.2f", enrollment.getScore())
                });
            }
            setCursor(Cursor.getDefaultCursor());
        } catch (RemoteException e) {
            setCursor(Cursor.getDefaultCursor());
            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi tải dữ liệu tổng kết:\n" + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        }
    }

    private void makeTableSearchable() {
        if (textfieldFindViewRoomResultSummary != null && tableViewRoomResultSummary != null) {
            rowSorter = new TableRowSorter<>(rowModel);
            tableViewRoomResultSummary.setRowSorter(rowSorter);

            textfieldFindViewRoomResultSummary.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    filter();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    filter();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    filter();
                }

                private void filter() {
                    String text = textfieldFindViewRoomResultSummary.getText().strip();
                    if (text.isEmpty()) {
                        rowSorter.setRowFilter(null);
                    } else {
                        rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                    }
                }
            });
        }
    }
}