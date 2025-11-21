package iuh.fit.se.gui;

import entity.Question;
import entity.QuestionAnswer;
import entity.User;
import iuh.fit.se.util.ServiceFactory;
import service.QuestionAnswerService;
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
import java.util.stream.Collectors;

public class QuestionAnswerManagement extends JFrame {
    private final User loginUser;
    private final Question parentQuestion;
    private  QuestionAnswerService questionAnswerService;

    private JPanel panelViewQuestionAnswerManagement;
    private JTextField textfieldQuestionAnswerIDViewQuestionAnswerManagement;
    private JTextField textfieldQuestionIDViewQuestionAnswerManagement;
    private JTextField textfieldAnswerContentViewQuestionAnswerManagement;
    private JCheckBox checkboxCorrectAnswerViewQuestionAnswerManagement;
    private JButton buttonAddViewQuestionAnswerManagement;
    private JButton buttonUpdateViewQuestionAnswerManagement;
    private JButton buttonDeleteViewQuestionAnswerManagement;
    private JButton buttonBackViewQuestionAnswerManagement;
    private JButton buttonRefreshViewQuestionAnswerManagement;
    private JTextField textfieldFindViewQuestionAnswerManagement;
    private JTable tableViewQuestionAnswerManagement;

    private DefaultTableModel columnModel;
    private DefaultTableModel rowModel;
    private TableRowSorter<TableModel> rowSorter = null;
    private List<QuestionAnswer> answerList;
    private QuestionAnswer chosenAnswer = null;

    public QuestionAnswerManagement(User user, Question question) {
        this.loginUser = user;
        this.parentQuestion = question;

        try {
            this.questionAnswerService = ServiceFactory.getQuestionAnswerService();
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

        this.setTitle("Quản Lý Đáp Án cho câu hỏi: " + parentQuestion.getQuestionId());
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setContentPane(panelViewQuestionAnswerManagement);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);

        loadAnswerData();
        makeTableSearchable();
    }

