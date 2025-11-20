package iuh.fit.se.gui;


import dao.UserDAO;
import entity.Exam;
import entity.User;
import service.impl.ExamServiceImpl;

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

public class ExamManagement extends JFrame {
    private final User loginUser;
    private JTextField textfieldSubjectNameViewExamManagement;
    private JTextField textfieldTotalQuestionViewExamManagement;
    private JTextField textfieldTotalScoreViewExamManagement;
    private JButton buttonAddViewExamManagement;
    private JButton buttonUpdateViewExamManagement;
    private JButton buttonDeleteViewExamManagement;
    private JButton buttonBackViewExamManagement;
    private JTextField textfieldFindViewExamManagement;
    private JTable tableViewExamManagement;
    private JLabel labelSubjectIDViewExamManagement;
    private JLabel labelTotalQuestionViewExamManagement;
    private JLabel labelTotalScoreViewExamManagement;
    private JLabel labelFindViewExamManagement;
    private JPanel panelViewExamManagement;
    private JButton buttonRefreshViewExamManagement;
    private JTextField textfieldExamIDViewExamManagement;
    private JLabel labelExamIDViewExamManagement;
    private DefaultTableModel columnModel;
    private DefaultTableModel rowModel;
    private TableRowSorter<TableModel> rowSorter = null;
    private List<Exam> list;
    private Exam chosenExam = null;

    private ExamServiceImpl examService; // Service layer for Exam operations

    public ExamManagement(User user) {
        this.loginUser = user;
        try {
            this.examService = new ExamServiceImpl();
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(this, "Error initializing exam service: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
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
        fillDataToTable();
        makeTableSearchable();
    }

    public static void main(String[] args) {
        // User admin = new User("admin", "admin", "admin", true); // Old User constructor, now requires entity.User
        // For testing purposes, create a dummy User entity
        User admin = new User("admin", "Admin User", UserDAO.encryptPassword("admin"), true); // Encrypt password
        EventQueue.invokeLater(() -> new ExamManagement(admin));
    }

    private void initComponents() {
        textfieldExamIDViewExamManagement.setEnabled(false);
        tableViewExamManagement.setDefaultEditor(Object.class, null);
        tableViewExamManagement.getTableHeader().setReorderingAllowed(false);
        columnModel = new DefaultTableModel(
                new Object[][]{},
                new String[]{"Mã Đề Thi", "Tên Môn Thi", "Số Câu Hỏi", "Điểm Tổng", "Điểm Mỗi Câu"}
        );
        tableViewExamManagement.setModel(columnModel);
        rowModel = (DefaultTableModel) tableViewExamManagement.getModel();
    }

    private void addActionEvent() {
        tableViewExamManagement.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                tableViewExamManagementMouseClicked();
            }

            private void tableViewExamManagementMouseClicked() {
                resetInputField();
                textfieldExamIDViewExamManagement.setEnabled(false);
                var index = tableViewExamManagement.getSelectedRow();
                if (index >= 0 && index < list.size()) {
                    chosenExam = list.get(index);
                    textfieldExamIDViewExamManagement.setText(String.valueOf(chosenExam.getExamId()));
                    textfieldSubjectNameViewExamManagement.setText(chosenExam.getSubject());
                    textfieldTotalQuestionViewExamManagement.setText(String.valueOf(chosenExam.getTotalQuestion()));
                    textfieldTotalScoreViewExamManagement.setText(String.valueOf(chosenExam.getTotalScore()));
                }
            }
        });

