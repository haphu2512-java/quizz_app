package iuh.fit.se.gui;

import dao.UserDAO;
import entity.Exam;
import entity.Room;
import entity.User;
import service.impl.ExamServiceImpl;
import service.impl.RoomServiceImpl;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.rmi.RemoteException;
import java.util.List;

public class RoomManagement extends JFrame {
    private final User loginUser;
    private JTextField textfiledExamIDViewRoomManagement;
    private JTextField textfieldRoomTitleViewRoomManaGement;
    private JTextField textfieldTimeLimitViewRoomManagement;
    private JRadioButton radiobuttonOpenViewRoomManagement;
    private JButton buttonAddViewRoomManagement;
    private JButton buttonUpdateViewRoomManagement;
    private JButton buttonDeleteViewRoomManagement;
    private JButton buttonRoomResultSummaryViewRoomManagement;
    private JButton buttonBackViewRoomManagement;
    private JTable tableViewRoomManagement;
    private JRadioButton radiobuttonCloseViewRoomManagement;
    private JLabel labelExamIDViewRoomManagement;
    private JLabel labelRoomTitleViewRoomManagement;
    private JLabel labelTimeLimitViewRoomManagement;
    private JLabel labelRoomPasswordViewRoomManagement;
    private JLabel labelRoomStatusViewRoomManagement;
    private JPanel panelViewRoomManagement;
    private JLabel labelFindViewRoomManagement;
    private JTextField textfieldFindViewRoomManagement;
    private JButton buttonRefreshViewRoomManagement;
    private JTextField textfieldRoomIDViewRoomManagement;
    private JLabel labelRoomIDViewRoomManagement;
    private JTextField textfieldRoomPasswordViewRoomManagement;
    private ButtonGroup buttongroupStatusViewRoomManagement;
    private DefaultTableModel columnModel;
    private DefaultTableModel rowModel;
    private TableRowSorter<TableModel> rowSorter = null;
    private List<Room> list;
    private Room chosenRoom = null;

    private RoomServiceImpl roomService;
    private ExamServiceImpl examService;

    public RoomManagement(User loginUser) {
        this.loginUser = loginUser;
        try {
            this.roomService = new RoomServiceImpl();
            this.examService = new ExamServiceImpl();
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(this, "Error initializing services: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        initComponents();
        addActionEvent();
        this.setTitle("Quản Lý Phòng Thi");
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(panelViewRoomManagement);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        fillDataToTable();
        makeTableSearchable();
    }

    public static void main(String[] args) {
        // User admin = new User("admin", "admin", "admin", true); // Old User constructor
        User admin = new User("admin", "Admin User", UserDAO.encryptPassword("admin"), true);
        EventQueue.invokeLater(() -> new RoomManagement(admin));
    }

    private void initComponents() {
        textfieldRoomIDViewRoomManagement.setEnabled(false);
        tableViewRoomManagement.setDefaultEditor(Object.class, null);
        tableViewRoomManagement.getTableHeader().setReorderingAllowed(false);
        columnModel = new DefaultTableModel(
                new Object[][]{},
                new String[]{"Mã Phòng thi", "Mã Đề thi", "Tiêu đề", "Thời gian", "Mật khẩu", "Trạng thái"}
        );
        tableViewRoomManagement.setModel(columnModel);
        rowModel = (DefaultTableModel) tableViewRoomManagement.getModel();

        buttongroupStatusViewRoomManagement = new ButtonGroup();
        buttongroupStatusViewRoomManagement.add(radiobuttonOpenViewRoomManagement);
        buttongroupStatusViewRoomManagement.add(radiobuttonCloseViewRoomManagement);
    }

    private void addActionEvent() {
        tableViewRoomManagement.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                tableViewRoomManagementMouseClicked();
            }

            private void tableViewRoomManagementMouseClicked() {
                resetInputField();
                textfieldRoomIDViewRoomManagement.setEnabled(false);
                var index = tableViewRoomManagement.getSelectedRow();
                if (index >= 0 && index < list.size()) {
                    chosenRoom = list.get(index);
                    textfieldRoomIDViewRoomManagement.setText(String.valueOf(chosenRoom.getRoomId()));
                    if (chosenRoom.getExam() != null) {
                        textfiledExamIDViewRoomManagement.setText(String.valueOf(chosenRoom.getExam().getExamId()));
                    } else {
                        textfiledExamIDViewRoomManagement.setText("null"); // Display "null" if no exam is linked
                    }
                    textfieldRoomTitleViewRoomManaGement.setText(chosenRoom.getTitle());
                    textfieldTimeLimitViewRoomManagement.setText(String.valueOf(chosenRoom.getTimeLimit()));
                    textfieldRoomPasswordViewRoomManagement.setText(chosenRoom.getPassword());
                    if (chosenRoom.isAvailable()) {
                        radiobuttonOpenViewRoomManagement.setSelected(true);
                    } else {
                        radiobuttonCloseViewRoomManagement.setSelected(true);
                    }
                }
            }
        });

