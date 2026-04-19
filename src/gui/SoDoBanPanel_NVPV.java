package gui;

import dao.BanDAO;
import entity.Ban;
import entity.NhanVien;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class SoDoBanPanel_NVPV extends JPanel {

    private static final long serialVersionUID = 1L;

    // KÍCH HOẠT LẠI CÁC MÀU SẮC ĐỂ HIỂN THỊ KHI TẮT CHẾ ĐỘ LỌC
    private final Color COLOR_TRONG = new Color(40, 167, 69);    // Xanh lá (Trống)
    private final Color COLOR_DUNG = new Color(220, 53, 69);     // Đỏ (Đang sử dụng)
    private final Color COLOR_DAT = new Color(255, 193, 7);      // Vàng (Đã đặt)
    private final Color COLOR_PINK = new Color(255, 182, 193);   
    private final Color TEXT_DARK = new Color(44, 56, 74);       
    
    private BanDAO banDAO;
    private JPanel pnlDanhSachBan;
    private JPanel pnlFloorFilter;
    private JTextField txtSearch;
    private JLabel lblTimeDate;
    private Timer timer;

    private String floorFilter = "Tất cả";
    
    // Nút Bật/Tắt chế độ hiển thị
    private JCheckBox chkChiHienBanCoKhach;

    private NhanVien nhanVien;
    private String maNhanVien; 

    public SoDoBanPanel_NVPV(String maNV) {
        this.maNhanVien = maNV;
        this.nhanVien = new NhanVien(maNV, "Nhân viên Phục vụ");

        banDAO = new BanDAO();
        initUI();
        
        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                SwingUtilities.invokeLater(() -> loadData(txtSearch != null ? txtSearch.getText().trim() : ""));
            }
        });
        
        loadData("");
    }

    private void initUI() {

        setLayout(new BorderLayout(0, 20)); 
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel topWrapper = new JPanel();
        topWrapper.setLayout(new BoxLayout(topWrapper, BoxLayout.Y_AXIS));
        topWrapper.setOpaque(false);

        // 1. Banner Thời Gian
        JPanel timeBanner = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        timeBanner.setBackground(new Color(255, 246, 246));
        timeBanner.putClientProperty("FlatLaf.style", "arc: 15");

        lblTimeDate = new JLabel();
        lblTimeDate.setFont(new Font("Segoe UI", Font.PLAIN, 15)); 
        lblTimeDate.setForeground(Color.BLACK);
        
        Locale localeVI = new Locale("vi", "VN");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("EEEE, 'ngày' dd/MM/yyyy - HH:mm:ss", localeVI);
        timer = new Timer(1000, e -> lblTimeDate.setText(LocalDateTime.now().format(dtf)));
        timer.start();
        
        timeBanner.add(lblTimeDate);
        topWrapper.add(timeBanner);
        topWrapper.add(Box.createRigidArea(new Dimension(0, 20)));

        // 2. Tiêu Đề & Ô Tìm Kiếm
        JPanel titleActionPanel = new JPanel(new BorderLayout());
        titleActionPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("GỌI MÓN");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(TEXT_DARK);
        titleActionPanel.add(lblTitle, BorderLayout.WEST);

        JPanel btnActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        btnActions.setOpaque(false);

        txtSearch = new JTextField(15);
        txtSearch.setPreferredSize(new Dimension(220, 40));
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm tên bàn...");
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { loadData(txtSearch.getText()); }
            public void removeUpdate(DocumentEvent e) { loadData(txtSearch.getText()); }
            public void changedUpdate(DocumentEvent e) { loadData(txtSearch.getText()); }
        });

        btnActions.add(txtSearch);
        titleActionPanel.add(btnActions, BorderLayout.EAST);

        topWrapper.add(titleActionPanel);
        topWrapper.add(Box.createRigidArea(new Dimension(0, 15)));

        // 3. Thanh Lọc Tầng và Checkbox Bật/Tắt
        JPanel pnlFilterBar = new JPanel(new BorderLayout());
        pnlFilterBar.setOpaque(false);

        pnlFloorFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlFloorFilter.setOpaque(false);
        String[] floors = {"Tất cả", "Ngoài trời", "Phòng VIP", "Tầng 1", "Tầng 2"};
        for (String f : floors) {
            pnlFloorFilter.add(createFloorButton(f));
        }

        JPanel pnlStatus = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlStatus.setOpaque(false);
        
        // ==============================================================================
        // ĐÃ NÂNG CẤP: Thay label bằng Checkbox Bật/Tắt để Phục vụ linh hoạt xem sơ đồ
        // ==============================================================================
        chkChiHienBanCoKhach = new JCheckBox(" Chỉ hiển thị các bàn ĐANG CÓ KHÁCH", true);
        chkChiHienBanCoKhach.setFont(new Font("Segoe UI", Font.BOLD, 14));
        chkChiHienBanCoKhach.setForeground(COLOR_DUNG);
        chkChiHienBanCoKhach.setOpaque(false);
        chkChiHienBanCoKhach.setCursor(new Cursor(Cursor.HAND_CURSOR));
        chkChiHienBanCoKhach.setToolTipText("Tắt dấu tick này để xem lại toàn cảnh sơ đồ nhà hàng (Bao gồm bàn trống)");
        
        chkChiHienBanCoKhach.addActionListener(e -> {
            loadData(txtSearch.getText().trim());
        });
        
        pnlStatus.add(chkChiHienBanCoKhach);

        pnlFilterBar.add(pnlFloorFilter, BorderLayout.WEST);
        pnlFilterBar.add(pnlStatus, BorderLayout.EAST);

        topWrapper.add(pnlFilterBar);
        add(topWrapper, BorderLayout.NORTH);

        // 4. Khu vực Lưới Bàn (Fix lỗi kẹp dọc bằng GridBagLayout)
        pnlDanhSachBan = new ScrollablePanel(); 
        pnlDanhSachBan.setLayout(new GridBagLayout()); 
        pnlDanhSachBan.setOpaque(false);
        
        JScrollPane scrollPane = new JScrollPane(pnlDanhSachBan);
        scrollPane.setBorder(null);
        scrollPane.setViewportBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JButton createFloorButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        boolean isActive = text.equals(floorFilter);
        btn.setBackground(isActive ? COLOR_PINK : Color.WHITE);
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.putClientProperty("JButton.buttonType", "roundRect");
        btn.putClientProperty("JButton.arc", 15);
        
        btn.addActionListener(e -> {
            floorFilter = text;
            for (Component c : pnlFloorFilter.getComponents()) {
                if (c instanceof JButton) {
                    JButton otherBtn = (JButton) c;
                    boolean active = otherBtn.getText().equals(floorFilter);
                    otherBtn.setBackground(active ? COLOR_PINK : Color.WHITE);
                }
            }
            loadData(txtSearch.getText());
        });
        return btn;
    }

    public void loadData(String query) {
        pnlDanhSachBan.removeAll();
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        List<Ban> dsAll = banDAO.getAllBan(); 
        
        String[] dsKhuVuc = {"Ngoài trời", "Phòng VIP", "Tầng 1", "Tầng 2"};
        for (String khuVuc : dsKhuVuc) {
            if (!floorFilter.equals("Tất cả") && !floorFilter.equalsIgnoreCase(khuVuc)) continue;
            List<Ban> dsTheoKV = dsAll.stream()
                .filter(b -> {
                    String viTriDB = b.getViTri().trim().toLowerCase();
                    if (khuVuc.equals("Ngoài trời")) return viTriDB.contains("ngoài trời");
                    return viTriDB.equalsIgnoreCase(khuVuc);
                })
                .filter(b -> {
                    String q = query.toLowerCase();
                    return b.getTenBan().toLowerCase().contains(q) || b.getMaBan().toLowerCase().contains(q);
                })
                .filter(b -> {
                    String tt = b.getTrangThai().toLowerCase();
                    // NẾU BẬT CHECKBOX -> CHỈ HIỆN BÀN ĐANG DÙNG
                    if (chkChiHienBanCoKhach.isSelected()) {
                        return tt.contains("dùng") || tt.contains("vụ") || tt.contains("sử dụng");
                    }
                    // NẾU TẮT CHECKBOX -> HIỂN THỊ TẤT CẢ (Để NV xem được toàn cảnh)
                    return true; 
                })
                .sorted((b1, b2) -> Integer.compare(b1.getSoGhe(), b2.getSoGhe()))
                .collect(Collectors.toList());
                
            if (!dsTheoKV.isEmpty()) {
                JPanel pnlKVTitle = new JPanel(new FlowLayout(FlowLayout.LEFT));
                pnlKVTitle.setOpaque(false);
                JLabel lblKV = new JLabel("--- " + khuVuc.toUpperCase() + " ---");
                lblKV.setFont(new Font("Segoe UI", Font.BOLD, 18));
                lblKV.setForeground(new Color(150, 50, 50));
                lblKV.setBorder(new EmptyBorder(10, 10, 5, 0));
                pnlKVTitle.add(lblKV);
                
                gbc.gridy++; 
                pnlDanhSachBan.add(pnlKVTitle, gbc);
                
                JPanel pnlGrid = new JPanel(new WrapLayout(FlowLayout.LEFT, 20, 20));
                pnlGrid.setOpaque(false);
                for (Ban b : dsTheoKV) {
                    pnlGrid.add(taoTheBan(b));
                }
                
                gbc.gridy++; 
                pnlDanhSachBan.add(pnlGrid, gbc);
            }
        }
        
        gbc.gridy++;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        JPanel filler = new JPanel();
        filler.setOpaque(false);
        pnlDanhSachBan.add(filler, gbc);

        pnlDanhSachBan.revalidate(); 
        pnlDanhSachBan.repaint();
    }

    private JPanel taoTheBan(Ban ban) {
        int width = (ban.getSoGhe() <= 4) ? 165 : (ban.getSoGhe() <= 8) ? 260 : 360;

        // Phục hồi lại chức năng màu sắc khi Nhân viên tắt tính năng "Chỉ hiện bàn có khách"
        Color tempBg = COLOR_TRONG; 
        String tt = ban.getTrangThai().toLowerCase();
        if (tt.contains("dùng") || tt.contains("vụ") || tt.contains("sử dụng")) tempBg = COLOR_DUNG;
        else if (tt.contains("đặt")) tempBg = COLOR_DAT;
        final Color bg = tempBg; 

        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(3f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 25, 25);
                g2.dispose();
            }
        };

        card.setLayout(new OverlayLayout(card));
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(width, 140));
        card.setBackground(bg);

        JPanel pnlContent = new JPanel(new BorderLayout());
        pnlContent.setOpaque(false);
        pnlContent.setBorder(new EmptyBorder(5, 5, 5, 5));

        JPanel pnlTopRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        pnlTopRight.setOpaque(false);
        pnlTopRight.setPreferredSize(new Dimension(width, 35));

        if (ban.getMaKhoi() != null && ban.getMaKhoi() > 0) {
            int gId = ban.getMaKhoi();
            JLabel lblGroup = new JLabel(String.valueOf(gId), SwingConstants.CENTER) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(0, 123, 255)); 
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10); 
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            lblGroup.setFont(new Font("Arial", Font.BOLD, 14));
            lblGroup.setForeground(Color.WHITE);
            lblGroup.setPreferredSize(new Dimension(24, 24));
            lblGroup.setToolTipText("Bàn thuộc Khối số " + gId);
            pnlTopRight.add(lblGroup);
        }
        
        pnlContent.add(pnlTopRight, BorderLayout.NORTH);

        JPanel pnlCenter = new JPanel(new GridLayout(2, 1));
        pnlCenter.setOpaque(false);

        JLabel lblTen = new JLabel(ban.getTenBan(), SwingConstants.CENTER);
        lblTen.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTen.setForeground(bg == COLOR_DAT ? Color.BLACK : Color.WHITE);

        JLabel lblSub = new JLabel(ban.getSoGhe() + " ghế - " + ban.getMaBan(), SwingConstants.CENTER);
        lblSub.setForeground(bg == COLOR_DAT ? new Color(0,0,0,150) : new Color(255,255,255,180));

        pnlCenter.add(lblTen);
        pnlCenter.add(lblSub);

        pnlContent.add(pnlCenter, BorderLayout.CENTER);
        card.add(pnlContent);

        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.setToolTipText("Nhấn đúp để gọi món");

        // ===== EVENT DOUBLE CLICK =====
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    // =========================================================================
                    // RÀNG BUỘC: CHỈ CHO PHÉP TẠO ĐƠN NẾU BÀN "ĐANG DÙNG"
                    // =========================================================================
                    String trangThaiBan = ban.getTrangThai().toLowerCase();
                    if (!(trangThaiBan.contains("dùng") || trangThaiBan.contains("vụ") || trangThaiBan.contains("sử dụng"))) {
                        JOptionPane.showMessageDialog(
                                SwingUtilities.getWindowAncestor(card),
                                "Lỗi thao tác!\nPhục vụ chỉ được phép tạo đơn/gọi món cho những bàn ĐANG CÓ KHÁCH.\n\nTrạng thái hiện tại của " + ban.getTenBan() + " là: " + ban.getTrangThai(),
                                "Từ chối quyền truy cập",
                                JOptionPane.ERROR_MESSAGE
                        );
                        return; // Ngăn không cho mở Dialog Gọi Món
                    }

                    Ban banTruyenVao = ban; 
                    
                    if (ban.getMaBanChinh() != null && !ban.getMaBanChinh().equalsIgnoreCase(ban.getMaBan())) {
                        banTruyenVao = new Ban();
                        banTruyenVao.setMaBan(ban.getMaBanChinh());
                        banTruyenVao.setTenBan(ban.getTenBan() + " (Nằm trong Khối " + ban.getMaBanChinh() + ")");
                    }
                    
                    GoiMonDialog dialog = new GoiMonDialog(
                            SwingUtilities.getWindowAncestor(card),
                            banTruyenVao, 
                            nhanVien
                    );
                    dialog.setVisible(true);
                }
            }
        });

        return card;
    }

    class ScrollablePanel extends JPanel implements Scrollable {
        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) { return 20; }
        @Override public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) { return 20; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
    }
}