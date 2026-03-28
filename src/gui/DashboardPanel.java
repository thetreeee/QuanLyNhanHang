package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import org.knowm.xchart.*; // Cần add thư viện xchart-3.8.8.jar vào Build Path
import org.knowm.xchart.style.Styler.LegendPosition;

public class DashboardPanel extends JPanel {

    public DashboardPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(240, 242, 245)); // Màu nền xám xanh nhẹ hiện đại
        setBorder(new EmptyBorder(25, 25, 25, 25));

        // --- PHẦN TIÊU ĐỀ ---
        JLabel lblHeader = new JLabel("Bảng Điều Khiển Thống Kê");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblHeader.setForeground(new Color(44, 62, 80));
        lblHeader.setBorder(new EmptyBorder(0, 0, 25, 0));
        add(lblHeader, BorderLayout.NORTH);

        // --- PHẦN THẺ THỐNG KÊ (4 CARDS) ---
        JPanel pnlCards = new JPanel(new GridLayout(1, 4, 20, 0));
        pnlCards.setOpaque(false);
        
        pnlCards.add(createStatCard("Doanh Thu Ngày", "4.500.000đ", new Color(52, 152, 219)));
        pnlCards.add(createStatCard("Bàn Đang Sử Dụng", "8 / 20", new Color(46, 204, 113)));
        pnlCards.add(createStatCard("Đơn Chờ Bếp", "12", new Color(241, 194, 50)));
        pnlCards.add(createStatCard("Khách Mới", "25", new Color(155, 89, 182)));

        // --- PHẦN BIỂU ĐỒ (CHART) ---
        JPanel pnlChartArea = new JPanel(new BorderLayout());
        pnlChartArea.setBackground(Color.WHITE);
        pnlChartArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            new EmptyBorder(20, 20, 20, 20)
        ));
        // Sử dụng tính năng bo góc của FlatLaf cho Panel biểu đồ
        pnlChartArea.putClientProperty("FlatLaf.style", "arc: 20");

        // Khởi tạo biểu đồ XChart
        CategoryChart chart = new CategoryChartBuilder()
                .title("Thống Kê Doanh Thu 7 Ngày Gần Nhất")
                .xAxisTitle("Ngày").yAxisTitle("Triệu VNĐ").build();
        
        chart.getStyler().setLegendPosition(LegendPosition.InsideNE);
        chart.getStyler().setChartBackgroundColor(Color.WHITE);
        chart.getStyler().setPlotGridLinesVisible(false);
        
        chart.addSeries("Doanh thu", 
            new java.util.ArrayList<>(java.util.Arrays.asList("Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "CN")), 
            new java.util.ArrayList<>(java.util.Arrays.asList(12, 15, 8, 22, 19, 35, 42)));

        XChartPanel<CategoryChart> chartPanel = new XChartPanel<>(chart);
        pnlChartArea.add(chartPanel, BorderLayout.CENTER);

        // Gom các phần vào trung tâm
        JPanel pnlCenter = new JPanel(new BorderLayout(0, 30));
        pnlCenter.setOpaque(false);
        pnlCenter.add(pnlCards, BorderLayout.NORTH);
        pnlCenter.add(pnlChartArea, BorderLayout.CENTER);

        add(pnlCenter, BorderLayout.CENTER);
    }

    // Hàm tạo Card thống kê chuyên nghiệp
    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.putClientProperty("FlatLaf.style", "arc: 15"); // Bo góc thẻ

        // Vạch màu trang trí bên trái
        JPanel pnlLine = new JPanel();
        pnlLine.setBackground(color);
        pnlLine.setPreferredSize(new Dimension(6, 0));
        card.add(pnlLine, BorderLayout.WEST);

        JPanel pnlText = new JPanel(new GridLayout(2, 1));
        pnlText.setOpaque(false);
        pnlText.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lblT = new JLabel(title);
        lblT.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblT.setForeground(Color.GRAY);

        JLabel lblV = new JLabel(value);
        lblV.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblV.setForeground(new Color(50, 50, 50));

        pnlText.add(lblT);
        pnlText.add(lblV);
        card.add(pnlText, BorderLayout.CENTER);

        return card;
    }
}