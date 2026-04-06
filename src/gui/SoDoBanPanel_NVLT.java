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

public class SoDoBanPanel_NVLT extends JPanel {

    private static final long serialVersionUID = 1L;

    private final Color COLOR_TRONG = new Color(40, 167, 69);   
    private final Color COLOR_DUNG = new Color(220, 53, 69);     
    private final Color COLOR_DAT = new Color(255, 193, 7);      
    private final Color COLOR_PINK = new Color(255, 182, 193);   
    private final Color TEXT_DARK = new Color(44, 56, 74);       
    private final Color BTN_YELLOW = new Color(255, 209, 102); 

    private BanDAO banDAO;
    private DonDatBanDAO donDatBanDAO; 
    private JPanel pnlDanhSachBan;
    private JPanel pnlFloorFilter;
    private JTextField txtSearch;
    private JLabel lblTimeDate;
    private Timer timer;
    private Timer autoCheckTimer; 
    
    private JButton btnDatBan;
    private Integer filterSoLuongKhach = null;
    private LocalDateTime filterThoiGianDat = null;

    private String floorFilter = "Tất cả";
    private String statusFilter = "Tất cả";

    private List<DonDatBan> dsDonDatHienTai = new ArrayList<>();

    public SoDoBanPanel_NVLT() {
        banDAO = new BanDAO();
        donDatBanDAO = new DonDatBanDAO(); 
        initUI();
        
        dsDonDatHienTai = donDatBanDAO.getAllDonDat(); 
        loadData("");
        startAutoCheckTimer(); 
    }

    private void startAutoCheckTimer() {
        autoCheckTimer = new Timer(30000, e -> {
            kiemTraVaCapNhatTrangThaiBan();
        });
        autoCheckTimer.start();
    }

