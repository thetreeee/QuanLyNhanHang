package gui;

import dao.ThongKeDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class ThongKePanel extends JPanel {

    private final Color BG_COLOR = new Color(245, 245, 245);
    private final Color TEXT_DARK = new Color(44, 56, 74);
    private final Color RED_ACCENT = new Color(225, 75, 50); 
    private final Color GREEN_ACCENT = new Color(46, 204, 113);

    private ThongKeDAO thongKeDAO;
    private DecimalFormat df = new DecimalFormat("#,### VNĐ");
    
    // Đưa mainContainer ra ngoài để loadData() có thể gọi tới
    private ScrollablePanel mainContainer;

    public ThongKePanel() {
        thongKeDAO = new ThongKeDAO();
        
        setLayout(new BorderLayout());
        setBackground(BG_COLOR);

        mainContainer = new ScrollablePanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setOpaque(false);
        mainContainer.setBorder(new EmptyBorder(30, 40, 30, 40)); 

        JScrollPane scroll = new JScrollPane(mainContainer);
        scroll.setBorder(null);
        scroll.setViewportBorder(null);
        scroll.getViewport().setBackground(BG_COLOR);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        
        add(scroll, BorderLayout.CENTER);

        // =========================================================================
        // GIẢI PHÁP REFRESH: Cứ mỗi lần Tab này hiện lên là vẽ lại toàn bộ dữ liệu
        // =========================================================================
        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                loadData();
            }
        });
        
        // Gọi lần đầu tiên khi mở app
        loadData();
    }

    // Hàm loadData sẽ "đập đi xây lại" giao diện với dữ liệu MỚI NHẤT từ Database
    public void loadData() {
        mainContainer.removeAll(); // Xóa sạch giao diện cũ

        // --- TITLE ---
        JLabel lblTitle = new JLabel("TỔNG QUAN");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitle.setForeground(TEXT_DARK);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContainer.add(lblTitle);
        mainContainer.add(Box.createRigidArea(new Dimension(0, 20)));

        // --- ROW 1: CARDS TỔNG QUAN ---
        JPanel pnlCards = new JPanel(new GridLayout(1, 2, 30, 0));
        pnlCards.setOpaque(false);
        pnlCards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150)); 
        pnlCards.setAlignmentX(Component.LEFT_ALIGNMENT);

        double doanhThuNay = thongKeDAO.getDoanhThuHomNay();
        pnlCards.add(createOverviewCard("Doanh thu hôm nay", df.format(doanhThuNay), RED_ACCENT));

        int[] banData = thongKeDAO.getThongKeBan();
        String banText = banData[0] + "/" + banData[1];
        pnlCards.add(createOverviewCard("Số bàn đang phục vụ", banText, TEXT_DARK));

        mainContainer.add(pnlCards);
        mainContainer.add(Box.createRigidArea(new Dimension(0, 30)));

        // --- ROW 2: BIỂU ĐỒ DOANH THU ---
        JPanel pnlChart = createChartPanel();
        pnlChart.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContainer.add(pnlChart);
        mainContainer.add(Box.createRigidArea(new Dimension(0, 30)));

        // --- ROW 3: TOP MÓN ĂN ---
        JPanel pnlTopItem = createTopItemPanel();
        pnlTopItem.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainContainer.add(pnlTopItem);

        // Cập nhật lại màn hình
        mainContainer.revalidate();
        mainContainer.repaint();
    }

    private JPanel createOverviewCard(String title, String value, Color valueColor) {
        JPanel card = new ModernPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(25, 30, 25, 30));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblTitle.setForeground(Color.GRAY);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 36));
        lblValue.setForeground(valueColor);

        card.add(lblTitle);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(lblValue);
        return card;
    }

    private JPanel createChartPanel() {
        JPanel card = new ModernPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(25, 30, 25, 30));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 350));
        card.setPreferredSize(new Dimension(800, 350));

        JLabel lblTitle = new JLabel("Thống kê doanh thu (7 ngày qua)");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        card.add(lblTitle, BorderLayout.NORTH);

        Map<String, Double> chartData = thongKeDAO.getDoanhThu7NgayQua();

        JPanel chartDrawArea = new JPanel() {
            // Biến dùng cho hiệu ứng Hover
            private int hoverIndex = -1;
            private List<Point> pointCoords = new ArrayList<>();
            private List<Double> valuesList = new ArrayList<>();

            {
                // ===============================================================
                // TÍNH NĂNG MỚI: Bắt sự kiện rê chuột để hiện Tooltip doanh thu
                // ===============================================================
                addMouseMotionListener(new MouseAdapter() {
                    @Override
                    public void mouseMoved(MouseEvent e) {
                        int oldIndex = hoverIndex;
                        hoverIndex = -1;
                        for (int i = 0; i < pointCoords.size(); i++) {
                            // Nếu chuột nằm trong bán kính 15px của điểm nào đó
                            if (pointCoords.get(i).distance(e.getPoint()) <= 15) {
                                hoverIndex = i;
                                break;
                            }
                        }
                        if (oldIndex != hoverIndex) repaint(); // Chỉ vẽ lại khi chuyển mục tiêu
                    }
                });

                // Khi chuột rời khỏi biểu đồ thì tắt tooltip
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseExited(MouseEvent e) {
                        hoverIndex = -1;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (chartData == null || chartData.isEmpty()) return;

                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth(); 
                int h = getHeight();
                int padding = 40;

                double maxVal = 0;
                for (Double val : chartData.values()) if (val > maxVal) maxVal = val;
                if (maxVal == 0) maxVal = 1;

                int n = chartData.size();
                int xStep = (w - 2 * padding) / Math.max(1, n - 1);

                pointCoords.clear();
                valuesList.clear();
                List<String> labels = new ArrayList<>(chartData.keySet());

                int i = 0;
                for (Double val : chartData.values()) {
                    int x = padding + i * xStep;
                    int y = h - padding - (int) ((val / maxVal) * (h - 2 * padding));
                    pointCoords.add(new Point(x, y));
                    valuesList.add(val);
                    
                    g2.setColor(Color.GRAY);
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    g2.drawString(labels.get(i), x - 15, h - 10);
                    i++;
                }

                Polygon p = new Polygon();
                p.addPoint(pointCoords.get(0).x, h - padding);
                for (int j = 0; j < n; j++) p.addPoint(pointCoords.get(j).x, pointCoords.get(j).y);
                p.addPoint(pointCoords.get(n - 1).x, h - padding);
                
                g2.setColor(new Color(225, 75, 50, 40)); 
                g2.fillPolygon(p);

                g2.setColor(RED_ACCENT);
                g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                for (int j = 0; j < n - 1; j++) {
                    g2.drawLine(pointCoords.get(j).x, pointCoords.get(j).y, pointCoords.get(j + 1).x, pointCoords.get(j + 1).y);
                }

                for (int j = 0; j < n; j++) {
                    // Nếu đang hover vào điểm này thì cho điểm đó to lên một chút
                    int radius = (j == hoverIndex) ? 14 : 10;
                    int offset = radius / 2;
                    
                    g2.setColor(Color.WHITE);
                    g2.fillOval(pointCoords.get(j).x - offset, pointCoords.get(j).y - offset, radius, radius);
                    g2.setColor(RED_ACCENT);
                    g2.drawOval(pointCoords.get(j).x - offset, pointCoords.get(j).y - offset, radius, radius);
                }

                // ===============================================================
                // VẼ POP-UP TOOLTIP (MÀU ĐEN HIỂN THỊ TIỀN) NẾU CÓ HOVER
                // ===============================================================
                if (hoverIndex != -1) {
                    String moneyText = df.format(valuesList.get(hoverIndex));
                    Point pt = pointCoords.get(hoverIndex);
                    
                    FontMetrics fm = g2.getFontMetrics(new Font("Segoe UI", Font.BOLD, 14));
                    int textWidth = fm.stringWidth(moneyText);
                    int textHeight = fm.getHeight();
                    
                    int boxW = textWidth + 20;
                    int boxH = textHeight + 10;
                    int boxX = pt.x - boxW / 2;
                    int boxY = pt.y - boxH - 15; // Đẩy lên trên cái chấm
                    
                    // Chống tràn popup ra ngoài lề trái/phải/trên
                    if (boxX < 0) boxX = 5;
                    if (boxX + boxW > w) boxX = w - boxW - 5;
                    if (boxY < 0) boxY = pt.y + 15; // Nếu tràn lên trên thì kéo nó xuống dưới chấm
                    
                    // Vẽ cục nền đen
                    g2.setColor(new Color(0, 0, 0, 200));
                    g2.fillRoundRect(boxX, boxY, boxW, boxH, 10, 10);
                    
                    // Vẽ chữ trắng
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                    g2.drawString(moneyText, boxX + 10, boxY + boxH - 8);
                }
            }
        };
        chartDrawArea.setOpaque(false);
        card.add(chartDrawArea, BorderLayout.CENTER);

        return card;
    }

    private JPanel createTopItemPanel() {
        JPanel card = new ModernPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(25, 30, 25, 30));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400)); 

        JLabel lblTitle = new JLabel("Thống kê món ăn bán chạy");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        card.add(lblTitle, BorderLayout.NORTH);

        JPanel listPnl = new JPanel();
        listPnl.setLayout(new BoxLayout(listPnl, BoxLayout.Y_AXIS));
        listPnl.setOpaque(false);
        listPnl.setBorder(new EmptyBorder(20, 0, 0, 0));

        Map<String, Double> topMon = thongKeDAO.getTopMonAnBanChay();
        
        for (Map.Entry<String, Double> entry : topMon.entrySet()) {
            JPanel row = new JPanel(new BorderLayout());
            row.setOpaque(false);
            row.setBorder(new EmptyBorder(10, 0, 10, 0));

            JLabel lblName = new JLabel(entry.getKey());
            lblName.setFont(new Font("Segoe UI", Font.BOLD, 15));
            lblName.setForeground(TEXT_DARK);

            JLabel lblMoney = new JLabel(df.format(entry.getValue()));
            lblMoney.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            lblMoney.setForeground(Color.GRAY);

            row.add(lblName, BorderLayout.WEST);
            row.add(lblMoney, BorderLayout.EAST);
            
            listPnl.add(row);
            
            JPanel line = new JPanel();
            line.setBackground(new Color(230, 230, 230));
            line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
            listPnl.add(line);
        }

        card.add(listPnl, BorderLayout.CENTER);
        return card;
    }

    class ModernPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
            g2.dispose();
        }
    }
    
    class ScrollablePanel extends JPanel implements Scrollable {
        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) { return 20; }
        @Override public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) { return 20; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
        @Override public boolean getScrollableTracksViewportWidth() { return true; } 
    }
}