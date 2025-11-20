package iuh.fit.se.gui;

import dao.UserDAO;
import entity.Question;
import entity.User;
import service.impl.ExamServiceImpl;
import service.impl.QuestionServiceImpl;

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

public class QuestionManagement extends JFrame {
    private final User loginUser;
    private JPanel panelViewQuestionManagement;
    private JTextField textfieldExamIDViewQuestionManagement;
    private JTextField textfieldQuestionContentViewQuestionManagement;
    private JButton buttonAddViewQuestionManagement;
    private JButton buttonUpdateViewQuestionManagement;
    private JButton buttonDeleteViewQuestionManagement;
    private JTable tableViewQuestionManagement;
    private JButton buttonQuestionAnswerViewQuestionMangagement;
    private JButton buttonBackViewQuestionManagement;
    private JTextField textfieldFindViewQuestionManagement;
    private JLabel labelFindViewQuestionManagement;
    private JLabel labelExamIDViewQuestionManagement;
    private JLabel labelQuestionContentViewQuestionManagement;
    private JLabel labelLevelViewQuestionManagenment;
    private JComboBox<String> comboboxLevelViewQuestionManagement;
    private JTextField textfieldQuestionIDViewQuestionManagement;
    private JLabel labelQuestionIDViewQuestionManagement;
    private JButton buttonRefreshViewQuestionManagement;
    private DefaultTableModel columnModel;
    private DefaultTableModel rowModel;
    private TableRowSorter<TableModel> rowSorter = null;
    private List<Question> list;
    private Question chosenQuestion = null;

    private QuestionServiceImpl questionService;
    private ExamServiceImpl examService;

    public QuestionManagement(User user) {
        this.loginUser = user;
        try {
            this.questionService = new QuestionServiceImpl();
            this.examService = new ExamServiceImpl();
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(this, "Error initializing services: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        initComponents();
        addActionEvent();
        this.setTitle("Quản Lý Câu Hỏi");
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(panelViewQuestionManagement);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        fillDataToTable();
        makeTableSearchable();
    }

    public static void main(String[] args) {
        // User admin = new User("admin", "admin", "admin", true); // Old User constructor
        User admin = new User("admin", "Admin User", UserDAO.encryptPassword("admin"), true);
        EventQueue.invokeLater(() -> new QuestionManagement(admin));
    }

    private void initComponents() {
        textfieldQuestionIDViewQuestionManagement.setEnabled(false);
        tableViewQuestionManagement.setDefaultEditor(Object.class, null);
        tableViewQuestionManagement.getTableHeader().setReorderingAllowed(false);
        columnModel = new DefaultTableModel(
                new Object[][]{},
                new String[]{"Mã Câu hỏi", "Mã Đề thi", "Mức độ khó", "Nội dung"}
        );
        tableViewQuestionManagement.setModel(columnModel);
        rowModel = (DefaultTableModel) tableViewQuestionManagement.getModel();
        comboboxLevelViewQuestionManagement.setModel(new DefaultComboBoxModel<>(new String[]{"1", "2", "3", "4", "5"}));
    }

    private void addActionEvent() {
        tableViewQuestionManagement.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                tableViewQuestionManagementMouseClicked();
            }

            private void tableViewQuestionManagementMouseClicked() {
                resetInputField();
                textfieldQuestionIDViewQuestionManagement.setEnabled(false);
                var index = tableViewQuestionManagement.getSelectedRow();
                if (index >= 0 && index < list.size()) {
                    chosenQuestion = list.get(index);
                    textfieldQuestionIDViewQuestionManagement.setText(String.valueOf(chosenQuestion.getQuestionId()));
                    if (chosenQuestion.getExam() != null) {
                        textfieldExamIDViewQuestionManagement.setText(String.valueOf(chosenQuestion.getExam().getExamId()));
                    } else {
                        textfieldExamIDViewQuestionManagement.setText(""); // Or handle appropriately
                    }
                    textfieldQuestionContentViewQuestionManagement.setText(chosenQuestion.getContent());
                    comboboxLevelViewQuestionManagement.setSelectedIndex(chosenQuestion.getLevel() - 1);
                }
            }
        });

