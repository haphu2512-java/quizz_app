package iuh.fit.se.gui;

import entity.Exam;
import entity.Room;
import entity.User;
import iuh.fit.se.util.ServiceFactory;
import service.ExamService;
import service.RoomService;

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
    private  RoomService roomService;
    private  ExamService examService;

    private JPanel panelViewRoomManagement;
    private JTextField textfieldRoomIDViewRoomManagement;
    private JTextField textfiledExamIDViewRoomManagement;
    private JTextField textfieldRoomTitleViewRoomManaGement;
    private JTextField textfieldTimeLimitViewRoomManagement;
    private JTextField textfieldRoomPasswordViewRoomManagement;
    private JRadioButton radiobuttonOpenViewRoomManagement;
    private JRadioButton radiobuttonCloseViewRoomManagement;
    private JButton buttonAddViewRoomManagement;
    private JButton buttonUpdateViewRoomManagement;
    private JButton buttonDeleteViewRoomManagement;
    private JButton buttonRoomResultSummaryViewRoomManagement;
    private JButton buttonBackViewRoomManagement;
    private JButton buttonRefreshViewRoomManagement;
    private JTextField textfieldFindViewRoomManagement;
    private JTable tableViewRoomManagement;
    private ButtonGroup buttongroupStatusViewRoomManagement;

    private DefaultTableModel columnModel;
    private DefaultTableModel rowModel;
    private TableRowSorter<TableModel> rowSorter = null;
    private List<Room> roomList;
    private Room chosenRoom = null;

    public RoomManagement(User user) {
        this.loginUser = user;

        try {
            this.roomService = ServiceFactory.getRoomService();
            this.examService = ServiceFactory.getExamService();
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

        this.setTitle("Quản Lý Phòng Thi");
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(panelViewRoomManagement);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);

        loadRoomData();
        makeTableSearchable();
    }

    private void initComponents() {
        panelViewRoomManagement = new JPanel(new BorderLayout(10, 10));
        panelViewRoomManagement.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- FORM PANEL (NORTH) ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Thông tin phòng thi"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Room ID
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Mã Phòng:"), gbc);
        gbc.gridx = 1;
        textfieldRoomIDViewRoomManagement = new JTextField(20);
        textfieldRoomIDViewRoomManagement.setEnabled(false);
        formPanel.add(textfieldRoomIDViewRoomManagement, gbc);

        // Exam ID
        gbc.gridx = 2; gbc.gridy = 0;
        formPanel.add(new JLabel("Mã Đề Thi:"), gbc);
        gbc.gridx = 3;
        textfiledExamIDViewRoomManagement = new JTextField(20);
        formPanel.add(textfiledExamIDViewRoomManagement, gbc);

        // Room Title
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Tiêu đề:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        textfieldRoomTitleViewRoomManaGement = new JTextField();
        formPanel.add(textfieldRoomTitleViewRoomManaGement, gbc);
        gbc.gridwidth = 1; // reset

        // Time Limit
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Thời gian (phút):"), gbc);
        gbc.gridx = 1;
        textfieldTimeLimitViewRoomManagement = new JTextField();
        formPanel.add(textfieldTimeLimitViewRoomManagement, gbc);

        // Password
        gbc.gridx = 2; gbc.gridy = 2;
        formPanel.add(new JLabel("Mật khẩu:"), gbc);
        gbc.gridx = 3;
        textfieldRoomPasswordViewRoomManagement = new JTextField();
        formPanel.add(textfieldRoomPasswordViewRoomManagement, gbc);
        
        // Status
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Trạng thái:"), gbc);
        gbc.gridx = 1;
        radiobuttonOpenViewRoomManagement = new JRadioButton("Mở");
        radiobuttonCloseViewRoomManagement = new JRadioButton("Đóng");
        buttongroupStatusViewRoomManagement = new ButtonGroup();
        buttongroupStatusViewRoomManagement.add(radiobuttonOpenViewRoomManagement);
        buttongroupStatusViewRoomManagement.add(radiobuttonCloseViewRoomManagement);
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        statusPanel.add(radiobuttonOpenViewRoomManagement);
        statusPanel.add(radiobuttonCloseViewRoomManagement);
        formPanel.add(statusPanel, gbc);


        panelViewRoomManagement.add(formPanel, BorderLayout.NORTH);

        // --- TABLE PANEL (CENTER) ---
        columnModel = new DefaultTableModel(
                new Object[][]{},
                new String[]{"Mã Phòng", "Mã Đề thi", "Tiêu đề", "Thời gian (phút)", "Mật khẩu", "Trạng thái"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableViewRoomManagement = new JTable(columnModel);
        tableViewRoomManagement.getTableHeader().setReorderingAllowed(false);
        rowModel = (DefaultTableModel) tableViewRoomManagement.getModel();
        JScrollPane tableScrollPane = new JScrollPane(tableViewRoomManagement);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("Danh sách phòng thi"));
        panelViewRoomManagement.add(tableScrollPane, BorderLayout.CENTER);


        // --- BUTTONS PANEL (SOUTH) ---
        JPanel southPanel = new JPanel(new BorderLayout(10,10));

        JPanel actionButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        buttonAddViewRoomManagement = new JButton("Thêm");
        buttonUpdateViewRoomManagement = new JButton("Cập nhật");
        buttonDeleteViewRoomManagement = new JButton("Xóa");
        buttonRefreshViewRoomManagement = new JButton("Làm mới");
        buttonRoomResultSummaryViewRoomManagement = new JButton("Thống kê kết quả");
        actionButtonsPanel.add(buttonAddViewRoomManagement);
        actionButtonsPanel.add(buttonUpdateViewRoomManagement);
        actionButtonsPanel.add(buttonDeleteViewRoomManagement);
        actionButtonsPanel.add(buttonRefreshViewRoomManagement);
        actionButtonsPanel.add(buttonRoomResultSummaryViewRoomManagement);
        southPanel.add(actionButtonsPanel, BorderLayout.NORTH);

        JPanel searchBackPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        searchBackPanel.add(new JLabel("Tìm kiếm:"));
        textfieldFindViewRoomManagement = new JTextField(20);
        searchBackPanel.add(textfieldFindViewRoomManagement);
        buttonBackViewRoomManagement = new JButton("Quay lại");
        searchBackPanel.add(buttonBackViewRoomManagement);
        southPanel.add(searchBackPanel, BorderLayout.SOUTH);

        panelViewRoomManagement.add(southPanel, BorderLayout.SOUTH);
    }

    private void addActionEvent() {
        tableViewRoomManagement.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleTableRowSelection();
            }
        });

        buttonAddViewRoomManagement.addActionListener(e -> handleAdd());
        buttonUpdateViewRoomManagement.addActionListener(e -> handleUpdate());
        buttonDeleteViewRoomManagement.addActionListener(e -> handleDelete());

        buttonRoomResultSummaryViewRoomManagement.addActionListener(e -> {
            this.dispose();
            new RoomResultSummary(loginUser);
        });

        buttonRefreshViewRoomManagement.addActionListener(e -> {
            resetInputFields();
            loadRoomData();
            textfieldFindViewRoomManagement.setText("");
        });

        buttonBackViewRoomManagement.addActionListener(e -> {
            this.dispose();
            if (loginUser.isHost()) { 
                new MenuHost(loginUser);
            } else {
                new MenuAdmin(loginUser);
            }
        });
    }

    private void handleTableRowSelection() {
        textfieldRoomIDViewRoomManagement.setEnabled(false);

        int modelRow = tableViewRoomManagement.getSelectedRow();
        if (modelRow != -1) {
            int viewRow = tableViewRoomManagement.convertRowIndexToModel(modelRow);
            if(viewRow >= 0 && viewRow < roomList.size()) {
                chosenRoom = roomList.get(viewRow);
                textfieldRoomIDViewRoomManagement.setText(String.valueOf(chosenRoom.getRoomId()));

                if (chosenRoom.getExam() != null) {
                    textfiledExamIDViewRoomManagement.setText(String.valueOf(chosenRoom.getExam().getExamId()));
                } else {
                    textfiledExamIDViewRoomManagement.setText("");
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
    }

    private void handleAdd() {
        String examIDStr = textfiledExamIDViewRoomManagement.getText().strip();
        String title = textfieldRoomTitleViewRoomManaGement.getText().strip();
        String timeLimitStr = textfieldTimeLimitViewRoomManagement.getText().strip();
        String password = textfieldRoomPasswordViewRoomManagement.getText().strip();
        boolean isOpen = radiobuttonOpenViewRoomManagement.isSelected();
        boolean isClosed = radiobuttonCloseViewRoomManagement.isSelected();

        // Validate
        if (examIDStr.isEmpty() || title.isEmpty() || timeLimitStr.isEmpty() ||
                password.isEmpty() || (!isOpen && !isClosed)) {
            JOptionPane.showMessageDialog(
                    this,
                    "Các trường thông tin không được bỏ trống!",
                    "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        try {
            long examId = Long.parseLong(examIDStr);
            int timeLimit = Integer.parseInt(timeLimitStr);

            if (timeLimit <= 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "Thời gian phải lớn hơn 0!",
                        "Lỗi nhập liệu",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            Exam exam = examService.findById(examId);
            if (exam == null) {
                setCursor(Cursor.getDefaultCursor());
                JOptionPane.showMessageDialog(
                        this,
                        "Mã đề thi không tồn tại!",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            Room newRoom = new Room(exam, title, timeLimit, password, isOpen);
            boolean success = roomService.save(newRoom);

            setCursor(Cursor.getDefaultCursor());

            if (success) {
                JOptionPane.showMessageDialog(
                        this,
                        "Thêm phòng thi thành công!",
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE
                );
                loadRoomData();
                resetInputFields();
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Thêm phòng thi thất bại!",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Mã đề thi và thời gian phải là số!",
                    "Lỗi nhập liệu",
                    JOptionPane.ERROR_MESSAGE
            );
        } catch (RemoteException e) {
            setCursor(Cursor.getDefaultCursor());
            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi kết nối server:\n" + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        }
    }

    private void handleUpdate() {
        if (chosenRoom == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn phòng thi cần cập nhật!",
                    "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        String examIDStr = textfiledExamIDViewRoomManagement.getText().strip();
        String title = textfieldRoomTitleViewRoomManaGement.getText().strip();
        String timeLimitStr = textfieldTimeLimitViewRoomManagement.getText().strip();
        String password = textfieldRoomPasswordViewRoomManagement.getText().strip();
        boolean isOpen = radiobuttonOpenViewRoomManagement.isSelected();

        if (examIDStr.isEmpty() || title.isEmpty() || timeLimitStr.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Các trường thông tin không được bỏ trống!",
                    "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        try {
            long examId = Long.parseLong(examIDStr);
            int timeLimit = Integer.parseInt(timeLimitStr);

            if (timeLimit <= 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "Thời gian phải lớn hơn 0!",
                        "Lỗi nhập liệu",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            Exam exam = examService.findById(examId);
            if (exam == null) {
                setCursor(Cursor.getDefaultCursor());
                JOptionPane.showMessageDialog(
                        this,
                        "Mã đề thi không tồn tại!",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            chosenRoom.setExam(exam);
            chosenRoom.setTitle(title);
            chosenRoom.setTimeLimit(timeLimit);
            chosenRoom.setPassword(password);
            chosenRoom.setAvailable(isOpen);

            boolean success = roomService.update(chosenRoom);

            setCursor(Cursor.getDefaultCursor());

            if (success) {
                JOptionPane.showMessageDialog(
                        this,
                        "Cập nhật phòng thi thành công!",
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE
                );
                loadRoomData();
                resetInputFields();
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Cập nhật phòng thi thất bại!",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Mã đề thi và thời gian phải là số!",
                    "Lỗi nhập liệu",
                    JOptionPane.ERROR_MESSAGE
            );
        } catch (RemoteException e) {
            setCursor(Cursor.getDefaultCursor());
            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi kết nối server:\n" + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        }
    }

    private void handleDelete() {
        if (chosenRoom == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn phòng thi cần xóa!",
                    "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn xóa phòng thi này?\nTất cả dữ liệu liên quan sẽ bị xóa!",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                boolean success = roomService.delete(chosenRoom.getRoomId());
                setCursor(Cursor.getDefaultCursor());

                if (success) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Xóa phòng thi thành công!",
                            "Thành công",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    loadRoomData();
                    resetInputFields();
                } else {
                    JOptionPane.showMessageDialog(
                            this,
                            "Xóa phòng thi thất bại!",
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            } catch (RemoteException e) {
                setCursor(Cursor.getDefaultCursor());
                JOptionPane.showMessageDialog(
                        this,
                        "Lỗi kết nối server:\n" + e.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE
                );
                e.printStackTrace();
            }
        }
    }

    private void loadRoomData() {
        if(rowModel == null) return;
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            roomList = roomService.getAll();
            rowModel.setRowCount(0);

            for (Room room : roomList) {
                String examId = room.getExam() != null ?
                        String.valueOf(room.getExam().getExamId()) : "N/A";

                rowModel.addRow(new Object[]{
                        room.getRoomId(),
                        examId,
                        room.getTitle(),
                        room.getTimeLimit(),
                        room.getPassword(),
                        room.isAvailable() ? "Mở" : "Đóng"
                });
            }
            setCursor(Cursor.getDefaultCursor());
        } catch (RemoteException e) {
            setCursor(Cursor.getDefaultCursor());
            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi tải dữ liệu từ server:\n" + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
            e.printStackTrace();
        }
    }

    private void makeTableSearchable() {
        if (textfieldFindViewRoomManagement != null && tableViewRoomManagement != null) {
            rowSorter = new TableRowSorter<>(rowModel);
            tableViewRoomManagement.setRowSorter(rowSorter);

            textfieldFindViewRoomManagement.getDocument().addDocumentListener(new DocumentListener() {
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
                    String text = textfieldFindViewRoomManagement.getText().strip();
                    if (text.isEmpty()) {
                        rowSorter.setRowFilter(null);
                    } else {
                        rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                    }
                }
            });
        }
    }

    private void resetInputFields() {
        textfieldRoomIDViewRoomManagement.setText("");
        textfiledExamIDViewRoomManagement.setText("");
        textfieldRoomTitleViewRoomManaGement.setText("");
        textfieldTimeLimitViewRoomManagement.setText("");
        textfieldRoomPasswordViewRoomManagement.setText("");
        buttongroupStatusViewRoomManagement.clearSelection();
        chosenRoom = null;
        tableViewRoomManagement.clearSelection();
    }
}