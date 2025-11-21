package iuh.fit.se.gui;

import entity.Question;
import entity.QuestionAnswer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Form hiển thị 1 câu hỏi và các đáp án
 * KHÔNG SỬ DỤNG FILE .form - Tạo 100% bằng code
 */
public class QuestionAndAnswerForm extends JPanel {
    private JLabel labelQuestionContent;
    private JPanel panelAnswers;
    private JButton buttonClearChoice;
    private ButtonGroup buttonGroup;

    private final Question question;
    private final List<QuestionAnswer> questionAnswerList;

    public QuestionAndAnswerForm(Question question, List<QuestionAnswer> questionAnswerList) {
        this.question = question;
        this.questionAnswerList = questionAnswerList;

        // Setup panel
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(Color.WHITE);

        // Create components
        createUIComponents();

        // Fill data
        showFormContent();

        // Add events
        addEvents();
    }

    /**
     * Tạo các UI components
     */
    private void createUIComponents() {
        // ============= QUESTION PANEL (TOP) =============
        JPanel questionPanel = createQuestionPanel();
        add(questionPanel, BorderLayout.NORTH);

        // ============= ANSWERS PANEL (CENTER) =============
        JScrollPane scrollPane = createAnswersPanel();
        add(scrollPane, BorderLayout.CENTER);

        // ============= CLEAR BUTTON (BOTTOM) =============
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Tạo panel hiển thị câu hỏi
     */
    private JPanel createQuestionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(240, 248, 255)); // Alice Blue
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 149, 237), 2),
                new EmptyBorder(15, 15, 15, 15)
        ));

        // Question label
        labelQuestionContent = new JLabel();
        labelQuestionContent.setFont(new Font("Arial", Font.BOLD, 16));
        labelQuestionContent.setForeground(new Color(25, 25, 112)); // Midnight Blue

        // Wrap text if too long
        labelQuestionContent.putClientProperty("html.disable", null);

        panel.add(labelQuestionContent, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Tạo panel chứa các đáp án
     */
    private JScrollPane createAnswersPanel() {
        panelAnswers = new JPanel();
        panelAnswers.setLayout(new BoxLayout(panelAnswers, BoxLayout.Y_AXIS));
        panelAnswers.setBackground(Color.WHITE);
        panelAnswers.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Button group cho radio buttons
        buttonGroup = new ButtonGroup();

        JScrollPane scrollPane = new JScrollPane(panelAnswers);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        return scrollPane;
    }

    /**
     * Tạo panel bottom với nút bỏ chọn
     */
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));

        buttonClearChoice = new JButton("🔄 Bỏ chọn đáp án");
        buttonClearChoice.setFont(new Font("Arial", Font.PLAIN, 14));
        buttonClearChoice.setFocusPainted(false);
        buttonClearChoice.setCursor(new Cursor(Cursor.HAND_CURSOR));
        buttonClearChoice.setBackground(new Color(255, 140, 0)); // Dark Orange
        buttonClearChoice.setForeground(Color.WHITE);
        buttonClearChoice.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 100, 0), 2),
                new EmptyBorder(8, 20, 8, 20)
        ));

        // Hover effect
        buttonClearChoice.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                buttonClearChoice.setBackground(new Color(255, 165, 0));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                buttonClearChoice.setBackground(new Color(255, 140, 0));
            }
        });

        panel.add(buttonClearChoice);

        return panel;
    }

    /**
     * Hiển thị nội dung câu hỏi và đáp án
     */
    private void showFormContent() {
        // Set question content
        String questionText = "<html><body style='width: 600px; padding: 10px;'>" +
                "<b>Câu hỏi:</b> " + question.getContent() +
                "</body></html>";
        labelQuestionContent.setText(questionText);

        // Add answer options
        char optionLabel = 'A';
        for (QuestionAnswer questionAnswer : questionAnswerList) {
            JRadioButton radioButton = createAnswerRadioButton(questionAnswer, optionLabel);
            buttonGroup.add(radioButton);
            panelAnswers.add(radioButton);
            panelAnswers.add(Box.createRigidArea(new Dimension(0, 10))); // Spacing
            optionLabel++;
        }
    }

    /**
     * Tạo radio button cho mỗi đáp án
     */
    private JRadioButton createAnswerRadioButton(QuestionAnswer questionAnswer, char optionLabel) {
        // Format: A. Đáp án...
        String answerText = "<html><body style='width: 500px;'>" +
                "<b>" + optionLabel + ".</b> " + questionAnswer.getContent() +
                "</body></html>";

        JRadioButton radioButton = new JRadioButton(answerText);
        radioButton.setFont(new Font("Arial", Font.PLAIN, 14));
        radioButton.setBackground(Color.WHITE);
        radioButton.setFocusPainted(false);
        radioButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Store answer content in action command for later retrieval
        radioButton.setActionCommand(questionAnswer.getContent().strip());

        // Add some padding
        radioButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(10, 15, 10, 15)
        ));

        // Change background when selected
        radioButton.addActionListener(e -> {
            // Reset all radio buttons' background
            Component[] components = panelAnswers.getComponents();
            for (Component comp : components) {
                if (comp instanceof JRadioButton) {
                    JRadioButton rb = (JRadioButton) comp;
                    if (rb.isSelected()) {
                        rb.setBackground(new Color(144, 238, 144)); // Light Green
                        rb.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(new Color(34, 139, 34), 2),
                                new EmptyBorder(10, 15, 10, 15)
                        ));
                    } else {
                        rb.setBackground(Color.WHITE);
                        rb.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                                new EmptyBorder(10, 15, 10, 15)
                        ));
                    }
                }
            }
        });

        // Hover effect
        radioButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (!radioButton.isSelected()) {
                    radioButton.setBackground(new Color(240, 248, 255)); // Alice Blue
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (!radioButton.isSelected()) {
                    radioButton.setBackground(Color.WHITE);
                }
            }
        });

        return radioButton;
    }

    /**
     * Add event listeners
     */
    private void addEvents() {
        buttonClearChoice.addActionListener(event -> {
            buttonGroup.clearSelection();

            // Reset all radio buttons' appearance
            Component[] components = panelAnswers.getComponents();
            for (Component comp : components) {
                if (comp instanceof JRadioButton) {
                    JRadioButton rb = (JRadioButton) comp;
                    rb.setBackground(Color.WHITE);
                    rb.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                            new EmptyBorder(10, 15, 10, 15)
                    ));
                }
            }

            // Show confirmation
            JOptionPane.showMessageDialog(
                    this,
                    "Đã bỏ chọn đáp án!",
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });
    }

    /**
     * Get button group để lấy đáp án đã chọn
     */
    public ButtonGroup getButtonGroup() {
        return buttonGroup;
    }

    /**
     * Get selected answer content
     */
    public String getSelectedAnswer() {
        if (buttonGroup.getSelection() != null) {
            return buttonGroup.getSelection().getActionCommand();
        }
        return null;
    }

    /**
     * Check if any answer is selected
     */
    public boolean hasSelectedAnswer() {
        return buttonGroup.getSelection() != null;
    }
}