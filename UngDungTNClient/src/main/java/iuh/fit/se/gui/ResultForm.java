package iuh.fit.se.gui;

import entity.Question;
import entity.User;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

public class ResultForm extends JFrame {
    private final User loginUser;
    private final List<Question> questionList;
    private final List<String> chosenAnswerList;
    private final List<String> correctAnswerList;
    private final List<String> resultList;
    private final double score;
    private final int totalCorrect;

    private JPanel panelViewResultForm;
    private JTable tableViewResultForm;
    private JButton buttonQuitViewResultForm;
    private JLabel labelResultViewResultForm;
    private JTextField textfieldFindViewResultForm;

    private DefaultTableModel columnModel;
    private DefaultTableModel rowModel;
    private TableRowSorter<TableModel> rowSorter;

    public ResultForm(User loginUser,
                      List<Question> questionList,
                      List<String> chosenAnswerList,
                      List<String> correctAnswerList,
                      List<String> resultList,
                      int totalCorrect,
                      double score) {
        this.loginUser = loginUser;
        this.questionList = questionList;
        this.chosenAnswerList = chosenAnswerList;
        this.correctAnswerList = correctAnswerList;
        this.resultList = resultList;
        this.totalCorrect = totalCorrect;
        this.score = score;

        initComponents();
        addActionEvent();
        fillData();
        makeTableSearchable();

        this.setTitle("Kết Quả Làm Bài");
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setContentPane(panelViewResultForm);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private void initComponents() {
        if (panelViewResultForm == null) {
            panelViewResultForm = new JPanel(new BorderLayout(10, 10));
            panelViewResultForm.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            // Top panel with result label
            JPanel topPanel = new JPanel(new BorderLayout(10, 10));

            // Search panel
            JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JLabel labelFind = new JLabel("Tìm kiếm:");
            textfieldFindViewResultForm = new JTextField(20);
            textfieldFindViewResultForm.setFont(new Font("Arial", Font.PLAIN, 14));
            searchPanel.add(labelFind);
            searchPanel.add(textfieldFindViewResultForm);

            buttonQuitViewResultForm = new JButton("Thoát");
            searchPanel.add(buttonQuitViewResultForm);

            topPanel.add(searchPanel, BorderLayout.NORTH);

            // Result label
            labelResultViewResultForm = new JLabel("", SwingConstants.CENTER);
            labelResultViewResultForm.setFont(new Font("Arial", Font.BOLD, 18));
            labelResultViewResultForm.setForeground(new Color(220, 20, 60));
            topPanel.add(labelResultViewResultForm, BorderLayout.CENTER);

            panelViewResultForm.add(topPanel, BorderLayout.NORTH);

            // Table
            columnModel = new DefaultTableModel(
                    new Object[][]{},
                    new String[]{"STT", "Câu hỏi", "Đáp án chọn", "Đáp án đúng", "Kết quả"}
            ) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            tableViewResultForm = new JTable(columnModel);
            tableViewResultForm.getTableHeader().setReorderingAllowed(false);
            tableViewResultForm.setRowHeight(30);

            rowModel = (DefaultTableModel) tableViewResultForm.getModel();

            JScrollPane scrollPane = new JScrollPane(tableViewResultForm);
            scrollPane.setPreferredSize(new Dimension(800, 400));
            panelViewResultForm.add(scrollPane, BorderLayout.CENTER);
        }
    }

    private void addActionEvent() {
        if (buttonQuitViewResultForm != null) {
            buttonQuitViewResultForm.addActionListener(event -> {
                this.dispose();
                new MenuAttendee(loginUser);
            });
        }
    }

    private void fillData() {
        rowModel.setRowCount(0);
        int index = 0;

        for (Question question : questionList) {
            String chosenAnswer = chosenAnswerList.get(index);
            String correctAnswer = correctAnswerList.get(index);
            String result = resultList.get(index);

            rowModel.addRow(new Object[]{
                    (index + 1),
                    question.getContent(),
                    chosenAnswer,
                    correctAnswer,
                    result
            });
            index++;
        }

        // Set result text
        String resultText = String.format(
                "KẾT QUẢ: %d / %d câu đúng - ĐIỂM: %.2f",
                totalCorrect,
                questionList.size(),
                score
        );

        if (labelResultViewResultForm != null) {
            labelResultViewResultForm.setText(resultText);
        }

        // Color coding for results
        tableViewResultForm.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (column == 4) { // Result column
                    String result = (String) table.getValueAt(row, column);
                    if ("Đúng".equals(result)) {
                        c.setBackground(new Color(144, 238, 144)); // Light green
                        c.setForeground(Color.BLACK);
                    } else {
                        c.setBackground(new Color(255, 182, 193)); // Light red
                        c.setForeground(Color.BLACK);
                    }
                } else if (!isSelected) {
                    c.setBackground(Color.WHITE);
                    c.setForeground(Color.BLACK);
                }

                return c;
            }
        });
    }

    private void makeTableSearchable() {
        if (textfieldFindViewResultForm != null && tableViewResultForm != null) {
            rowSorter = new TableRowSorter<>(rowModel);

            for (int i = 0; i < columnModel.getColumnCount(); i++) {
                rowSorter.setSortable(i, false);
            }

            tableViewResultForm.setRowSorter(rowSorter);

            textfieldFindViewResultForm.getDocument().addDocumentListener(new DocumentListener() {
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
                    String text = textfieldFindViewResultForm.getText().strip();
                    if (text.isEmpty()) {
                        rowSorter.setRowFilter(null);
                    } else {
                        rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                    }
                }
            });
        }
    }
}