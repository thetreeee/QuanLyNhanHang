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

public class SoDoBan_Gop_Panel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final Color COLOR_TRONG = new Color(40, 167, 69);   
    private final Color COLOR_DUNG = new Color(220, 53, 69);      
    private final Color COLOR_DAT = new Color(255, 193, 7);      
    private final Color COLOR_PINK = new Color(255, 182, 193);   
    private final Color BTN_YELLOW = new Color(255, 209, 102); 

    private BanDAO banDAO;
    private DonDatBanDAO donDatBanDAO; 
    private DonDatMon_DAO donDatMonDAO; 
    
    private JPanel pnlDanhSachBan;
    private JScrollPane scrollPaneDanhSach;
    private JPanel pnlFloorFilter;
    private JTextField txtSearch;
    
    private JButton btnXacNhanGop;
    private JButton btnHuyGop;

    private boolean isGopBanMode = true; 
    private List<Ban> listBanGop = new ArrayList<>();
    private String floorFilter = "Tất cả";
    
    private String maNV;
    private List<DonDatBan> dsDonDatHienTai = new ArrayList<>();

    public SoDoBan_Gop_Panel(String maNV) {
        this.maNV = maNV;
        banDAO = new BanDAO();
        donDatBanDAO = new DonDatBanDAO(); 
        donDatMonDAO = new DonDatMon_DAO();
        
        initUI();
        loadData("");
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
        pnlHuongDan.setBackground(new Color(230, 240, 255));
        pnlHuongDan.putClientProperty("FlatLaf.style", "arc: 15");
        JLabel lblHuongDan = new JLabel("CHẾ ĐỘ GỘP BÀN: Click chọn các bàn cần gộp trên sơ đồ, sau đó bấm Xác nhận ở thanh công cụ.");
        lblHuongDan.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblHuongDan.setForeground(new Color(0, 86, 179));
        pnlHuongDan.add(lblHuongDan);
        topWrapper.add(pnlHuongDan);
        topWrapper.add(Box.createRigidArea(new Dimension(0, 15)));

        // --- THANH CÔNG CỤ XỬ LÝ GỘP BÀN ---
        JPanel titleActionPanel = new JPanel(new BorderLayout());
        titleActionPanel.setOpaque(false);
        JPanel pnlLeftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        pnlLeftActions.setOpaque(false);
        
        btnXacNhanGop = createSpecialButton("XÁC NHẬN GỘP BÀN");
        btnXacNhanGop.setBackground(new Color(40, 167, 69));
        btnXacNhanGop.setForeground(Color.WHITE);
        btnXacNhanGop.addActionListener(evt -> xuLyXacNhanGopBan());
        pnlLeftActions.add(btnXacNhanGop);
        
        btnHuyGop = createSpecialButton("Hủy / Chọn lại");
        btnHuyGop.setBackground(Color.GRAY); 
        btnHuyGop.setForeground(Color.WHITE);
        btnHuyGop.addActionListener(evt -> clearSelection());
        pnlLeftActions.add(btnHuyGop);
        
        titleActionPanel.add(pnlLeftActions, BorderLayout.WEST);

        // --- Ô TÌM KIẾM BÀN ---
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

        // --- THANH LỌC TẦNG ---
        pnlFloorFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlFloorFilter.setOpaque(false);
        String[] floors = {"Tất cả", "Ngoài trời", "Phòng VIP", "Tầng 1", "Tầng 2"};
        for (String f : floors) pnlFloorFilter.add(createFloorButton(f));
        topWrapper.add(pnlFloorFilter);

        add(topWrapper, BorderLayout.NORTH);

        // --- KHU VỰC HIỂN THỊ SƠ ĐỒ BÀN ---
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
        listBanGop.clear();
        loadData(txtSearch.getText().trim());
    }

    // =========================================================================
    // XỬ LÝ CHUẨN 3 TRƯỜNG HỢP GỘP BÀN TẠI CHỖ (WALK-IN & ĐANG ĂN)
    // =========================================================================
    private void xuLyXacNhanGopBan() {
        if (listBanGop.size() < 2) {
            JOptionPane.showMessageDialog(this, "Vui lòng tick chọn ít nhất 2 bàn để thao tác gộp!", "Chưa đủ dữ kiện", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // --- RÀO CHẮN 1: KIỂM TRA CÙNG TẦNG ---
        String khuVucChung = listBanGop.get(0).getViTri();
        boolean cungKhuVuc = listBanGop.stream().allMatch(b -> b.getViTri().equalsIgnoreCase(khuVucChung));
        if (!cungKhuVuc) {
            JOptionPane.showMessageDialog(this, "Lỗi: Không thể ghép bàn từ các tầng/khu vực khác nhau!\nVui lòng chọn lại.", "Sai nghiệp vụ", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // --- RÀO CHẮN 1.5: CHẶN VIỆC CHỈ CHỌN LẠI CÁC BÀN ĐÃ GỘP TỪ TRƯỚC ---
        boolean allSameKhoi = true;
        Integer firstKhoi = null;
        boolean hasEmptyKhoi = false;

        for(Ban b : listBanGop) {
            if(b.getMaKhoi() == null || b.getMaKhoi() == 0) {
                hasEmptyKhoi = true;
            } else {
                if (firstKhoi == null) {
                    firstKhoi = b.getMaKhoi();
                } else if (!firstKhoi.equals(b.getMaKhoi())) {
                    allSameKhoi = false; // Có sự xuất hiện của Khối khác
                }
            }
        }

        // Nếu tất cả các bàn được tick đều có chung 1 mã khối và KHÔNG có bàn lẻ nào được thêm vào
        if (firstKhoi != null && allSameKhoi && !hasEmptyKhoi) {
             JOptionPane.showMessageDialog(this, "Các bàn này đã nằm chung một Khối từ trước.\nVui lòng tick chọn thêm bàn trống để thực hiện Gộp!", "Thao tác thừa", JOptionPane.WARNING_MESSAGE);
             return; 
        }

        // --- RÀO CHẮN 2: KHÔNG GỘP BÀN MÀU VÀNG (ĐÃ ĐẶT CHƯA CHECK-IN) ---
        long countDaDat = listBanGop.stream().filter(b -> b.getTrangThai().equalsIgnoreCase("Đã đặt")).count();
        if (countDaDat > 0) {
            JOptionPane.showMessageDialog(this, "Bàn đang màu Vàng (Đã đặt) không thể gộp tự do.\nVui lòng 'Check-in' bàn đó trước ở màn hình Sơ Đồ Bàn, sau đó dùng tính năng gộp bàn!", "Lỗi trạng thái", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Phân loại các bàn được chọn
        List<Ban> dsTrong = listBanGop.stream().filter(b -> b.getTrangThai().equalsIgnoreCase("Trống")).collect(Collectors.toList());
        List<Ban> dsDangDung = listBanGop.stream().filter(b -> b.getTrangThai().equalsIgnoreCase("Đang dùng") || b.getTrangThai().equalsIgnoreCase("Checked-in")).collect(Collectors.toList());
        List<String> tatCaMaBan = listBanGop.stream().map(Ban::getMaBan).collect(Collectors.toList());

        // --- RÀO CHẮN 3: KIỂM TRA BÀN TRỐNG CÓ DÍNH LỊCH ĐẶT TRƯỚC KHÔNG ---
        dsDonDatHienTai = donDatBanDAO.getAllDonDat();
        LocalDateTime now = LocalDateTime.now();
        for (Ban bTrong : dsTrong) {
            for (DonDatBan don : dsDonDatHienTai) {
                if (don.getMaBan().equals(bTrong.getMaBan()) && !don.getTrangThai().equalsIgnoreCase("Đã hủy") && don.getNgayDat().equals(LocalDate.now())) {
                    LocalDateTime thoiGianDat = LocalDateTime.of(don.getNgayDat(), don.getThoiGian());
                    long phutConLai = Duration.between(now, thoiGianDat).toMinutes();
                    if (phutConLai > -120 && phutConLai <= 120) {
                        JOptionPane.showMessageDialog(this, 
                            "BÀN TRỐNG BỊ KẸT LỊCH: Bàn " + bTrong.getMaBan() + " sắp có khách đặt trước đến nhận!\n" +
                            "Hệ thống từ chối gộp bàn này để bảo vệ dữ liệu. Vui lòng chọn bàn trống khác.", 
                            "Cảnh báo kẹt lịch", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
            }
        }

        // =====================================================================
        // TRƯỜNG HỢP 1: TẤT CẢ CÁC BÀN ĐỀU TRỐNG (Khách Walk-in đi đông người)
        // =====================================================================
        if (dsDangDung.isEmpty()) {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "GỘP BÀN TRỐNG: Nhóm khách vãng lai đi đông người.\n" +
                "Hệ thống sẽ dồn các bàn trống này thành 1 khối để phục vụ.\nBạn có xác nhận không?", 
                "Xác nhận gộp bàn mới", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                String maBanChinh = dsTrong.get(0).getMaBan(); 
                banDAO.taoKhoiBan(tatCaMaBan, maBanChinh); 
                for (Ban b : dsTrong) banDAO.updateTrangThaiBan(b.getMaBan(), "Đang dùng"); 
                
                JOptionPane.showMessageDialog(this, "Đã tạo khối và mở bàn thành công!");
                clearSelection();
            }
            return;
        }

        // =====================================================================
        // TRƯỜNG HỢP 2: CÓ 1 BÀN ĐANG ĂN + CÁC BÀN TRỐNG (Khách gọi thêm người)
        // =====================================================================
        if (dsDangDung.size() == 1) {
            Ban banChinh = dsDangDung.get(0);
            
            int confirm = JOptionPane.showConfirmDialog(this, 
                "THÊM BÀN CHO KHÁCH ĐANG ĂN:\n" +
                "- Bàn chính (Đang có hóa đơn): " + banChinh.getMaBan() + "\n" +
                "- Hệ thống sẽ tự động ghép các bàn trống bạn vừa chọn vào hóa đơn này.\n\n" +
                "Bạn có xác nhận không?", 
                "Xác nhận thêm bàn", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                banDAO.taoKhoiBan(tatCaMaBan, banChinh.getMaBan());
                for(Ban b : dsTrong) banDAO.updateTrangThaiBan(b.getMaBan(), "Đang dùng"); 
                
                JOptionPane.showMessageDialog(this, "Đã ghép thêm bàn thành công!");
                clearSelection();
            }
            return;
        }

        // =====================================================================
        // TRƯỜNG HỢP 3: CÓ NHIỀU BÀN ĐANG ĂN GỘP LẠI (Bạn bè gặp nhau)
        // =====================================================================
        if (dsDangDung.size() > 1) {
            JComboBox<String> cbMain = new JComboBox<>();
            for (Ban b : dsDangDung) {
                cbMain.addItem(b.getMaBan() + " - " + b.getTenBan());
            }
            
            JPanel pnl = new JPanel(new BorderLayout(0, 10));
            pnl.add(new JLabel("GỘP NHIỀU BÀN ĐANG ĂN: Vui lòng chọn BÀN CHÍNH để dồn tất cả món ăn về 1 Hóa đơn duy nhất:"), BorderLayout.NORTH);
            pnl.add(cbMain, BorderLayout.CENTER);

            int confirm = JOptionPane.showConfirmDialog(this, pnl, "Xác nhận dồn Hóa đơn", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            
            if (confirm == JOptionPane.OK_OPTION) {
                String maBanChinh = cbMain.getSelectedItem().toString().split(" - ")[0];
                
                List<String> dsMaBanPhuCanGopBill = dsDangDung.stream()
                        .map(Ban::getMaBan)
                        .filter(m -> !m.equals(maBanChinh))
                        .collect(Collectors.toList());
                
                // 1. Dồn Hóa Đơn
                boolean success = donDatMonDAO.gopDonDatMon(maBanChinh, dsMaBanPhuCanGopBill);
                
                if (success) {
                    // 2. Tạo khối
                    banDAO.taoKhoiBan(tatCaMaBan, maBanChinh);
                    
                    // 3. Mở dư bàn trống (nếu có)
                    for(Ban b : dsTrong) banDAO.updateTrangThaiBan(b.getMaBan(), "Đang dùng");
                    
                    JOptionPane.showMessageDialog(this, "Ghép khối và Dồn hóa đơn thành công!");
                    clearSelection(); 
                } else {
                    JOptionPane.showMessageDialog(this, "Lỗi xử lý hóa đơn CSDL!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
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
                
                // Hiển thị vòng tròn chọn (Tick)
                int size = 26; int x = 10; int y = 10;
                boolean isSelected = listBanGop.stream().anyMatch(b -> b.getMaBan().equals(ban.getMaBan()));
                
                if (isSelected) {
                    g2.setColor(new Color(0, 122, 255)); 
                    g2.fillOval(x, y, size, size);
                    g2.setColor(Color.WHITE); g2.setStroke(new BasicStroke(3));
                    g2.drawLine(x + 7, y + 13, x + 11, y + 18);
                    g2.drawLine(x + 11, y + 18, x + 19, y + 8);
                } else {
                    g2.setColor(new Color(255, 255, 255, 200));
                    g2.fillOval(x, y, size, size);
                    g2.setColor(Color.GRAY); g2.setStroke(new BasicStroke(2));
                    g2.drawOval(x, y, size, size);
                }
                
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
        // ĐÃ CẬP NHẬT: LOGIC CHỌN THÔNG MINH CẢ KHỐI (SMART SELECTION)
        // =========================================================================
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                boolean alreadySelected = listBanGop.stream().anyMatch(b -> b.getMaBan().equals(ban.getMaBan()));
                
                // NẾU BÀN ĐÃ CÓ KHỐI -> BẬT/TẮT CHỌN CẢ KHỐI CÙNG LÚC
                if (ban.getMaKhoi() != null && ban.getMaKhoi() > 0) {
                    List<Ban> allBan = banDAO.getAllBan();
                    List<Ban> sameBlockTables = allBan.stream()
                            .filter(b -> b.getMaKhoi() != null && b.getMaKhoi().equals(ban.getMaKhoi()))
                            .collect(Collectors.toList());
                            
                    if (alreadySelected) {
                        // Bỏ tick tất cả bàn trong khối
                        listBanGop.removeIf(b -> b.getMaKhoi() != null && b.getMaKhoi().equals(ban.getMaKhoi()));
                    } else {
                        // Tick tất cả bàn trong khối
                        for (Ban blockBan : sameBlockTables) {
                            if (listBanGop.stream().noneMatch(b -> b.getMaBan().equals(blockBan.getMaBan()))) {
                                listBanGop.add(blockBan);
                            }
                        }
                    }
                } 
                // NẾU LÀ BÀN LẺ -> BẬT/TẮT BÌNH THƯỜNG
                else {
                    if (alreadySelected) {
                        listBanGop.removeIf(b -> b.getMaBan().equals(ban.getMaBan()));
                    } else {
                        listBanGop.add(ban);
                    }
                }
                
                // Vẽ lại toàn bộ danh sách để cập nhật dấu tick xanh mượt mà
                pnlDanhSachBan.repaint(); 
                if(scrollPaneDanhSach != null) scrollPaneDanhSach.repaint();
            }
        });

        return card;
    }

    private JButton createSpecialButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setPreferredSize(new Dimension(170, 42));
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