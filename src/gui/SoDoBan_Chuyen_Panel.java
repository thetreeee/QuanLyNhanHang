package gui;

import dao.BanDAO;
import dao.DonDatBanDAO;
import dao.DonDatMon_DAO;
import entity.Ban;
import entity.DonDatBan;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class SoDoBan_Chuyen_Panel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final Color COLOR_TRONG = new Color(40, 167, 69);   
    private final Color COLOR_DUNG = new Color(220, 53, 69);      
    private final Color COLOR_DAT = new Color(255, 193, 7);      
    private final Color COLOR_PINK = new Color(255, 182, 193);   

    private BanDAO banDAO;
    private DonDatBanDAO donDatBanDAO; 
    private DonDatMon_DAO donDatMonDAO; 
    
    private JPanel pnlDanhSachBan;
    private JScrollPane scrollPaneDanhSach;
    private JPanel pnlFloorFilter;
    private JTextField txtSearch;
    
    private JButton btnXacNhanChuyen;
    private JButton btnHuyChuyen;

    // ==========================================================
    // ĐÃ NÂNG CẤP: Dùng List thay vì biến đơn để hỗ trợ chuyển CẢ KHỐI
    // ==========================================================
    private List<Ban> listBanCu = new ArrayList<>();
    private List<Ban> listBanMoi = new ArrayList<>();
    
    private String floorFilter = "Tất cả";
    private String maNV;
    private List<DonDatBan> dsDonDatHienTai = new ArrayList<>();
    private Timer autoCheckTimer;
    private String previousStateHash = "";

    public SoDoBan_Chuyen_Panel(String maNV) {
        this.maNV = maNV;
        banDAO = new BanDAO();
        donDatBanDAO = new DonDatBanDAO(); 
        donDatMonDAO = new DonDatMon_DAO();
        
        initUI();
        loadData("");

        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                SwingUtilities.invokeLater(() -> {
                    clearSelection(); 
                    if (txtSearch != null) loadData(txtSearch.getText().trim()); 
                    else loadData("");
                });
            }
        });

        autoCheckTimer = new Timer(3000, evt -> {
            List<Ban> currentData = banDAO.getAllBan();
            String currentHash = currentData.stream().map(b -> b.getMaBan() + b.getTrangThai() + b.getMaKhoi() + b.getMaBanChinh()).collect(java.util.stream.Collectors.joining("|"));
            
            if (!currentHash.equals(previousStateHash)) {
                previousStateHash = currentHash;
                loadData(txtSearch != null ? txtSearch.getText().trim() : "");
            }
        });
        autoCheckTimer.start();
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 20)); 
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel topWrapper = new JPanel();
        topWrapper.setLayout(new BoxLayout(topWrapper, BoxLayout.Y_AXIS));
        topWrapper.setOpaque(false);

        // --- HƯỚNG DẪN DÀNH CHO NHÂN VIÊN ---
        JPanel pnlHuongDan = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        pnlHuongDan.setBackground(new Color(255, 245, 230));
        pnlHuongDan.putClientProperty("FlatLaf.style", "arc: 15");
        JLabel lblHuongDan = new JLabel("CHUYỂN NHÓM (CẢ KHỐI): Chọn 1 Bàn cũ (Hệ thống tự quét cả khối) ➔ Chọn NHIỀU bàn trống mới ➔ Bấm Xác nhận.");
        lblHuongDan.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblHuongDan.setForeground(new Color(220, 100, 0));
        pnlHuongDan.add(lblHuongDan);
        topWrapper.add(pnlHuongDan);
        topWrapper.add(Box.createRigidArea(new Dimension(0, 15)));

        JPanel titleActionPanel = new JPanel(new BorderLayout());
        titleActionPanel.setOpaque(false);
        JPanel pnlLeftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        pnlLeftActions.setOpaque(false);
        
        btnXacNhanChuyen = createSpecialButton("XÁC NHẬN CHUYỂN BÀN");
        btnXacNhanChuyen.setBackground(new Color(220, 53, 69)); 
        btnXacNhanChuyen.setForeground(Color.WHITE);
        btnXacNhanChuyen.addActionListener(evt -> xuLyXacNhanChuyenBan());
        pnlLeftActions.add(btnXacNhanChuyen);
        
        btnHuyChuyen = createSpecialButton("Hủy / Chọn lại");
        btnHuyChuyen.setBackground(Color.GRAY); 
        btnHuyChuyen.setForeground(Color.WHITE);
        btnHuyChuyen.addActionListener(evt -> clearSelection());
        pnlLeftActions.add(btnHuyChuyen);
        
        titleActionPanel.add(pnlLeftActions, BorderLayout.WEST);

        JPanel btnActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        btnActions.setOpaque(false);
        txtSearch = new JTextField(20); txtSearch.setPreferredSize(new Dimension(250, 40));
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

        pnlFloorFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlFloorFilter.setOpaque(false);
        String[] floors = {"Tất cả", "Ngoài trời", "Phòng VIP", "Tầng 1", "Tầng 2"};
        for (String f : floors) pnlFloorFilter.add(createFloorButton(f));
        topWrapper.add(pnlFloorFilter);

        add(topWrapper, BorderLayout.NORTH);

        pnlDanhSachBan = new ScrollablePanel(); 
        pnlDanhSachBan.setLayout(new GridBagLayout()); 
        pnlDanhSachBan.setOpaque(false);
        
        scrollPaneDanhSach = new JScrollPane(pnlDanhSachBan); 
        scrollPaneDanhSach.setBorder(null);
        scrollPaneDanhSach.setViewportBorder(null);
        scrollPaneDanhSach.getViewport().setBackground(Color.WHITE);
        scrollPaneDanhSach.getVerticalScrollBar().setUnitIncrement(30); 
        scrollPaneDanhSach.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPaneDanhSach, BorderLayout.CENTER);
    }

    private void clearSelection() {
        listBanCu.clear();
        listBanMoi.clear();
        if (pnlDanhSachBan != null) pnlDanhSachBan.repaint();
        if (scrollPaneDanhSach != null) scrollPaneDanhSach.repaint();
    }

    // =========================================================================
    // XỬ LÝ NGHIỆP VỤ CHUYỂN BÀN THÔNG MINH (CHUYỂN 1->1 HOẶC KHỐI->KHỐI)
    // =========================================================================
 // =========================================================================
    // XỬ LÝ NGHIỆP VỤ CHUYỂN BÀN THÔNG MINH (CHUYỂN 1->1 HOẶC KHỐI->KHỐI)
    // =========================================================================
    private void xuLyXacNhanChuyenBan() {
        if (listBanCu.isEmpty() || listBanMoi.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng tick chọn ít nhất 1 Bàn Đang dùng và 1 Bàn Trống!", "Chưa đủ dữ kiện", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // ==============================================================
        // RÀO CHẮN KIỂM TRA SỨC CHỨA
        // ==============================================================
        int tongGheCu = listBanCu.stream().mapToInt(Ban::getSoGhe).sum();
        int tongGheMoi = listBanMoi.stream().mapToInt(Ban::getSoGhe).sum();

        if (tongGheMoi < tongGheCu) {
            int warn = JOptionPane.showConfirmDialog(this, 
                "CẢNH BÁO THIẾU CHỖ:\nSức chứa của Bàn Mới (" + tongGheMoi + " ghế) đang NHỎ HƠN Bàn Cũ (" + tongGheCu + " ghế).\n\n" +
                "Khách có thể không đủ chỗ ngồi. Bạn vẫn chắc chắn muốn chuyển?", 
                "Cảnh báo sức chứa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (warn != JOptionPane.YES_OPTION) return; 
        } 
        else if (tongGheMoi > tongGheCu + 4) { 
            int warn = JOptionPane.showConfirmDialog(this, 
                "CẢNH BÁO LÃNG PHÍ BÀN:\nSức chứa của Bàn Mới (" + tongGheMoi + " ghế) đang DƯ THỪA quá nhiều so với Bàn Cũ (" + tongGheCu + " ghế).\n\n" +
                "Điều này gây lãng phí không gian nhà hàng. Bạn vẫn chắc chắn muốn chuyển?", 
                "Cảnh báo sức chứa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (warn != JOptionPane.YES_OPTION) return; 
        }
        // ==============================================================

        String sourceStr = listBanCu.size() > 1 ? "Nhóm Khối số " + listBanCu.get(0).getMaKhoi() + " (" + listBanCu.size() + " bàn)" : "Bàn " + listBanCu.get(0).getTenBan();
        String targetStr = listBanMoi.size() > 1 ? "Ghép khối " + listBanMoi.size() + " bàn mới" : "Bàn " + listBanMoi.get(0).getTenBan();

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Xác nhận dời khách và TOÀN BỘ ĐƠN HÀNG:\n\n" +
            "Từ: " + sourceStr + "\n" +
            "Đến: " + targetStr + "\n\n" +
            "Bạn có chắc chắn muốn thực hiện thao tác này?", 
            "Xác nhận Chuyển bàn", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // Xác định Mã Bàn chứa Hóa Đơn gốc
                String oldMaBanChinh = (listBanCu.get(0).getMaBanChinh() != null && !listBanCu.get(0).getMaBanChinh().isEmpty()) 
                                        ? listBanCu.get(0).getMaBanChinh() : listBanCu.get(0).getMaBan();
                
                // Xác định Mã Bàn sẽ nhận Hóa Đơn mới
                String newMaBanChinh = listBanMoi.get(0).getMaBan();

                // 1. DỜI PHIẾU BẾP (HÓA ĐƠN) SANG BÀN MỚI
                boolean chuyenBillThanhCong = donDatMonDAO.chuyenBan(oldMaBanChinh, newMaBanChinh);
                
                if (chuyenBillThanhCong) {
                    
                    // =========================================================================
                    // ĐÃ FIX LỖI "BÓNG MA": DỜI LUÔN PHIẾU ĐẶT BÀN SANG BÀN MỚI
                    // =========================================================================
                    dsDonDatHienTai = donDatBanDAO.getAllDonDat();
                    for (DonDatBan don : dsDonDatHienTai) {
                        // Tìm xem bàn cũ có đang dính Đơn đặt bàn nào của ông A (trạng thái Checked-in) không
                        if (don.getMaBan().contains(oldMaBanChinh) && don.getTrangThai().equalsIgnoreCase("Checked-in")) {
                            List<String> listMaBanMoiIds = listBanMoi.stream().map(Ban::getMaBan).collect(Collectors.toList());
                            
                            // Ép Database nhổ rễ ông A từ bàn cũ, cắm sang nhóm bàn mới
                            donDatBanDAO.capNhatBanChoDonDat(don.getMaDon(), listMaBanMoiIds, don.getSoLuongKhach());
                            break; 
                        }
                    }
                    // =========================================================================

                    // 2. GIẢI TÁN BÀN CŨ (Đập khối nếu có)
                    Integer oldKhoi = listBanCu.get(0).getMaKhoi();
                    if (oldKhoi != null && oldKhoi > 0) {
                        banDAO.giaiTanKhoi(oldKhoi); // Lệnh này đã bao gồm cập nhật về Trống
                    } else {
                        banDAO.updateTrangThaiBan(oldMaBanChinh, "Trống");
                    }

                    // 3. THIẾT LẬP BÀN MỚI
                    if (listBanMoi.size() == 1) {
                        banDAO.updateTrangThaiBan(newMaBanChinh, "Đang dùng");
                    } else {
                        List<String> listMaBanMoiIds = listBanMoi.stream().map(Ban::getMaBan).collect(Collectors.toList());
                        banDAO.taoKhoiBan(listMaBanMoiIds, newMaBanChinh); // Tự động tạo khối và set Đang Dùng
                    }

                    JOptionPane.showMessageDialog(this, "Chuyển bàn và dời toàn bộ đơn hàng thành công!");
                    clearSelection();
                    loadData(txtSearch.getText().trim());
                } else {
                    JOptionPane.showMessageDialog(this, "Thao tác thất bại: Bàn " + sourceStr + " hiện không có Hóa đơn nào chưa thanh toán để dời đi!", "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi hệ thống: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void loadData(String query) {
        int currentScrollValue = 0;
        if (scrollPaneDanhSach != null) {
            currentScrollValue = scrollPaneDanhSach.getVerticalScrollBar().getValue();
        }

        pnlDanhSachBan.removeAll();
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        
        List<Ban> dsAll = banDAO.getAllBan(); 

        String[] dsKhuVuc = {"Ngoài trời", "Phòng VIP", "Tầng 1", "Tầng 2"};
        for (String khuVuc : dsKhuVuc) {
            if (!floorFilter.equals("Tất cả") && !floorFilter.equalsIgnoreCase(khuVuc)) continue;
            
            List<Ban> dsTheoKV = dsAll.stream()
                .filter(b -> b.getViTri().toLowerCase().contains(khuVuc.toLowerCase()))
                .filter(b -> b.getTenBan().toLowerCase().contains(query.toLowerCase()))
                .sorted((b1, b2) -> Integer.compare(b1.getSoGhe(), b2.getSoGhe()))
                .collect(Collectors.toList());

            if (!dsTheoKV.isEmpty()) {
                JPanel pnlKVTitle = new JPanel(new FlowLayout(FlowLayout.LEFT));
                pnlKVTitle.setOpaque(false);
                JLabel lblKV = new JLabel("--- " + khuVuc.toUpperCase() + " ---");
                lblKV.setFont(new Font("Segoe UI", Font.BOLD, 18));
                lblKV.setForeground(new Color(150, 50, 50));
                pnlKVTitle.add(lblKV);
                
                gbc.gridy++; pnlDanhSachBan.add(pnlKVTitle, gbc);
                
                JPanel pnlGrid = new JPanel(new WrapLayout(FlowLayout.LEFT, 20, 20));
                pnlGrid.setOpaque(false);
                for (Ban b : dsTheoKV) pnlGrid.add(taoTheBan(b));
                
                gbc.gridy++; pnlDanhSachBan.add(pnlGrid, gbc);
            }
        }
        
        gbc.gridy++; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.BOTH;
        JPanel filler = new JPanel(); filler.setOpaque(false);
        pnlDanhSachBan.add(filler, gbc);
        
        pnlDanhSachBan.revalidate(); 
        pnlDanhSachBan.repaint();

        if (scrollPaneDanhSach != null) {
            final int finalScrollValue = currentScrollValue;
            Timer restoreScroll = new Timer(80, e -> {
                scrollPaneDanhSach.getVerticalScrollBar().setValue(finalScrollValue);
            });
            restoreScroll.setRepeats(false);
            restoreScroll.start();
        }
    }

    private JPanel taoTheBan(Ban ban) {
        int width = (ban.getSoGhe() <= 4) ? 165 : (ban.getSoGhe() <= 8) ? 260 : 360;
        Color tempBg = COLOR_TRONG; 
        String tt = ban.getTrangThai().toLowerCase();
        if (tt.contains("dùng") || tt.contains("sử dụng")) tempBg = COLOR_DUNG;
        else if (tt.contains("đặt")) tempBg = COLOR_DAT;
        final Color bg = tempBg; 
        
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25); 
                
                boolean isCu = listBanCu.stream().anyMatch(b -> b.getMaBan().equals(ban.getMaBan()));
                boolean isMoi = listBanMoi.stream().anyMatch(b -> b.getMaBan().equals(ban.getMaBan()));

                if (isCu) {
                    g2.setColor(new Color(0, 122, 255)); 
                    g2.fillRoundRect(5, 5, 80, 25, 10, 10);
                    g2.setColor(Color.WHITE); g2.setFont(new Font("Segoe UI", Font.BOLD, 12)); g2.drawString("CHUYỂN ĐI", 12, 22);
                    g2.setColor(new Color(0, 122, 255)); g2.setStroke(new BasicStroke(3));
                    g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 25, 25);
                } 
                else if (isMoi) {
                    g2.setColor(new Color(255, 152, 0)); 
                    g2.fillRoundRect(5, 5, 85, 25, 10, 10);
                    g2.setColor(Color.WHITE); g2.setFont(new Font("Segoe UI", Font.BOLD, 12)); g2.drawString("CHUYỂN ĐẾN", 10, 22);
                    g2.setColor(new Color(255, 152, 0)); g2.setStroke(new BasicStroke(3));
                    g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 25, 25);
                }
                
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
        card.setLayout(new BorderLayout());
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(width, 140));
        card.setBackground(bg);
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel pnlContent = new JPanel(new BorderLayout());
        pnlContent.setOpaque(false);

        JPanel pnlTopRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
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
            pnlTopRight.add(lblGroup);
        }
        
        pnlContent.add(pnlTopRight, BorderLayout.NORTH);

        JPanel pnlInfo = new JPanel(new GridLayout(2, 1));
        pnlInfo.setOpaque(false);
        JLabel lblTen = new JLabel(ban.getTenBan(), SwingConstants.CENTER); 
        lblTen.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTen.setForeground(bg == COLOR_DAT ? Color.BLACK : Color.WHITE);
        JLabel lblSub = new JLabel(ban.getSoGhe() + " ghế - " + ban.getMaBan(), SwingConstants.CENTER);
        lblSub.setForeground(bg == COLOR_DAT ? new Color(0,0,0,150) : new Color(255,255,255,180));
        pnlInfo.add(lblTen);
        pnlInfo.add(lblSub);
        
        pnlContent.add(pnlInfo, BorderLayout.CENTER);
        card.add(pnlContent, BorderLayout.CENTER);

        // =========================================================================
        // LOGIC CHỌN BÀN THÔNG MINH CHO TÍNH NĂNG CHUYỂN BÀN
        // =========================================================================
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                boolean isCu = listBanCu.stream().anyMatch(b -> b.getMaBan().equals(ban.getMaBan()));
                boolean isMoi = listBanMoi.stream().anyMatch(b -> b.getMaBan().equals(ban.getMaBan()));

                if (isCu) {
                    // Nếu bỏ tick 1 bàn trong khối -> bỏ tick toàn khối
                    if (ban.getMaKhoi() != null && ban.getMaKhoi() > 0) {
                        listBanCu.removeIf(b -> b.getMaKhoi() != null && b.getMaKhoi().equals(ban.getMaKhoi()));
                    } else {
                        listBanCu.removeIf(b -> b.getMaBan().equals(ban.getMaBan()));
                    }
                } 
                else if (isMoi) {
                    listBanMoi.removeIf(b -> b.getMaBan().equals(ban.getMaBan()));
                } 
                else {
                    // BƯỚC 1: Chọn bàn cũ (Màu Đỏ)
                    if (ban.getTrangThai().toLowerCase().contains("dùng") || ban.getTrangThai().toLowerCase().contains("checked-in")) {
                        if (!listBanCu.isEmpty()) {
                            JOptionPane.showMessageDialog(SoDoBan_Chuyen_Panel.this, "Chỉ được chọn 1 Bàn (hoặc 1 Khối) để chuyển đi!\nVui lòng bỏ chọn bàn cũ trước nếu muốn đổi.", "Nhắc nhở", JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        
                        // Smart Selection: Nạp toàn bộ khối nếu có
                        if (ban.getMaKhoi() != null && ban.getMaKhoi() > 0) {
                            List<Ban> sameBlock = banDAO.getAllBan().stream()
                                .filter(b -> b.getMaKhoi() != null && b.getMaKhoi().equals(ban.getMaKhoi())).collect(Collectors.toList());
                            for (Ban blockBan : sameBlock) {
                                if (listBanCu.stream().noneMatch(b -> b.getMaBan().equals(blockBan.getMaBan()))) listBanCu.add(blockBan);
                            }
                        } else {
                            listBanCu.add(ban);
                        }
                    } 
                    // BƯỚC 2: Chọn nhiều bàn mới (Màu Xanh)
                    else if (ban.getTrangThai().toLowerCase().contains("trống")) {
                        dsDonDatHienTai = donDatBanDAO.getAllDonDat();
                        LocalDateTime now = LocalDateTime.now();
                        boolean biKẹt = false;
                        for (DonDatBan don : dsDonDatHienTai) {
                        	if (don.getMaBan().contains(ban.getMaBan()) 
                                    && !don.getTrangThai().equalsIgnoreCase("Đã hủy") 
                                    && !don.getTrangThai().equalsIgnoreCase("Hoàn thành") 
                                    && don.getNgayDat().equals(LocalDate.now())) {
                                    
                                    LocalDateTime thoiGianDat = LocalDateTime.of(don.getNgayDat(), don.getThoiGian());
                                    long phutConLai = Duration.between(now, thoiGianDat).toMinutes();
                                if (Duration.between(now, thoiGianDat).toMinutes() > -120 && Duration.between(now, thoiGianDat).toMinutes() <= 120) {
                                    biKẹt = true; break;
                                }
                            }
                        }
                        if (biKẹt) JOptionPane.showMessageDialog(SoDoBan_Chuyen_Panel.this, "Bàn này sắp có khách đặt trước đến nhận!\nVui lòng chọn Bàn trống khác.", "Cảnh báo kẹt lịch", JOptionPane.ERROR_MESSAGE);
                        else listBanMoi.add(ban); 
                    } 
                    else {
                        JOptionPane.showMessageDialog(SoDoBan_Chuyen_Panel.this, "Không thể chuyển bàn Đã Đặt (Màu vàng). Vui lòng Check-in trước!", "Lỗi", JOptionPane.WARNING_MESSAGE);
                    }
                }
                
                pnlDanhSachBan.repaint(); 
                if(scrollPaneDanhSach != null) scrollPaneDanhSach.repaint();
            }
        });

        return card;
    }

    private JButton createSpecialButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setPreferredSize(new Dimension(200, 42));
        btn.putClientProperty("JButton.buttonType", "roundRect");
        btn.putClientProperty("JButton.arc", 15);
        return btn;
    }

    private JButton createFloorButton(String text) {
        JButton btn = new JButton(text);
        boolean isActive = text.equals(floorFilter);
        
        btn.setBackground(isActive ? COLOR_PINK : Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13)); 
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR)); 
        btn.putClientProperty("JButton.buttonType", "roundRect");
        btn.putClientProperty("JButton.arc", 15);
        
        btn.addActionListener(evt -> {
            floorFilter = text;
            for (Component c : pnlFloorFilter.getComponents()) {
                if (c instanceof JButton) {
                    JButton b = (JButton) c;
                    b.setBackground(b.getText().equals(floorFilter) ? COLOR_PINK : Color.WHITE);
                }
            }
            loadData(txtSearch.getText());
        });
        return btn;
    }

    class ScrollablePanel extends JPanel implements Scrollable {
        public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) { return 20; }
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) { return 20; }
        public boolean getScrollableTracksViewportWidth() { return true; }
        public boolean getScrollableTracksViewportHeight() { return false; }
    }
}