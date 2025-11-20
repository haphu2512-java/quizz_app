package iuh.fit.se;

import com.formdev.flatlaf.FlatLightLaf;
import iuh.fit.se.gui.Login;
import iuh.fit.se.util.ServiceFactory;

import javax.swing.*;
import java.awt.*;

/**
 * Main entry point cho ứng dụng Quiz Client
 */
public class Main {
    public static void main(String[] args) {
        // Thiết lập Look and Feel hiện đại
        try {
            FlatLightLaf.setup();
            UIManager.put("Button.arc", 10);
            UIManager.put("Component.arc", 10);
            UIManager.put("TextComponent.arc", 10);
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback to system look and feel
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        // Kiểm tra kết nối RMI Server
        EventQueue.invokeLater(() -> {
            // Show splash screen while connecting
            JDialog splash = createSplashScreen();
            splash.setVisible(true);

            // Test connection in background
            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    Thread.sleep(1000); // Give user time to see splash
                    return ServiceFactory.testConnection();
                }

                @Override
                protected void done() {
                    splash.dispose();
                    try {
                        if (get()) {
                            // Connection successful, show login
                            new Login();
                        } else {
                            showConnectionError();
                        }
                    } catch (Exception e) {
                        showConnectionError();
                    }
                }
            };
            worker.execute();
        });

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            ServiceFactory.close();
            System.out.println("Client đã đóng kết nối!");
        }));
    }

    private static JDialog createSplashScreen() {
        JDialog splash = new JDialog((Frame) null, "Đang kết nối...", false);
        splash.setUndecorated(true);
        splash.setSize(400, 200);
        splash.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Ứng Dụng Trắc Nghiệm", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(0, 102, 204));

        JLabel statusLabel = new JLabel("Đang kết nối tới server...", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(statusLabel, BorderLayout.CENTER);
        panel.add(progressBar, BorderLayout.SOUTH);

        splash.add(panel);
        return splash;
    }

    private static void showConnectionError() {
        JOptionPane.showMessageDialog(
                null,
                "Không thể kết nối tới RMI Server!\n\n" +
                        "Vui lòng kiểm tra:\n" +
                        "1. Server đã được khởi động chưa?\n" +
                        "2. Port 1262 có đang được sử dụng không?\n" +
                        "3. Firewall có chặn kết nối không?",
                "Lỗi Kết Nối",
                JOptionPane.ERROR_MESSAGE
        );
        System.exit(1);
    }
}