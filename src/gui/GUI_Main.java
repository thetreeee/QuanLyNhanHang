package gui;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import java.awt.*;

public class GUI_Main {
    public static void main(String[] args) {
        try {
            // Thiết lập Look and Feel
            UIManager.setLookAndFeel(new FlatLightLaf());
            
            // Cấu hình bo góc (UI Defaults)
            UIManager.put("Button.arc", 15);
            UIManager.put("Component.arc", 15);
            UIManager.put("TextComponent.arc", 15);
            UIManager.put("TableHeader.background", new Color(255, 235, 235));
            
            // Cấu hình Font chữ tiếng Việt đồng bộ
            Font defaultFont = new Font("Segoe UI", Font.PLAIN, 14);
            UIManager.put("Label.font", defaultFont);
            UIManager.put("Button.font", new Font("Segoe UI", Font.BOLD, 14));
            UIManager.put("Table.font", defaultFont);
            
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Chạy chương trình
        SwingUtilities.invokeLater(() -> {
            GUITaiKhoan loginFrame = new GUITaiKhoan();
            loginFrame.setVisible(true);
        });
    }
}