    private void kiemTraVaCapNhatTrangThaiBan() {
        LocalDateTime bayGio = LocalDateTime.now();
        boolean coThayDoi = false;

        for (DonDatBan don : dsDonDatHienTai) {
            if (don.getTrangThai().equalsIgnoreCase("Đã hủy")) continue;

            LocalDateTime thoiDiemHen = LocalDateTime.of(don.getNgayDat(), don.getThoiGian());
            long soPhutConLai = Duration.between(bayGio, thoiDiemHen).toMinutes();

            if (soPhutConLai <= 120 && soPhutConLai > -30) {
                Ban b = banDAO.getBanByMa(don.getMaBan());
                if (b != null && b.getTrangThai().equalsIgnoreCase("Trống")) {
                    banDAO.updateTrangThaiBan(b.getMaBan(), "Đã đặt");
                    donDatBanDAO.updateTrangThaiDon(don.getMaDon(), "Đã đặt");
                    don.setTrangThai("Đã đặt");
                    coThayDoi = true;
                }
            }
            else if (soPhutConLai > 120) {
                Ban b = banDAO.getBanByMa(don.getMaBan());
                if (b != null && b.getTrangThai().equalsIgnoreCase("Đã đặt")) {
                    banDAO.updateTrangThaiBan(b.getMaBan(), "Trống");
                    coThayDoi = true;
                }
            }
        }
        if (coThayDoi) loadData(txtSearch.getText()); 
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
        lblTimeDate.setForeground(Color.BLACK);
        
        Locale localeVI = new Locale("vi", "VN");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("EEEE, 'ngày' dd/MM/yyyy - HH:mm:ss", localeVI);
        timer = new Timer(1000, e -> lblTimeDate.setText(LocalDateTime.now().format(dtf)));
        timer.start();
        
        timeBanner.add(lblTimeDate);
        topWrapper.add(timeBanner);
        topWrapper.add(Box.createRigidArea(new Dimension(0, 20)));

        JPanel titleActionPanel = new JPanel(new BorderLayout());
        titleActionPanel.setOpaque(false);

        JPanel pnlLeftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        pnlLeftActions.setOpaque(false);
        pnlLeftActions.add(createSpecialButton("Gộp bàn"));
        pnlLeftActions.add(createSpecialButton("Check-in"));
        
        btnDatBan = createSpecialButton("Đặt bàn");
        btnDatBan.addActionListener(e -> xuLyNutDatBan());
        pnlLeftActions.add(btnDatBan);

        titleActionPanel.add(pnlLeftActions, BorderLayout.WEST);

        JPanel btnActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        btnActions.setOpaque(false);
        txtSearch = new JTextField(15);
        txtSearch.setPreferredSize(new Dimension(220, 40));
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

        JPanel pnlFilterBar = new JPanel(new BorderLayout());
        pnlFilterBar.setOpaque(false);
        pnlFloorFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlFloorFilter.setOpaque(false);
        String[] floors = {"Tất cả", "Ngoài trời", "Phòng VIP", "Tầng 1", "Tầng 2"};
        for (String f : floors) pnlFloorFilter.add(createFloorButton(f));

        JPanel pnlStatus = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlStatus.setOpaque(false);
        pnlStatus.add(createStatusButton("Tất cả", Color.GRAY));
        pnlStatus.add(createStatusButton("Trống", COLOR_TRONG));
        pnlStatus.add(createStatusButton("Đã đặt", COLOR_DAT));
        pnlStatus.add(createStatusButton("Đang dùng", COLOR_DUNG));

        pnlFilterBar.add(pnlFloorFilter, BorderLayout.WEST);
        pnlFilterBar.add(pnlStatus, BorderLayout.EAST);

        topWrapper.add(pnlFilterBar);
        add(topWrapper, BorderLayout.NORTH);

        pnlDanhSachBan = new JPanel();
        pnlDanhSachBan.setLayout(new BoxLayout(pnlDanhSachBan, BoxLayout.Y_AXIS));
        pnlDanhSachBan.setOpaque(false);
        JScrollPane scrollPane = new JScrollPane(pnlDanhSachBan);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void xuLyNutDatBan() {
        if (filterSoLuongKhach != null || filterThoiGianDat != null) {
            filterSoLuongKhach = null;
            filterThoiGianDat = null;
            btnDatBan.setText("Đặt bàn");
            btnDatBan.setBackground(BTN_YELLOW);
            loadData(txtSearch.getText());
            return;
        }

        JPanel pnl = new JPanel(new GridLayout(3, 2, 10, 15));
        JTextField txtNgay = new JTextField(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        JTextField txtGio = new JTextField(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        JTextField txtSL = new JTextField("2");

        pnl.add(new JLabel("Ngày đặt (dd/MM/yyyy):")); pnl.add(txtNgay);
        pnl.add(new JLabel("Giờ đặt (HH:mm):")); pnl.add(txtGio);
        pnl.add(new JLabel("Số lượng khách:")); pnl.add(txtSL);

        int result = JOptionPane.showConfirmDialog(this, pnl, "Tìm Bàn Trống Phù Hợp", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                LocalDate ngay = LocalDate.parse(txtNgay.getText().trim(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                LocalTime gio = LocalTime.parse(txtGio.getText().trim(), DateTimeFormatter.ofPattern("HH:mm"));
                int sl = Integer.parseInt(txtSL.getText().trim());

                filterSoLuongKhach = sl;
                filterThoiGianDat = LocalDateTime.of(ngay, gio);

                btnDatBan.setText("Hủy lọc bàn");
                btnDatBan.setBackground(COLOR_PINK); 
                
                dsDonDatHienTai = donDatBanDAO.getAllDonDat();
                loadData(txtSearch.getText());
                
                JOptionPane.showMessageDialog(this, "Đã lọc các bàn trống phù hợp với yêu cầu của bạn!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Định dạng ngày giờ hoặc số lượng không hợp lệ!");
            }
        }
    }

    private JButton createSpecialButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(BTN_YELLOW); 
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setPreferredSize(new Dimension(130, 42));
        btn.putClientProperty("JButton.buttonType", "roundRect");
        btn.putClientProperty("JButton.arc", 15);
        return btn;
    }

    private JButton createFloorButton(String text) {
        JButton btn = new JButton(text);
        boolean isActive = text.equals(floorFilter);
        btn.setBackground(isActive ? COLOR_PINK : Color.WHITE);
        btn.putClientProperty("JButton.buttonType", "roundRect");
        btn.putClientProperty("JButton.arc", 15);
        btn.addActionListener(e -> {
            floorFilter = text;
            loadData(txtSearch.getText());
        });
        return btn;
    }

    private JButton createStatusButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(color); 
        btn.putClientProperty("JButton.buttonType", "roundRect");
        btn.putClientProperty("JButton.arc", 15);
        btn.addActionListener(e -> {
            statusFilter = text;
            loadData(txtSearch.getText());
        });
        return btn;
    }

    public void loadData(String query) {
        pnlDanhSachBan.removeAll();
        List<Ban> dsAll = banDAO.getAllBan();
        String[] dsKhuVuc = {"Ngoài trời", "Phòng VIP", "Tầng 1", "Tầng 2"};
        for (String khuVuc : dsKhuVuc) {
            if (!floorFilter.equals("Tất cả") && !floorFilter.equalsIgnoreCase(khuVuc)) continue;
            
            List<Ban> dsTheoKV = dsAll.stream()
                .filter(b -> b.getViTri().toLowerCase().contains(khuVuc.toLowerCase()))
                .filter(b -> b.getTenBan().toLowerCase().contains(query.toLowerCase()))
                .filter(b -> {
                    if (statusFilter.equals("Tất cả")) return true;
                    String tt = b.getTrangThai().toLowerCase();
                    if (statusFilter.equals("Trống")) return tt.contains("trống");
                    if (statusFilter.equals("Đã đặt")) return tt.contains("đặt");
                    if (statusFilter.equals("Đang dùng")) return tt.contains("dùng") || tt.contains("sử dụng");
                    return tt.equalsIgnoreCase(statusFilter);
                })
                .filter(b -> {
                    if (filterSoLuongKhach != null && b.getSoGhe() < filterSoLuongKhach) {
                        return false;
                    }
                    if (filterThoiGianDat != null) {
                        boolean biTrungLich = dsDonDatHienTai.stream()
                            .filter(d -> d.getMaBan().equals(b.getMaBan()) && !d.getTrangThai().equalsIgnoreCase("Đã hủy"))
                            .anyMatch(d -> {
                                LocalDateTime thoiGianHenCu = LocalDateTime.of(d.getNgayDat(), d.getThoiGian());
                                long phutCachNhau = Math.abs(Duration.between(thoiGianHenCu, filterThoiGianDat).toMinutes());
                                return phutCachNhau < 120; 
                            });
                        if (biTrungLich) return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());

            if (!dsTheoKV.isEmpty()) {
                JLabel lblKV = new JLabel(khuVuc.toUpperCase());
                lblKV.setFont(new Font("Segoe UI", Font.BOLD, 16));
                lblKV.setBorder(new EmptyBorder(10, 5, 10, 0));
                pnlDanhSachBan.add(lblKV);
                JPanel pnlGrid = new JPanel(new WrapLayout(FlowLayout.LEFT, 20, 20));
                pnlGrid.setOpaque(false);
                for (Ban b : dsTheoKV) pnlGrid.add(taoTheBan(b));
                pnlDanhSachBan.add(pnlGrid);
            }
        }
        pnlDanhSachBan.revalidate(); 
        pnlDanhSachBan.repaint();
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
                g2.dispose();
            }
        };
        card.setLayout(new OverlayLayout(card));
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(width, 140));
        card.setBackground(bg);

        JPanel pnlContent = new JPanel(new BorderLayout());
        pnlContent.setOpaque(false);

        JPanel pnlInfo = new JPanel(new GridLayout(2, 1));
        pnlInfo.setOpaque(false);
        JLabel lblTen = new JLabel(ban.getTenBan(), SwingConstants.CENTER); 
        lblTen.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTen.setForeground(bg == COLOR_DAT ? Color.BLACK : Color.WHITE);
        JLabel lblSub = new JLabel(ban.getSoGhe() + " ghế - " + ban.getMaBan(), SwingConstants.CENTER);
        lblSub.setForeground(bg == COLOR_DAT ? new Color(0,0,0,150) : new Color(255,255,255,180));
        pnlInfo.add(lblTen);
        pnlInfo.add(lblSub);
        
        pnlInfo.setBorder(new EmptyBorder(15, 0, 0, 0));
        pnlContent.add(pnlInfo, BorderLayout.CENTER);

        JPanel pnlOverlay;
        if (ban.getSoGhe() <= 4) {
            pnlOverlay = new JPanel(new GridBagLayout());
        } else {
            pnlOverlay = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 50));
        }
        pnlOverlay.setOpaque(false);
        pnlOverlay.setVisible(false); 

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 5, 0, 5); 
        gbc.gridy = 0;

        JButton btnTao = createActionBtn("Tạo");
        JButton btnSua = createActionBtn("Sửa");

        if (bg == COLOR_TRONG) {
            gbc.gridx = 0;
            pnlOverlay.add(btnTao, gbc);
        } else if (bg == COLOR_DAT) {
            gbc.gridx = 0;
            pnlOverlay.add(btnTao, gbc);
            gbc.gridx = 1;
            pnlOverlay.add(btnSua, gbc);
        }

        card.add(pnlOverlay);
        card.add(pnlContent);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    if (filterSoLuongKhach != null && filterThoiGianDat != null) {
                        if (bg != COLOR_DUNG) { 
                            boolean isVisible = !pnlOverlay.isVisible();
                            pnlOverlay.setVisible(isVisible);
                            pnlInfo.setVisible(!isVisible); 
                        }
                    }
                } else if (e.getClickCount() == 2) {
                    moChiTietDatBan(card, ban);
                }
            }
        });

        // --- XỬ LÝ NÚT TẠO CÓ TRUYỀN DỮ LIỆU BỘ LỌC ---
        btnTao.addActionListener(e -> {
            DialogTaoDonDat diag = new DialogTaoDonDat(SwingUtilities.getWindowAncestor(this), ban);
            
            // BẮN DỮ LIỆU SANG DIALOG TRƯỚC KHI HIỂN THỊ
            if (filterThoiGianDat != null && filterSoLuongKhach != null) {
                String strNgay = filterThoiGianDat.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                String strGio = filterThoiGianDat.format(DateTimeFormatter.ofPattern("HH:mm"));
                String strSL = String.valueOf(filterSoLuongKhach);
                
                diag.setDuLieuTuBoLoc(strNgay, strGio, strSL);
            }

            diag.setVisible(true); // Hiển thị Dialog
            
            if (diag.isThanhCong()) {
                try {
                    LocalDate ngayMoi = LocalDate.parse(diag.getNgay(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    LocalTime gioMoi = LocalTime.parse(diag.getGio(), DateTimeFormatter.ofPattern("HH:mm"));

                    boolean isHopLe = true;
                    dsDonDatHienTai = donDatBanDAO.getAllDonDat(); 
                    
                    for (DonDatBan donCu : dsDonDatHienTai) {
                        if (donCu.getMaBan().equals(ban.getMaBan()) 
                            && donCu.getNgayDat().equals(ngayMoi) 
                            && !donCu.getTrangThai().equalsIgnoreCase("Đã hủy")) {
                            
                            long phutCachNhau = Math.abs(Duration.between(donCu.getThoiGian(), gioMoi).toMinutes());
                            if (phutCachNhau < 120) {
                                DateTimeFormatter fTime = DateTimeFormatter.ofPattern("HH:mm");
                                String gioCu = donCu.getThoiGian().format(fTime);
                                String trucTruoc = donCu.getThoiGian().minusHours(2).format(fTime);
                                String sauĐo = donCu.getThoiGian().plusHours(2).format(fTime);
                                
                                JOptionPane.showMessageDialog(this, 
                                    "Bàn này đã có lịch đặt lúc " + gioCu + ".\n" +
                                    "Vui lòng chọn giờ đặt cách ít nhất 2 tiếng!\n" +
                                    "(Gợi ý: Đặt trước " + trucTruoc + " hoặc sau " + sauĐo + ")", 
                                    "Lỗi trùng lịch", JOptionPane.WARNING_MESSAGE);
                                isHopLe = false;
                                break;
                            }
                        }
                    }

                    if (!isHopLe) return; 

                    DonDatBan donMoi = new DonDatBan(
                        diag.getMaDon(), 
                        ban.getMaBan(), 
                        ngayMoi, 
                        gioMoi, 
                        Integer.parseInt(diag.getSoLuong()), 
                        diag.getGhiChu(), 
                        "Đang chờ"
                    );
                    
                    donMoi.setTenKhachHang(diag.getHoTen());
                    donMoi.setSoDienThoai(diag.getSdt());
                    
                    if (donDatBanDAO.insertDonDat(donMoi)) {
                        dsDonDatHienTai.add(donMoi); 
                        kiemTraVaCapNhatTrangThaiBan();
                        JOptionPane.showMessageDialog(this, "Đã tạo đơn thành công cho khách: " + diag.getHoTen());
                        
                        if (filterSoLuongKhach != null) {
                            filterSoLuongKhach = null;
                            filterThoiGianDat = null;
                            btnDatBan.setText("Đặt bàn");
                            btnDatBan.setBackground(BTN_YELLOW);
                        }
                        
                        loadData(txtSearch.getText()); 
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Lỗi định dạng ngày/giờ! Vui lòng nhập đúng dd/MM/yyyy và HH:mm");
                }
            }
        });
        
        // --- XỬ LÝ NÚT SỬA ---
        btnSua.addActionListener(e -> {
            dsDonDatHienTai = donDatBanDAO.getAllDonDat();
            List<DonDatBan> dsCuaBan = dsDonDatHienTai.stream()
                .filter(d -> d.getMaBan().equals(ban.getMaBan()))
                .collect(Collectors.toList());

            if (!dsCuaBan.isEmpty()) {
                DatBanDialog dialog = new DatBanDialog(SwingUtilities.getWindowAncestor(this), ban, dsCuaBan);
                dialog.setVisible(true);
                
                dsDonDatHienTai = donDatBanDAO.getAllDonDat();
                kiemTraVaCapNhatTrangThaiBan();
                loadData(txtSearch.getText());
            } else {
                JOptionPane.showMessageDialog(this, "Bàn này hiện chưa có đơn đặt!");
            }
        });

        return card;
    }

    private JButton createActionBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBackground(Color.WHITE);
        b.setPreferredSize(new Dimension(75, 30));
        b.putClientProperty("JButton.buttonType", "roundRect");
        return b;
    }

    private void moChiTietDatBan(Component parent, Ban ban) {
        dsDonDatHienTai = donDatBanDAO.getAllDonDat();
        List<DonDatBan> dsCuaBan = dsDonDatHienTai.stream()
            .filter(d -> d.getMaBan().equals(ban.getMaBan()))
            .collect(Collectors.toList());

        DatBanDialog dialog = new DatBanDialog(SwingUtilities.getWindowAncestor(parent), ban, dsCuaBan);
        dialog.setVisible(true);
        loadData(txtSearch.getText());
    }
}