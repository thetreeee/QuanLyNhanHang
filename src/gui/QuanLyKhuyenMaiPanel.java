package gui;

import javax.swing.*;
import java.awt.*;

public class QuanLyKhuyenMaiPanel extends JPanel {
    public QuanLyKhuyenMaiPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        add(new JLabel("MÀN HÌNH QUẢN LÝ KHUYẾN MÃI", SwingConstants.CENTER), BorderLayout.NORTH);
        
        String[] columns = {"Mã KM", "Tên CT", "Phần trăm giảm"};
        Object[][] data = {{"KM01", "Khai trương", "20%"}, {"KM02", "Lễ 2/9", "10%"}};
        JTable table = new JTable(data, columns);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }
}