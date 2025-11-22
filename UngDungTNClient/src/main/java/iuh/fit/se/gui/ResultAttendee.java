package iuh.fit.se.gui;

import entity.Enrollment;
import entity.Exam;
import entity.Room;
import entity.User;
import iuh.fit.se.util.ServiceFactory;
import service.EnrollmentService;
import service.RoomService;
import service.TakeExamService;

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

// ResultAttendee.java - Xem điểm của học viên
public class ResultAttendee extends JFrame {
    private final User loginUser;
    private EnrollmentService enrollmentService;
    private TakeExamService takeExamService;
    private RoomService roomService;

    private JPanel panelViewResultAttendee;
    private JTextField textfieldFindViewResultAttendee;
    private JTable tableViewResultAttendee;
    private JButton buttonBackViewResutlAttendee;

    private DefaultTableModel columnModel;
    private DefaultTableModel rowModel;
    private TableRowSorter<TableModel> rowSorter;

    public ResultAttendee(User loginUser) {
        this.loginUser = loginUser;

        try {
            this.enrollmentService = ServiceFactory.getEnrollmentService();
            this.takeExamService = ServiceFactory.getTakeExamService();
            this.roomService = ServiceFactory.getRoomService();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi kết nối service: " + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
            this.dispose();
            new MenuAttendee(loginUser);
            return;
        }

        initComponents();
        addActionEvent();

        this.setTitle("Xem Điểm Thi");
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setContentPane(panelViewResultAttendee);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);

        loadResultData();
        makeTableSearchable();
    }

    private void initComponents() {
        panelViewResultAttendee = new JPanel(new BorderLayout(10, 10));
        panelViewResultAttendee.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panelViewResultAttendee.setPreferredSize(new Dimension(800, 600));


        // --- TABLE ---
        columnModel = new DefaultTableModel(
                new Object[][]{},
                new String[]{"Mã phòng thi", "Tiêu đề", "Điểm thi", "Điểm tối đa"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableViewResultAttendee = new JTable(columnModel);
        tableViewResultAttendee.getTableHeader().setReorderingAllowed(false);
        rowModel = (DefaultTableModel) tableViewResultAttendee.getModel();
        JScrollPane tableScrollPane = new JScrollPane(tableViewResultAttendee);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("Kết quả thi"));
        panelViewResultAttendee.add(tableScrollPane, BorderLayout.CENTER);


        // --- BUTTONS & SEARCH (SOUTH) ---
        JPanel southPanel = new JPanel(new BorderLayout(10,10));
        
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Tìm kiếm:"));
        textfieldFindViewResultAttendee = new JTextField(20);
        searchPanel.add(textfieldFindViewResultAttendee);
        southPanel.add(searchPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonBackViewResutlAttendee = new JButton("Quay lại");
        buttonPanel.add(buttonBackViewResutlAttendee);
        southPanel.add(buttonPanel, BorderLayout.EAST);
        
        panelViewResultAttendee.add(southPanel, BorderLayout.SOUTH);
    }

    private void addActionEvent() {
        buttonBackViewResutlAttendee.addActionListener(event -> {
            this.dispose();
            new MenuAttendee(loginUser);
        });
    }

    private void loadResultData() {
        if(rowModel == null) return;
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            List<Enrollment> enrollments = enrollmentService.selectByUserID(loginUser.getUserId());
            rowModel.setRowCount(0);

            Map<Long, String> roomTitleCache = new HashMap<>();

            for (Enrollment enrollment : enrollments) {
                String roomId = "N/A";
                String roomTitle = "N/A";
                double maxScore = 0;

                if (enrollment.getRoom() != null) {
                    long rId = enrollment.getRoom().getRoomId();
                    roomId = String.valueOf(rId);
                    
                    if (roomTitleCache.containsKey(rId)) {
                        roomTitle = roomTitleCache.get(rId);
                    } else {
                        Room room = roomService.findById(rId);
                        if (room != null) {
                            roomTitle = room.getTitle();
                            roomTitleCache.put(rId, roomTitle);
                        }
                    }

                    // Fetch the full exam object to avoid LazyInitializationException
                    Exam exam = takeExamService.selectExamOfRoom(rId);
                    if (exam != null) {
                        maxScore = exam.getTotalScore();
                    }
                }

                rowModel.addRow(new Object[]{
                        roomId,
                        roomTitle,
                        String.format("%.2f", enrollment.getScore()),
                        String.format("%.2f", maxScore)
                });
            }
            setCursor(Cursor.getDefaultCursor());
        } catch (RemoteException e) {
            setCursor(Cursor.getDefaultCursor());
            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi tải dữ liệu điểm thi:\n" + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        }
    }

    private void makeTableSearchable() {
        if (textfieldFindViewResultAttendee != null && tableViewResultAttendee != null) {
            rowSorter = new TableRowSorter<>(rowModel);
            tableViewResultAttendee.setRowSorter(rowSorter);

            textfieldFindViewResultAttendee.getDocument().addDocumentListener(new DocumentListener() {
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
                    String text = textfieldFindViewResultAttendee.getText().strip();
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