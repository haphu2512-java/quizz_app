package iuh.fit.se.gui;

import dao.UserDAO;
import entity.QuestionAnswer;
import entity.User;
import service.impl.QuestionAnswerServiceImpl;
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

public class QuestionAnswerManagement extends JFrame {
    private final User loginUser;
    private JTextField textfieldFindViewQuestionAnswerManagement;
    private JTable tableViewQuestionAnswerManagement;
    private JCheckBox checkboxCorrectAnswerViewQuestionAnswerManagement;
    private JButton buttonAddViewQuestionAnswerManagement;
    private JButton buttonUpdateViewQuestionAnswerManagement;
    private JButton buttonDeleteViewQuestionAnswerManagement;
    private JButton buttonBackViewQuestionAnswerManagement;
    private JTextField textfieldQuestionIDViewQuestionAnswerManagement;
    private JTextField textfieldAnswerContentViewQuestionAnswerManagement;
    private JLabel labelQuestionIDViewQuestionAnswerManagement;
    private JLabel labelAnswerContentViewQuestionAnswerManagement;
    private JLabel labelFindViewQuestionAnswerManagement;
    private JPanel panelViewQuestionAnswerManagement;
    private JButton buttonRefreshViewQuestionAnswerManagement;
    private JTextField textfieldQuestionAnswerIDViewQuestionAnswerManagement;
    private JLabel labelQuestionAnswerIDViewQuestionAnswerManagement;
    private DefaultTableModel columnModel;
    private DefaultTableModel rowModel;
    private TableRowSorter<TableModel> rowSorter = null;
    private List<QuestionAnswer> list;
    private QuestionAnswer chosenQuestionAnswer = null;

    private QuestionAnswerServiceImpl questionAnswerService;
    private QuestionServiceImpl questionService;

    public QuestionAnswerManagement(User user) {
        this.loginUser = user;
        try {
            this.questionAnswerService = new QuestionAnswerServiceImpl();
            this.questionService = new QuestionServiceImpl();
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(this, "Error initializing services: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        initComponents();
        addActionEvent();
        this.setTitle("Quản Lý Đáp Án Câu Hỏi");
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(panelViewQuestionAnswerManagement);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        fillDataToTable();
        makeTableSearchable();
    }

    public static void main(String[] args) {
        // User admin = new User("admin", "admin", "admin", true); // Old User constructor
        User admin = new User("admin", "Admin User", UserDAO.encryptPassword("admin"), true);
        EventQueue.invokeLater(() -> new QuestionAnswerManagement(admin));
    }

    private void createUIComponents() {
    }

    private void initComponents() {
        textfieldQuestionAnswerIDViewQuestionAnswerManagement.setEnabled(false);
        tableViewQuestionAnswerManagement.setDefaultEditor(Object.class, null);
        tableViewQuestionAnswerManagement.getTableHeader().setReorderingAllowed(false);
        columnModel = new DefaultTableModel(
                new Object[][]{},
                new String[]{"Mã Đáp án", "Mã Câu hỏi", "Nội dung", "Đáp án đúng"}
        );
        tableViewQuestionAnswerManagement.setModel(columnModel);
        rowModel = (DefaultTableModel) tableViewQuestionAnswerManagement.getModel();
    }

    private void addActionEvent() {
        tableViewQuestionAnswerManagement.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                tableViewQuestionAnswerManagementMouseClicked();
            }

            private void tableViewQuestionAnswerManagementMouseClicked() {
                resetInputField();
                textfieldQuestionAnswerIDViewQuestionAnswerManagement.setEnabled(false);
                var index = tableViewQuestionAnswerManagement.getSelectedRow();
                if (index >= 0 && index < list.size()) {
                    chosenQuestionAnswer = list.get(index);
                    textfieldQuestionAnswerIDViewQuestionAnswerManagement.setText(String.valueOf(chosenQuestionAnswer.getQuestionAnswerId()));
                    // Ensure Question is loaded before trying to get its ID
                    if (chosenQuestionAnswer.getQuestion() != null) {
                        textfieldQuestionIDViewQuestionAnswerManagement.setText(String.valueOf(chosenQuestionAnswer.getQuestion().getQuestionId()));
                    } else {
                        textfieldQuestionIDViewQuestionAnswerManagement.setText(""); // Or handle appropriately
                    }
                    textfieldAnswerContentViewQuestionAnswerManagement.setText(chosenQuestionAnswer.getContent());
                    checkboxCorrectAnswerViewQuestionAnswerManagement.setSelected(chosenQuestionAnswer.isCorrect());
                }
            }
        });

