package iuh.fit.se.gui;

import entity.Question;
import entity.QuestionAnswer;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class QuestionAndAnswerForm extends JPanel {
    private JLabel labelQuestionContentViewTakeExamAttendee;
    private JPanel panelAnswerGroupViewTakeExamAttendee;
    private JPanel panelTakeExamViewTakeExamAttendee;
    private JButton buttonClearChoiceViewTakeExamAttendee;

    private ButtonGroup buttonGroup;

    public QuestionAndAnswerForm(Question question, List<QuestionAnswer> questionAnswerList) {
        initComponents();
        addEvents();
        showFormContent(question, questionAnswerList);
        this.add(panelTakeExamViewTakeExamAttendee);
    }

    private void initComponents() {
        buttonGroup = new ButtonGroup();
        panelAnswerGroupViewTakeExamAttendee.setLayout(new GridLayout(5, 1));
    }

    private void addEvents() {
        buttonClearChoiceViewTakeExamAttendee.addActionListener(event -> {
            buttonGroup.clearSelection();
        });
    }

    private void showFormContent(Question question, List<QuestionAnswer> questionAnswerList) {
        labelQuestionContentViewTakeExamAttendee.setText(question.getContent());
        for (var questionAnswer : questionAnswerList) {
            var radioButton = new JRadioButton(questionAnswer.getContent());
            // Store the ID of the QuestionAnswer in the action command
            // This is useful for identifying the selected answer later
            radioButton.setActionCommand(String.valueOf(questionAnswer.getQuestionAnswerId())); // Use getter
            buttonGroup.add(radioButton);
            panelAnswerGroupViewTakeExamAttendee.add(radioButton);
        }
    }

    public ButtonGroup getButtonGroup() {
        return buttonGroup;
    }
}