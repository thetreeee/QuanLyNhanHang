package gui;

import dao.BanDAO;
import dao.DonDatBanDAO;
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
import java.time.LocalDateTime;
import java.time.Duration; 
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.Set;
import java.util.HashSet;

public class SoDoBan_Normal_Panel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final Color COLOR_TRONG = new Color(40, 167, 69);   
    private final Color COLOR_DUNG = new Color(220, 53, 69);      
    private final Color COLOR_DAT = new Color(255, 193, 7);      
    private final Color COLOR_PINK = new Color(255, 182, 193);   
    private final Color BTN_YELLOW = new Color(255, 209, 102); 

    private BanDAO banDAO;
    private DonDatBanDAO donDatBanDAO; 
    
    private JPanel pnlDanhSachBan;
    
    // ĐÃ THÊM: Chuyển thanh cuộn thành biến toàn cục để kiểm soát dễ dàng
    private JScrollPane scrollPaneDanhSach; 
    
    private JPanel pnlFloorFilter;
    private JTextField txtSearch;
    private JTextField txtTimSoGhe; 
    private JLabel lblErrorSoGhe; 
    
    private JLabel lblTimeDate;
    private Timer timer;
    private Timer autoCheckTimer; 
    
    private JButton btnDatBan;
    private JButton btnCheckIn;
    
    private boolean isDatNhieuBanMode = false;
    private List<Ban> listBanDatNhieu = new ArrayList<>();
    private JButton btnXacNhanDatNhieu;
    
    private Integer filterSoLuongKhach = null;
    private LocalDateTime filterThoiGianDat = null;

    private String floorFilter = "Tất cả";
    private String statusFilter = "Tất cả";

    private List<DonDatBan> dsDonDatHienTai = new ArrayList<>();
    private Set<String> cacDonDaCanhBao = new HashSet<>();
    
    private String maNV;

    public SoDoBan_Normal_Panel(String maNV) {
        this.maNV = maNV;
        banDAO = new BanDAO();
        donDatBanDAO = new DonDatBanDAO(); 
        
        initUI();
        
        dsDonDatHienTai = donDatBanDAO.getAllDonDat(); 
        loadData("");
        startAutoCheckTimer(); 

        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                SwingUtilities.invokeLater(() -> {
                    if (txtSearch != null) loadData(txtSearch.getText().trim()); 
                    else loadData("");
                });
            }
        });
    }

    private boolean isBanInDon(String maBanCuaDon, String maBanCanTim) {
        if (maBanCuaDon == null || maBanCanTim == null) return false;
        String[] arr = maBanCuaDon.split(", ");
        for (String s : arr) {
            if (s.equals(maBanCanTim)) return true;
        }
        return false;
    }

    private void startAutoCheckTimer() {
        autoCheckTimer = new Timer(5000, evt -> {
            if (donDatBanDAO.autoUpdateMauBan()) loadData(txtSearch.getText());
            kiemTraKhachDenTre(); 
        });
        autoCheckTimer.start();
    }
    
    private void kiemTraKhachDenTre() {
        dsDonDatHienTai = donDatBanDAO.getAllDonDat(); 
        LocalDateTime now = LocalDateTime.now();
        
        for (DonDatBan don : dsDonDatHienTai) {
            if (don.getTrangThai().equalsIgnoreCase("Đã đặt")) {
                LocalDateTime thoiGianDat = LocalDateTime.of(don.getNgayDat(), don.getThoiGian());
                long phutTre = Duration.between(thoiGianDat, now).toMinutes();
                
                if (phutTre > 15 && phutTre <= 120 && !cacDonDaCanhBao.contains(don.getMaDon())) {
                    cacDonDaCanhBao.add(don.getMaDon()); 
                    String tenKH = don.getTenKhachHang() != null ? don.getTenKhachHang() : "Khách vãng lai";
                    String sdtKH = don.getSoDienThoai() != null ? don.getSoDienThoai() : "Không có";
                    
                    String msg = String.format(
                        "CẢNH BÁO: KHÁCH ĐẾN TRỄ HƠN 15 PHÚT!\n\n- Bàn: %s\n- Mã đơn: %s\n- Tên khách: %s\n- Số ĐT: %s\n\nVui lòng liên hệ khách hàng để giữ bàn hoặc hủy đơn!",
                        don.getMaBan(), don.getMaDon(), tenKH, sdtKH
                    );
                    Toolkit.getDefaultToolkit().beep();
                    JOptionPane.showMessageDialog(this, msg, "Cảnh báo quá giờ", JOptionPane.WARNING_MESSAGE);
                }
            }
        }
    }

    private void initUI() {
        setLayout(new BorderLayout(0, 20)); 
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel topWrapper = new JPanel();
        topWrapper.setLayout(new BoxLayout(topWrapper, BoxLayout.Y_AXIS));
        topWrapper.setOpaque(false);

        JPanel timeBanner = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        timeBanner.setBackground(new Color(255, 246, 246));
        timeBanner.putClientProperty("FlatLaf.style", "arc: 15");

        lblTimeDate = new JLabel();
        lblTimeDate.setFont(new Font("Segoe UI", Font.PLAIN, 15)); 
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("EEEE, 'ngày' dd/MM/yyyy - HH:mm:ss", new Locale("vi", "VN"));
        timer = new Timer(1000, evt -> lblTimeDate.setText(LocalDateTime.now().format(dtf)));
        timer.start();
        timeBanner.add(lblTimeDate);
        topWrapper.add(timeBanner);
        topWrapper.add(Box.createRigidArea(new Dimension(0, 20)));

        JPanel titleActionPanel = new JPanel(new BorderLayout());
        titleActionPanel.setOpaque(false);
        JPanel pnlLeftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        pnlLeftActions.setOpaque(false);
        
        btnDatBan = createSpecialButton("Tìm bàn trống"); 
        btnDatBan.addActionListener(evt -> xuLyNutDatBan());
        pnlLeftActions.add(btnDatBan);

        btnCheckIn = createSpecialButton("Check-in");
        btnCheckIn.addActionListener(evt -> {
            DialogCheckIn dialog = new DialogCheckIn(SwingUtilities.getWindowAncestor(this));
            dialog.setVisible(true);
            if (dialog.isThanhCong()) loadData(txtSearch.getText().trim());
        });
        pnlLeftActions.add(btnCheckIn);
        
        btnXacNhanDatNhieu = new JButton("Xác nhận Đặt nhóm");
        btnXacNhanDatNhieu.setBackground(new Color(40, 167, 69));
        btnXacNhanDatNhieu.setForeground(Color.WHITE);
        btnXacNhanDatNhieu.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnXacNhanDatNhieu.setPreferredSize(new Dimension(160, 42));
        btnXacNhanDatNhieu.putClientProperty("JButton.buttonType", "roundRect");
        btnXacNhanDatNhieu.setVisible(false);
        btnXacNhanDatNhieu.addActionListener(evt -> xuLyXacNhanDatNhieu());
        pnlLeftActions.add(btnXacNhanDatNhieu);
        
        titleActionPanel.add(pnlLeftActions, BorderLayout.WEST);

        JPanel btnActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        btnActions.setOpaque(false);
        txtTimSoGhe = new JTextField(5); txtTimSoGhe.setPreferredSize(new Dimension(100, 40));
        txtTimSoGhe.putClientProperty("JTextField.placeholderText", "Số ghế...");
        txtTimSoGhe.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { checkGheAndLoad(); }
            public void removeUpdate(DocumentEvent e) { checkGheAndLoad(); }
            public void changedUpdate(DocumentEvent e) { checkGheAndLoad(); }
        });
        txtSearch = new JTextField(15); txtSearch.setPreferredSize(new Dimension(220, 40));
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm tên bàn...");
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { loadData(txtSearch.getText()); }
            public void removeUpdate(DocumentEvent e) { loadData(txtSearch.getText()); }
            public void changedUpdate(DocumentEvent e) { loadData(txtSearch.getText()); }
        });
        btnActions.add(txtTimSoGhe); btnActions.add(txtSearch);
        titleActionPanel.add(btnActions, BorderLayout.EAST);
        
        lblErrorSoGhe = new JLabel("* Vui lòng nhập đúng số ghế");
        lblErrorSoGhe.setForeground(Color.RED); lblErrorSoGhe.setFont(new Font("Segoe UI", Font.ITALIC, 12)); lblErrorSoGhe.setVisible(false); 
        JPanel pnlErr = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0)); pnlErr.setOpaque(false); pnlErr.add(lblErrorSoGhe);
        titleActionPanel.add(pnlErr, BorderLayout.SOUTH);

        topWrapper.add(titleActionPanel);
        topWrapper.add(Box.createRigidArea(new Dimension(0, 15)));

        JPanel pnlFilterBar = new JPanel(new BorderLayout());
        pnlFilterBar.setOpaque(false);
        pnlFloorFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0)); pnlFloorFilter.setOpaque(false);
        String[] floors = {"Tất cả", "Ngoài trời", "Phòng VIP", "Tầng 1", "Tầng 2"};
        for (String f : floors) pnlFloorFilter.add(createFloorButton(f));
        
        JPanel pnlStatus = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); pnlStatus.setOpaque(false);
        pnlStatus.add(createStatusButton("Tất cả", Color.GRAY));
        pnlStatus.add(createStatusButton("Trống", COLOR_TRONG));
        pnlStatus.add(createStatusButton("Đã đặt", COLOR_DAT));
        pnlStatus.add(createStatusButton("Đang dùng", COLOR_DUNG));
        pnlStatus.add(createStatusButton("Quá hạn", Color.RED)); 
        
        pnlFilterBar.add(pnlFloorFilter, BorderLayout.WEST);
        pnlFilterBar.add(pnlStatus, BorderLayout.EAST);
        topWrapper.add(pnlFilterBar);
        add(topWrapper, BorderLayout.NORTH);

        pnlDanhSachBan = new ScrollablePanel(); 
        pnlDanhSachBan.setLayout(new GridBagLayout()); 
        pnlDanhSachBan.setOpaque(false);
        
        // ĐÃ SỬA: Gắn JScrollPane vào biến toàn cục
        scrollPaneDanhSach = new JScrollPane(pnlDanhSachBan); 
        scrollPaneDanhSach.setBorder(null);
        scrollPaneDanhSach.setViewportBorder(null);
        scrollPaneDanhSach.getViewport().setBackground(Color.WHITE);
        scrollPaneDanhSach.getVerticalScrollBar().setUnitIncrement(30); 
        scrollPaneDanhSach.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        
        add(scrollPaneDanhSach, BorderLayout.CENTER);
    }

    private void checkGheAndLoad() {
        String text = txtTimSoGhe.getText().trim();
        if (text.isEmpty() || text.matches("\\d+")) {
            txtTimSoGhe.putClientProperty("JComponent.outline", null); 
            txtTimSoGhe.setBorder(UIManager.getBorder("TextField.border")); 
            lblErrorSoGhe.setVisible(false); 
            loadData(txtSearch.getText()); 
        } else {
            txtTimSoGhe.putClientProperty("JComponent.outline", "error"); 
            txtTimSoGhe.setBorder(BorderFactory.createLineBorder(Color.RED, 1)); 
            lblErrorSoGhe.setVisible(true); 
        }
    }

    private void xuLyNutDatBan() {
        if (filterSoLuongKhach != null || filterThoiGianDat != null) {
            filterSoLuongKhach = null;
            filterThoiGianDat = null;
            isDatNhieuBanMode = false;
            listBanDatNhieu.clear();
            btnXacNhanDatNhieu.setVisible(false);
            
            btnDatBan.setText("Tìm bàn trống");
            btnDatBan.setBackground(BTN_YELLOW);
            btnCheckIn.setVisible(true); 
            
            loadData(txtSearch.getText());
            return;
        }

        JTextField txtNgay = new JTextField(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        JTextField txtGio = new JTextField(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        JTextField txtSL = new JTextField("2");

        JPanel pnl = new JPanel(new GridLayout(3, 2, 10, 15));
        pnl.add(new JLabel("Ngày đặt (dd/MM/yyyy):")); pnl.add(txtNgay);
        pnl.add(new JLabel("Giờ đặt (HH:mm):")); pnl.add(txtGio);
        pnl.add(new JLabel("Số lượng khách:")); pnl.add(txtSL);

        while (true) {
            int result = JOptionPane.showConfirmDialog(this, pnl, "Tìm Bàn Trống Phù Hợp", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                try {
                    LocalDate ngay = LocalDate.parse(txtNgay.getText().trim(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    LocalTime gio = LocalTime.parse(txtGio.getText().trim(), DateTimeFormatter.ofPattern("HH:mm"));
                    int sl = Integer.parseInt(txtSL.getText().trim());

                    LocalDateTime thoiGianChon = LocalDateTime.of(ngay, gio);
                    if (thoiGianChon.isBefore(LocalDateTime.now())) {
                        JOptionPane.showMessageDialog(this, "Thời gian không hợp lệ!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                        continue; 
                    }

                    if (sl > 12) {
                        JOptionPane.showMessageDialog(this, 
                            "Số lượng khách (" + sl + " người) vượt quá sức chứa của 1 bàn.\n\n" +
                            "👉 Hệ thống tự động kích hoạt [Chế độ đặt nhiều bàn].\nVui lòng tick chọn các bàn trên sơ đồ để ghép chỗ cho khách.", 
                            "Khách đoàn", JOptionPane.INFORMATION_MESSAGE);
                        isDatNhieuBanMode = true;
                        btnXacNhanDatNhieu.setVisible(true);
                    } else {
                        isDatNhieuBanMode = false;
                        btnXacNhanDatNhieu.setVisible(false);
                    }

                    filterSoLuongKhach = sl;
                    filterThoiGianDat = thoiGianChon;

                    btnDatBan.setText("Hủy lọc bàn");
                    btnDatBan.setBackground(COLOR_PINK); 
                    btnCheckIn.setVisible(false); 
                    
                    dsDonDatHienTai = donDatBanDAO.getAllDonDat();
                    loadData(txtSearch.getText());
                    break; 

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Định dạng ngày/giờ không hợp lệ!\nVí dụ chuẩn: 19/04/2026 và 14:30", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                    continue; 
                }
            } else {
                break; 
            }
        }
    }

    private void xuLyXacNhanDatNhieu() {
        if (listBanDatNhieu.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng tick chọn ít nhất 1 bàn trên sơ đồ!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // ==============================================================
        // 1. ĐÃ THÊM: KIỂM TRA CÁC BÀN ĐƯỢC CHỌN PHẢI NẰM CÙNG 1 TẦNG
        // ==============================================================
        String khuVucChung = listBanDatNhieu.get(0).getViTri(); // Lấy vị trí của bàn đầu tiên làm mốc
        boolean cungKhuVuc = listBanDatNhieu.stream().allMatch(b -> b.getViTri().equalsIgnoreCase(khuVucChung));
        
        if (!cungKhuVuc) {
            JOptionPane.showMessageDialog(this, 
                "Lỗi ghép bàn: Các bàn được chọn phải nằm trên cùng 1 khu vực!\n\n" +
                "\nVui lòng chọn lại bàn hợp lý.", 
                "Lỗi khác khu vực", JOptionPane.ERROR_MESSAGE);
            return; // Chặn đứng ngay lập tức
        }

        int totalSeats = listBanDatNhieu.stream().mapToInt(Ban::getSoGhe).sum();
        
        // 2. CHẶN NẾU CHỌN THIẾU GHẾ
        if (totalSeats < filterSoLuongKhach) {
            JOptionPane.showMessageDialog(this, "Bạn mới chọn được " + totalSeats + " ghế, chưa đủ cho " + filterSoLuongKhach + " khách!\nVui lòng tick chọn thêm bàn.", "Nhắc nhở", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 3. CẢNH BÁO NẾU CHỌN DƯ QUÁ NHIỀU GHẾ (Vượt ngưỡng +5)
        if (totalSeats > filterSoLuongKhach + 5) {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Số ghế bạn chọn (" + totalSeats + " ghế) đang vượt quá khá nhiều so với lượng khách dự kiến (" + filterSoLuongKhach + " người).\n\n" +
                "\nBạn có chắc chắn muốn giữ nguyên số lượng bàn này không?", 
                "Xác nhận dư chỗ ngồi", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            
            // Nếu Lễ tân chọn "No" -> Dừng lại để họ bỏ tick bớt bàn
            if (confirm != JOptionPane.YES_OPTION) {
                return; 
            }
        }

        Ban banDaiDien = listBanDatNhieu.get(0);
        String strNgay = filterThoiGianDat.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String strGio = filterThoiGianDat.format(DateTimeFormatter.ofPattern("HH:mm"));
        String strSL = String.valueOf(filterSoLuongKhach);

        DialogTaoDonDat diag = new DialogTaoDonDat(SwingUtilities.getWindowAncestor(this), banDaiDien, strNgay, strGio, strSL);
        diag.setVisible(true);
        
        if (diag.isThanhCong()) {
            try {
                LocalDate ngayMoi = LocalDate.parse(diag.getNgay(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                LocalTime gioMoi = LocalTime.parse(diag.getGio(), DateTimeFormatter.ofPattern("HH:mm"));
                
                DonDatBan donMoi = new DonDatBan(diag.getMaDon(), "NHIỀU BÀN", ngayMoi, gioMoi, Integer.parseInt(diag.getSoLuong()), diag.getGhiChu(), "Đã đặt");
                donMoi.setTenKhachHang(diag.getHoTen()); 
                donMoi.setSoDienThoai(diag.getSdt());
                
                List<String> dsMaBanChon = listBanDatNhieu.stream().map(Ban::getMaBan).collect(Collectors.toList());

                if (donDatBanDAO.insertDonDat(donMoi, dsMaBanChon)) {
                    dsDonDatHienTai.add(donMoi); 
                    if (donDatBanDAO.autoUpdateMauBan()) loadData(txtSearch.getText());
                    JOptionPane.showMessageDialog(this, "Đã tạo ĐƠN ĐẶT NHÓM thành công cho " + dsMaBanChon.size() + " bàn!");
                    
                    filterSoLuongKhach = null; filterThoiGianDat = null; isDatNhieuBanMode = false;
                    listBanDatNhieu.clear(); btnXacNhanDatNhieu.setVisible(false);
                    btnDatBan.setText("Tìm bàn trống"); btnDatBan.setBackground(BTN_YELLOW);
                    btnCheckIn.setVisible(true);
                    
                    loadData(txtSearch.getText()); 
                }
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Lỗi lưu dữ liệu Đặt nhóm!"); }
        }
    }
    private JButton createSpecialButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(BTN_YELLOW); 
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setPreferredSize(new Dimension(140, 42));
        btn.putClientProperty("JButton.buttonType", "roundRect");
        btn.putClientProperty("JButton.arc", 15);
        return btn;
    }

    private JButton createFloorButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(text.equals(floorFilter) ? COLOR_PINK : Color.WHITE);
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

    private JButton createStatusButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(color); 
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR)); 
        btn.putClientProperty("JButton.buttonType", "roundRect");
        btn.putClientProperty("JButton.arc", 15);
        btn.addActionListener(evt -> {
            statusFilter = text;
            loadData(txtSearch.getText());
        });
        return btn;
    }

    public void loadData(String query) {
        // ==============================================================
        // BƯỚC 1: LƯU LẠI VỊ TRÍ CỦA THANH CUỘN (SCROLLBAR) HIỆN TẠI
        // Đã sử dụng biến toàn cục để không bao giờ bị mất vị trí
        // ==============================================================
        int currentScrollValue = 0;
        if (scrollPaneDanhSach != null) {
            currentScrollValue = scrollPaneDanhSach.getVerticalScrollBar().getValue();
        }

        pnlDanhSachBan.removeAll();
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        
        List<Ban> dsAll = banDAO.getAllBan(); 
        Integer walkInSeats = null;
        try {
            String textSL = txtTimSoGhe.getText().trim();
            if (!textSL.isEmpty()) walkInSeats = Integer.parseInt(textSL);
        } catch (NumberFormatException ignored) {}

        String[] dsKhuVuc = {"Ngoài trời", "Phòng VIP", "Tầng 1", "Tầng 2"};
        for (String khuVuc : dsKhuVuc) {
            if (!floorFilter.equals("Tất cả") && !floorFilter.equalsIgnoreCase(khuVuc)) continue;
            
            final Integer finalWalkInSeats = walkInSeats;
            
            List<Ban> dsTheoKV = dsAll.stream()
                .filter(b -> b.getViTri().toLowerCase().contains(khuVuc.toLowerCase()))
                .filter(b -> b.getTenBan().toLowerCase().contains(query.toLowerCase()))
                .filter(b -> {
                    if (statusFilter.equals("Tất cả")) return true;
                    String tt = b.getTrangThai().toLowerCase();
                    if (statusFilter.equals("Quá hạn")) {
                        if (!tt.contains("đặt")) return false; 
                        LocalDateTime now = LocalDateTime.now();
                        for (DonDatBan don : dsDonDatHienTai) {
                            if (isBanInDon(don.getMaBan(), b.getMaBan()) && !don.getTrangThai().equalsIgnoreCase("Đã hủy")) {
                                LocalDateTime thoiGianDat = LocalDateTime.of(don.getNgayDat(), don.getThoiGian());
                                if (Duration.between(thoiGianDat, now).toMinutes() > 15) return true;
                            }
                        }
                        return false;
                    }
                    if (statusFilter.equals("Trống")) return tt.contains("trống");
                    if (statusFilter.equals("Đã đặt")) return tt.contains("đặt");
                    if (statusFilter.equals("Đang dùng")) return tt.contains("dùng") || tt.contains("sử dụng");
                    return tt.equalsIgnoreCase(statusFilter);
                })
                .filter(b -> {
                    if (filterSoLuongKhach != null && !isDatNhieuBanMode) {
                        int targetSeats = (filterSoLuongKhach % 2 == 0) ? filterSoLuongKhach : filterSoLuongKhach + 1;
                        if (!(b.getSoGhe() == targetSeats || b.getSoGhe() == targetSeats + 2)) return false;
                    }
                    if (filterThoiGianDat != null) {
                        LocalDate filterNgay = filterThoiGianDat.toLocalDate();
                        LocalTime filterGio = filterThoiGianDat.toLocalTime();
                        for (DonDatBan don : dsDonDatHienTai) {
                            if (isBanInDon(don.getMaBan(), b.getMaBan()) && !don.getTrangThai().equalsIgnoreCase("Đã hủy") && don.getNgayDat().equals(filterNgay)) {
                                if (Math.abs(Duration.between(don.getThoiGian(), filterGio).toMinutes()) < 120) return false; 
                            }
                        }

                        String ttBan = b.getTrangThai().toLowerCase();
                        if (ttBan.contains("dùng") || ttBan.contains("checked-in")) {
                            if (filterNgay.equals(LocalDate.now())) {
                                long phutChoDenGioDat = Duration.between(LocalDateTime.now(), filterThoiGianDat).toMinutes();
                                if (phutChoDenGioDat >= 0 && phutChoDenGioDat < 120) {
                                    return false; 
                                }
                            }
                        }
                    }
                    if (finalWalkInSeats != null) {
                        int targetSeats = (finalWalkInSeats % 2 != 0) ? finalWalkInSeats + 1 : finalWalkInSeats;
                        if (!(b.getSoGhe() == targetSeats || b.getSoGhe() == targetSeats + 2)) return false;
                        String tt = b.getTrangThai().toLowerCase();
                        if (tt.contains("đặt") || tt.contains("dùng") || tt.contains("quá hạn")) return false;
                        LocalDateTime now = LocalDateTime.now();
                        for (DonDatBan don : dsDonDatHienTai) {
                            if (isBanInDon(don.getMaBan(), b.getMaBan()) && !don.getTrangThai().equalsIgnoreCase("Đã hủy") && don.getNgayDat().equals(LocalDate.now())) {
                                long phutConLai = Duration.between(now, LocalDateTime.of(don.getNgayDat(), don.getThoiGian())).toMinutes();
                                if (phutConLai > -120 && phutConLai <= 120) return false; 
                            }
                        }
                    }
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

        // ==============================================================
        // BƯỚC 2: MẸO DOUBLE INVOKELATER CỦA SWING
        // Chờ giao diện mới vẽ xong hoàn toàn rồi mới kéo thanh cuộn lại
        // ==============================================================
     // THAY BẰNG đoạn này:
        if (scrollPaneDanhSach != null) {
            final int finalScrollValue = currentScrollValue;
            // Dùng Timer 80ms để chờ Swing hoàn tất layout hoàn toàn rồi mới set lại scroll
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
                
                if (isDatNhieuBanMode) {
                    boolean isSelected = listBanDatNhieu.stream().anyMatch(b -> b.getMaBan().equals(ban.getMaBan()));
                    int size = 26; int x = 10; int y = 10;
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
                }
                g2.dispose();
            }
        };
        card.setLayout(new OverlayLayout(card));
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(width, 140));
        card.setBackground(bg);
        if(isDatNhieuBanMode) card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel pnlContent = new JPanel(new BorderLayout());
        pnlContent.setOpaque(false);

        int warningLevel = 0; 
        if (bg == COLOR_DAT) {
            LocalDateTime now = LocalDateTime.now();
            for (DonDatBan don : dsDonDatHienTai) {
                if (isBanInDon(don.getMaBan(), ban.getMaBan()) && !don.getTrangThai().equalsIgnoreCase("Đã hủy")) {
                    LocalDateTime thoiGianDat = LocalDateTime.of(don.getNgayDat(), don.getThoiGian());
                    long phutConLai = Duration.between(now, thoiGianDat).toMinutes();
                    if (phutConLai <= 15 && phutConLai >= 0) { warningLevel = 1; break; } 
                    else if (phutConLai < 0 && phutConLai >= -120) { warningLevel = 2; break; }
                }
            }
        }

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

        if (warningLevel > 0) {
            final int fWarn = warningLevel;
            JLabel lblWarning = new JLabel("!", SwingConstants.CENTER) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(fWarn == 1 ? Color.YELLOW : Color.RED); 
                    g2.fillOval(0, 0, getWidth(), getHeight());
                    g2.setColor(fWarn == 1 ? Color.RED : Color.WHITE); 
                    g2.setStroke(new BasicStroke(2));
                    g2.drawOval(1, 1, getWidth() - 2, getHeight() - 2);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            lblWarning.setFont(new Font("Arial", Font.BOLD, 16));
            lblWarning.setForeground(fWarn == 1 ? Color.RED : Color.WHITE); 
            lblWarning.setPreferredSize(new Dimension(24, 24));
            pnlTopRight.add(lblWarning);
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

        JPanel pnlOverlay = (ban.getSoGhe() <= 4) ? new JPanel(new GridBagLayout()) : new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 50));
        pnlOverlay.setOpaque(false);
        pnlOverlay.setVisible(false); 

        GridBagConstraints gbcOverlay = new GridBagConstraints();
        gbcOverlay.insets = new Insets(0, 5, 0, 5); 
        gbcOverlay.gridy = 0;

        boolean isFiltered = (filterSoLuongKhach != null && filterThoiGianDat != null);
        JButton btnMoBan = createActionBtn("Mở bàn");
        
        JButton btnChuyenTrong = createActionBtn("Về Trống");
        btnChuyenTrong.addActionListener(evt -> {
            dsDonDatHienTai = donDatBanDAO.getAllDonDat();
            boolean dangCoDonDat = dsDonDatHienTai.stream()
                .anyMatch(d -> isBanInDon(d.getMaBan(), ban.getMaBan()) && (d.getTrangThai().equalsIgnoreCase("Checked-in") || d.getTrangThai().equalsIgnoreCase("Đang dùng")));

            if (dangCoDonDat) {
                JOptionPane.showMessageDialog(this, 
                    "Bàn đang dính Đơn đặt bàn. Vui lòng chuyển trạng thái Đơn đặt thành 'Hoàn thành' hoặc 'Đã hủy' ở tab Danh Sách Đặt Bàn trước.", 
                    "Bảo vệ dữ liệu", JOptionPane.ERROR_MESSAGE);
                return; 
            }

            if (JOptionPane.showConfirmDialog(this, "Chuyển bàn về trạng thái Trống?", "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                if (ban.getMaKhoi() != null) banDAO.giaiTanKhoi(ban.getMaKhoi());
                else banDAO.updateTrangThaiBan(ban.getMaBan(), "Trống");
                loadData(txtSearch.getText());
            }
        });

        JButton btnDatTruoc = createActionBtn("Đặt trước");

        if (bg == COLOR_TRONG) {
            gbcOverlay.gridx = 0; 
            pnlOverlay.add(isFiltered ? btnDatTruoc : btnMoBan, gbcOverlay); 
        } else if (bg == COLOR_DUNG) {
            gbcOverlay.gridx = 0; pnlOverlay.add(btnChuyenTrong, gbcOverlay);
        } else if (bg == COLOR_DAT) {
            gbcOverlay.gridx = 0; pnlOverlay.add(btnMoBan, gbcOverlay);
        }

        if (!isDatNhieuBanMode) {
            card.add(pnlOverlay);
        }
        card.add(pnlContent);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (isDatNhieuBanMode) {
                    boolean alreadySelected = listBanDatNhieu.stream().anyMatch(b -> b.getMaBan().equals(ban.getMaBan()));
                    if (alreadySelected) listBanDatNhieu.removeIf(b -> b.getMaBan().equals(ban.getMaBan()));
                    else listBanDatNhieu.add(ban);
                    card.repaint(); 
                    return;
                }
                
                if (e.getClickCount() == 1) {
                    pnlOverlay.setVisible(!pnlOverlay.isVisible());
                    pnlInfo.setVisible(!pnlOverlay.isVisible()); 
                } else if (e.getClickCount() == 2) {
                    xemChiTietDatBan(card, ban); 
                }
            }
        });

        btnMoBan.addActionListener(evt -> {
            boolean isBlocked = false; 
            boolean isKhachTre = false; 
            String msgText = ""; 
            String maDonBiTre = "";
            
            LocalDateTime now = LocalDateTime.now();
            dsDonDatHienTai = donDatBanDAO.getAllDonDat();

            for (DonDatBan don : dsDonDatHienTai) {
                if (isBanInDon(don.getMaBan(), ban.getMaBan()) && don.getTrangThai().equalsIgnoreCase("Đã đặt") && don.getNgayDat().equals(LocalDate.now())) {
                    LocalDateTime thoiGianDat = LocalDateTime.of(don.getNgayDat(), don.getThoiGian());
                    long phutConLai = Duration.between(now, thoiGianDat).toMinutes();
                    DateTimeFormatter fTime = DateTimeFormatter.ofPattern("HH:mm");

                    if (phutConLai >= 0 && phutConLai <= 120) {
                        isBlocked = true;
                        msgText = "TỪ CHỐI MỞ BÀN: Bàn này đã được khách đặt trước vào lúc " + don.getThoiGian().format(fTime) + "!\n\n"
                                + "Hệ thống tự động khóa để đảm bảo giữ chỗ cho khách. Vui lòng chọn bàn khác.";
                        break;
                    } 
                    else if (phutConLai < 0 && phutConLai >= -120) {
                        isKhachTre = true; 
                        maDonBiTre = don.getMaDon(); 
                        msgText = "CẢNH BÁO: Bàn này có khách đặt lúc " + don.getThoiGian().format(fTime) + " nhưng đang ĐẾN TRỄ!\n\n"
                                + "Nếu bạn mở bàn cho khách vãng lai, hệ thống sẽ TỰ ĐỘNG HỦY đơn đặt bàn của khách đến trễ.\nBạn có chắc chắn muốn ép mở bàn không?";
                        break;
                    }
                }
            }

            if (isBlocked) {
                JOptionPane.showMessageDialog(this, msgText, "Khóa bàn", JOptionPane.ERROR_MESSAGE);
                return; 
            } 
            else if (isKhachTre) {
                if (JOptionPane.showConfirmDialog(this, msgText, "Khách đến trễ", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                    if (!maDonBiTre.isEmpty()) donDatBanDAO.updateTrangThaiCuaDon(maDonBiTre, "Đã hủy"); 
                    if (banDAO.updateTrangThaiBan(ban.getMaBan(), "Đang dùng")) loadData(txtSearch.getText());
                }
            } 
            else {
                if (JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn mở bàn " + ban.getTenBan() + " cho khách vãng lai?", "Xác nhận mở bàn", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    if (banDAO.updateTrangThaiBan(ban.getMaBan(), "Đang dùng")) loadData(txtSearch.getText());
                }
            }
        });

        btnDatTruoc.addActionListener(evt -> {
            String strNgay = filterThoiGianDat != null ? filterThoiGianDat.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : null;
            String strGio = filterThoiGianDat != null ? filterThoiGianDat.format(DateTimeFormatter.ofPattern("HH:mm")) : null;
            String strSL = filterSoLuongKhach != null ? String.valueOf(filterSoLuongKhach) : null;

            DialogTaoDonDat diag = new DialogTaoDonDat(SwingUtilities.getWindowAncestor(this), ban, strNgay, strGio, strSL);
            diag.setVisible(true);
            
            if (diag.isThanhCong()) {
                try {
                    LocalDate ngayMoi = LocalDate.parse(diag.getNgay(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    LocalTime gioMoi = LocalTime.parse(diag.getGio(), DateTimeFormatter.ofPattern("HH:mm"));
                    dsDonDatHienTai = donDatBanDAO.getAllDonDat(); 
                    
                    for (DonDatBan donCu : dsDonDatHienTai) {
                        if (isBanInDon(donCu.getMaBan(), ban.getMaBan()) && donCu.getNgayDat().equals(ngayMoi) && !donCu.getTrangThai().equalsIgnoreCase("Đã hủy")) {
                            if (Math.abs(Duration.between(donCu.getThoiGian(), gioMoi).toMinutes()) < 120) {
                                JOptionPane.showMessageDialog(this, "Bàn này đã có lịch đặt!\nVui lòng chọn giờ đặt cách ít nhất 2 tiếng!", "Lỗi trùng lịch", JOptionPane.WARNING_MESSAGE);
                                return;
                            }
                        }
                    }

                    DonDatBan donMoi = new DonDatBan(diag.getMaDon(), ban.getMaBan(), ngayMoi, gioMoi, Integer.parseInt(diag.getSoLuong()), diag.getGhiChu(), "Đã đặt");
                    donMoi.setTenKhachHang(diag.getHoTen()); donMoi.setSoDienThoai(diag.getSdt());
                    
                    if (donDatBanDAO.insertDonDat(donMoi)) {
                        dsDonDatHienTai.add(donMoi); 
                        if (donDatBanDAO.autoUpdateMauBan()) loadData(txtSearch.getText());
                        JOptionPane.showMessageDialog(this, "Đã tạo đơn thành công cho khách: " + diag.getHoTen());
                        if (filterSoLuongKhach != null) {
                            filterSoLuongKhach = null; filterThoiGianDat = null;
                            btnDatBan.setText("Tìm bàn trống"); btnDatBan.setBackground(BTN_YELLOW);
                            btnCheckIn.setVisible(true); 
                        }
                        loadData(txtSearch.getText()); 
                    }
                } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Lỗi lưu dữ liệu!"); }
            }
        });

        return card;
    }

    private JButton createActionBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBackground(Color.WHITE);
        b.setPreferredSize(new Dimension(85, 30));
        b.putClientProperty("JButton.buttonType", "roundRect");
        return b;
    }

    private void xemChiTietDatBan(Component parent, Ban ban) {
        dsDonDatHienTai = donDatBanDAO.getAllDonDat();
        List<DonDatBan> dsCuaBan = dsDonDatHienTai.stream().filter(d -> isBanInDon(d.getMaBan(), ban.getMaBan())).collect(Collectors.toList());
        DatBanDialog dialog = new DatBanDialog(SwingUtilities.getWindowAncestor(parent), ban, dsCuaBan);
        dialog.setVisible(true);
        loadData(txtSearch.getText());
    }

    class ScrollablePanel extends JPanel implements Scrollable {
        public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) { return 20; }
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) { return 20; }
        public boolean getScrollableTracksViewportWidth() { return true; }
        public boolean getScrollableTracksViewportHeight() { return false; }
    }
}