        buttonAddViewQuestionAnswerManagement.addActionListener(event -> {
            if (textfieldQuestionIDViewQuestionAnswerManagement.getText().isEmpty()
                    || textfieldAnswerContentViewQuestionAnswerManagement.getText().isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Các trường thông tin không được bỏ trống!",
                        "Cảnh Báo",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }
            try {
                var questionId = Long.parseLong(textfieldQuestionIDViewQuestionAnswerManagement.getText().strip());
                var checkValidQuestion = questionService.findById(questionId);
                if (checkValidQuestion == null) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Mã câu hỏi không tồn tại. Hãy kiểm tra và thử lại sau!",
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }
                var content = textfieldAnswerContentViewQuestionAnswerManagement.getText().strip();
                var isCorrect = checkboxCorrectAnswerViewQuestionAnswerManagement.isSelected();
                var questionAnswer = new QuestionAnswer(checkValidQuestion, content, isCorrect);
                var isSuccess = questionAnswerService.save(questionAnswer);
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
                JOptionPane.showMessageDialog(this, "Mã câu hỏi không hợp lệ.", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
            } catch (RemoteException e) {
                JOptionPane.showMessageDialog(this, "Lỗi khi thêm đáp án: " + e.getMessage(), "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
            }
        });

        buttonUpdateViewQuestionAnswerManagement.addActionListener(event -> {
            if (textfieldQuestionAnswerIDViewQuestionAnswerManagement.getText().isEmpty()
                    || textfieldQuestionIDViewQuestionAnswerManagement.getText().isEmpty()
                    || textfieldAnswerContentViewQuestionAnswerManagement.getText().isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Các trường thông tin không được bỏ trống!",
                        "Cảnh Báo",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }
            try {
                var questionAnswerId = Long.parseLong(textfieldQuestionAnswerIDViewQuestionAnswerManagement.getText().strip());
                var questionId = Long.parseLong(textfieldQuestionIDViewQuestionAnswerManagement.getText().strip());

                var existingQuestionAnswer = questionAnswerService.findById(questionAnswerId);
                if (existingQuestionAnswer == null) {
                    JOptionPane.showMessageDialog(this, "Không tìm thấy đáp án cần cập nhật.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                var checkValidQuestion = questionService.findById(questionId);
                if (checkValidQuestion == null) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Mã câu hỏi không tồn tại. Hãy kiểm tra và thử lại sau!",
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }

                existingQuestionAnswer.setQuestion(checkValidQuestion);
                existingQuestionAnswer.setContent(textfieldAnswerContentViewQuestionAnswerManagement.getText().strip());
                existingQuestionAnswer.setCorrect(checkboxCorrectAnswerViewQuestionAnswerManagement.isSelected());

                var isSuccess = questionAnswerService.update(existingQuestionAnswer);
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
                JOptionPane.showMessageDialog(this, "ID đáp án hoặc mã câu hỏi không hợp lệ.", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
            } catch (RemoteException e) {
                JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật đáp án: " + e.getMessage(), "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
            }
        });

        buttonDeleteViewQuestionAnswerManagement.addActionListener(event -> {
            var questionAnswerIDText = textfieldQuestionAnswerIDViewQuestionAnswerManagement.getText().strip();
            if (!questionAnswerIDText.isEmpty()) {
                var confirmation = JOptionPane.showConfirmDialog(
                    this,
                    "Bạn có chắc muốn xóa đáp án này không?",
                    "Xác nhận xóa",
                    JOptionPane.YES_NO_OPTION);

                if (confirmation == JOptionPane.YES_OPTION) {
                    try {
                        var questionAnswerId = Long.parseLong(questionAnswerIDText);
                        var isSuccess = questionAnswerService.delete(questionAnswerId);
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
                        JOptionPane.showMessageDialog(this, "ID đáp án không hợp lệ.", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                    } catch (RemoteException e) {
                        JOptionPane.showMessageDialog(this, "Lỗi khi xoá đáp án: " + e.getMessage(), "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Hãy chọn đáp án cần xoá để tiến hành xoá!",
                        "Cảnh Báo",
                        JOptionPane.WARNING_MESSAGE
                );
            }
        });

        buttonRefreshViewQuestionAnswerManagement.addActionListener(event -> {
            resetInputField();
            textfieldFindViewQuestionAnswerManagement.setText("");
            fillDataToTable();
        });

        buttonBackViewQuestionAnswerManagement.addActionListener(event -> {
            this.dispose();
            // Assuming this leads back to QuestionManagement or a similar screen
            // If it's always for a specific question, the back logic might need to be more precise
            // For now, it just closes this window.
            // If this window is always opened from QuestionManagement with a specific question,
            // then it might be useful to pass that question/user back to QuestionManagement.
        });
    }

    private void resetInputField() {
        textfieldQuestionAnswerIDViewQuestionAnswerManagement.setText("");
        textfieldQuestionIDViewQuestionAnswerManagement.setText("");
        textfieldAnswerContentViewQuestionAnswerManagement.setText("");
        checkboxCorrectAnswerViewQuestionAnswerManagement.setSelected(false);
        chosenQuestionAnswer = null; // Clear chosen answer
        textfieldQuestionAnswerIDViewQuestionAnswerManagement.setEnabled(false); // Disable ID field after reset
    }

    private void fillDataToTable() {
        try {
            list = questionAnswerService.getAll();
            rowModel.setRowCount(0);
            for (var questionAnswer : list) {
                rowModel.addRow(new Object[]{
                        questionAnswer.getQuestionAnswerId(),
                        // Ensure Question is loaded; if it's lazy, this might trigger a fetch
                        (questionAnswer.getQuestion() != null ? questionAnswer.getQuestion().getQuestionId() : "N/A"),
                        questionAnswer.getContent(),
                        questionAnswer.isCorrect()
                });
            }
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi tải dữ liệu đáp án: " + e.getMessage(), "Lỗi hệ thống", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void makeTableSearchable() {
        rowSorter = new TableRowSorter<>(rowModel);
        var i = 0;
        while (i < columnModel.getColumnCount()) {
            rowSorter.setSortable(i, false);
            ++i;
        }
        tableViewQuestionAnswerManagement.setRowSorter(rowSorter);
        textfieldFindViewQuestionAnswerManagement
                .getDocument()
                .addDocumentListener(new DocumentListener() {
                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        var text = textfieldFindViewQuestionAnswerManagement.getText().strip();
                        if (text.length() != 0) {
                            rowSorter.setRowFilter(RowFilter.regexFilter(text));
                        } else {
                            rowSorter.setRowFilter(null);
                        }
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        var text = textfieldFindViewQuestionAnswerManagement.getText().strip();
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
}