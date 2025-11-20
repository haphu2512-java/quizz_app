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
import java.util.concurrent.ExecutionException;

/**
 * Form quản lý đề thi - Sử dụng RMI Service và SwingWorker để tránh block UI
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
        // Initialize table model
        columnModel = new DefaultTableModel(
                new Object[][]{},
                new String[]{"Mã Đề Thi", "Tên Môn Thi", "Số Câu Hỏi", "Điểm Tổng", "Điểm Mỗi Câu"}
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };

        if (tableViewExamManagement != null) {
            tableViewExamManagement.setModel(columnModel);
            tableViewExamManagement.getTableHeader().setReorderingAllowed(false);
            rowModel = (DefaultTableModel) tableViewExamManagement.getModel();
        }

        if (textfieldExamIDViewExamManagement != null) {
            textfieldExamIDViewExamManagement.setEnabled(false);
        }
    }

    private void addActionEvent() {
        // Table mouse click event
        if (tableViewExamManagement != null) {
            tableViewExamManagement.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    handleTableRowSelection();
                }
            });
        }

        // Add button
        if (buttonAddViewExamManagement != null) {
            buttonAddViewExamManagement.addActionListener(e -> handleAdd());
        }

        // Update button
        if (buttonUpdateViewExamManagement != null) {
            buttonUpdateViewExamManagement.addActionListener(e -> handleUpdate());
        }

        // Delete button
        if (buttonDeleteViewExamManagement != null) {
            buttonDeleteViewExamManagement.addActionListener(e -> handleDelete());
        }

        // Refresh button
        if (buttonRefreshViewExamManagement != null) {
            buttonRefreshViewExamManagement.addActionListener(e -> {
                resetInputFields();
                loadExamData();
                if (textfieldFindViewExamManagement != null) {
                    textfieldFindViewExamManagement.setText("");
                }
            });
        }

        // Back button
        if (buttonBackViewExamManagement != null) {
            buttonBackViewExamManagement.addActionListener(e -> {
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
        int modelRow = tableViewExamManagement.getSelectedRow();
        if (modelRow != -1) {
            int sortedRow = tableViewExamManagement.convertRowIndexToModel(modelRow);
            chosenExam = examList.get(sortedRow);
            textfieldExamIDViewExamManagement.setText(String.valueOf(chosenExam.getExamId()));
            textfieldSubjectNameViewExamManagement.setText(chosenExam.getSubject());
            textfieldTotalQuestionViewExamManagement.setText(String.valueOf(chosenExam.getTotalQuestion()));
            textfieldTotalScoreViewExamManagement.setText(String.valueOf(chosenExam.getTotalScore()));
        }
    }

    private void handleAdd() {
        String subject = textfieldSubjectNameViewExamManagement.getText().strip();
        String totalQuestionStr = textfieldTotalQuestionViewExamManagement.getText().strip();
        String totalScoreStr = textfieldTotalScoreViewExamManagement.getText().strip();

        // Validate input
        if (subject.isEmpty() || totalQuestionStr.isEmpty() || totalScoreStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Các trường thông tin không được bỏ trống!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int totalQuestion = Integer.parseInt(totalQuestionStr);
            int totalScore = Integer.parseInt(totalScoreStr);

            if (totalQuestion <= 0 || totalScore <= 0) {
                JOptionPane.showMessageDialog(this, "Số câu hỏi và tổng điểm phải lớn hơn 0!", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double scorePerQuestion = totalScore / (double) totalQuestion;
            Exam newExam = new Exam(subject, totalQuestion, totalScore, scorePerQuestion);

            // Use SwingWorker to perform RMI call in the background
            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return examService.save(newExam);
                }

                @Override
                protected void done() {
                    try {
                        boolean success = get();
                        if (success) {
                            JOptionPane.showMessageDialog(ExamManagement.this, "Thêm đề thi thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                            loadExamData();
                            resetInputFields();
                        } else {
                            JOptionPane.showMessageDialog(ExamManagement.this, "Thêm đề thi thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (InterruptedException | ExecutionException e) {
                        handleRemoteException(e);
                    } finally {
                        setCursor(Cursor.getDefaultCursor());
                    }
                }
            }.execute();
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Số câu hỏi và tổng điểm phải là số nguyên!", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleUpdate() {
        if (chosenExam == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn đề thi cần cập nhật!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String subject = textfieldSubjectNameViewExamManagement.getText().strip();
        String totalQuestionStr = textfieldTotalQuestionViewExamManagement.getText().strip();
        String totalScoreStr = textfieldTotalScoreViewExamManagement.getText().strip();

        if (subject.isEmpty() || totalQuestionStr.isEmpty() || totalScoreStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Các trường thông tin không được bỏ trống!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int totalQuestion = Integer.parseInt(totalQuestionStr);
            int totalScore = Integer.parseInt(totalScoreStr);

            if (totalQuestion <= 0 || totalScore <= 0) {
                JOptionPane.showMessageDialog(this, "Số câu hỏi và tổng điểm phải lớn hơn 0!", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                return;
            }

            chosenExam.setSubject(subject);
            chosenExam.setTotalQuestion(totalQuestion);
            chosenExam.setTotalScore(totalScore);
            chosenExam.setScorePerQuestion(totalScore / (double) totalQuestion);

            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return examService.update(chosenExam);
                }

                @Override
                protected void done() {
                    try {
                        boolean success = get();
                        if (success) {
                            JOptionPane.showMessageDialog(ExamManagement.this, "Cập nhật đề thi thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                            loadExamData();
                            resetInputFields();
                        } else {
                            JOptionPane.showMessageDialog(ExamManagement.this, "Cập nhật đề thi thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (InterruptedException | ExecutionException e) {
                        handleRemoteException(e);
                    } finally {
                        setCursor(Cursor.getDefaultCursor());
                    }
                }
            }.execute();
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Số câu hỏi và tổng điểm phải là số nguyên!", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDelete() {
        if (chosenExam == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn đề thi cần xóa!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa đề thi này?\nLưu ý: Tất cả câu hỏi liên quan cũng sẽ bị xóa!", "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return examService.delete(chosenExam.getExamId());
                }

                @Override
                protected void done() {
                    try {
                        boolean success = get();
                        if (success) {
                            JOptionPane.showMessageDialog(ExamManagement.this, "Xóa đề thi thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                            loadExamData();
                            resetInputFields();
                        } else {
                            JOptionPane.showMessageDialog(ExamManagement.this, "Xóa đề thi thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (InterruptedException | ExecutionException e) {
                        handleRemoteException(e);
                    } finally {
                        setCursor(Cursor.getDefaultCursor());
                    }
                }
            }.execute();
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        }
    }

    private void loadExamData() {
        new SwingWorker<List<Exam>, Void>() {
            @Override
            protected List<Exam> doInBackground() throws Exception {
                return examService.getAll();
            }

            @Override
            protected void done() {
                try {
                    examList = get();
                    rowModel.setRowCount(0); // Clear existing data
                    for (Exam exam : examList) {
                        rowModel.addRow(new Object[]{
                                exam.getExamId(),
                                exam.getSubject(),
                                exam.getTotalQuestion(),
                                exam.getTotalScore(),
                                String.format("%.2f", exam.getScorePerQuestion())
                        });
                    }
                } catch (InterruptedException | ExecutionException e) {
                    handleRemoteException(e);
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        }.execute();
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }

    private void makeTableSearchable() {
        if (textfieldFindViewExamManagement != null && tableViewExamManagement != null) {
            rowSorter = new TableRowSorter<>(rowModel);
            tableViewExamManagement.setRowSorter(rowSorter);

            textfieldFindViewExamManagement.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) { filter(); }
                @Override
                public void removeUpdate(DocumentEvent e) { filter(); }
                @Override
                public void changedUpdate(DocumentEvent e) { filter(); }

                private void filter() {
                    String text = textfieldFindViewExamManagement.getText().strip();
                    if (text.isEmpty()) {
                        rowSorter.setRowFilter(null);
                    } else {
                        rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                    }
                }
            });
        }
    }
    
    private void handleRemoteException(Exception e) {
        setCursor(Cursor.getDefaultCursor());
        Throwable cause = e.getCause();
        if (cause instanceof RemoteException) {
            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi kết nối server:\n" + cause.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi không xác định:\n" + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
        }
        e.printStackTrace();
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