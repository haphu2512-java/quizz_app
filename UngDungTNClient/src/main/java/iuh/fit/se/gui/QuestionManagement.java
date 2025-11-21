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
    private final User loginUser;
    private  QuestionService questionService;
    private  ExamService examService;

    private JPanel panelViewQuestionManagement;
    private JTextField textfieldQuestionIDViewQuestionManagement;
    private JTextField textfieldExamIDViewQuestionManagement;
    private JTextArea textfieldQuestionContentViewQuestionManagement;
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
        panelViewQuestionManagement = new JPanel(new BorderLayout(10, 10));
        panelViewQuestionManagement.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- FORM PANEL (NORTH) ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Thông tin câu hỏi"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Question ID
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Mã Câu Hỏi:"), gbc);
        gbc.gridx = 1;
        textfieldQuestionIDViewQuestionManagement = new JTextField(15);
        textfieldQuestionIDViewQuestionManagement.setEnabled(false);
        formPanel.add(textfieldQuestionIDViewQuestionManagement, gbc);

        // Exam ID
        gbc.gridx = 2; gbc.gridy = 0;
        formPanel.add(new JLabel("Mã Đề Thi:"), gbc);
        gbc.gridx = 3;
        textfieldExamIDViewQuestionManagement = new JTextField(15);
        formPanel.add(textfieldExamIDViewQuestionManagement, gbc);

        // Level
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Mức độ:"), gbc);
        gbc.gridx = 1;
        comboboxLevelViewQuestionManagement = new JComboBox<>(new String[]{"1", "2", "3", "4", "5"});
        formPanel.add(comboboxLevelViewQuestionManagement, gbc);
        
        // Content
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Nội dung:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.BOTH;
        textfieldQuestionContentViewQuestionManagement = new JTextArea(3, 40);
        textfieldQuestionContentViewQuestionManagement.setLineWrap(true);
        textfieldQuestionContentViewQuestionManagement.setWrapStyleWord(true);
        JScrollPane contentScrollPane = new JScrollPane(textfieldQuestionContentViewQuestionManagement);
        formPanel.add(contentScrollPane, gbc);


        panelViewQuestionManagement.add(formPanel, BorderLayout.NORTH);

        // --- TABLE PANEL (CENTER) ---
        columnModel = new DefaultTableModel(
                new Object[][]{},
                new String[]{"Mã Câu hỏi", "Mã Đề thi", "Mức độ", "Nội dung"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableViewQuestionManagement = new JTable(columnModel);
        tableViewQuestionManagement.getTableHeader().setReorderingAllowed(false);
        rowModel = (DefaultTableModel) tableViewQuestionManagement.getModel();
        JScrollPane tableScrollPane = new JScrollPane(tableViewQuestionManagement);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("Danh sách câu hỏi"));
        panelViewQuestionManagement.add(tableScrollPane, BorderLayout.CENTER);


        // --- BUTTONS PANEL (SOUTH) ---
        JPanel southPanel = new JPanel(new BorderLayout(10,10));

        JPanel actionButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        buttonAddViewQuestionManagement = new JButton("Thêm");
        buttonUpdateViewQuestionManagement = new JButton("Cập nhật");
        buttonDeleteViewQuestionManagement = new JButton("Xóa");
        buttonRefreshViewQuestionManagement = new JButton("Làm mới");
        buttonQuestionAnswerViewQuestionMangagement = new JButton("Quản lý Đáp án");
        
        actionButtonsPanel.add(buttonAddViewQuestionManagement);
        actionButtonsPanel.add(buttonUpdateViewQuestionManagement);
        actionButtonsPanel.add(buttonDeleteViewQuestionManagement);
        actionButtonsPanel.add(buttonRefreshViewQuestionManagement);
        actionButtonsPanel.add(buttonQuestionAnswerViewQuestionMangagement);
        southPanel.add(actionButtonsPanel, BorderLayout.NORTH);

        JPanel searchBackPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        searchBackPanel.add(new JLabel("Tìm kiếm:"));
        textfieldFindViewQuestionManagement = new JTextField(20);
        searchBackPanel.add(textfieldFindViewQuestionManagement);
        buttonBackViewQuestionManagement = new JButton("Quay lại");
        searchBackPanel.add(buttonBackViewQuestionManagement);
        southPanel.add(searchBackPanel, BorderLayout.SOUTH);

        panelViewQuestionManagement.add(southPanel, BorderLayout.SOUTH);
    }

    private void addActionEvent() {
        tableViewQuestionManagement.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleTableRowSelection();
            }
        });

        buttonAddViewQuestionManagement.addActionListener(e -> handleAdd());
        buttonUpdateViewQuestionManagement.addActionListener(e -> handleUpdate());
        buttonDeleteViewQuestionManagement.addActionListener(e -> handleDelete());

        buttonQuestionAnswerViewQuestionMangagement.addActionListener(e -> {
            if(chosenQuestion == null) {
                 JOptionPane.showMessageDialog(this, "Vui lòng chọn một câu hỏi để quản lý đáp án.", "Chưa chọn câu hỏi", JOptionPane.WARNING_MESSAGE);
                 return;
            }
            new QuestionAnswerManagement(loginUser, chosenQuestion);
            this.dispose();
        });

        buttonRefreshViewQuestionManagement.addActionListener(e -> {
            resetInputFields();
            loadQuestionData();
            textfieldFindViewQuestionManagement.setText("");
        });

        buttonBackViewQuestionManagement.addActionListener(e -> {
            this.dispose();
             if (loginUser.isHost()) { 
                new MenuHost(loginUser);
            } else {
                new MenuAdmin(loginUser);
            }
        });
    }

    private void handleTableRowSelection() {
        textfieldQuestionIDViewQuestionManagement.setEnabled(false);

        int modelRow = tableViewQuestionManagement.getSelectedRow();
        if (modelRow != -1) {
            int viewRow = tableViewQuestionManagement.convertRowIndexToModel(modelRow);
             if (viewRow >= 0 && viewRow < questionList.size()) {
                chosenQuestion = questionList.get(viewRow);
                textfieldQuestionIDViewQuestionManagement.setText(String.valueOf(chosenQuestion.getQuestionId()));

                if (chosenQuestion.getExam() != null) {
                    textfieldExamIDViewQuestionManagement.setText(String.valueOf(chosenQuestion.getExam().getExamId()));
                }

                textfieldQuestionContentViewQuestionManagement.setText(chosenQuestion.getContent());
                comboboxLevelViewQuestionManagement.setSelectedIndex(chosenQuestion.getLevel() - 1);
            }
        }
    }

    private void handleAdd() {
        String examIDStr = textfieldExamIDViewQuestionManagement.getText().strip();
        String content = textfieldQuestionContentViewQuestionManagement.getText().strip();
        int level = comboboxLevelViewQuestionManagement.getSelectedIndex() + 1;

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
        int level = comboboxLevelViewQuestionManagement.getSelectedIndex() + 1;

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
        if (rowModel == null) return;
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
        textfieldQuestionIDViewQuestionManagement.setText("");
        textfieldExamIDViewQuestionManagement.setText("");
        textfieldQuestionContentViewQuestionManagement.setText("");
        comboboxLevelViewQuestionManagement.setSelectedIndex(0);
        chosenQuestion = null;
        tableViewQuestionManagement.clearSelection();
    }
}