        buttonAddViewRoomManagement.addActionListener(event -> {
            var radioOpen = radiobuttonOpenViewRoomManagement.isSelected();
            var radioClose = radiobuttonCloseViewRoomManagement.isSelected();
            var radioIsSelected = radioOpen || radioClose;
            if (textfiledExamIDViewRoomManagement.getText().isEmpty()
                    || textfieldRoomTitleViewRoomManaGement.getText().isEmpty()
                    || textfieldTimeLimitViewRoomManagement.getText().isEmpty()
                    || textfieldRoomPasswordViewRoomManagement.getText().isEmpty()
                    || !radioIsSelected) {
                JOptionPane.showMessageDialog(
                        this,
                        "Các trường thông tin không được bỏ trống!",
                        "Cảnh Báo",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }
            try {
                long examId;
                // Handle "null" input for exam_id if the user can set it as such
                if (textfiledExamIDViewRoomManagement.getText().strip().equalsIgnoreCase("null")) {
                    examId = 0; // Represents no exam for this room, or handle as null
                } else {
                    examId = Long.parseLong(textfiledExamIDViewRoomManagement.getText().strip());
                }
                
                Exam checkValidExam = null;
                if (examId != 0) { // Only check if examId is provided
                    checkValidExam = examService.findById(examId);
                    if (checkValidExam == null) {
                        JOptionPane.showMessageDialog(
                                this,
                                "Mã đề thi không tồn tại. Hãy kiểm tra và thử lại sau!",
                                "Lỗi",
                                JOptionPane.ERROR_MESSAGE
                        );
                        return;
                    }
                }

                var title = textfieldRoomTitleViewRoomManaGement.getText().strip();
                var time_limit = Integer.parseInt(textfieldTimeLimitViewRoomManagement.getText().strip());
                var password = textfieldRoomPasswordViewRoomManagement.getText().strip();
                var is_available = radiobuttonOpenViewRoomManagement.isSelected();
                var room = new Room(checkValidExam, title, time_limit, password, is_available);
                var isSuccess = roomService.save(room);
                if (isSuccess) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Thêm thành công.",
                            "Thông Báo",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    fillDataToTable();
                } else {
                    JOptionPane.showMessageDialog(
                            this,
                            "Thêm thất bại. Xin hãy thử lại!",
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
                resetInputField();
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this, "Mã đề thi hoặc thời gian giới hạn không hợp lệ.", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
            } catch (RemoteException re) {
                JOptionPane.showMessageDialog(this, "Lỗi khi thêm phòng thi: " + re.getMessage(), "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
            }
        });

        buttonUpdateViewRoomManagement.addActionListener(event -> {
            var radioOpen = radiobuttonOpenViewRoomManagement.isSelected();
            var radioClose = radiobuttonCloseViewRoomManagement.isSelected();
            var radioIsSelected = radioOpen || radioClose;
            if (textfieldRoomIDViewRoomManagement.getText().isEmpty()
                    || textfiledExamIDViewRoomManagement.getText().isEmpty()
                    || textfieldRoomTitleViewRoomManaGement.getText().isEmpty()
                    || textfieldTimeLimitViewRoomManagement.getText().isEmpty()
                    || textfieldRoomPasswordViewRoomManagement.getText().isEmpty()
                    || !radioIsSelected) {
                JOptionPane.showMessageDialog(
                        this,
                        "Các trường thông tin không được bỏ trống!",
                        "Cảnh Báo",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }
            try {
                var roomId = Long.parseLong(textfieldRoomIDViewRoomManagement.getText().strip());
                long examId;
                if (textfiledExamIDViewRoomManagement.getText().strip().equalsIgnoreCase("null")) {
                    examId = 0;
                } else {
                    examId = Long.parseLong(textfiledExamIDViewRoomManagement.getText().strip());
                }

                var existingRoom = roomService.findById(roomId);
                if (existingRoom == null) {
                    JOptionPane.showMessageDialog(this, "Không tìm thấy phòng thi cần cập nhật.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Exam checkValidExam = null;
                if (examId != 0) {
                    checkValidExam = examService.findById(examId);
                    if (checkValidExam == null) {
                        JOptionPane.showMessageDialog(
                                this,
                                "Mã đề thi không tồn tại. Hãy kiểm tra và thử lại sau!",
                                "Lỗi",
                                JOptionPane.ERROR_MESSAGE
                        );
                        return;
                    }
                }
                existingRoom.setExam(checkValidExam); // Set null if examId was 0 or "null"
                existingRoom.setTitle(textfieldRoomTitleViewRoomManaGement.getText().strip());
                existingRoom.setTimeLimit(Integer.parseInt(textfieldTimeLimitViewRoomManagement.getText().strip()));
                existingRoom.setPassword(textfieldRoomPasswordViewRoomManagement.getText().strip());
                existingRoom.setAvailable(radiobuttonOpenViewRoomManagement.isSelected());

                var isSuccess = roomService.update(existingRoom);
                if (isSuccess) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Cập nhật thành công.",
                            "Thông Báo",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    fillDataToTable();
                } else {
                    JOptionPane.showMessageDialog(
                            this,
                            "Cập nhật thất bại. Xin hãy thử lại!",
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
                resetInputField();
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(this, "ID phòng thi, mã đề thi hoặc thời gian giới hạn không hợp lệ.", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
            } catch (RemoteException re) {
                JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật phòng thi: " + re.getMessage(), "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
            }
        });

        buttonDeleteViewRoomManagement.addActionListener(event -> {
            var roomIDText = textfieldRoomIDViewRoomManagement.getText().strip();
            if (!roomIDText.isEmpty()) {
                var confirmation = JOptionPane.showConfirmDialog(
                    this,
                    "Bạn có chắc muốn xóa phòng thi này không?",
                    "Xác nhận xóa",
                    JOptionPane.YES_NO_OPTION);

                if (confirmation == JOptionPane.YES_OPTION) {
                    try {
                        var roomId = Long.parseLong(roomIDText);
                        var isSuccess = roomService.delete(roomId);
                        if (isSuccess) {
                            JOptionPane.showMessageDialog(
                                    this,
                                    "Xoá thành công.",
                                    "Thông Báo",
                                    JOptionPane.INFORMATION_MESSAGE
                            );
                            fillDataToTable();
                        } else {
                            JOptionPane.showMessageDialog(
                                    this,
                                    "Xoá thất bại. Xin hãy thử lại!",
                                    "Lỗi",
                                    JOptionPane.ERROR_MESSAGE
                            );
                        }
                        resetInputField();
                    } catch (NumberFormatException nfe) {
                        JOptionPane.showMessageDialog(this, "ID phòng thi không hợp lệ.", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                    } catch (RemoteException re) {
                        JOptionPane.showMessageDialog(this, "Lỗi khi xoá phòng thi: " + re.getMessage(), "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Hãy chọn phòng cần xoá để tiến hành xoá!",
                        "Cảnh Báo",
                        JOptionPane.WARNING_MESSAGE
                );
            }
        });

        buttonRoomResultSummaryViewRoomManagement.addActionListener(event -> {
            // Need to pass the chosenRoom to RoomResultSummary if it's meant to show results for a specific room.
            // For now, it opens a generic RoomResultSummary window.
            this.dispose();
            new RoomResultSummary(loginUser);
        });

        buttonRefreshViewRoomManagement.addActionListener(event -> {
            resetInputField();
            textfieldFindViewRoomManagement.setText("");
            fillDataToTable();
        });

        buttonBackViewRoomManagement.addActionListener(event -> {
            if (loginUser.getUserId().equals(Login.username_admin)) { // Use constant for admin check
                this.dispose();
                new MenuAdmin(loginUser);
            } else {
                this.dispose();
                new MenuHost(loginUser);
            }
        });
    }

    private void resetInputField() {
        textfieldRoomIDViewRoomManagement.setText("");
        textfiledExamIDViewRoomManagement.setText("");
        textfieldRoomTitleViewRoomManaGement.setText("");
        textfieldTimeLimitViewRoomManagement.setText("");
        textfieldRoomPasswordViewRoomManagement.setText("");
        buttongroupStatusViewRoomManagement.clearSelection();
        chosenRoom = null; // Clear chosen room
        textfieldRoomIDViewRoomManagement.setEnabled(false); // Disable ID field after reset
    }

    private void fillDataToTable() {
        try {
            list = roomService.getAll();
            rowModel.setRowCount(0);
            for (var room : list) {
                rowModel.addRow(new Object[]{
                        room.getRoomId(),
                        (room.getExam() != null ? room.getExam().getExamId() : "N/A"), // Use getter, display N/A for null
                        room.getTitle(),
                        room.getTimeLimit(),
                        room.getPassword(),
                        room.isAvailable()
                });
            }
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải dữ liệu phòng thi: " + e.getMessage(), "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void makeTableSearchable() {
        rowSorter = new TableRowSorter<>(rowModel);
        var i = 0;
        while (i < columnModel.getColumnCount()) {
            rowSorter.setSortable(i, false);
            ++i;
        }
        tableViewRoomManagement.setRowSorter(rowSorter);
        textfieldFindViewRoomManagement
                .getDocument()
                .addDocumentListener(new DocumentListener() {
                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        var text = textfieldFindViewRoomManagement.getText().strip();
                        if (text.length() != 0) {
                            rowSorter.setRowFilter(RowFilter.regexFilter(text));
                        } else {
                            rowSorter.setRowFilter(null);
                        }
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        var text = textfieldFindViewRoomManagement.getText().strip();
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