package iuh.fit.se.gui;

import entity.Exam;
import entity.Question;
import entity.User;
import iuh.fit.se.util.ServiceFactory;
import service.ExamService;
import service.QuestionService;

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
    private  User loginUser;
    private  QuestionService questionService;
    private  ExamService examService;

    private JPanel panelViewQuestionManagement;
    private JTextField textfieldQuestionIDViewQuestionManagement;
    private JTextField textfieldExamIDViewQuestionManagement;
    private JTextField textfieldQuestionContentViewQuestionManagement;
    private JComboBox<String> comboboxLevelViewQuestionManagement;
    private JButton buttonAddViewQuestionManagement;
    private JButton buttonUpdateViewQuestionManagement;
    private JButton buttonDeleteViewQuestionManagement;
    private JButton buttonQuestionAnswerViewQuestionMangagement;
    private JButton buttonBackViewQuestionManagement;
    private JButton buttonRefreshViewQuestionManagement;
    private JTextField textfieldFindViewQuestionManagement;
    private JTable tableViewQuestionManagement;

    private DefaultTableModel columnModel;
    private DefaultTableModel rowModel;
    private TableRowSorter<TableModel> rowSorter = null;
    private List<Question> questionList;
    private Question chosenQuestion = null;

    public QuestionManagement(User user) {
        this.loginUser = user;

        try {
            this.questionService = ServiceFactory.getQuestionService();
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

        this.setTitle("Quản Lý Câu Hỏi");
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setContentPane(panelViewQuestionManagement);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);

        loadQuestionData();
        makeTableSearchable();
    }

    private void initComponents() {
        columnModel = new DefaultTableModel(
                new Object[][]{},
                new String[]{"Mã Câu hỏi", "Mã Đề thi", "Mức độ", "Nội dung"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        if (tableViewQuestionManagement != null) {
            tableViewQuestionManagement.setModel(columnModel);
            tableViewQuestionManagement.getTableHeader().setReorderingAllowed(false);
            rowModel = (DefaultTableModel) tableViewQuestionManagement.getModel();
        }

        if (textfieldQuestionIDViewQuestionManagement != null) {
            textfieldQuestionIDViewQuestionManagement.setEnabled(false);
        }

        if (comboboxLevelViewQuestionManagement != null) {
            comboboxLevelViewQuestionManagement.setModel(
                    new DefaultComboBoxModel<>(new String[]{"1", "2", "3", "4", "5"})
            );
        }
    }

    private void addActionEvent() {
        if (tableViewQuestionManagement != null) {
            tableViewQuestionManagement.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    handleTableRowSelection();
                }
            });
        }

        if (buttonAddViewQuestionManagement != null) {
            buttonAddViewQuestionManagement.addActionListener(e -> handleAdd());
        }

        if (buttonUpdateViewQuestionManagement != null) {
            buttonUpdateViewQuestionManagement.addActionListener(e -> handleUpdate());
        }

        if (buttonDeleteViewQuestionManagement != null) {
            buttonDeleteViewQuestionManagement.addActionListener(e -> handleDelete());
        }

        if (buttonQuestionAnswerViewQuestionMangagement != null) {
            buttonQuestionAnswerViewQuestionMangagement.addActionListener(e -> {
                new QuestionAnswerManagement(loginUser);
            });
        }

        if (buttonRefreshViewQuestionManagement != null) {
            buttonRefreshViewQuestionManagement.addActionListener(e -> {
                resetInputFields();
                loadQuestionData();
                if (textfieldFindViewQuestionManagement != null) {
                    textfieldFindViewQuestionManagement.setText("");
                }
            });
        }

        if (buttonBackViewQuestionManagement != null) {
            buttonBackViewQuestionManagement.addActionListener(e -> {
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

        if (textfieldQuestionIDViewQuestionManagement != null) {
            textfieldQuestionIDViewQuestionManagement.setEnabled(false);
        }

        int index = tableViewQuestionManagement.getSelectedRow();
        if (index >= 0 && index < questionList.size()) {
            chosenQuestion = questionList.get(index);
            textfieldQuestionIDViewQuestionManagement.setText(String.valueOf(chosenQuestion.getQuestionId()));

            if (chosenQuestion.getExam() != null) {
                textfieldExamIDViewQuestionManagement.setText(String.valueOf(chosenQuestion.getExam().getExamId()));
            }

            textfieldQuestionContentViewQuestionManagement.setText(chosenQuestion.getContent());

            if (comboboxLevelViewQuestionManagement != null) {
                comboboxLevelViewQuestionManagement.setSelectedIndex(chosenQuestion.getLevel() - 1);
            }
        }
    }

    private void handleAdd() {
        String examIDStr = textfieldExamIDViewQuestionManagement.getText().strip();
        String content = textfieldQuestionContentViewQuestionManagement.getText().strip();
        int level = comboboxLevelViewQuestionManagement != null ?
                comboboxLevelViewQuestionManagement.getSelectedIndex() + 1 : 1;

        if (examIDStr.isEmpty() || content.isEmpty()) {
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

            Question newQuestion = new Question(exam, level, content);
            boolean success = questionService.save(newQuestion);

            setCursor(Cursor.getDefaultCursor());

            if (success) {
                JOptionPane.showMessageDialog(
                        this,
                        "Thêm câu hỏi thành công!",
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE
                );
                loadQuestionData();
                resetInputFields();
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Thêm câu hỏi thất bại!",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Mã đề thi phải là số!",
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
        if (chosenQuestion == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn câu hỏi cần cập nhật!",
                    "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        String examIDStr = textfieldExamIDViewQuestionManagement.getText().strip();
        String content = textfieldQuestionContentViewQuestionManagement.getText().strip();
        int level = comboboxLevelViewQuestionManagement != null ?
                comboboxLevelViewQuestionManagement.getSelectedIndex() + 1 : 1;

        if (examIDStr.isEmpty() || content.isEmpty()) {
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

            chosenQuestion.setExam(exam);
            chosenQuestion.setLevel(level);
            chosenQuestion.setContent(content);

            boolean success = questionService.update(chosenQuestion);

            setCursor(Cursor.getDefaultCursor());

            if (success) {
                JOptionPane.showMessageDialog(
                        this,
                        "Cập nhật câu hỏi thành công!",
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE
                );
                loadQuestionData();
                resetInputFields();
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Cập nhật câu hỏi thất bại!",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Mã đề thi phải là số!",
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
        if (chosenQuestion == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Vui lòng chọn câu hỏi cần xóa!",
                    "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn xóa câu hỏi này?\nTất cả đáp án liên quan cũng sẽ bị xóa!",
                "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                boolean success = questionService.delete(chosenQuestion.getQuestionId());
                setCursor(Cursor.getDefaultCursor());

                if (success) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Xóa câu hỏi thành công!",
                            "Thành công",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                    loadQuestionData();
                    resetInputFields();
                } else {
                    JOptionPane.showMessageDialog(
                            this,
                            "Xóa câu hỏi thất bại!",
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

    private void loadQuestionData() {
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            questionList = questionService.getAll();
            rowModel.setRowCount(0);

            for (Question question : questionList) {
                String examId = question.getExam() != null ?
                        String.valueOf(question.getExam().getExamId()) : "N/A";

                rowModel.addRow(new Object[]{
                        question.getQuestionId(),
                        examId,
                        question.getLevel(),
                        question.getContent()
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
        if (textfieldFindViewQuestionManagement != null && tableViewQuestionManagement != null) {
            rowSorter = new TableRowSorter<>(rowModel);

            for (int i = 0; i < columnModel.getColumnCount(); i++) {
                rowSorter.setSortable(i, false);
            }

            tableViewQuestionManagement.setRowSorter(rowSorter);

            textfieldFindViewQuestionManagement.getDocument().addDocumentListener(new DocumentListener() {
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
                    String text = textfieldFindViewQuestionManagement.getText().strip();
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
        if (textfieldQuestionIDViewQuestionManagement != null) {
            textfieldQuestionIDViewQuestionManagement.setText("");
        }
        if (textfieldExamIDViewQuestionManagement != null) {
            textfieldExamIDViewQuestionManagement.setText("");
        }
        if (textfieldQuestionContentViewQuestionManagement != null) {
            textfieldQuestionContentViewQuestionManagement.setText("");
        }
        if (comboboxLevelViewQuestionManagement != null) {
            comboboxLevelViewQuestionManagement.setSelectedIndex(0);
        }

        chosenQuestion = null;

        if (tableViewQuestionManagement != null) {
            tableViewQuestionManagement.clearSelection();
        }
    }
}