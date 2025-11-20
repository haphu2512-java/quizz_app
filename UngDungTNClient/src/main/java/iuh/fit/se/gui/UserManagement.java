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
    private final UserService userService;

    private JPanel panelViewUserManagement;
    private JTextField textfieldUserIDViewUserManagement;
    private JTextField textfieldFullnameViewUserManagement;
    private JTextField textfieldPasswordViewUserManagement;
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
        buttonGroupViewUserManagement = new ButtonGroup();
        if (radiobuttonHostViewUserManagement != null && radiobuttonAttendeeViewUserManagement != null) {
            buttonGroupViewUserManagement.add(radiobuttonHostViewUserManagement);
            buttonGroupViewUserManagement.add(radiobuttonAttendeeViewUserManagement);
        }

        columnModel = new DefaultTableModel(
                new Object[][]{},
                new String[]{"UserID", "Họ Tên", "Mật Khẩu", "Vai trò"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        if (tableViewUserManagement != null) {
            tableViewUserManagement.setModel(columnModel);
            tableViewUserManagement.getTableHeader().setReorderingAllowed(false);
            rowModel = (DefaultTableModel) tableViewUserManagement.getModel();
        }

        if (checkboxChangePasswordViewUserManagement != null) {
            checkboxChangePasswordViewUserManagement.setSelected(true);
        }
    }

    private void addActionEvent() {
        if (tableViewUserManagement != null) {
            tableViewUserManagement.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    handleTableRowSelection();
                }
            });
        }

        if (checkboxChangePasswordViewUserManagement != null && textfieldPasswordViewUserManagement != null) {
            checkboxChangePasswordViewUserManagement.addActionListener(e ->
                    textfieldPasswordViewUserManagement.setEnabled(
                            checkboxChangePasswordViewUserManagement.isSelected()
                    )
            );
        }

        if (buttonAddViewUserManagement != null) {
            buttonAddViewUserManagement.addActionListener(e -> handleAdd());
        }

        if (buttonUpdateViewUserManagement != null) {
            buttonUpdateViewUserManagement.addActionListener(e -> handleUpdate());
        }

        if (buttonDeleteViewUserManagement != null) {
            buttonDeleteViewUserManagement.addActionListener(e -> handleDelete());
        }

        if (buttonRefreshViewUserManagement != null) {
            buttonRefreshViewUserManagement.addActionListener(e -> {
                resetInputFields();
                loadUserData();
                if (textfieldFindViewUserManagement != null) {
                    textfieldFindViewUserManagement.setText("");
                }
            });
        }

        if (buttonBackViewUserManagement != null) {
            buttonBackViewUserManagement.addActionListener(e -> {
                this.dispose();
                if (loginUser.getUserId().equals(Login.username_admin)) {
                    new MenuAdmin(loginUser);
                } else {
                    new MenuHost(loginUser);
                }
            });
        }
    }

    private void handleTableRowSelection() {
        resetInputFields();

        if (textfieldUserIDViewUserManagement != null) {
            textfieldUserIDViewUserManagement.setEnabled(false);
        }
        if (checkboxChangePasswordViewUserManagement != null) {
            checkboxChangePasswordViewUserManagement.setSelected(false);
        }
        if (textfieldPasswordViewUserManagement != null) {
            textfieldPasswordViewUserManagement.setEnabled(false);
        }

        int index = tableViewUserManagement.getSelectedRow();
        if (index >= 0 && index < userList.size()) {
            chosenUser = userList.get(index);
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

    private void handleAdd() {
        String userID = textfieldUserIDViewUserManagement.getText().strip();
        String fullName = textfieldFullnameViewUserManagement.getText().strip();
        String password = textfieldPasswordViewUserManagement.getText().strip();
        boolean isHost = radiobuttonHostViewUserManagement != null && radiobuttonHostViewUserManagement.isSelected();
        boolean isAttendee = radiobuttonAttendeeViewUserManagement != null && radiobuttonAttendeeViewUserManagement.isSelected();

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
        String password = textfieldPasswordViewUserManagement.getText().strip();
        boolean isHost = radiobuttonHostViewUserManagement != null && radiobuttonHostViewUserManagement.isSelected();

        if (fullName.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Các trường thông tin không được bỏ trống!",
                    "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            chosenUser.setFullName(fullName);
            chosenUser.setHost(isHost);

            // Only update password if changed
            boolean passwordChanged = checkboxChangePasswordViewUserManagement != null &&
                    checkboxChangePasswordViewUserManagement.isSelected() &&
                    !password.equals("••••••••");
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

            for (int i = 0; i < columnModel.getColumnCount(); i++) {
                rowSorter.setSortable(i, false);
            }

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
        if (textfieldUserIDViewUserManagement != null) {
            textfieldUserIDViewUserManagement.setText("");
            textfieldUserIDViewUserManagement.setEnabled(true);
        }
        if (textfieldFullnameViewUserManagement != null) {
            textfieldFullnameViewUserManagement.setText("");
        }
        if (textfieldPasswordViewUserManagement != null) {
            textfieldPasswordViewUserManagement.setText("");
        }
        if (buttonGroupViewUserManagement != null) {
            buttonGroupViewUserManagement.clearSelection();
        }
        if (checkboxChangePasswordViewUserManagement != null) {
            checkboxChangePasswordViewUserManagement.setSelected(true);
            textfieldPasswordViewUserManagement.setEnabled(true);
        }

        chosenUser = null;
        passwordBeforeChanged = null;

        if (tableViewUserManagement != null) {
            tableViewUserManagement.clearSelection();
        }
    }
}