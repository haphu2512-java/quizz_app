package iuh.fit.se.gui;

import entity.Enrollment;
import entity.Room;
import entity.User;
import iuh.fit.se.util.ServiceFactory;
import service.EnrollmentService;
import service.RoomService;
import service.UserService;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoomResultSummary extends JFrame {
    private final User loginUser;
    private EnrollmentService enrollmentService;
    private UserService userService;
    private RoomService roomService;

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
            this.userService = ServiceFactory.getUserService();
            this.roomService = ServiceFactory.getRoomService();
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

            Map<String, String> userNameCache = new HashMap<>();
            Map<Long, String> roomTitleCache = new HashMap<>();

            for (Enrollment enrollment : enrollments) {
                String userId = "N/A";
                String userName = "N/A";
                String roomId = "N/A";
                String roomTitle = "N/A";

                if (enrollment.getUser() != null) {
                    userId = enrollment.getUser().getUserId();
                    if (userNameCache.containsKey(userId)) {
                        userName = userNameCache.get(userId);
                    } else {
                        try {
                            User u = userService.findById(userId);
                            if (u != null) {
                                userName = u.getFullName();
                                userNameCache.put(userId, userName);
                            }
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }

                if (enrollment.getRoom() != null) {
                    long rId = enrollment.getRoom().getRoomId();
                    roomId = String.valueOf(rId);
                    if (roomTitleCache.containsKey(rId)) {
                        roomTitle = roomTitleCache.get(rId);
                    } else {
                        try {
                            Room r = roomService.findById(rId);
                            if (r != null) {
                                roomTitle = r.getTitle();
                                roomTitleCache.put(rId, roomTitle);
                            }
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }

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
