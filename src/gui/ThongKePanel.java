package gui;

import dao.ThongKeDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

import com.toedter.calendar.JDateChooser;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileOutputStream;
import java.io.ByteArrayOutputStream;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

public class ThongKePanel extends JPanel {

    private final Color BG_COLOR = new Color(241, 245, 249); // Màu nền xám nhạt như JavaFX
    private final Color TEXT_DARK = new Color(30, 58, 138); // Xanh Navy
    private final Color BLUE_ACCENT = new Color(59, 130, 246);
    private final Color GREEN_ACCENT = new Color(16, 185, 129);
    
    // Bảng màu cho Biểu đồ tròn
    private final Color[] PIE_COLORS = {
        new Color(239, 68, 68), new Color(245, 158, 11), 
        new Color(16, 185, 129), new Color(59, 130, 246), new Color(139, 92, 246)
    };

    private ThongKeDAO thongKeDAO;
    private DecimalFormat dfMoney = new DecimalFormat("#,### đ");
    private DecimalFormat dfPercent = new DecimalFormat("#.##");

    private ScrollablePanel mainContainer;
    
    // --- Các UI Controls cho Bộ lọc ---
    private JComboBox<String> cbxLoaiThongKe;
    
    private JPanel pnlNgay, pnlThang, pnlQuy;
    private JDateChooser dpTuNgay, dpDenNgay;
    private JComboBox<Integer> cbxThang, cbxNamThang;
    private JComboBox<Integer> cbxQuy, cbxNamQuy;
    
    private JButton btnExport;

    // --- Labels KPI ---
    private JLabel lblTongDoanhThu, lblTongDon, lblTrungBinhDon;
    
    // --- Khu vực biểu đồ ---
    private PieChartDrawArea pieChartArea;
    private LineChartDrawArea lineChartArea;
    private JTable tblMonAn;
    private DefaultTableModel modelMonAn;
    private CardLayout chartCardLayout;
    private JPanel pnlChartsContainer;

