package iuh.fit.se.gui;

import entity.Exam;
import entity.User;
import iuh.fit.se.util.ServiceFactory;
import service.ExamService;

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

/**
 * Form quản lý đề thi - Sử dụng RMI Service
 */
public class ExamManagement extends JFrame {
    private final User loginUser;
    private ExamService examService;

    // UI Components
    private JPanel panelViewExamManagement;
    private JTextField textfieldExamIDViewExamManagement;
    private JTextField textfieldSubjectNameViewExamManagement;
    private JTextField textfieldTotalQuestionViewExamManagement;
    private JTextField textfieldTotalScoreViewExamManagement;
    private JButton buttonAddViewExamManagement;
    private JButton buttonUpdateViewExamManagement;
    private JButton buttonDeleteViewExamManagement;
    private JButton buttonBackViewExamManagement;
    private JButton buttonRefreshViewExamManagement;
    private JTextField textfieldFindViewExamManagement;
    private JTable tableViewExamManagement;

    // Table models
    private DefaultTableModel columnModel;
    private DefaultTableModel rowModel;
    private TableRowSorter<TableModel> rowSorter = null;
    private List<Exam> examList;
    private Exam chosenExam = null;

    public ExamManagement(User user) {
        this.loginUser = user;

        // Khởi tạo service từ ServiceFactory
        try {
            this.examService = ServiceFactory.getExamService();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi kết nối ExamService: " + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
            this.dispose();
            return;
        }

        initComponents();
        addActionEvent();

        this.setTitle("Quản Lý Đề Thi");
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(panelViewExamManagement);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);

