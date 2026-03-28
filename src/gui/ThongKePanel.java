package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ThongKePanel extends JPanel {

    private final Color BG_COLOR = new Color(240, 242, 245);
    private final Color BLUE_ACCENT = new Color(54, 92, 245);
    private final Color TEXT_DARK = new Color(44, 56, 74);
    private final Color GREEN_SOFT = new Color(46, 204, 113);
    private final Color RED_SOFT = new Color(231, 76, 60);

    public ThongKePanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(BG_COLOR);
        setBorder(new EmptyBorder(25, 30, 25, 30));

        // --- 1. HEADER AREA: Bộ lọc thời gian ---
        JPanel topFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        topFilter.setOpaque(false);
        topFilter.add(new JLabel("Từ ngày:"));
        topFilter.add(new JTextField("18/03/2026", 8));
        topFilter.add(new JLabel("Đến ngày:"));
        topFilter.add(new JTextField("22/03/2026", 8));
        JButton btnView = new JButton("Xem thống kê");
        btnView.setBackground(BLUE_ACCENT);
        btnView.setForeground(Color.WHITE);
        topFilter.add(btnView);
        add(topFilter, BorderLayout.NORTH);

        // --- 2. CENTER AREA: Chia làm 2 cột chính ---
        JPanel centerContainer = new JPanel(new GridLayout(2, 2, 25, 25));
        centerContainer.setOpaque(false);

        // A. Biểu đồ doanh thu (Góc trên trái)
        centerContainer.add(createChartPanel("Thống kê Doanh thu", "Doanh thu"));

        // B. Hoạt động hiện tại - Real-time (Góc trên phải)
        centerContainer.add(createRealTimePanel());

        // C. Xu hướng món ăn (Góc dưới trái)
        centerContainer.add(createTrendPanel());

        // D. Top nhân viên chăm chỉ (Góc dưới phải)
        centerContainer.add(createTopStaffPanel());

        add(centerContainer, BorderLayout.CENTER);
    }

    // --- PANEL BIỂU ĐỒ ---
    private JPanel createChartPanel(String title, String unit) {
        JPanel p = new ModernPanel();
        p.setLayout(new BorderLayout());
        p.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        p.add(lblTitle, BorderLayout.NORTH);

        // Giả lập vẽ biểu đồ bằng một Panel ẩn
        JPanel chartArea = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BLUE_ACCENT);
                g2.setStroke(new BasicStroke(3));
                // Vẽ đường zigzag mô phỏng biểu đồ hình 1
                int[] x = {50, 100, 150, 200, 250, 300, 350, 400};
                int[] y = {150, 140, 145, 130, 155, 120, 40, 130};
                g2.drawPolyline(x, y, x.length);
                for(int i=0; i<x.length; i++) g2.fillOval(x[i]-4, y[i]-4, 8, 8);
            }
        };
        chartArea.setBackground(Color.WHITE);
        p.add(chartArea, BorderLayout.CENTER);

        return p;
    }

    // --- PANEL REAL-TIME ---
    private JPanel createRealTimePanel() {
        JPanel p = new ModernPanel();
        p.setLayout(new BorderLayout());
        p.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblTitle = new JLabel("Hoạt động hiện tại (Real-time)");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        p.add(lblTitle, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);

        // Trống - Phục vụ - Đã đặt
        JPanel stats = new JPanel(new GridLayout(1, 3));
        stats.setOpaque(false);
        stats.add(createStatItem("Trống", "9", GREEN_SOFT));
        stats.add(createStatItem("Phục vụ", "1", RED_SOFT));
        stats.add(createStatItem("Đã đặt", "0", Color.ORANGE));
        content.add(stats, BorderLayout.NORTH);

        // Nhân viên trong ca
        JPanel staffInShift = new JPanel(new BorderLayout());
        staffInShift.setOpaque(false);
        staffInShift.setBorder(new EmptyBorder(20, 0, 0, 0));
        JLabel lblStaff = new JLabel("Nhân viên đang trong ca:");
        lblStaff.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JLabel lblStatus = new JLabel("(Không có nhân viên nào đang trực)");
        lblStatus.setForeground(Color.GRAY);
        staffInShift.add(lblStaff, BorderLayout.NORTH);
        staffInShift.add(lblStatus, BorderLayout.CENTER);

        content.add(staffInShift, BorderLayout.CENTER);
        p.add(content, BorderLayout.CENTER);
        return p;
    }

    // --- PANEL XU HƯỚNG MÓN ĂN ---
    private JPanel createTrendPanel() {
        JPanel p = new ModernPanel();
        p.setLayout(new BorderLayout());
        p.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblTitle = new JLabel("Xu hướng món ăn (7 ngày qua)");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        p.add(lblTitle, BorderLayout.NORTH);

        JPanel list = new JPanel();
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        list.setOpaque(false);
        list.setBorder(new EmptyBorder(15, 0, 0, 0));

        list.add(createTrendItem("1", "Vang trắng Pháp", "44 suất", BLUE_ACCENT));
        list.add(Box.createRigidArea(new Dimension(0, 10)));
        list.add(createTrendItem("2", "Cà phê sữa đá", "22 suất", Color.GRAY));
        list.add(Box.createRigidArea(new Dimension(0, 10)));
        list.add(createTrendItem("3", "Cà ri vịt", "20 suất", new Color(160, 82, 45)));

        p.add(list, BorderLayout.CENTER);
        return p;
    }

    // --- PANEL TOP NHÂN VIÊN ---
    private JPanel createTopStaffPanel() {
        JPanel p = new ModernPanel();
        p.setLayout(new BorderLayout());
        p.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel lblTitle = new JLabel("Top Nhân viên chăm chỉ");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        p.add(lblTitle, BorderLayout.NORTH);

        JLabel emptyMsg = new JLabel("Chưa có dữ liệu chấm công tuần này.", SwingConstants.CENTER);
        emptyMsg.setForeground(Color.GRAY);
        p.add(emptyMsg, BorderLayout.CENTER);

        return p;
    }

    // Helper: Tạo mục thống kê số
    private JPanel createStatItem(String label, String value, Color color) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel l = new JLabel(label, SwingConstants.CENTER);
        JLabel v = new JLabel(value, SwingConstants.CENTER);
        v.setFont(new Font("Segoe UI", Font.BOLD, 24));
        v.setForeground(color);
        p.add(l, BorderLayout.SOUTH);
        p.add(v, BorderLayout.CENTER);
        return p;
    }

    // Helper: Tạo dòng xu hướng món ăn
    private JPanel createTrendItem(String rank, String name, String qty, Color rankColor) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(500, 40));

        JLabel lblRank = new JLabel(rank, SwingConstants.CENTER);
        lblRank.setPreferredSize(new Dimension(30, 30));
        lblRank.setOpaque(true);
        lblRank.setBackground(rankColor);
        lblRank.setForeground(Color.WHITE);

        JLabel lblName = new JLabel("  " + name);
        JLabel lblQty = new JLabel(qty);

        p.add(lblRank, BorderLayout.WEST);
        p.add(lblName, BorderLayout.CENTER);
        p.add(lblQty, BorderLayout.EAST);
        return p;
    }

    // Lớp Panel bo góc trắng
    class ModernPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            g2.dispose();
        }
    }
}