    // --- State ---
    private String currentTab = "DOANH_THU"; // DOANH_THU or MON_AN
    private JButton btnTabDoanhThu, btnTabMonAn;

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

        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                // Tự động kích hoạt nút Lọc khi mở Tab
            	handleFilterData();
            }
        });
        
        initUI();
    }

    private void initUI() {
        // 1. DÒNG TITLE & LỌC
        mainContainer.add(createFilterPanel());
        mainContainer.add(Box.createRigidArea(new Dimension(0, 25)));

        // 2. DÒNG 3 THẺ KPI
        mainContainer.add(createKPICardsPanel());
        mainContainer.add(Box.createRigidArea(new Dimension(0, 30)));

        // 3. DÒNG BIỂU ĐỒ
        chartCardLayout = new CardLayout();
        pnlChartsContainer = new JPanel(chartCardLayout);
        pnlChartsContainer.setOpaque(false);
        
        // --- Tab Doanh Thu (1 Line Chart) ---
        lineChartArea = new LineChartDrawArea();
        JPanel pnlDoanhThuChart = new JPanel(new BorderLayout());
        pnlDoanhThuChart.setOpaque(false);
        pnlDoanhThuChart.add(createChartWrapper("Biểu đồ tăng trưởng doanh thu", lineChartArea), BorderLayout.CENTER);
        
        // --- Tab Món Ăn (Bảng + Pie) ---
        JPanel pnlMonAnCharts = new JPanel(new GridLayout(1, 2, 30, 0));
        pnlMonAnCharts.setOpaque(false);
        
        modelMonAn = new DefaultTableModel(new String[]{"STT", "Mã món", "Tên món", "Số lượng bán"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblMonAn = new JTable(modelMonAn);
        tblMonAn.setRowHeight(35);
        tblMonAn.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        tblMonAn.getTableHeader().setBackground(new Color(241, 245, 249));
        tblMonAn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tblMonAn.setSelectionBackground(new Color(226, 232, 240));
        tblMonAn.setSelectionForeground(Color.BLACK);
        
        JScrollPane scrollTable = new JScrollPane(tblMonAn);
        scrollTable.getViewport().setBackground(Color.WHITE);
        scrollTable.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        
        pieChartArea = new PieChartDrawArea();
        pnlMonAnCharts.add(createChartWrapper("Bảng Xếp Hạng Món Ăn Tốt Nhất", scrollTable));
        pnlMonAnCharts.add(createChartWrapper("Tỷ trọng số lượng bán theo Món", pieChartArea));
        
        pnlChartsContainer.add(pnlDoanhThuChart, "DOANH_THU");
        pnlChartsContainer.add(pnlMonAnCharts, "MON_AN");
        
        mainContainer.add(pnlChartsContainer);
        
        // Gọi dữ liệu lần đầu sau khi toàn bộ UI đã được khởi tạo
        handleFilterData();
    }

    private JPanel createFilterPanel() {
        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.setOpaque(false);
        
        // --- TITLE ---
        JLabel lblTitle = new JLabel("THỐNG KÊ");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(TEXT_DARK);
        lblTitle.setBorder(new EmptyBorder(0, 0, 15, 0));
        pnlTop.add(lblTitle, BorderLayout.NORTH);
        
        // --- FILTER & TABS AREA ---
        JPanel pnlFilter = new ModernPanel();
        pnlFilter.setLayout(new WrapLayout(FlowLayout.LEFT, 0, 0));
        
        // Trái: Các bộ lọc
        JPanel pnlFilterLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 12));
        pnlFilterLeft.setOpaque(false);
        pnlFilterLeft.setBorder(new EmptyBorder(5, 10, 5, 0));
        
        pnlFilterLeft.add(createLabel("Xem theo:"));
        cbxLoaiThongKe = new JComboBox<>(new String[]{"Ngày", "Tháng", "Quý"});
        cbxLoaiThongKe.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        pnlFilterLeft.add(cbxLoaiThongKe);

        // --- Panel Chọn Ngày ---
        pnlNgay = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlNgay.setOpaque(false);
        
        LocalDate today = LocalDate.now();
        
        dpTuNgay = createModernDateChooser();
        dpTuNgay.setDate(java.sql.Date.valueOf(today.minusDays(7)));
        
        dpDenNgay = createModernDateChooser();
        dpDenNgay.setDate(java.sql.Date.valueOf(today));
        
        pnlNgay.add(createLabel("Từ ngày:")); pnlNgay.add(dpTuNgay);
        pnlNgay.add(createLabel("Đến ngày:")); pnlNgay.add(dpDenNgay);

        // --- Panel Chọn Tháng ---
        pnlThang = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlThang.setOpaque(false);
        cbxThang = new JComboBox<>(); for(int i=1; i<=12; i++) cbxThang.addItem(i);
        cbxNamThang = new JComboBox<>(); for(int i=2024; i<=2030; i++) cbxNamThang.addItem(i);
        cbxThang.setSelectedItem(today.getMonthValue());
        cbxNamThang.setSelectedItem(today.getYear());
        pnlThang.add(createLabel("Tháng:")); pnlThang.add(cbxThang);
        pnlThang.add(createLabel("Năm:")); pnlThang.add(cbxNamThang);

        // --- Panel Chọn Quý ---
        pnlQuy = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlQuy.setOpaque(false);
        cbxQuy = new JComboBox<>(new Integer[]{1, 2, 3, 4});
        cbxNamQuy = new JComboBox<>(); for(int i=2024; i<=2030; i++) cbxNamQuy.addItem(i);
        cbxQuy.setSelectedItem((today.getMonthValue() - 1) / 3 + 1);
        cbxNamQuy.setSelectedItem(today.getYear());
        pnlQuy.add(createLabel("Quý:")); pnlQuy.add(cbxQuy);
        pnlQuy.add(createLabel("Năm:")); pnlQuy.add(cbxNamQuy);

        // Mặc định ẩn Tháng và Quý
        pnlThang.setVisible(false);
        pnlQuy.setVisible(false);
        
        JLabel lblErrorMsg = new JLabel(" ");
        lblErrorMsg.setForeground(Color.RED);
        lblErrorMsg.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        lblErrorMsg.setBorder(new EmptyBorder(0, 15, 5, 0));

        pnlFilterLeft.add(pnlNgay);
        pnlFilterLeft.add(pnlThang);
        pnlFilterLeft.add(pnlQuy);
        
        // Phải: Tabs & Nút Lọc
        JPanel pnlFilterRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        pnlFilterRight.setOpaque(false);
        pnlFilterRight.setBorder(new EmptyBorder(5, 0, 5, 10));
        
        btnTabDoanhThu = createTabButton("Doanh thu", true);
        btnTabMonAn = createTabButton("Món ăn", false);

        btnExport = new JButton("Xuất báo cáo"); // Đã xóa emoji gây lỗi font
        btnExport.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnExport.setBackground(GREEN_ACCENT);
        btnExport.setForeground(Color.WHITE);
        btnExport.setFocusPainted(false);
        btnExport.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnExport.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(GREEN_ACCENT, 1),
            new EmptyBorder(6, 15, 6, 15)
        ));
        
        pnlFilterRight.add(btnTabDoanhThu);
        pnlFilterRight.add(btnTabMonAn);
        pnlFilterRight.add(btnExport);

        Component dynamicGlue = new Box.Filler(new Dimension(0,0), new Dimension(0,0), new Dimension(0,0)) {
            @Override
            public Dimension getPreferredSize() {
                Container parent = getParent();
                if (parent == null || parent.getWidth() == 0) return new Dimension(0, 0);
                int pw = parent.getWidth();
                int lw = pnlFilterLeft.getPreferredSize().width;
                int rw = pnlFilterRight.getPreferredSize().width;
                int space = pw - lw - rw - 15;
                return space > 0 ? new Dimension(space, 0) : new Dimension(0, 0);
            }
        };

        pnlFilter.add(pnlFilterLeft);
        pnlFilter.add(dynamicGlue);
        pnlFilter.add(pnlFilterRight);

        Runnable checkDateLogic = () -> {
            if (!cbxLoaiThongKe.getSelectedItem().toString().equals("Ngày")) {
                lblErrorMsg.setText(" ");
                btnExport.setEnabled(true);
                dpTuNgay.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1), BorderFactory.createEmptyBorder(2, 2, 2, 2)));
                dpDenNgay.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1), BorderFactory.createEmptyBorder(2, 2, 2, 2)));
                return;
            }
            
            boolean hasError = false;
            String errorMsg = " ";
            
            String textTu = ((JTextField) dpTuNgay.getDateEditor().getUiComponent()).getText();
            String textDen = ((JTextField) dpDenNgay.getDateEditor().getUiComponent()).getText();
            
            java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(java.time.format.ResolverStyle.STRICT);
            LocalDate tuNgay = null;
            LocalDate denNgay = null;
            
            try {
                if (textTu != null && !textTu.trim().isEmpty()) tuNgay = LocalDate.parse(textTu, fmt);
                else hasError = true;
            } catch (Exception ex) {
                hasError = true;
                errorMsg = "Sai định dạng 'Từ ngày' (dd/MM/yyyy)!";
            }
            
            if (!hasError) {
                try {
                    if (textDen != null && !textDen.trim().isEmpty()) denNgay = LocalDate.parse(textDen, fmt);
                    else hasError = true;
                } catch (Exception ex) {
                    hasError = true;
                    errorMsg = "Sai định dạng 'Đến ngày' (dd/MM/yyyy)!";
                }
            }
            
            if (!hasError && tuNgay != null && denNgay != null) {
                if (tuNgay.isAfter(denNgay)) {
                    hasError = true;
                    errorMsg = "'Từ ngày' không được lớn hơn 'Đến ngày'!";
                } else if (java.time.temporal.ChronoUnit.DAYS.between(tuNgay, denNgay) > 30) {
                    hasError = true;
                    errorMsg = "Chỉ được chọn tối đa 30 ngày!";
                }
            }
            
            if (hasError) {
                if (errorMsg.trim().isEmpty()) errorMsg = "Vui lòng nhập đầy đủ ngày hợp lệ!";
                lblErrorMsg.setText(errorMsg);
                dpTuNgay.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.RED, 1), BorderFactory.createEmptyBorder(2, 2, 2, 2)));
                dpDenNgay.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.RED, 1), BorderFactory.createEmptyBorder(2, 2, 2, 2)));
                btnExport.setEnabled(false);
            } else {
                lblErrorMsg.setText(" ");
                dpTuNgay.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1), BorderFactory.createEmptyBorder(2, 2, 2, 2)));
                dpDenNgay.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1), BorderFactory.createEmptyBorder(2, 2, 2, 2)));
                btnExport.setEnabled(true);
                
                // Real-time update
                handleFilterData();
            }
        };

        javax.swing.event.DocumentListener docListener = new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { checkDateLogic.run(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { checkDateLogic.run(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { checkDateLogic.run(); }
        };
        ((JTextField) dpTuNgay.getDateEditor().getUiComponent()).getDocument().addDocumentListener(docListener);
        ((JTextField) dpDenNgay.getDateEditor().getUiComponent()).getDocument().addDocumentListener(docListener);
        dpTuNgay.addPropertyChangeListener(e -> { if ("date".equals(e.getPropertyName())) checkDateLogic.run(); });
        dpDenNgay.addPropertyChangeListener(e -> { if ("date".equals(e.getPropertyName())) checkDateLogic.run(); });

        // --- EVENTS ---
        Runnable updateFilterUI = () -> {
            String loai = cbxLoaiThongKe.getSelectedItem().toString();
            pnlNgay.setVisible(loai.equals("Ngày"));
            checkDateLogic.run();
            
            pnlThang.setVisible(loai.equals("Tháng"));
            pnlQuy.setVisible(loai.equals("Quý"));
            
            if (currentTab.equals("DOANH_THU")) {
                cbxThang.setVisible(false); // Chỉ hiển thị chọn Năm
                cbxQuy.setVisible(false); // Chỉ hiển thị chọn Năm
                pnlThang.getComponent(0).setVisible(false); // Ẩn label "Tháng:"
                pnlQuy.getComponent(0).setVisible(false); // Ẩn label "Quý:"
            } else {
                cbxThang.setVisible(true);
                cbxQuy.setVisible(true);
                pnlThang.getComponent(0).setVisible(true);
                pnlQuy.getComponent(0).setVisible(true);
            }
            pnlFilter.revalidate(); pnlFilter.repaint();
        };

        cbxLoaiThongKe.addActionListener(e -> { updateFilterUI.run(); handleFilterData(); });
        cbxThang.addActionListener(e -> handleFilterData());
        cbxNamThang.addActionListener(e -> handleFilterData());
        cbxQuy.addActionListener(e -> handleFilterData());
        cbxNamQuy.addActionListener(e -> handleFilterData());

        btnTabDoanhThu.addActionListener(e -> {
            currentTab = "DOANH_THU";
            btnTabDoanhThu.setBackground(TEXT_DARK);
            btnTabDoanhThu.setForeground(Color.WHITE);
            btnTabMonAn.setBackground(Color.WHITE);
            btnTabMonAn.setForeground(TEXT_DARK);
            chartCardLayout.show(pnlChartsContainer, "DOANH_THU");
            updateFilterUI.run();
            handleFilterData();
        });

        btnTabMonAn.addActionListener(e -> {
            currentTab = "MON_AN";
            btnTabMonAn.setBackground(TEXT_DARK);
            btnTabMonAn.setForeground(Color.WHITE);
            btnTabDoanhThu.setBackground(Color.WHITE);
            btnTabDoanhThu.setForeground(TEXT_DARK);
            chartCardLayout.show(pnlChartsContainer, "MON_AN");
            updateFilterUI.run();
            handleFilterData();
        });

        btnExport.addActionListener(e -> exportToExcel());

        updateFilterUI.run(); // Init state

        JPanel pnlTitleWrapper = new JPanel(new BorderLayout());
        pnlTitleWrapper.setOpaque(false);
        pnlTitleWrapper.add(pnlFilter, BorderLayout.CENTER);
        pnlTitleWrapper.add(lblErrorMsg, BorderLayout.SOUTH); // Đưa thông báo lỗi xuống dưới cùng để không lệch form
        pnlTop.add(pnlTitleWrapper, BorderLayout.CENTER);

        return pnlTop;
    }

    private JButton createTabButton(String text, boolean active) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        if (active) {
            btn.setBackground(TEXT_DARK);
            btn.setForeground(Color.WHITE);
        } else {
            btn.setBackground(Color.WHITE);
            btn.setForeground(TEXT_DARK);
        }
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(TEXT_DARK, 1),
            new EmptyBorder(6, 15, 6, 15)
        ));
        return btn;
    }

    private JDateChooser createModernDateChooser() {
        JDateChooser dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("dd/MM/yyyy");
        dateChooser.setPreferredSize(new Dimension(140, 32));
        dateChooser.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        // Tùy chỉnh editor (ô nhập text)
        JTextField editor = (JTextField) dateChooser.getDateEditor().getUiComponent();
        editor.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        editor.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        editor.setBackground(Color.WHITE);
        
        // Tùy chỉnh nút bấm (calendar icon)
        JButton btn = dateChooser.getCalendarButton();
        btn.setIcon(null);
        btn.setText("📅"); 
        btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        btn.setBackground(Color.WHITE);
        btn.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        
        // Cập nhật viền cho toàn bộ JDateChooser
        dateChooser.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(2, 2, 2, 2)
        ));
        dateChooser.setBackground(Color.WHITE);
        
        return dateChooser;
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(new Color(71, 85, 105));
        return lbl;
    }

    // ==============================================================
    // 2. KHUNG KPI TỔNG QUAN
    // ==============================================================
    private JPanel createKPICardsPanel() {
        JPanel pnlCards = new JPanel(new GridLayout(1, 3, 25, 0));
        pnlCards.setOpaque(false);
        pnlCards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120)); 
        
        JPanel card1 = createSingleKPI("DOANH THU HÔM NAY", "0 đ", new Color(254, 243, 199), new Color(245, 158, 11));
        lblTongDoanhThu = (JLabel) card1.getClientProperty("valLabel");
        
        JPanel card2 = createSingleKPI("MÓN BÁN CHẠY HÔM NAY", "Chưa có", new Color(209, 250, 229), new Color(16, 185, 129));
        lblTongDon = (JLabel) card2.getClientProperty("valLabel");
        lblTongDon.setFont(new Font("Segoe UI", Font.BOLD, 20)); // Giảm font để hiện tên món ăn
        
        JPanel card3 = createSingleKPI("SỐ MÓN ĐANG BÁN", "0", new Color(219, 234, 254), new Color(59, 130, 246));
        lblTrungBinhDon = (JLabel) card3.getClientProperty("valLabel");

        pnlCards.add(card1);
        pnlCards.add(card2);
        pnlCards.add(card3);

        return pnlCards;
    }

    private JPanel createSingleKPI(String title, String value, Color iconBg, Color iconColor) {
        JPanel card = new ModernPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel pnlText = new JPanel(new GridLayout(2, 1, 0, 5));
        pnlText.setOpaque(false);
        
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(new Color(100, 116, 139));

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblValue.setForeground(TEXT_DARK);
        
        pnlText.add(lblTitle);
        pnlText.add(lblValue);

        // Giả lập Icon vuông bên góc phải
        JPanel pnlIcon = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(iconBg);
                g2.fillRoundRect(0, 0, 50, 50, 15, 15);
                g2.setColor(iconColor);
                g2.fillOval(20, 20, 10, 10); // Vẽ cái chấm tròn giả làm icon
                g2.dispose();
            }
        };
        pnlIcon.setOpaque(false);
        pnlIcon.setPreferredSize(new Dimension(50, 50));

        card.add(pnlText, BorderLayout.CENTER);
        card.add(pnlIcon, BorderLayout.EAST);
        
        card.putClientProperty("valLabel", lblValue); // Lưu tham chiếu để update
        return card;
    }

    private JPanel createChartWrapper(String title, JComponent chartPanel) {
        JPanel card = new ModernPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(20, 20, 20, 20));
        card.setPreferredSize(new Dimension(400, 350));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(TEXT_DARK);
        lblTitle.setBorder(new EmptyBorder(0, 0, 15, 0));
        card.add(lblTitle, BorderLayout.NORTH);
        
        card.add(chartPanel, BorderLayout.CENTER);
        return card;
    }

    // ==============================================================
    // LOGIC XỬ LÝ LẤY DỮ LIỆU TỪ DAO
    // ==============================================================
    private void handleFilterData() {
        if (lblTongDoanhThu == null) return; // UI chưa khởi tạo xong thì bỏ qua
        
        try {
            String loaiThongKe = cbxLoaiThongKe.getSelectedItem().toString();
            LocalDate tuNgay = null;
            LocalDate denNgay = null;

            if (loaiThongKe.equals("Ngày")) {
                if (!btnExport.isEnabled()) return;
                
                java.util.Date dTu = dpTuNgay.getDate();
                java.util.Date dDen = dpDenNgay.getDate();
                if (dTu == null || dDen == null) {
                    JOptionPane.showMessageDialog(this, "Vui lòng chọn ngày hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                tuNgay = new java.sql.Date(dTu.getTime()).toLocalDate();
                denNgay = new java.sql.Date(dDen.getTime()).toLocalDate();
            } 
            else if (loaiThongKe.equals("Tháng")) {
                int y = (int) cbxNamThang.getSelectedItem();
                if (currentTab.equals("DOANH_THU")) {
                    tuNgay = LocalDate.of(y, 1, 1);
                    denNgay = LocalDate.of(y, 12, 31);
                } else {
                    int m = (int) cbxThang.getSelectedItem();
                    tuNgay = LocalDate.of(y, m, 1);
                    denNgay = YearMonth.of(y, m).atEndOfMonth();
                }
            } 
            else if (loaiThongKe.equals("Quý")) {
                int y = (int) cbxNamQuy.getSelectedItem();
                if (currentTab.equals("DOANH_THU")) {
                    tuNgay = LocalDate.of(y, 1, 1);
                    denNgay = LocalDate.of(y, 12, 31);
                } else {
                    int q = (int) cbxQuy.getSelectedItem();
                    tuNgay = LocalDate.of(y, (q - 1) * 3 + 1, 1);
                    denNgay = tuNgay.plusMonths(2).withDayOfMonth(tuNgay.plusMonths(2).lengthOfMonth());
                }
            }

            // Kiểm tra tính hợp lệ
            if (tuNgay.isAfter(denNgay)) {
                JOptionPane.showMessageDialog(this, "Lỗi: 'Từ ngày' không được lớn hơn 'Đến ngày'!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Gọi DAO chung cho KPI
            Object[] kpi = thongKeDAO.getKPIHomNay();

            // Cập nhật giao diện KPI
            lblTongDoanhThu.setText(dfMoney.format((Double) kpi[0]));
            String monBanChay = (String) kpi[1];
            if (monBanChay == null) monBanChay = "Chưa có";
            lblTongDon.setText(monBanChay);
            lblTrungBinhDon.setText(String.valueOf((Integer) kpi[2]));

            // Cập nhật biểu đồ theo Tab
            if (currentTab.equals("DOANH_THU")) {
                Map<String, Double> lineData = thongKeDAO.getDoanhThuBieuDo(tuNgay, denNgay, loaiThongKe);
                lineChartArea.updateData(lineData);
            } else {
                java.util.List<Object[]> tableData = thongKeDAO.getThongKeMonAnBang(tuNgay, denNgay);
                Map<String, Double> pieData = new java.util.LinkedHashMap<>();
                int pieCount = 0;
                for (Object[] row : tableData) {
                    if (pieCount >= 5) break;
                    pieData.put(row[1].toString(), Double.parseDouble(row[2].toString()));
                    pieCount++;
                }
                
                modelMonAn.setRowCount(0);
                int stt = 1;
                for (Object[] row : tableData) {
                    modelMonAn.addRow(new Object[]{stt++, row[0], row[1], row[2]});
                }
                
                pieChartArea.updateData(pieData);
            }

        } catch (java.time.format.DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Định dạng ngày không hợp lệ! Vui lòng nhập chuẩn dd/MM/yyyy", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // ==============================================================
    // LOGIC XUẤT EXCEL
    // ==============================================================
    private void exportToExcel() {
        try {
            String userHome = System.getProperty("user.home");
            JFileChooser fileChooser = new JFileChooser(new File(userHome, "Downloads"));
            fileChooser.setDialogTitle("Chọn nơi lưu báo cáo Excel");
            
            // Tạo tên file mặc định
            String loaiThongKe = cbxLoaiThongKe.getSelectedItem().toString();
            String defaultFileName = "Bao_cao_thong_ke.xlsx";
            String titleThoiGian = "";
            
            if (loaiThongKe.equals("Ngày")) {
                java.util.Date dTu = dpTuNgay.getDate();
                java.util.Date dDen = dpDenNgay.getDate();
                if (dTu != null && dDen != null) {
                    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                    String tu = dTu.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate().format(fmt);
                    String den = dDen.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate().format(fmt);
                    defaultFileName = "Bao_cao_thong_ke_" + tu + "_den_" + den + ".xlsx";
                    titleThoiGian = "Từ ngày: " + tu.replace("-", "/") + " đến ngày: " + den.replace("-", "/");
                }
            } else if (loaiThongKe.equals("Tháng")) {
                int y = (int) cbxNamThang.getSelectedItem();
                int m = (int) cbxThang.getSelectedItem();
                defaultFileName = "Bao_cao_thong_ke_thang_" + m + "_" + y + ".xlsx";
                titleThoiGian = "Tháng " + m + " năm " + y;
            } else if (loaiThongKe.equals("Quý")) {
                int y = (int) cbxNamQuy.getSelectedItem();
                int q = (int) cbxQuy.getSelectedItem();
                defaultFileName = "Bao_cao_thong_ke_quy_" + q + "_" + y + ".xlsx";
                titleThoiGian = "Quý " + q + " năm " + y;
            }
            
            fileChooser.setSelectedFile(new File(defaultFileName));
            int userSelection = fileChooser.showSaveDialog(this);
            
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                if (!fileToSave.getName().toLowerCase().endsWith(".xlsx")) {
                    fileToSave = new File(fileToSave.getParentFile(), fileToSave.getName() + ".xlsx");
                }
                
                Workbook workbook = new XSSFWorkbook();
                
                // Style cho Header
                CellStyle headerStyle = workbook.createCellStyle();
                org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerFont.setFontHeightInPoints((short) 12);
                headerStyle.setFont(headerFont);
                headerStyle.setAlignment(HorizontalAlignment.CENTER);
                headerStyle.setBorderBottom(BorderStyle.THIN);
                headerStyle.setBorderTop(BorderStyle.THIN);
                headerStyle.setBorderLeft(BorderStyle.THIN);
                headerStyle.setBorderRight(BorderStyle.THIN);
                
                // Style cho Data
                CellStyle dataStyle = workbook.createCellStyle();
                dataStyle.setBorderBottom(BorderStyle.THIN);
                dataStyle.setBorderTop(BorderStyle.THIN);
                dataStyle.setBorderLeft(BorderStyle.THIN);
                dataStyle.setBorderRight(BorderStyle.THIN);

                CellStyle moneyStyle = workbook.createCellStyle();
                moneyStyle.cloneStyleFrom(dataStyle);
                DataFormat format = workbook.createDataFormat();
                moneyStyle.setDataFormat(format.getFormat("#,##0"));
                
                CellStyle totalMoneyStyle = workbook.createCellStyle();
                totalMoneyStyle.cloneStyleFrom(moneyStyle);
                totalMoneyStyle.setFont(headerFont);
                
                // --- SHEET 1: DOANH THU ---
                Sheet sheetDT = workbook.createSheet("Doanh Thu");
                
                Row rowTitle = sheetDT.createRow(0);
                Cell cellTitle = rowTitle.createCell(0);
                cellTitle.setCellValue("BÁO CÁO THỐNG KÊ DOANH THU");
                
                Row rowTime = sheetDT.createRow(1);
                rowTime.createCell(0).setCellValue("Thời gian: " + titleThoiGian);
                
                Row rowExport = sheetDT.createRow(2);
                rowExport.createCell(0).setCellValue("Ngày xuất báo cáo: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                
                Row rowHeader = sheetDT.createRow(4);
                String[] dtHeaders = {"STT", "Thời gian", "Doanh thu (VNĐ)"};
                for (int i = 0; i < dtHeaders.length; i++) {
                    Cell c = rowHeader.createCell(i);
                    c.setCellValue(dtHeaders[i]);
                    c.setCellStyle(headerStyle);
                }
                
                // LẤY DỮ LIỆU TRỰC TIẾP TỪ DAO ĐỂ TRÁNH LỖI TAB BỊ ẨN KHÔNG CÓ DỮ LIỆU
                LocalDate tuNgay_DT = null, denNgay_DT = null;
                LocalDate tuNgay_MA = null, denNgay_MA = null;
                if (loaiThongKe.equals("Ngày")) {
                    tuNgay_DT = new java.sql.Date(dpTuNgay.getDate().getTime()).toLocalDate();
                    denNgay_DT = new java.sql.Date(dpDenNgay.getDate().getTime()).toLocalDate();
                    tuNgay_MA = tuNgay_DT; denNgay_MA = denNgay_DT;
                } else if (loaiThongKe.equals("Tháng")) {
                    int y = (int) cbxNamThang.getSelectedItem();
                    int m = (int) cbxThang.getSelectedItem();
                    tuNgay_DT = LocalDate.of(y, 1, 1);
                    denNgay_DT = LocalDate.of(y, 12, 31);
                    tuNgay_MA = LocalDate.of(y, m, 1);
                    denNgay_MA = YearMonth.of(y, m).atEndOfMonth();
                } else if (loaiThongKe.equals("Quý")) {
                    int y = (int) cbxNamQuy.getSelectedItem();
                    int q = (int) cbxQuy.getSelectedItem();
                    tuNgay_DT = LocalDate.of(y, 1, 1);
                    denNgay_DT = LocalDate.of(y, 12, 31);
                    tuNgay_MA = LocalDate.of(y, (q - 1) * 3 + 1, 1);
                    denNgay_MA = tuNgay_MA.plusMonths(2).withDayOfMonth(tuNgay_MA.plusMonths(2).lengthOfMonth());
                }
                
                Map<String, Double> dataDoanhThu = thongKeDAO.getDoanhThuBieuDo(tuNgay_DT, denNgay_DT, loaiThongKe);
                java.util.List<Object[]> tableData = thongKeDAO.getThongKeMonAnBang(tuNgay_MA, denNgay_MA);
                Map<String, Double> pieData = thongKeDAO.getTopMonAnBanChay(tuNgay_MA, denNgay_MA);
                
                int rowIdx = 5;
                int stt = 1;
                double tongDoanhThu = 0;
                if (dataDoanhThu != null) {
                    for (Map.Entry<String, Double> entry : dataDoanhThu.entrySet()) {
                        Row r = sheetDT.createRow(rowIdx++);
                        Cell c0 = r.createCell(0); c0.setCellValue(stt++); c0.setCellStyle(dataStyle);
                        Cell c1 = r.createCell(1); c1.setCellValue(entry.getKey()); c1.setCellStyle(dataStyle);
                        Cell c2 = r.createCell(2); c2.setCellValue(entry.getValue()); c2.setCellStyle(moneyStyle);
                        tongDoanhThu += entry.getValue();
                    }
                    
                    // Thêm dòng Tổng cộng
                    Row rTotal = sheetDT.createRow(rowIdx++);
                    Cell cTotal0 = rTotal.createCell(0);
                    cTotal0.setCellValue("Tổng cộng");
                    cTotal0.setCellStyle(headerStyle);
                    
                    Cell cTotal1 = rTotal.createCell(1);
                    cTotal1.setCellStyle(headerStyle);
                    
                    Cell cTotal2 = rTotal.createCell(2);
                    cTotal2.setCellValue(tongDoanhThu);
                    cTotal2.setCellStyle(totalMoneyStyle);
                    
                    // Gộp 2 ô STT và Thời gian lại
                    sheetDT.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(rowIdx - 1, rowIdx - 1, 0, 1));
                }
                
                for(int i=0; i<3; i++) sheetDT.autoSizeColumn(i);
                
                // Vẽ Biểu đồ Cột (Bar Chart) cho Doanh Thu và chèn vào Excel
                if (dataDoanhThu != null && !dataDoanhThu.isEmpty()) {
                    BarChartDrawArea barChart = new BarChartDrawArea();
                    barChart.setSize(600, 350);
                    barChart.updateData(dataDoanhThu);
                    
                    BufferedImage imgBar = new BufferedImage(600, 350, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2dBar = imgBar.createGraphics();
                    g2dBar.setColor(Color.WHITE);
                    g2dBar.fillRect(0, 0, 600, 350); // Nền trắng
                    barChart.paint(g2dBar);
                    g2dBar.dispose();
                    
                    ByteArrayOutputStream baosBar = new ByteArrayOutputStream();
                    ImageIO.write(imgBar, "png", baosBar);
                    int picIdxBar = workbook.addPicture(baosBar.toByteArray(), Workbook.PICTURE_TYPE_PNG);
                    
                    CreationHelper helperBar = workbook.getCreationHelper();
                    Drawing<?> drawingBar = sheetDT.createDrawingPatriarch();
                    ClientAnchor anchorBar = helperBar.createClientAnchor();
                    
                    anchorBar.setCol1(4); 
                    anchorBar.setRow1(0); 
                    Picture pictBar = drawingBar.createPicture(anchorBar, picIdxBar);
                    pictBar.resize();
                }
                
                // --- SHEET 2: MÓN ĂN ---
                Sheet sheetMonAn = workbook.createSheet("Món Ăn");
                
                Row rowTitleMA = sheetMonAn.createRow(0);
                Cell cellTitleMA = rowTitleMA.createCell(0);
                cellTitleMA.setCellValue("BÁO CÁO THỐNG KÊ MÓN ĂN");
                
                Row rowTimeMA = sheetMonAn.createRow(1);
                rowTimeMA.createCell(0).setCellValue("Thời gian: " + titleThoiGian);
                
                Row rowExportMA = sheetMonAn.createRow(2);
                rowExportMA.createCell(0).setCellValue("Ngày xuất báo cáo: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                
                Row rowHeaderMA = sheetMonAn.createRow(4);
                String[] maHeaders = {"STT", "Mã món", "Tên món", "Số lượng bán"};
                for (int i = 0; i < maHeaders.length; i++) {
                    Cell c = rowHeaderMA.createCell(i);
                    c.setCellValue(maHeaders[i]);
                    c.setCellStyle(headerStyle);
                }
                
                int rowIdxMA = 5;
                int sttMA = 1;
                if (tableData != null) {
                    for (Object[] row : tableData) {
                        Row r = sheetMonAn.createRow(rowIdxMA++);
                        Cell c0 = r.createCell(0); c0.setCellValue(sttMA++); c0.setCellStyle(dataStyle);
                        Cell c1 = r.createCell(1); c1.setCellValue(row[0].toString()); c1.setCellStyle(dataStyle);
                        Cell c2 = r.createCell(2); c2.setCellValue(row[1].toString()); c2.setCellStyle(dataStyle);
                        Cell c3 = r.createCell(3); c3.setCellValue(Double.parseDouble(row[2].toString())); c3.setCellStyle(dataStyle);
                    }
                }
                for(int i=0; i<4; i++) sheetMonAn.autoSizeColumn(i);
                
                if (pieData != null && !pieData.isEmpty()) {
                    PieChartDrawArea tempPie = new PieChartDrawArea();
                    tempPie.setSize(450, 350);
                    tempPie.updateData(pieData);
                    
                    BufferedImage img = new BufferedImage(450, 350, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2d = img.createGraphics();
                    tempPie.paint(g2d);
                    g2d.dispose();
                    
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(img, "png", baos);
                    int pictureIdx = workbook.addPicture(baos.toByteArray(), Workbook.PICTURE_TYPE_PNG);
                    
                    CreationHelper helper = workbook.getCreationHelper();
                    Drawing<?> drawing = sheetMonAn.createDrawingPatriarch();
                    ClientAnchor anchor = helper.createClientAnchor();
                    
                    anchor.setCol1(5); 
                    anchor.setRow1(0); 
                    Picture pict = drawing.createPicture(anchor, pictureIdx);
                    pict.resize(); 
                }
                
                try (FileOutputStream fos = new FileOutputStream(fileToSave)) {
                    workbook.write(fos);
                }
                workbook.close();
                
                JOptionPane.showMessageDialog(this, "Xuất báo cáo Excel thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                Desktop.getDesktop().open(fileToSave);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi xuất Excel: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==============================================================
    // CLASS VẼ BIỂU ĐỒ ĐƯỜNG BẰNG JAVA 2D GRAPHICS
    // ==============================================================
    class LineChartDrawArea extends JPanel {
        private Map<String, Double> data;
        private int hoverIndex = -1;

        public LineChartDrawArea() { setOpaque(false); }
        
        public Map<String, Double> getCurrentData() { return data; }

        public void updateData(Map<String, Double> newData) {
            this.data = newData;
            this.hoverIndex = -1;
            repaint();
            
            for(java.awt.event.MouseMotionListener l : getMouseMotionListeners()) removeMouseMotionListener(l);
            for(java.awt.event.MouseListener l : getMouseListeners()) removeMouseListener(l);

            addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    if (data == null || data.isEmpty()) return;
                    int paddingX = 85; 
                    int width = getWidth() - paddingX - 40; 
                    int n = data.size();
                    if (n == 0) return;
                    
                    int oldHover = hoverIndex;
                    int step = (n == 1) ? width : width / (n - 1);
                    
                    int minDistance = Integer.MAX_VALUE;
                    int bestIndex = -1;
                    
                    for (int i = 0; i < n; i++) {
                        int pointX = paddingX + i * step;
                        int dist = Math.abs(e.getX() - pointX);
                        if (dist < minDistance) {
                            minDistance = dist;
                            bestIndex = i;
                        }
                    }
                    
                    hoverIndex = bestIndex;
                    if (oldHover != hoverIndex) repaint();
                }
            });

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseExited(MouseEvent e) {
                    if (hoverIndex != -1) {
                        hoverIndex = -1;
                        repaint();
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (data == null || data.isEmpty()) {
                g.setColor(Color.GRAY);
                g.drawString("Không có dữ liệu trong khoảng thời gian này.", 50, getHeight() / 2);
                return;
            }

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int paddingX = 85, paddingY = 35;
            int width = getWidth() - paddingX - 40;
            int height = getHeight() - paddingY * 2 - 20;

            double maxVal = data.values().stream().mapToDouble(Double::doubleValue).max().orElse(1);
            if (maxVal == 0) maxVal = 1;

            // Lưới ngang (Grid lines) và Nhãn trục Y (Oy)
            g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{5f}, 0f));
            for (int k = 0; k <= 5; k++) {
                int yLine = getHeight() - paddingY - (k * height / 5);
                g2.setColor(new Color(230, 230, 230));
                g2.drawLine(paddingX, yLine, paddingX + width, yLine);
                
                g2.setColor(new Color(130, 130, 130));
                g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                String yLabel = dfMoney.format(maxVal * k / 5).replace(" đ", "");
                int lw = g2.getFontMetrics().stringWidth(yLabel);
                g2.drawString(yLabel, paddingX - lw - 10, yLine + 4);
            }

            // Vẽ trục ngang Ox (Solid line)
            g2.setColor(new Color(180, 180, 180));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawLine(paddingX, getHeight() - paddingY, paddingX + width, getHeight() - paddingY);

            int n = data.size();
            int step = (n == 1) ? width : width / (n - 1);
            
            int[] xPoints = new int[n];
            int[] yPoints = new int[n];
            
            int i = 0;
            for (Map.Entry<String, Double> entry : data.entrySet()) {
                double val = entry.getValue();
                xPoints[i] = paddingX + i * step;
                int h = (int) ((val / maxVal) * height);
                if (val > 0 && h < 8) {
                    h = 8; // Đảm bảo đỉnh có doanh thu luôn nhô lên khỏi trục hoành (ít nhất 8px)
                }
                yPoints[i] = getHeight() - paddingY - h;
                i++;
            }

            // Fill màu bên dưới đường (Gradient)
            if (n > 1) {
                int[] xPoly = new int[n + 2];
                int[] yPoly = new int[n + 2];
                System.arraycopy(xPoints, 0, xPoly, 0, n);
                System.arraycopy(yPoints, 0, yPoly, 0, n);
                xPoly[n] = xPoints[n-1];
                yPoly[n] = getHeight() - paddingY;
                xPoly[n+1] = xPoints[0];
                yPoly[n+1] = getHeight() - paddingY;

                GradientPaint gp = new GradientPaint(
                    0, getHeight() - paddingY - height, new Color(59, 130, 246, 120),
                    0, getHeight() - paddingY, new Color(59, 130, 246, 10)
                );
                g2.setPaint(gp);
                g2.fillPolygon(xPoly, yPoly, n + 2);
            }

            // Vẽ đường gấp khúc (Line)
            g2.setColor(BLUE_ACCENT);
            g2.setStroke(new BasicStroke(3f));
            for (int j = 0; j < n - 1; j++) {
                g2.drawLine(xPoints[j], yPoints[j], xPoints[j+1], yPoints[j+1]);
            }

            // Vẽ Điểm, Nhãn và Tooltip
            i = 0;
            int spacePerPoint = (n == 1) ? width : width / (n - 1);
            
            for (Map.Entry<String, Double> entry : data.entrySet()) {
                int px = xPoints[i];
                int py = yPoints[i];

                // Điểm mặc định
                g2.setColor(Color.WHITE);
                g2.fillOval(px - 5, py - 5, 10, 10);
                g2.setColor(BLUE_ACCENT);
                g2.setStroke(new BasicStroke(2f));
                g2.drawOval(px - 5, py - 5, 10, 10);

                // Nhãn X
                g2.setColor(Color.GRAY);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                String label = entry.getKey();
                int lw = g2.getFontMetrics().stringWidth(label);
                
                // Tránh đè chữ
                if (lw > spacePerPoint && n > 12) {
                    int skip = lw / spacePerPoint + 1;
                    if (i % skip == 0) g2.drawString(label, px - lw/2, getHeight() - paddingY + 20);
                } else {
                    g2.drawString(label, px - lw/2, getHeight() - paddingY + 20);
                }

                // Vẽ Crosshair và Tooltip đẹp kiểu Google nếu đang Hover
                if (i == hoverIndex) {
                    // Vẽ Crosshair Line dọc từ trên xuống trục X
                    g2.setColor(new Color(150, 150, 150, 180));
                    Stroke dashed = new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{5f}, 0.0f);
                    g2.setStroke(dashed);
                    g2.drawLine(px, paddingY, px, getHeight() - paddingY);
                    
                    // Vẽ lại điểm cho nổi lên trên Crosshair
                    g2.setColor(Color.WHITE);
                    g2.fillOval(px - 6, py - 6, 12, 12);
                    g2.setColor(new Color(245, 158, 11));
                    g2.setStroke(new BasicStroke(3f));
                    g2.drawOval(px - 6, py - 6, 12, 12);

                    // Khung Tooltip
                    String moneyStr = dfMoney.format(entry.getValue());
                    String dateStr = label;

                    g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    int twMoney = g2.getFontMetrics().stringWidth(moneyStr);
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                    int twDate = g2.getFontMetrics().stringWidth(dateStr);
                    int tw = Math.max(twMoney, twDate);
                    
                    int boxW = tw + 24;
                    int boxH = 46;
                    int boxX = px - boxW/2;
                    int boxY = py - boxH - 15;
                    
                    // Giữ Tooltip không bị tràn màn hình
                    if (boxX < 0) boxX = 5;
                    if (boxX + boxW > getWidth()) boxX = getWidth() - boxW - 5;
                    if (boxY < 0) boxY = py + 15; 

                    g2.setColor(new Color(30, 41, 59, 220)); 
                    g2.fillRoundRect(boxX, boxY, boxW, boxH, 8, 8);
                    
                    // Nhãn Ngày Tháng
                    g2.setColor(new Color(203, 213, 225));
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                    g2.drawString(dateStr, boxX + (boxW - twDate)/2, boxY + 16);
                    
                    // Nhãn Tiền
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    g2.drawString(moneyStr, boxX + (boxW - twMoney)/2, boxY + 36);
                }
                i++;
            }
            g2.dispose();
        }
    }

    // ==============================================================
    // CLASS VẼ BIỂU ĐỒ CỘT (DÀNH CHO EXCEL)
    // ==============================================================
    class BarChartDrawArea extends JPanel {
        private Map<String, Double> data;
        
        public BarChartDrawArea() { setOpaque(false); }

        public void updateData(Map<String, Double> newData) {
            this.data = newData;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (data == null || data.isEmpty()) return;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int paddingX = 70, paddingY = 60; // Tăng paddingY để có chỗ cho chữ nghiêng
            int width = getWidth() - paddingX * 2;
            int height = getHeight() - paddingY * 2 - 20;

            double maxVal = 0;
            for (double v : data.values()) if (v > maxVal) maxVal = v;
            if (maxVal == 0) maxVal = 1;

            // Draw grid lines
            g2.setColor(new Color(220, 220, 220));
            g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{5f}, 0f));
            for (int i = 0; i <= 5; i++) {
                int y = getHeight() - paddingY - 20 - (i * height / 5);
                g2.drawLine(paddingX, y, paddingX + width, y);
                g2.setColor(Color.GRAY);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                String yLabel = dfMoney.format(maxVal * i / 5).replace(" đ", "");
                int lw = g2.getFontMetrics().stringWidth(yLabel);
                g2.drawString(yLabel, paddingX - lw - 10, y + 4);
                g2.setColor(new Color(220, 220, 220));
            }

            int n = data.size();
            int barWidth = Math.min(50, (width / n) - 10);
            if (barWidth < 10) barWidth = 10;
            int step = width / n;

            int i = 0;
            for (Map.Entry<String, Double> entry : data.entrySet()) {
                int x = paddingX + i * step + (step - barWidth) / 2;
                int barHeight = (int) ((entry.getValue() / maxVal) * height);
                int y = getHeight() - paddingY - 20 - barHeight;

                // Gradient Color (Emerald Green)
                GradientPaint gp = new GradientPaint(x, y, new Color(16, 185, 129), x, y + barHeight, new Color(4, 120, 87));
                g2.setPaint(gp);
                g2.fillRect(x, y, barWidth, barHeight);

                // Label X (Xoay nghiêng 45 độ để không bị đè chữ khi có 30 ngày)
                g2.setColor(Color.DARK_GRAY);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                String label = entry.getKey();
                int lw = g2.getFontMetrics().stringWidth(label);
                
                java.awt.geom.AffineTransform oldTrans = g2.getTransform();
                g2.translate(x + barWidth/2, getHeight() - paddingY + 15);
                g2.rotate(-Math.PI / 4); // Xoay -45 độ
                g2.drawString(label, -lw + 5, 0);
                g2.setTransform(oldTrans);

                // Value on top
                if (entry.getValue() > 0) {
                    String vStr = dfMoney.format(entry.getValue()).replace(" đ", "");
                    int vw = g2.getFontMetrics().stringWidth(vStr);
                    g2.setColor(new Color(4, 120, 87));
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                    g2.drawString(vStr, x + barWidth/2 - vw/2, y - 5);
                }
                i++;
            }
            g2.dispose();
        }
    }

    // ==============================================================
    // CLASS VẼ BIỂU ĐỒ TRÒN BẰNG JAVA 2D GRAPHICS
    // ==============================================================
    class PieChartDrawArea extends JPanel {
        private Map<String, Double> data;
        
        public PieChartDrawArea() { setOpaque(false); }

        public void updateData(Map<String, Double> newData) {
            this.data = newData;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (data == null || data.isEmpty()) {
                g.setColor(Color.GRAY);
                g.drawString("Chưa có món ăn nào được bán.", 50, getHeight() / 2);
                return;
            }

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            double total = data.values().stream().mapToDouble(Double::doubleValue).sum();
            if (total == 0) return;

            int width = getWidth();
            int height = getHeight();
            
            int pieSize = Math.min(width / 2, height - 40);
            int pieX = 10;
            int pieY = (height - pieSize) / 2;

            int startAngle = 90; // Bắt đầu từ góc 12h
            int colorIdx = 0;
            
            int legendX = pieX + pieSize + 30;
            int legendY = pieY + 20;

            for (Map.Entry<String, Double> entry : data.entrySet()) {
                double value = entry.getValue();
                int arcAngle = (int) Math.round((value / total) * 360);
                
                Color c = PIE_COLORS[colorIdx % PIE_COLORS.length];
                g2.setColor(c);
                g2.fillArc(pieX, pieY, pieSize, pieSize, startAngle, arcAngle);
                startAngle += arcAngle;

                // Legend Color Box
                g2.fillOval(legendX, legendY - 10, 12, 12);
                
                // Legend Text
                g2.setColor(TEXT_DARK);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                String legendText = entry.getKey() + " (" + dfPercent.format((value / total) * 100) + "%)";
                g2.drawString(legendText, legendX + 20, legendY);
                
                // Số lượng
                g2.setColor(Color.GRAY);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                g2.drawString(Math.round(value) + " phần", legendX + 20, legendY + 15);
                
                legendY += 40;
                colorIdx++;
            }
            g2.dispose();
        }
    }

    class ModernPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            // Đổ bóng mờ nhạt
            g2.setColor(new Color(0, 0, 0, 10));
            g2.fillRoundRect(2, 2, getWidth() - 2, getHeight() - 2, 15, 15);
            
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 15, 15);
            
            // Vẽ viền xám nhạt
            g2.setColor(new Color(226, 232, 240));
            g2.drawRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 15, 15);
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