        loadExamData();
        makeTableSearchable();
    }

    private void initComponents() {
        panelViewExamManagement = new JPanel(new BorderLayout(10, 10));
        panelViewExamManagement.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- FORM PANEL (NORTH) ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Thông tin đề thi"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Exam ID
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Mã Đề Thi:"), gbc);
        gbc.gridx = 1;
        textfieldExamIDViewExamManagement = new JTextField(20);
        textfieldExamIDViewExamManagement.setEnabled(false);
        formPanel.add(textfieldExamIDViewExamManagement, gbc);

        // Subject Name
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Tên Môn Thi:"), gbc);
        gbc.gridx = 1;
        textfieldSubjectNameViewExamManagement = new JTextField(20);
        formPanel.add(textfieldSubjectNameViewExamManagement, gbc);

        // Total Questions
        gbc.gridx = 2; gbc.gridy = 0;
        formPanel.add(new JLabel("Số Câu Hỏi:"), gbc);
        gbc.gridx = 3;
        textfieldTotalQuestionViewExamManagement = new JTextField(10);
        formPanel.add(textfieldTotalQuestionViewExamManagement, gbc);

        // Total Score
        gbc.gridx = 2; gbc.gridy = 1;
        formPanel.add(new JLabel("Điểm Tổng:"), gbc);
        gbc.gridx = 3;
        textfieldTotalScoreViewExamManagement = new JTextField(10);
        formPanel.add(textfieldTotalScoreViewExamManagement, gbc);

        panelViewExamManagement.add(formPanel, BorderLayout.NORTH);

        // --- TABLE PANEL (CENTER) ---
        columnModel = new DefaultTableModel(
                new Object[][]{},
                new String[]{"Mã Đề Thi", "Tên Môn Thi", "Số Câu Hỏi", "Điểm Tổng", "Điểm Mỗi Câu"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        tableViewExamManagement = new JTable(columnModel);
        tableViewExamManagement.getTableHeader().setReorderingAllowed(false);
        rowModel = (DefaultTableModel) tableViewExamManagement.getModel();
        JScrollPane scrollPane = new JScrollPane(tableViewExamManagement);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Danh sách đề thi"));
        panelViewExamManagement.add(scrollPane, BorderLayout.CENTER);


        // --- BUTTONS PANEL (SOUTH) ---
        JPanel southPanel = new JPanel(new BorderLayout(10,10));

        // Action Buttons
        JPanel actionButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonAddViewExamManagement = new JButton("Thêm");
        buttonUpdateViewExamManagement = new JButton("Cập nhật");
        buttonDeleteViewExamManagement = new JButton("Xóa");
        buttonRefreshViewExamManagement = new JButton("Làm mới");
        actionButtonsPanel.add(buttonAddViewExamManagement);
        actionButtonsPanel.add(buttonUpdateViewExamManagement);
        actionButtonsPanel.add(buttonDeleteViewExamManagement);
        actionButtonsPanel.add(buttonRefreshViewExamManagement);
        southPanel.add(actionButtonsPanel, BorderLayout.NORTH);

        // Search and Back buttons
        JPanel searchBackPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        searchBackPanel.add(new JLabel("Tìm kiếm:"));
        textfieldFindViewExamManagement = new JTextField(20);
        searchBackPanel.add(textfieldFindViewExamManagement);
        buttonBackViewExamManagement = new JButton("Quay lại");
        searchBackPanel.add(buttonBackViewExamManagement);
        southPanel.add(searchBackPanel, BorderLayout.SOUTH);

        panelViewExamManagement.add(southPanel, BorderLayout.SOUTH);
    }

    private void addActionEvent() {
        // Table mouse click event
        tableViewExamManagement.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleTableRowSelection();
            }
        });

        // Add button
        buttonAddViewExamManagement.addActionListener(e -> handleAdd());

        // Update button
        buttonUpdateViewExamManagement.addActionListener(e -> handleUpdate());

        // Delete button
        buttonDeleteViewExamManagement.addActionListener(e -> handleDelete());

        // Refresh button
        buttonRefreshViewExamManagement.addActionListener(e -> {
            resetInputFields();
            loadExamData();
            textfieldFindViewExamManagement.setText("");
        });

        // Back button
        buttonBackViewExamManagement.addActionListener(e -> {
            this.dispose();
            // Assuming MenuAdmin is for admins and MenuHost for hosts.
            // You might need to adjust this logic based on your application's roles.
             if (loginUser.isHost()&&!loginUser.getUserId().equals("admin")) { // A simple check, might need to be more robust
                new MenuHost(loginUser);
            } else {
                new MenuAdmin(loginUser);
            }
        });
    }

    private void handleTableRowSelection() {
        int modelRow = tableViewExamManagement.getSelectedRow();
        if (modelRow != -1) {
            int viewRow = tableViewExamManagement.convertRowIndexToModel(modelRow);
            // Ensure index is valid for the underlying list
            if (viewRow >= 0 && viewRow < examList.size()) {
                chosenExam = examList.get(viewRow);
                textfieldExamIDViewExamManagement.setText(String.valueOf(chosenExam.getExamId()));
                textfieldSubjectNameViewExamManagement.setText(chosenExam.getSubject());
                textfieldTotalQuestionViewExamManagement.setText(String.valueOf(chosenExam.getTotalQuestion()));
                textfieldTotalScoreViewExamManagement.setText(String.valueOf(chosenExam.getTotalScore()));
            }
        }
    }

    private void handleAdd() {
        String subject = textfieldSubjectNameViewExamManagement.getText().strip();
        String totalQuestionStr = textfieldTotalQuestionViewExamManagement.getText().strip();
        String totalScoreStr = textfieldTotalScoreViewExamManagement.getText().strip();

        // Validate input
        if (subject.isEmpty() || totalQuestionStr.isEmpty() || totalScoreStr.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Các trường thông tin không được bỏ trống!",
                    "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        try {
            int totalQuestion = Integer.parseInt(totalQuestionStr);
            int totalScore = Integer.parseInt(totalScoreStr);

            if (totalQuestion <= 0 || totalScore <= 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "Số câu hỏi và tổng điểm phải lớn hơn 0!",
                        "Lỗi nhập liệu",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            double scorePerQuestion = totalScore / (double) totalQuestion;
            Exam newExam = new Exam(subject, totalQuestion, totalScore, scorePerQuestion);

            // Call RMI service
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            boolean success = examService.save(newExam);
            setCursor(Cursor.getDefaultCursor());

            if (success) {
                JOptionPane.showMessageDialog(
                        this,
                        "Thêm đề thi thành công!",
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE
                );
                loadExamData();
                resetInputFields();
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Thêm đề thi thất bại!",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Số câu hỏi và tổng điểm phải là số nguyên!",
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
        if (chosenExam == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn đề thi cần cập nhật!",
                    "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        String subject = textfieldSubjectNameViewExamManagement.getText().strip();
        String totalQuestionStr = textfieldTotalQuestionViewExamManagement.getText().strip();
        String totalScoreStr = textfieldTotalScoreViewExamManagement.getText().strip();

        if (subject.isEmpty() || totalQuestionStr.isEmpty() || totalScoreStr.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Các trường thông tin không được bỏ trống!",
                    "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        try {
            int totalQuestion = Integer.parseInt(totalQuestionStr);
            int totalScore = Integer.parseInt(totalScoreStr);

            if (totalQuestion <= 0 || totalScore <= 0) {
                JOptionPane.showMessageDialog(
                        this,
                        "Số câu hỏi và tổng điểm phải lớn hơn 0!",
                        "Lỗi nhập liệu",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            chosenExam.setSubject(subject);
            chosenExam.setTotalQuestion(totalQuestion);
            chosenExam.setTotalScore(totalScore);
            chosenExam.setScorePerQuestion(totalScore / (double) totalQuestion);

            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            boolean success = examService.update(chosenExam);
            setCursor(Cursor.getDefaultCursor());

            if (success) {
                JOptionPane.showMessageDialog(
                        this,
                        "Cập nhật đề thi thành công!",
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE
                );
                loadExamData();
                resetInputFields();
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Cập nhật đề thi thất bại!",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Số câu hỏi và tổng điểm phải là số nguyên!",
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
        if (chosenExam == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn đề thi cần xóa!",
                    "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn xóa đề thi này?\nLưu ý: Tất cả câu hỏi liên quan cũng sẽ bị xóa!",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                boolean success = examService.delete(chosenExam.getExamId());
                setCursor(Cursor.getDefaultCursor());

                if (success) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Xóa đề thi thành công!",
                            "Thành công",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    loadExamData();
                    resetInputFields();
                } else {
                    JOptionPane.showMessageDialog(
                            this,
                            "Xóa đề thi thất bại!",
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

    private void loadExamData() {
        if (rowModel == null) return; // Guard against null model
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            examList = examService.getAll();
            rowModel.setRowCount(0);

            for (Exam exam : examList) {
                rowModel.addRow(new Object[]{
                        exam.getExamId(),
                        exam.getSubject(),
                        exam.getTotalQuestion(),
                        exam.getTotalScore(),
                        String.format("%.2f", exam.getScorePerQuestion())
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
        if (textfieldFindViewExamManagement != null && tableViewExamManagement != null) {
            rowSorter = new TableRowSorter<>(rowModel);
            tableViewExamManagement.setRowSorter(rowSorter);

            textfieldFindViewExamManagement.getDocument().addDocumentListener(new DocumentListener() {
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
                    String text = textfieldFindViewExamManagement.getText().strip();
                    if (text.isEmpty()) {
                        rowSorter.setRowFilter(null);
                    } else {
                        // Case-insensitive search on all columns
                        rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                    }
                }
            });
        }
    }

    private void resetInputFields() {
        textfieldExamIDViewExamManagement.setText("");
        textfieldSubjectNameViewExamManagement.setText("");
        textfieldTotalQuestionViewExamManagement.setText("");
        textfieldTotalScoreViewExamManagement.setText("");
        chosenExam = null;
        tableViewExamManagement.clearSelection();
    }
}