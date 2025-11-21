package iuh.fit.se.gui;

import entity.User;
import iuh.fit.se.util.ServiceFactory;
import service.UserService;

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

public class UserManagement extends JFrame {
    private final User loginUser;
    private  UserService userService;

    private JPanel panelViewUserManagement;
    private JTextField textfieldUserIDViewUserManagement;
    private JTextField textfieldFullnameViewUserManagement;
    private JPasswordField textfieldPasswordViewUserManagement;
    private JRadioButton radiobuttonHostViewUserManagement;
    private JRadioButton radiobuttonAttendeeViewUserManagement;
    private JButton buttonAddViewUserManagement;
    private JButton buttonUpdateViewUserManagement;
    private JButton buttonDeleteViewUserManagement;
    private JButton buttonBackViewUserManagement;
    private JButton buttonRefreshViewUserManagement;
    private JTextField textfieldFindViewUserManagement;
    private JTable tableViewUserManagement;
    private JCheckBox checkboxChangePasswordViewUserManagement;
    private ButtonGroup buttonGroupViewUserManagement;

    private DefaultTableModel columnModel;
    private DefaultTableModel rowModel;
    private TableRowSorter<TableModel> rowSorter = null;
    private List<User> userList;
    private User chosenUser = null;
    private String passwordBeforeChanged;

    public UserManagement(User user) {
        this.loginUser = user;

        try {
            this.userService = ServiceFactory.getUserService();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi kết nối UserService: " + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
            this.dispose();
            return;
        }

        initComponents();
        addActionEvent();

        this.setTitle("Quản Lý Người Dùng");
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(panelViewUserManagement);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);