    private void initComponents() {
        panelViewQuestionAnswerManagement = new JPanel(new BorderLayout(10, 10));
        panelViewQuestionAnswerManagement.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- FORM PANEL (NORTH) ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Thông tin đáp án"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Mã Đáp Án:"), gbc);
        gbc.gridx = 1;
        textfieldQuestionAnswerIDViewQuestionAnswerManagement = new JTextField(20);
        textfieldQuestionAnswerIDViewQuestionAnswerManagement.setEnabled(false);
        formPanel.add(textfieldQuestionAnswerIDViewQuestionAnswerManagement, gbc);

        gbc.gridx = 2; gbc.gridy = 0;
        formPanel.add(new JLabel("Mã Câu Hỏi:"), gbc);
        gbc.gridx = 3;
        textfieldQuestionIDViewQuestionAnswerManagement = new JTextField(20);
        textfieldQuestionIDViewQuestionAnswerManagement.setText(String.valueOf(parentQuestion.getQuestionId()));
        textfieldQuestionIDViewQuestionAnswerManagement.setEnabled(false);
        formPanel.add(textfieldQuestionIDViewQuestionAnswerManagement, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Nội dung:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        textfieldAnswerContentViewQuestionAnswerManagement = new JTextField();
        formPanel.add(textfieldAnswerContentViewQuestionAnswerManagement, gbc);
        gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Là đáp án đúng:"), gbc);
        gbc.gridx = 1;
        checkboxCorrectAnswerViewQuestionAnswerManagement = new JCheckBox();
        formPanel.add(checkboxCorrectAnswerViewQuestionAnswerManagement, gbc);

        panelViewQuestionAnswerManagement.add(formPanel, BorderLayout.NORTH);

        // --- TABLE ---
        columnModel = new DefaultTableModel(
                new Object[][]{},
                new String[]{"Mã Đáp án", "Mã Câu hỏi", "Nội dung", "Đáp án đúng"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableViewQuestionAnswerManagement = new JTable(columnModel);
        tableViewQuestionAnswerManagement.getTableHeader().setReorderingAllowed(false);
        rowModel = (DefaultTableModel) tableViewQuestionAnswerManagement.getModel();
        JScrollPane tableScrollPane = new JScrollPane(tableViewQuestionAnswerManagement);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("Danh sách đáp án"));
        panelViewQuestionAnswerManagement.add(tableScrollPane, BorderLayout.CENTER);


        // --- BUTTONS ---
        JPanel southPanel = new JPanel(new BorderLayout(10,10));
        JPanel actionButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        buttonAddViewQuestionAnswerManagement = new JButton("Thêm");
        buttonUpdateViewQuestionAnswerManagement = new JButton("Cập nhật");
        buttonDeleteViewQuestionAnswerManagement = new JButton("Xóa");
        buttonRefreshViewQuestionAnswerManagement = new JButton("Làm mới");
        actionButtonsPanel.add(buttonAddViewQuestionAnswerManagement);
        actionButtonsPanel.add(buttonUpdateViewQuestionAnswerManagement);
        actionButtonsPanel.add(buttonDeleteViewQuestionAnswerManagement);
        actionButtonsPanel.add(buttonRefreshViewQuestionAnswerManagement);
        southPanel.add(actionButtonsPanel, BorderLayout.NORTH);

        JPanel searchBackPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        searchBackPanel.add(new JLabel("Tìm kiếm:"));
        textfieldFindViewQuestionAnswerManagement = new JTextField(20);
        searchBackPanel.add(textfieldFindViewQuestionAnswerManagement);
        buttonBackViewQuestionAnswerManagement = new JButton("Quay lại");
        searchBackPanel.add(buttonBackViewQuestionAnswerManagement);
        southPanel.add(searchBackPanel, BorderLayout.SOUTH);

        panelViewQuestionAnswerManagement.add(southPanel, BorderLayout.SOUTH);
    }

    private void addActionEvent() {
        tableViewQuestionAnswerManagement.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleTableRowSelection();
            }
        });

        buttonAddViewQuestionAnswerManagement.addActionListener(e -> handleAdd());
        buttonUpdateViewQuestionAnswerManagement.addActionListener(e -> handleUpdate());
        buttonDeleteViewQuestionAnswerManagement.addActionListener(e -> handleDelete());

        buttonRefreshViewQuestionAnswerManagement.addActionListener(e -> {
            resetInputFields();
            loadAnswerData();
            textfieldFindViewQuestionAnswerManagement.setText("");
        });

        buttonBackViewQuestionAnswerManagement.addActionListener(e -> {
            this.dispose();
            new QuestionManagement(loginUser);
        });
    }

    private void handleTableRowSelection() {
        resetInputFields();
        textfieldQuestionAnswerIDViewQuestionAnswerManagement.setEnabled(false);

        int modelRow = tableViewQuestionAnswerManagement.getSelectedRow();
        if(modelRow != -1) {
            int viewRow = tableViewQuestionAnswerManagement.convertRowIndexToModel(modelRow);
            if(viewRow >= 0 && viewRow < answerList.size()) {
                chosenAnswer = answerList.get(viewRow);
                textfieldQuestionAnswerIDViewQuestionAnswerManagement.setText(
                        String.valueOf(chosenAnswer.getQuestionAnswerId())
                );
                textfieldQuestionIDViewQuestionAnswerManagement.setText(
                        String.valueOf(chosenAnswer.getQuestion().getQuestionId())
                );
                textfieldAnswerContentViewQuestionAnswerManagement.setText(chosenAnswer.getContent());
                checkboxCorrectAnswerViewQuestionAnswerManagement.setSelected(chosenAnswer.isCorrect());
            }
        }
    }

    private void handleAdd() {
        String content = textfieldAnswerContentViewQuestionAnswerManagement.getText().strip();
        boolean isCorrect = checkboxCorrectAnswerViewQuestionAnswerManagement.isSelected();

        if (content.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nội dung đáp án không được bỏ trống!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            QuestionAnswer newAnswer = new QuestionAnswer(parentQuestion, content, isCorrect);
            boolean success = questionAnswerService.save(newAnswer);

            setCursor(Cursor.getDefaultCursor());

            if (success) {
                JOptionPane.showMessageDialog(this, "Thêm đáp án thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadAnswerData();
                resetInputFields();
            } else {
                JOptionPane.showMessageDialog(this, "Thêm đáp án thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (RemoteException e) {
            setCursor(Cursor.getDefaultCursor());
            JOptionPane.showMessageDialog(this, "Lỗi kết nối server:\n" + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void handleUpdate() {
        if (chosenAnswer == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn đáp án cần cập nhật!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String content = textfieldAnswerContentViewQuestionAnswerManagement.getText().strip();
        boolean isCorrect = checkboxCorrectAnswerViewQuestionAnswerManagement.isSelected();

        if (content.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nội dung đáp án không được bỏ trống!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            
            chosenAnswer.setContent(content);
            chosenAnswer.setCorrect(isCorrect);
            // The question associated with the answer should not be changed here.

            boolean success = questionAnswerService.update(chosenAnswer);

            setCursor(Cursor.getDefaultCursor());

            if (success) {
                JOptionPane.showMessageDialog(this, "Cập nhật đáp án thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadAnswerData();
                resetInputFields();
            } else {
                JOptionPane.showMessageDialog(this, "Cập nhật đáp án thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (RemoteException e) {
            setCursor(Cursor.getDefaultCursor());
            JOptionPane.showMessageDialog(this, "Lỗi kết nối server:\n" + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void handleDelete() {
        if (chosenAnswer == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn đáp án cần xóa!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa đáp án này?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                boolean success = questionAnswerService.delete(chosenAnswer.getQuestionAnswerId());
                setCursor(Cursor.getDefaultCursor());

                if (success) {
                    JOptionPane.showMessageDialog(this, "Xóa đáp án thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    loadAnswerData();
                    resetInputFields();
                } else {
                    JOptionPane.showMessageDialog(this, "Xóa đáp án thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (RemoteException e) {
                setCursor(Cursor.getDefaultCursor());
                JOptionPane.showMessageDialog(this, "Lỗi kết nối server:\n" + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private void loadAnswerData() {
        if (rowModel == null) return;
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            // Instead of getting all, we should get by question ID.
            // Assuming the service can provide this, otherwise filter client-side.
            List<QuestionAnswer> allAnswers = questionAnswerService.getAll();
            answerList = allAnswers.stream()
                                    .filter(answer -> answer.getQuestion().getQuestionId() == parentQuestion.getQuestionId())
                                    .collect(Collectors.toList());

            rowModel.setRowCount(0);

            for (QuestionAnswer answer : answerList) {
                rowModel.addRow(new Object[]{
                        answer.getQuestionAnswerId(),
                        answer.getQuestion().getQuestionId(),
                        answer.getContent(),
                        answer.isCorrect() ? "Đúng" : "Sai"
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
        if (textfieldFindViewQuestionAnswerManagement != null && tableViewQuestionAnswerManagement != null) {
            rowSorter = new TableRowSorter<>(rowModel);
            tableViewQuestionAnswerManagement.setRowSorter(rowSorter);

            textfieldFindViewQuestionAnswerManagement.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) { filter(); }
                @Override
                public void removeUpdate(DocumentEvent e) { filter(); }
                @Override
                public void changedUpdate(DocumentEvent e) { filter(); }

                private void filter() {
                    String text = textfieldFindViewQuestionAnswerManagement.getText().strip();
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
        textfieldQuestionAnswerIDViewQuestionAnswerManagement.setText("");
        // textfieldQuestionIDViewQuestionAnswerManagement should not be reset
        textfieldAnswerContentViewQuestionAnswerManagement.setText("");
        checkboxCorrectAnswerViewQuestionAnswerManagement.setSelected(false);
        chosenAnswer = null;
        tableViewQuestionAnswerManagement.clearSelection();
    }
}