        buttonAddViewExamManagement.addActionListener(event -> {
            if (textfieldSubjectNameViewExamManagement.getText().isEmpty()
                    || textfieldTotalQuestionViewExamManagement.getText().isEmpty()
                    || textfieldTotalScoreViewExamManagement.getText().isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Các trường thông tin không được bỏ trống!",
                        "Cảnh Báo",
                        JOptionPane.WARNING_MESSAGE
                );
            } else {
                try {
                    var subject = textfieldSubjectNameViewExamManagement.getText().strip();
                    var totalQuestion = Integer.parseInt(textfieldTotalQuestionViewExamManagement.getText().strip());
                    var totalScore = Integer.parseInt(textfieldTotalScoreViewExamManagement.getText().strip());
                    
                    if (totalQuestion <= 0 || totalScore <= 0) {
                        JOptionPane.showMessageDialog(this, "Số câu hỏi và tổng điểm phải lớn hơn 0.", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    var scorePerQuestion = totalScore / (double) totalQuestion;
                    var exam = new Exam(subject, totalQuestion, totalScore, scorePerQuestion);
                    var isSuccess = examService.save(exam); // Use service to save
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
                    JOptionPane.showMessageDialog(this, "Số câu hỏi hoặc tổng điểm không hợp lệ.", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                } catch (RemoteException e) {
                    JOptionPane.showMessageDialog(this, "Lỗi khi thêm đề thi: " + e.getMessage(), "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        buttonUpdateViewExamManagement.addActionListener(event -> {
            if (textfieldExamIDViewExamManagement.getText().isEmpty()
                    || textfieldSubjectNameViewExamManagement.getText().isEmpty()
                    || textfieldTotalQuestionViewExamManagement.getText().isEmpty()
                    || textfieldTotalScoreViewExamManagement.getText().isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Các trường thông tin không được bỏ trống!",
                        "Cảnh Báo",
                        JOptionPane.WARNING_MESSAGE
                );
            } else {
                try {
                    var examId = Long.parseLong(textfieldExamIDViewExamManagement.getText().strip());
                    var existingExam = examService.findById(examId); // Find existing exam
                    if (existingExam != null) {
                        existingExam.setSubject(textfieldSubjectNameViewExamManagement.getText().strip());
                        int totalQuestion = Integer.parseInt(textfieldTotalQuestionViewExamManagement.getText().strip());
                        int totalScore = Integer.parseInt(textfieldTotalScoreViewExamManagement.getText().strip());
                        
                        if (totalQuestion <= 0 || totalScore <= 0) {
                            JOptionPane.showMessageDialog(this, "Số câu hỏi và tổng điểm phải lớn hơn 0.", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        existingExam.setTotalQuestion(totalQuestion);
                        existingExam.setTotalScore(totalScore);
                        // Recalculate score per question
                        existingExam.setScorePerQuestion(existingExam.getTotalScore() / (double) existingExam.getTotalQuestion());

                        var isSuccess = examService.update(existingExam); // Use service to update
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
                    } else {
                        JOptionPane.showMessageDialog(this, "Không tìm thấy đề thi cần cập nhật.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                    resetInputField();
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(this, "ID đề thi, số câu hỏi hoặc tổng điểm không hợp lệ.", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                } catch (RemoteException e) {
                    JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật đề thi: " + e.getMessage(), "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        buttonDeleteViewExamManagement.addActionListener(event -> {
            var examIDText = textfieldExamIDViewExamManagement.getText().strip();
            if (!examIDText.isEmpty()) {
                var confirmation = JOptionPane.showConfirmDialog(
                    this,
                    "Bạn có chắc muốn xóa đề thi này không?",
                    "Xác nhận xóa",
                    JOptionPane.YES_NO_OPTION);

                if (confirmation == JOptionPane.YES_OPTION) {
                    try {
                        var examId = Long.parseLong(examIDText);
                        var isSuccess = examService.delete(examId); // Use service to delete
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
                        JOptionPane.showMessageDialog(this, "ID đề thi không hợp lệ.", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                    } catch (RemoteException e) {
                        JOptionPane.showMessageDialog(this, "Lỗi khi xoá đề thi: " + e.getMessage(), "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Hãy chọn đề thi cần xoá để tiến hành xoá!",
                        "Cảnh Báo",
                        JOptionPane.WARNING_MESSAGE
                );
            }
        });

        buttonRefreshViewExamManagement.addActionListener(event -> {
            resetInputField();
            textfieldFindViewExamManagement.setText("");
            fillDataToTable(); // Refresh table data as well
        });

        buttonBackViewExamManagement.addActionListener(event -> {
            // Use loginUser.getUserId() for consistency
            // Assuming username_admin is "admin" from Login.java static final
            if (loginUser.getUserId().equals(Login.username_admin)) { // Compare with constant for admin check
                this.dispose();
                new MenuAdmin(loginUser);
            } else {
                this.dispose();
                new MenuHost(loginUser);
            }
        });
    }

    private void fillDataToTable() {
        try {
            list = examService.getAll(); // Use service to get all exams
            rowModel.setRowCount(0);
            for (var exam : list) {
                rowModel.addRow(new Object[]{
                        exam.getExamId(), // Use getter
                        exam.getSubject(),
                        exam.getTotalQuestion(), // Use getter
                        exam.getTotalScore(), // Use getter
                        exam.getScorePerQuestion() // Use getter
                });
            }
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải dữ liệu đề thi: " + e.getMessage(), "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void makeTableSearchable() {
        rowSorter = new TableRowSorter<>(rowModel);
        var i = 0;
        while (i < columnModel.getColumnCount()) {
            rowSorter.setSortable(i, false);
            ++i;
        }
        tableViewExamManagement.setRowSorter(rowSorter);
        textfieldFindViewExamManagement
                .getDocument()
                .addDocumentListener(new DocumentListener() {
                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        var text = textfieldFindViewExamManagement.getText().strip();
                        if (text.length() != 0) {
                            rowSorter.setRowFilter(RowFilter.regexFilter(text));
                        } else {
                            rowSorter.setRowFilter(null);
                        }
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        var text = textfieldFindViewExamManagement.getText().strip();
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

    private void resetInputField() {
        textfieldExamIDViewExamManagement.setText("");
        textfieldSubjectNameViewExamManagement.setText("");
        textfieldTotalQuestionViewExamManagement.setText("");
        textfieldTotalScoreViewExamManagement.setText("");
        chosenExam = null; // Clear chosen exam
        textfieldExamIDViewExamManagement.setEnabled(false); // Disable ID field after reset
    }

    private void createUIComponents() {
    }
}