        loadUserData();
        makeTableSearchable();
    }

    private void initComponents() {
        panelViewUserManagement = new JPanel(new BorderLayout(10, 10));
        panelViewUserManagement.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- FORM PANEL (NORTH) ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Thông tin người dùng"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // User ID
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("UserID:"), gbc);
        gbc.gridx = 1;
        textfieldUserIDViewUserManagement = new JTextField(20);
        formPanel.add(textfieldUserIDViewUserManagement, gbc);

        // Full Name
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Họ Tên:"), gbc);
        gbc.gridx = 1;
        textfieldFullnameViewUserManagement = new JTextField(20);
        formPanel.add(textfieldFullnameViewUserManagement, gbc);

        // Password
        gbc.gridx = 2; gbc.gridy = 0;
        formPanel.add(new JLabel("Mật khẩu:"), gbc);
        gbc.gridx = 3;
        textfieldPasswordViewUserManagement = new JPasswordField(20);
        formPanel.add(textfieldPasswordViewUserManagement, gbc);
        
        // Change Password Checkbox
        gbc.gridx = 4; gbc.gridy = 0;
        checkboxChangePasswordViewUserManagement = new JCheckBox("Đổi mật khẩu");
        checkboxChangePasswordViewUserManagement.setSelected(true);
        formPanel.add(checkboxChangePasswordViewUserManagement, gbc);

        // Role
        gbc.gridx = 2; gbc.gridy = 1;
        formPanel.add(new JLabel("Vai trò:"), gbc);
        
        JPanel rolePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        radiobuttonHostViewUserManagement = new JRadioButton("Host");
        radiobuttonAttendeeViewUserManagement = new JRadioButton("Attendee");
        buttonGroupViewUserManagement = new ButtonGroup();
        buttonGroupViewUserManagement.add(radiobuttonHostViewUserManagement);
        buttonGroupViewUserManagement.add(radiobuttonAttendeeViewUserManagement);
        rolePanel.add(radiobuttonHostViewUserManagement);
        rolePanel.add(radiobuttonAttendeeViewUserManagement);
        
        gbc.gridx = 3;
        formPanel.add(rolePanel, gbc);

        panelViewUserManagement.add(formPanel, BorderLayout.NORTH);

        // --- TABLE PANEL (CENTER) ---
        columnModel = new DefaultTableModel(
                new Object[][]{},
                new String[]{"UserID", "Họ Tên", "Mật Khẩu", "Vai trò"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableViewUserManagement = new JTable(columnModel);
        tableViewUserManagement.getTableHeader().setReorderingAllowed(false);
        rowModel = (DefaultTableModel) tableViewUserManagement.getModel();
        JScrollPane scrollPane = new JScrollPane(tableViewUserManagement);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Danh sách người dùng"));
        panelViewUserManagement.add(scrollPane, BorderLayout.CENTER);

        // --- BUTTONS PANEL (SOUTH) ---
        JPanel southPanel = new JPanel(new BorderLayout(10,10));

        JPanel actionButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonAddViewUserManagement = new JButton("Thêm");
        buttonUpdateViewUserManagement = new JButton("Cập nhật");
        buttonDeleteViewUserManagement = new JButton("Xóa");
        buttonRefreshViewUserManagement = new JButton("Làm mới");
        actionButtonsPanel.add(buttonAddViewUserManagement);
        actionButtonsPanel.add(buttonUpdateViewUserManagement);
        actionButtonsPanel.add(buttonDeleteViewUserManagement);
        actionButtonsPanel.add(buttonRefreshViewUserManagement);
        southPanel.add(actionButtonsPanel, BorderLayout.NORTH);

        JPanel searchBackPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        searchBackPanel.add(new JLabel("Tìm kiếm:"));
        textfieldFindViewUserManagement = new JTextField(20);
        searchBackPanel.add(textfieldFindViewUserManagement);
        buttonBackViewUserManagement = new JButton("Quay lại");
        searchBackPanel.add(buttonBackViewUserManagement);
        southPanel.add(searchBackPanel, BorderLayout.SOUTH);
        
        panelViewUserManagement.add(southPanel, BorderLayout.SOUTH);
    }

    private void addActionEvent() {
        tableViewUserManagement.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleTableRowSelection();
            }
        });

        checkboxChangePasswordViewUserManagement.addActionListener(e ->
                textfieldPasswordViewUserManagement.setEnabled(
                        checkboxChangePasswordViewUserManagement.isSelected()
                )
        );

        buttonAddViewUserManagement.addActionListener(e -> handleAdd());
        buttonUpdateViewUserManagement.addActionListener(e -> handleUpdate());
        buttonDeleteViewUserManagement.addActionListener(e -> handleDelete());

        buttonRefreshViewUserManagement.addActionListener(e -> {
            resetInputFields();
            loadUserData();
            textfieldFindViewUserManagement.setText("");
        });
        
        buttonBackViewUserManagement.addActionListener(e -> {
            this.dispose();
            if (loginUser.getUserId().equals(Login.username_admin)) {
                new MenuAdmin(loginUser);
            } else {
                new MenuHost(loginUser);
            }
        });
    }

    private void handleTableRowSelection() {
        textfieldUserIDViewUserManagement.setEnabled(false);
        checkboxChangePasswordViewUserManagement.setSelected(false);
        textfieldPasswordViewUserManagement.setEnabled(false);

        int modelRow = tableViewUserManagement.getSelectedRow();
        if(modelRow != -1) {
            int viewRow = tableViewUserManagement.convertRowIndexToModel(modelRow);
            if (viewRow >= 0 && viewRow < userList.size()) {
                chosenUser = userList.get(viewRow);
                passwordBeforeChanged = chosenUser.getPasswordHash();

                textfieldUserIDViewUserManagement.setText(chosenUser.getUserId());
                textfieldFullnameViewUserManagement.setText(chosenUser.getFullName());
                textfieldPasswordViewUserManagement.setText("••••••••"); // Masked password

                if (chosenUser.isHost()) {
                    radiobuttonHostViewUserManagement.setSelected(true);
                } else {
                    radiobuttonAttendeeViewUserManagement.setSelected(true);
                }
            }
        }
    }

    private void handleAdd() {
        String userID = textfieldUserIDViewUserManagement.getText().strip();
        String fullName = textfieldFullnameViewUserManagement.getText().strip();
        String password = new String(textfieldPasswordViewUserManagement.getPassword()).strip();
        boolean isHost = radiobuttonHostViewUserManagement.isSelected();
        boolean isAttendee = radiobuttonAttendeeViewUserManagement.isSelected();

        // Validate
        if (userID.isEmpty() || fullName.isEmpty() || password.isEmpty() || (!isHost && !isAttendee)) {
            JOptionPane.showMessageDialog(
                    this,
                    "Các trường thông tin không được bỏ trống!",
                    "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (userID.equalsIgnoreCase("admin")) {
            JOptionPane.showMessageDialog(
                    this,
                    "Không thể tạo tài khoản với UserID này!",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            // Check if user exists
            User existingUser = userService.findById(userID);
            if (existingUser != null) {
                setCursor(Cursor.getDefaultCursor());
                JOptionPane.showMessageDialog(
                        this,
                        "UserID đã tồn tại!",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            User newUser = new User(userID, fullName, password, isHost);
            boolean success = userService.save(newUser);

            setCursor(Cursor.getDefaultCursor());

            if (success) {
                JOptionPane.showMessageDialog(
                        this,
                        "Thêm người dùng thành công!",
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE
                );
                loadUserData();
                resetInputFields();
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Thêm người dùng thất bại!",
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

    private void handleUpdate() {
        if (chosenUser == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn người dùng cần cập nhật!",
                    "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        String fullName = textfieldFullnameViewUserManagement.getText().strip();
        String password = new String(textfieldPasswordViewUserManagement.getPassword()).strip();
        boolean isHost = radiobuttonHostViewUserManagement.isSelected();

        if (fullName.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Họ tên không được bỏ trống!",
                    "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        
        boolean passwordChanged = checkboxChangePasswordViewUserManagement.isSelected();
        if(passwordChanged && password.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Mật khẩu không được bỏ trống khi cập nhật!",
                    "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            chosenUser.setFullName(fullName);
            chosenUser.setHost(isHost);

            if (passwordChanged) {
                chosenUser.setPasswordHash(password);
            } else {
                chosenUser.setPasswordHash(passwordBeforeChanged);
            }

            boolean success = userService.update(chosenUser);

            setCursor(Cursor.getDefaultCursor());

            if (success) {
                JOptionPane.showMessageDialog(
                        this,
                        "Cập nhật người dùng thành công!",
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE
                );
                loadUserData();
                resetInputFields();
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Cập nhật người dùng thất bại!",
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

    private void handleDelete() {
        if (chosenUser == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn người dùng cần xóa!",
                    "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (chosenUser.getUserId().equals(Login.username_admin)) {
            JOptionPane.showMessageDialog(
                    this,
                    "Không thể xóa tài khoản admin!",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        
        if (chosenUser.getUserId().equals(loginUser.getUserId())) {
            JOptionPane.showMessageDialog(
                    this,
                    "Không thể tự xóa tài khoản của chính mình!",
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }


        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn xóa người dùng này?",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                boolean success = userService.delete(chosenUser.getUserId());
                setCursor(Cursor.getDefaultCursor());

                if (success) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Xóa người dùng thành công!",
                            "Thành công",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    loadUserData();
                    resetInputFields();
                } else {
                    JOptionPane.showMessageDialog(
                            this,
                            "Xóa người dùng thất bại!",
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

    private void loadUserData() {
        if (rowModel == null) return;
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            userList = userService.getAll();
            rowModel.setRowCount(0);

            for (User user : userList) {
                rowModel.addRow(new Object[]{
                        user.getUserId(),
                        user.getFullName(),
                        "••••••••", // Masked password
                        user.isHost() ? "Host" : "Attendee"
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
        if (textfieldFindViewUserManagement != null && tableViewUserManagement != null) {
            rowSorter = new TableRowSorter<>(rowModel);
            tableViewUserManagement.setRowSorter(rowSorter);

            textfieldFindViewUserManagement.getDocument().addDocumentListener(new DocumentListener() {
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
                    String text = textfieldFindViewUserManagement.getText().strip();
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
        textfieldUserIDViewUserManagement.setText("");
        textfieldUserIDViewUserManagement.setEnabled(true);
        textfieldFullnameViewUserManagement.setText("");
        textfieldPasswordViewUserManagement.setText("");
        buttonGroupViewUserManagement.clearSelection();
        checkboxChangePasswordViewUserManagement.setSelected(true);
        textfieldPasswordViewUserManagement.setEnabled(true);

        chosenUser = null;
        passwordBeforeChanged = null;

        tableViewUserManagement.clearSelection();
    }
}