        buttonAddViewQuestionManagement.addActionListener(e -> {
            if (textfieldExamIDViewQuestionManagement.getText().isEmpty()
                    || textfieldQuestionContentViewQuestionManagement.getText().isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Các trường thông tin không được bỏ trống!",
                        "Cảnh Báo",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }
            try {
                var examId = Long.parseLong(textfieldExamIDViewQuestionManagement.getText().strip());
                var checkValidExam = examService.findById(examId);
                if (checkValidExam == null) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Mã đề thi không tồn tại. Hãy kiểm tra và thử lại sau!",
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }
                var level = comboboxLevelViewQuestionManagement.getSelectedIndex() + 1;
                var content = textfieldQuestionContentViewQuestionManagement.getText().strip();
                var question = new Question(checkValidExam, level, content);
                var isSuccess = questionService.save(question);
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
                JOptionPane.showMessageDialog(this, "Mã đề thi không hợp lệ.", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
            } catch (RemoteException re) {
                JOptionPane.showMessageDialog(this, "Lỗi khi thêm câu hỏi: " + re.getMessage(), "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
            }
        });

        buttonUpdateViewQuestionManagement.addActionListener(e -> {
            if (textfieldQuestionIDViewQuestionManagement.getText().isEmpty()
                    || textfieldExamIDViewQuestionManagement.getText().isEmpty()
                    || textfieldQuestionContentViewQuestionManagement.getText().isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Các trường thông tin không được bỏ trống!",
                        "Cảnh Báo",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }
            try {
                var questionId = Long.parseLong(textfieldQuestionIDViewQuestionManagement.getText().strip());
                var examId = Long.parseLong(textfieldExamIDViewQuestionManagement.getText().strip());

                var existingQuestion = questionService.findById(questionId);
                if (existingQuestion == null) {
                    JOptionPane.showMessageDialog(this, "Không tìm thấy câu hỏi cần cập nhật.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                var checkValidExam = examService.findById(examId);
                if (checkValidExam == null) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Mã đề thi không tồn tại. Hãy kiểm tra và thử lại sau!",
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }
                existingQuestion.setExam(checkValidExam);
                existingQuestion.setLevel(comboboxLevelViewQuestionManagement.getSelectedIndex() + 1);
                existingQuestion.setContent(textfieldQuestionContentViewQuestionManagement.getText().strip());

                var isSuccess = questionService.update(existingQuestion);
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
                JOptionPane.showMessageDialog(this, "ID câu hỏi hoặc mã đề thi không hợp lệ.", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
            } catch (RemoteException re) {
                JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật câu hỏi: " + re.getMessage(), "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
            }
        });

        buttonDeleteViewQuestionManagement.addActionListener(e -> {
            var questionIDText = textfieldQuestionIDViewQuestionManagement.getText().strip();
            if (!questionIDText.isEmpty()) {
                var confirmation = JOptionPane.showConfirmDialog(
                    this,
                    "Bạn có chắc muốn xóa câu hỏi này không?",
                    "Xác nhận xóa",
                    JOptionPane.YES_NO_OPTION);

                if (confirmation == JOptionPane.YES_OPTION) {
                    try {
                        var questionId = Long.parseLong(questionIDText);
                        var isSuccess = questionService.delete(questionId);
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
                        JOptionPane.showMessageDialog(this, "ID câu hỏi không hợp lệ.", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                    } catch (RemoteException re) {
                        JOptionPane.showMessageDialog(this, "Lỗi khi xoá câu hỏi: " + re.getMessage(), "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Hãy chọn câu hỏi cần xoá để tiến hành xoá!",
                        "Cảnh Báo",
                        JOptionPane.WARNING_MESSAGE
                );
            }
        });

        buttonQuestionAnswerViewQuestionMangagement.addActionListener(e -> {
            // It might be better to pass the chosenQuestion to QuestionAnswerManagement
            // so that it can filter answers for that specific question.
            // For now, it opens a generic QuestionAnswerManagement window.
            new QuestionAnswerManagement(loginUser);
        });

        buttonRefreshViewQuestionManagement.addActionListener(e -> {
            resetInputField();
            textfieldFindViewQuestionManagement.setText("");
            fillDataToTable();
        });

        buttonBackViewQuestionManagement.addActionListener(e -> {
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
        textfieldQuestionIDViewQuestionManagement.setText("");
        textfieldExamIDViewQuestionManagement.setText("");
        textfieldQuestionContentViewQuestionManagement.setText("");
        comboboxLevelViewQuestionManagement.setSelectedIndex(0);
        chosenQuestion = null; // Clear chosen question
        textfieldQuestionIDViewQuestionManagement.setEnabled(false); // Disable ID field after reset
    }

    private void fillDataToTable() {
        try {
            list = questionService.getAll();
            rowModel.setRowCount(0);
            for (var question : list) {
                rowModel.addRow(new Object[]{
                        question.getQuestionId(),
                        (question.getExam() != null ? question.getExam().getExamId() : "N/A"), // Use getter
                        question.getLevel(),
                        question.getContent()
                });
            }
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải dữ liệu câu hỏi: " + e.getMessage(), "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void makeTableSearchable() {
        rowSorter = new TableRowSorter<>(rowModel);
        var i = 0;
        while (i < columnModel.getColumnCount()) {
            rowSorter.setSortable(i, false);
            ++i;
        }
        tableViewQuestionManagement.setRowSorter(rowSorter);
        textfieldFindViewQuestionManagement
                .getDocument()
                .addDocumentListener(new DocumentListener() {
                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        var text = textfieldFindViewQuestionManagement.getText().strip();
                        if (text.length() != 0) {
                            rowSorter.setRowFilter(RowFilter.regexFilter(text));
                        } else {
                            rowSorter.setRowFilter(null);
                        }
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        var text = textfieldFindViewQuestionManagement.getText().strip();
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