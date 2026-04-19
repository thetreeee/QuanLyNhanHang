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
import java.time.LocalDateTime;
import java.time.Duration; 
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.Set;
import java.util.HashSet;

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
    private DonDatMon_DAO donDatMonDAO; // Gọi để gộp món nếu bàn đang ăn
    
    private JPanel pnlDanhSachBan;
    private JPanel pnlFloorFilter;
    private JTextField txtSearch;
    private JTextField txtTimSoGhe; 
    private JLabel lblErrorSoGhe; 
    
    private JLabel lblTimeDate;
    private Timer timer;
    private Timer autoCheckTimer; 
    
    private JButton btnDatBan;
    private Integer filterSoLuongKhach = null;
    private LocalDateTime filterThoiGianDat = null;

    private String floorFilter = "Tất cả";
    private String statusFilter = "Tất cả";

    private List<DonDatBan> dsDonDatHienTai = new ArrayList<>();
    private Set<String> cacDonDaCanhBao = new HashSet<>();

    private boolean isGopBanMode = false;
    private List<Ban> listBanGop = new ArrayList<>();
    private JButton btnGopBan;
    private JButton btnHuyGop;
    
    private String maNV;

    public SoDoBanPanel_NVLT(String maNV) {
        this.maNV = maNV;
        banDAO = new BanDAO();
        donDatBanDAO = new DonDatBanDAO(); 
        donDatMonDAO = new DonDatMon_DAO();
        
        initUI();
        
        dsDonDatHienTai = donDatBanDAO.getAllDonDat(); 
        loadData("");
        startAutoCheckTimer(); 

        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                // CHÌA KHÓA: Đợi cửa sổ hiển thị xong xuôi (có độ rộng thật) rồi mới vẽ bàn
                SwingUtilities.invokeLater(() -> {
                    if (txtSearch != null) {
                        loadData(txtSearch.getText().trim()); 
                    } else {
                        loadData("");
                    }
                });
            }
        });
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
            if (don.getTrangThai().equalsIgnoreCase("Đang chờ") || don.getTrangThai().equalsIgnoreCase("Đã đặt")) {
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

        // 1. Banner thời gian
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

        // 2. Các nút chức năng trái
        JPanel titleActionPanel = new JPanel(new BorderLayout());
        titleActionPanel.setOpaque(false);
        JPanel pnlLeftActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        pnlLeftActions.setOpaque(false);
        
        btnGopBan = createSpecialButton("Gộp bàn");
        btnGopBan.addActionListener(evt -> xuLyNutGopBan());
        pnlLeftActions.add(btnGopBan);
        
        btnHuyGop = createSpecialButton("Hủy thao tác");
        btnHuyGop.setBackground(Color.GRAY); btnHuyGop.setForeground(Color.WHITE); btnHuyGop.setVisible(false);
        btnHuyGop.addActionListener(evt -> exitGopBanMode());
        pnlLeftActions.add(btnHuyGop);
        
        JButton btnCheckIn = createSpecialButton("Check-in");
        btnCheckIn.addActionListener(evt -> {
            DialogCheckIn dialog = new DialogCheckIn(SwingUtilities.getWindowAncestor(this));
            dialog.setVisible(true);
            if (dialog.isThanhCong()) loadData(txtSearch.getText().trim());
        });
        pnlLeftActions.add(btnCheckIn);
        
        // ==========================================
        // ĐÃ THÊM NÚT CHUYỂN BÀN Ở ĐÂY
        // ==========================================
        JButton btnChuyenBan = createSpecialButton("Chuyển bàn");
        btnChuyenBan.addActionListener(evt -> xuLyNutChuyenBan());
        pnlLeftActions.add(btnChuyenBan);
        // ==========================================
        
        btnDatBan = createSpecialButton("Đặt bàn");
        btnDatBan.addActionListener(evt -> xuLyNutDatBan());
        pnlLeftActions.add(btnDatBan);
        titleActionPanel.add(pnlLeftActions, BorderLayout.WEST);

        // 3. Ô tìm kiếm phải
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

        // 4. Thanh lọc tầng và trạng thái
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

        // =====================================================================
        // GIẢI PHÁP ĐỘC QUYỀN: Dùng GridBagLayout + ScrollablePanel 
        // để fix cả lỗi tràn lề lẫn lỗi kẹp 1 dòng khi khởi động
        // =====================================================================
        pnlDanhSachBan = new ScrollablePanel(); 
        
        // Đã thay BoxLayout thành GridBagLayout
        pnlDanhSachBan.setLayout(new GridBagLayout()); 
        pnlDanhSachBan.setOpaque(false);
        
        JScrollPane scrollPane = new JScrollPane(pnlDanhSachBan);
        scrollPane.setBorder(null);
        scrollPane.setViewportBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(30); 
        
        // Khóa chặt thanh trượt ngang để chống tràn
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        
        add(scrollPane, BorderLayout.CENTER);
    }

	// =========================================================================
	// ĐÃ SỬA LẠI HOÀN TOÀN: GỘP BÀN BẰNG CỘT maKhoi TRONG DATABASE
	// =========================================================================
	private void xuLyNutGopBan() {
		if (!isGopBanMode) {
			isGopBanMode = true;
			listBanGop.clear();
			
			if (filterThoiGianDat != null) {
				btnGopBan.setText("Xác nhận Đặt Gộp");
			} else {
				btnGopBan.setText("Xác nhận khối");
			}
			
			btnGopBan.setBackground(new Color(40, 167, 69)); 
			btnGopBan.setForeground(Color.WHITE);
			btnHuyGop.setVisible(true); 
			loadData(txtSearch.getText().trim()); 
		} else {
			if (listBanGop.isEmpty()) {
				JOptionPane.showMessageDialog(this, "Vui lòng tick chọn ít nhất 1 bàn trên sơ đồ!", "Nhắc nhở", JOptionPane.WARNING_MESSAGE);
				return;
			}

			// --- TRƯỜNG HỢP 1: LÀM ĐƠN ĐẶT BÀN TRƯỚC (GỘP) ---
			if (filterThoiGianDat != null) {
				Ban firstBan = listBanGop.get(0); 
				String strNgay = filterThoiGianDat.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
				String strGio = filterThoiGianDat.format(DateTimeFormatter.ofPattern("HH:mm"));
				String strSL = filterSoLuongKhach != null ? String.valueOf(filterSoLuongKhach) : "2";

				DialogTaoDonDat diag = new DialogTaoDonDat(SwingUtilities.getWindowAncestor(this), firstBan, strNgay, strGio, strSL);
				diag.setVisible(true);
				
				if (diag.isThanhCong()) {
					try {
						LocalDate ngayMoi = LocalDate.parse(diag.getNgay(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
						LocalTime gioMoi = LocalTime.parse(diag.getGio(), DateTimeFormatter.ofPattern("HH:mm"));
						dsDonDatHienTai = donDatBanDAO.getAllDonDat(); 
						int index = 1;
						
						for (Ban b : listBanGop) {
							boolean isHopLe = true;
							for (DonDatBan donCu : dsDonDatHienTai) {
								if (donCu.getMaBan().equals(b.getMaBan()) && donCu.getNgayDat().equals(ngayMoi) && !donCu.getTrangThai().equalsIgnoreCase("Đã hủy")) {
									if (Math.abs(Duration.between(donCu.getThoiGian(), gioMoi).toMinutes()) < 120) {
										JOptionPane.showMessageDialog(this, "Bàn " + b.getMaBan() + " đã có lịch đặt!\nVui lòng chọn bàn khác.", "Lỗi trùng lịch", JOptionPane.WARNING_MESSAGE);
										isHopLe = false; break;
									}
								}
							}
							if (!isHopLe) continue; 

							String newMaDon = diag.getMaDon() + (listBanGop.size() > 1 ? "-" + index : "");
							DonDatBan donMoi = new DonDatBan(newMaDon, b.getMaBan(), ngayMoi, gioMoi, Integer.parseInt(diag.getSoLuong()), diag.getGhiChu(), "Đang chờ");
							donMoi.setTenKhachHang(diag.getHoTen());
							donMoi.setSoDienThoai(diag.getSdt());
							
							if (donDatBanDAO.insertDonDat(donMoi)) dsDonDatHienTai.add(donMoi); 
							index++;
						}
						
						if (donDatBanDAO.autoUpdateMauBan()) loadData(txtSearch.getText());
						JOptionPane.showMessageDialog(this, "Đã tạo đơn gộp thành công cho khách: " + diag.getHoTen());
						
						exitGopBanMode();
						filterSoLuongKhach = null; filterThoiGianDat = null;
						btnDatBan.setText("Đặt bàn"); btnDatBan.setBackground(BTN_YELLOW);
						loadData(txtSearch.getText()); 
					} catch (Exception ex) { JOptionPane.showMessageDialog(this, "Lỗi lưu dữ liệu!"); }
				}
			} 
			// --- TRƯỜNG HỢP 2: TẠO KHỐI BÀN ---
			else {
				if (listBanGop.size() < 2) {
					JOptionPane.showMessageDialog(this, "Vui lòng tick chọn ít nhất 2 bàn để ghép khối!", "Nhắc nhở", JOptionPane.WARNING_MESSAGE);
					return;
				}

                boolean tatCaDeuTrong = listBanGop.stream().noneMatch(b -> 
                    b.getTrangThai().equalsIgnoreCase("Đang dùng") || b.getTrangThai().equalsIgnoreCase("Đã đặt")
                );

                List<String> dsMaBan = listBanGop.stream().map(Ban::getMaBan).collect(Collectors.toList());

                // Nếu TẤT CẢ CÁC BÀN ĐỀU ĐANG TRỐNG -> Chỉ cần Database BanDAO tạo khối là xong, siêu nhàn!
                if (tatCaDeuTrong) {
                    String maBanChinh = listBanGop.get(0).getMaBan();
                    banDAO.taoKhoiBan(dsMaBan, maBanChinh); 
                    
                    JOptionPane.showMessageDialog(this, "Đã tạo Khối bàn thành công! Số đánh dấu đã được hiển thị.");
                    exitGopBanMode();
                    return;
                }

                // Nếu có bàn đang ăn (Có phiếu Gọi món) -> Cho Lễ tân chọn Bàn chính, rồi gộp Phiếu Gọi Món, sau đó gộp Khối
				JComboBox<String> cbMain = new JComboBox<>();
				for (Ban b : listBanGop) {
					cbMain.addItem(b.getMaBan() + " - " + b.getTenBan());
				}

				JPanel pnl = new JPanel(new BorderLayout(0, 10));
				pnl.add(new JLabel("Vui lòng chọn Bàn Chính (Hóa đơn của các bàn khác sẽ dồn về đây):"), BorderLayout.NORTH);
				pnl.add(cbMain, BorderLayout.CENTER);

				int confirm = JOptionPane.showConfirmDialog(this, pnl, "Xác nhận tạo khối", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
				
				if (confirm == JOptionPane.OK_OPTION) {
					String selected = cbMain.getSelectedItem().toString();
					String maBanChinh = selected.split(" - ")[0];
					List<String> listBanPhu = dsMaBan.stream().filter(m -> !m.equals(maBanChinh)).collect(Collectors.toList());

                    // Gọi DonDatMon_DAO để dồn món ăn sang Phiếu của Bàn Chính
					boolean success = donDatMonDAO.gopDonDatMon(maBanChinh, listBanPhu); 
					
					if (success) {
                        // Gọi BanDAO để khóa các bàn này thành 1 Khối trong DB
						banDAO.taoKhoiBan(dsMaBan, maBanChinh);
						JOptionPane.showMessageDialog(this, "Ghép khối bàn thành công!");
						exitGopBanMode(); 
					} else {
						JOptionPane.showMessageDialog(this, "Có lỗi xảy ra khi xử lý khối ở Database!", "Lỗi", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		}
	}

    private void exitGopBanMode() {
        isGopBanMode = false;
        listBanGop.clear();
        btnGopBan.setText("Gộp bàn");
        btnGopBan.setBackground(BTN_YELLOW);
        btnGopBan.setForeground(Color.BLACK);
        btnHuyGop.setVisible(false);
        loadData(txtSearch.getText().trim());
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
        // Nút này đang ở trạng thái "Hủy lọc", bấm vào thì reset về ban đầu
        if (filterSoLuongKhach != null || filterThoiGianDat != null) {
            filterSoLuongKhach = null;
            filterThoiGianDat = null;
            btnDatBan.setText("Đặt bàn");
            btnDatBan.setBackground(BTN_YELLOW);
            loadData(txtSearch.getText());
            return;
        }

        // Khởi tạo các ô nhập liệu
        JTextField txtNgay = new JTextField(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        JTextField txtGio = new JTextField(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        JTextField txtSL = new JTextField("2");

        JPanel pnl = new JPanel(new GridLayout(3, 2, 10, 15));
        pnl.add(new JLabel("Ngày đặt (dd/MM/yyyy):")); pnl.add(txtNgay);
        pnl.add(new JLabel("Giờ đặt (HH:mm):")); pnl.add(txtGio);
        pnl.add(new JLabel("Số lượng khách:")); pnl.add(txtSL);

        // Vòng lặp giúp giữ nguyên Form nếu người dùng nhập sai
        while (true) {
            int result = JOptionPane.showConfirmDialog(this, pnl, "Tìm Bàn Trống Phù Hợp", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            
            if (result == JOptionPane.OK_OPTION) {
                try {
                    LocalDate ngay = LocalDate.parse(txtNgay.getText().trim(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    LocalTime gio = LocalTime.parse(txtGio.getText().trim(), DateTimeFormatter.ofPattern("HH:mm"));
                    int sl = Integer.parseInt(txtSL.getText().trim());

                    // ==========================================================
                    // VALIDATE: CHẶN TÌM BÀN Ở QUÁ KHỨ NGAY TẠI FORM LỌC
                    // ==========================================================
                    LocalDateTime thoiGianChon = LocalDateTime.of(ngay, gio);
                    if (thoiGianChon.isBefore(LocalDateTime.now())) {
                        JOptionPane.showMessageDialog(this, 
                            "Thời gian không hợp lệ!", 
                            "Cảnh báo", 
                            JOptionPane.WARNING_MESSAGE);
                        continue; // Lỗi thì quay lại vòng lặp (hiện lại Form để sửa)
                    }

                    // Nếu hợp lệ thì tiến hành lọc sơ đồ bàn
                    filterSoLuongKhach = sl;
                    filterThoiGianDat = thoiGianChon;

                    btnDatBan.setText("Hủy lọc bàn");
                    btnDatBan.setBackground(COLOR_PINK); 
                    
                    dsDonDatHienTai = donDatBanDAO.getAllDonDat();
                    loadData(txtSearch.getText());
                    break; // Thành công thì thoát vòng lặp đóng form

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, 
                        "Định dạng ngày/giờ không hợp lệ!\nVí dụ chuẩn: 19/04/2026 và 14:30", 
                        "Lỗi nhập liệu", 
                        JOptionPane.ERROR_MESSAGE);
                    continue; // Nhập chữ bậy bạ cũng quay lại form
                }
            } else {
                break; // Người dùng bấm Cancel (Hủy bỏ) thì thoát vòng lặp
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

    // ĐÃ XÓA HÀM updateMapBanGopId() VÌ BÂY GIỜ CHÚNG TA KHÔNG CẦN QUÉT TÌM CHỮ (GỒM:) NỮA.

    public void loadData(String query) {
        pnlDanhSachBan.removeAll();
        
        // Thiết lập bộ quy tắc cho GridBagLayout
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
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
                            if (don.getMaBan().equals(b.getMaBan()) && !don.getTrangThai().equalsIgnoreCase("Đã hủy")) {
                                LocalDateTime thoiGianDat = LocalDateTime.of(don.getNgayDat(), don.getThoiGian());
                                long phutTre = java.time.Duration.between(thoiGianDat, now).toMinutes();
                                if (phutTre > 15) return true;
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
                    // 1. Lọc theo số lượng khách
                    if (filterSoLuongKhach != null && !isGopBanMode) {
                        int minSize = (filterSoLuongKhach % 2 == 0) ? filterSoLuongKhach : filterSoLuongKhach + 1;
                        if (b.getSoGhe() < minSize) return false;
                    }
                    
                    // ======================================================================
                    // 2. ĐÃ SỬA LỖI: Lọc ẩn luôn các bàn bị trùng lịch trong vòng 2 tiếng
                    // ======================================================================
                    if (filterThoiGianDat != null && !isGopBanMode) {
                        LocalDate filterNgay = filterThoiGianDat.toLocalDate();
                        LocalTime filterGio = filterThoiGianDat.toLocalTime();
                        
                        for (DonDatBan don : dsDonDatHienTai) {
                            // Nếu bàn đang xét trùng mã với một đơn đặt nào đó chưa bị hủy
                            if (don.getMaBan().equals(b.getMaBan()) && !don.getTrangThai().equalsIgnoreCase("Đã hủy")) {
                                // Và cùng ngày đặt
                                if (don.getNgayDat().equals(filterNgay)) {
                                    // Kiểm tra khoảng cách giờ
                                    long diff = Math.abs(Duration.between(don.getThoiGian(), filterGio).toMinutes());
                                    if (diff < 120) {
                                        return false; // Ẩn luôn bàn này khỏi sơ đồ!
                                    }
                                }
                            }
                        }
                    }
                    
                    return true;
                })
                .sorted((b1, b2) -> Integer.compare(b1.getSoGhe(), b2.getSoGhe()))
                .collect(Collectors.toList());

            if (!dsTheoKV.isEmpty()) {
                // 1. Thêm Tiêu Đề Khu Vực
                JPanel pnlKVTitle = new JPanel(new FlowLayout(FlowLayout.LEFT));
                pnlKVTitle.setOpaque(false);
                JLabel lblKV = new JLabel("--- " + khuVuc.toUpperCase() + " ---");
                lblKV.setFont(new Font("Segoe UI", Font.BOLD, 18));
                lblKV.setForeground(new Color(150, 50, 50));
                lblKV.setBorder(new EmptyBorder(10, 10, 5, 0));
                pnlKVTitle.add(lblKV);
                
                gbc.gridy++; // Xuống dòng
                pnlDanhSachBan.add(pnlKVTitle, gbc);
                
                // 2. Thêm Lưới Bàn
                JPanel pnlGrid = new JPanel(new WrapLayout(FlowLayout.LEFT, 20, 20));
                pnlGrid.setOpaque(false);
                for (Ban b : dsTheoKV) {
                    pnlGrid.add(taoTheBan(b));
                }
                
                gbc.gridy++; // Xuống dòng
                pnlDanhSachBan.add(pnlGrid, gbc);
            }
        }
        
        // 3. Đệm dưới cùng (Filler): Đẩy tất cả các tầng lên sát phía trên
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
                
                if (isGopBanMode) {
                    int size = 26;
                    int x = 10;
                    int y = 10;
                    boolean isSelected = listBanGop.stream().anyMatch(b -> b.getMaBan().equals(ban.getMaBan()));
                    
                    if (isSelected) {
                        g2.setColor(new Color(0, 122, 255)); 
                        g2.fillOval(x, y, size, size);
                        g2.setColor(Color.WHITE);
                        g2.setStroke(new BasicStroke(3));
                        g2.drawLine(x + 7, y + 13, x + 11, y + 18);
                        g2.drawLine(x + 11, y + 18, x + 19, y + 8);
                    } else {
                        g2.setColor(new Color(255, 255, 255, 200));
                        g2.fillOval(x, y, size, size);
                        g2.setColor(Color.GRAY);
                        g2.setStroke(new BasicStroke(2));
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

        JPanel pnlContent = new JPanel(new BorderLayout());
        pnlContent.setOpaque(false);

        int warningLevel = 0; 
        
        if (bg == COLOR_DAT) {
            LocalDateTime now = LocalDateTime.now();
            for (DonDatBan don : dsDonDatHienTai) {
                if (don.getMaBan().equals(ban.getMaBan()) && !don.getTrangThai().equalsIgnoreCase("Đã hủy")) {
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

        // =========================================================================
        // ĐÃ ĐƠN GIẢN HÓA CỰC KỲ: CHỈ CẦN GET MAKHOI TỪ ĐỐI TƯỢNG BAN
        // =========================================================================
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
            lblGroup.setToolTipText("Bàn thuộc Nhóm Khối số " + gId);
            pnlTopRight.add(lblGroup);
        }

        if (warningLevel > 0) {
            final int finalWarningLevel = warningLevel;
            final Color alertBgColor = (warningLevel == 1) ? Color.YELLOW : Color.RED;
            final Color alertFgColor = (warningLevel == 1) ? Color.RED : Color.WHITE;
            final String tooltipMsg = (warningLevel == 1) ? "Sắp đến giờ nhận bàn!" : "Khách đã trễ giờ nhận bàn!";
            
            JLabel lblWarning = new JLabel("!", SwingConstants.CENTER) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(alertBgColor); 
                    g2.fillOval(0, 0, getWidth(), getHeight());
                    g2.setColor(Color.RED); 
                    if (finalWarningLevel == 2) g2.setColor(Color.WHITE);
                    g2.setStroke(new BasicStroke(2));
                    g2.drawOval(1, 1, getWidth() - 2, getHeight() - 2);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            lblWarning.setFont(new Font("Arial", Font.BOLD, 16));
            lblWarning.setForeground(alertFgColor); 
            lblWarning.setPreferredSize(new Dimension(24, 24));
            lblWarning.setToolTipText(tooltipMsg);
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

        JPanel pnlOverlay;
        if (ban.getSoGhe() <= 4) pnlOverlay = new JPanel(new GridBagLayout());
        else pnlOverlay = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 50));
        
        pnlOverlay.setOpaque(false);
        pnlOverlay.setVisible(false); 

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 5, 0, 5); 
        gbc.gridy = 0;

        boolean isFiltered = (filterSoLuongKhach != null && filterThoiGianDat != null);
        JButton btnMoBan = createActionBtn("Mở bàn");
        
        // =========================================================================================
        // ĐÃ NÂNG CẤP LẠI NÚT "VỀ TRỐNG": GIẢI TÁN KHỐI TRỰC TIẾP TỪ DATABASE
        // =========================================================================================
        JButton btnChuyenTrong = createActionBtn("Về Trống");
        btnChuyenTrong.addActionListener(evt -> {
            dsDonDatHienTai = donDatBanDAO.getAllDonDat();
            boolean dangCoDonDat = dsDonDatHienTai.stream()
                .anyMatch(d -> d.getMaBan().equals(ban.getMaBan()) && (d.getTrangThai().equalsIgnoreCase("Checked-in") || d.getTrangThai().equalsIgnoreCase("Đang dùng")));

            if (dangCoDonDat) {
                JOptionPane.showMessageDialog(SoDoBanPanel_NVLT.this, 
                    "Thao tác bị từ chối!\nBàn " + ban.getTenBan() + " đang dính ĐƠN ĐẶT BÀN (của Lễ tân).\n\n" +
                    "👉 Vui lòng vào menu 'Danh sách đặt bàn' bên trái để chuyển Đơn Đặt Bàn này sang trạng thái 'Hoàn thành' hoặc 'Đã hủy' trước khi dọn bàn.", 
                    "Bảo vệ dữ liệu", JOptionPane.ERROR_MESSAGE);
                return; 
            }

            int confirm = JOptionPane.showConfirmDialog(SoDoBanPanel_NVLT.this, "Chuyển bàn " + ban.getTenBan() + " về trạng thái Trống?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                
                // Nếu bàn này đang thuộc 1 khối -> Giải tán nguyên khối (chỉ bằng 1 dòng DAO)
                if (ban.getMaKhoi() != null) {
                    banDAO.giaiTanKhoi(ban.getMaKhoi());
                } else {
                    // Nếu là bàn đơn -> Trả về trống bình thường
                    banDAO.updateTrangThaiBan(ban.getMaBan(), "Trống");
                }
                
                loadData(txtSearch.getText());
            }
        });

        JButton btnDatTruoc = createActionBtn("Đặt trước");

        if (bg == COLOR_TRONG) {
            if (isFiltered) { gbc.gridx = 0; pnlOverlay.add(btnDatTruoc, gbc); } 
            else { gbc.gridx = 0; pnlOverlay.add(btnMoBan, gbc); }
        } else if (bg == COLOR_DUNG) {
            gbc.gridx = 0; pnlOverlay.add(btnChuyenTrong, gbc);
        } else if (bg == COLOR_DAT) {
            gbc.gridx = 0; pnlOverlay.add(btnMoBan, gbc);
        }

        card.add(pnlOverlay);
        card.add(pnlContent);

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (isGopBanMode) {
                    boolean alreadySelected = listBanGop.stream().anyMatch(b -> b.getMaBan().equals(ban.getMaBan()));
                    if (alreadySelected) {
                        listBanGop.removeIf(b -> b.getMaBan().equals(ban.getMaBan()));
                    } else {
                        if (filterThoiGianDat == null && ban.getTrangThai().equalsIgnoreCase("Đã đặt")) {
                            long countDat = listBanGop.stream().filter(b -> b.getTrangThai().equalsIgnoreCase("Đã đặt")).count();
                            if (countDat >= 1) {
                                JOptionPane.showMessageDialog(SoDoBanPanel_NVLT.this, 
                                    "Không thể ghép các bàn 'Đã đặt' với nhau!\nChỉ được tối đa 1 bàn 'Đã đặt' trong nhóm ghép.", 
                                    "Lỗi quy tắc", JOptionPane.WARNING_MESSAGE);
                                return;
                            }
                        }
                        listBanGop.add(ban);
                    }
                    card.repaint(); 
                    return; 
                }
                
                if (e.getClickCount() == 1) {
                    boolean isVisible = !pnlOverlay.isVisible();
                    pnlOverlay.setVisible(isVisible);
                    pnlInfo.setVisible(!isVisible); 
                } else if (e.getClickCount() == 2) {
                    xemChiTietDatBan(card, ban); 
                }
            }
        });

        btnMoBan.addActionListener(evt -> {
            boolean canWarning = false;
            String warningMsg = "";
            boolean isKhachTre = false;
            String maDonBiTre = "";
            
            LocalDateTime now = LocalDateTime.now();
            dsDonDatHienTai = donDatBanDAO.getAllDonDat();

            for (DonDatBan don : dsDonDatHienTai) {
                if (don.getMaBan().equals(ban.getMaBan()) && 
                   (don.getTrangThai().equalsIgnoreCase("Đang chờ") || don.getTrangThai().equalsIgnoreCase("Đã đặt"))) {
                    
                    if (don.getNgayDat().equals(LocalDate.now())) {
                        LocalDateTime thoiGianDat = LocalDateTime.of(don.getNgayDat(), don.getThoiGian());
                        long phutConLai = Duration.between(now, thoiGianDat).toMinutes();

                        DateTimeFormatter fTime = DateTimeFormatter.ofPattern("HH:mm");

                        if (phutConLai >= 0 && phutConLai <= 120) {
                            canWarning = true;
                            warningMsg = "CẢNH BÁO: Bàn này đã được đặt vào lúc " + don.getThoiGian().format(fTime) + "!\n\n"
                                       + "Khách vãng lai phải trả bàn TRƯỚC " + don.getThoiGian().minusMinutes(15).format(fTime) + ".\nBạn có chắc chắn muốn ép mở bàn không?";
                            break;
                        }
                        else if (phutConLai < 0 && phutConLai >= -120) {
                            canWarning = true;
                            isKhachTre = true;
                            maDonBiTre = don.getMaDon(); 
                            warningMsg = "CẢNH BÁO: Bàn này có khách đặt lúc " + don.getThoiGian().format(fTime) + " nhưng đang ĐẾN TRỄ!\n\n"
                                       + "Nếu bạn mở bàn cho khách vãng lai, hệ thống sẽ TỰ ĐỘNG HỦY đơn đặt bàn của khách đến trễ.\nBạn có chắc chắn muốn ép mở bàn không?";
                            break;
                        }
                    }
                }
            }

            if (canWarning) {
                int confirm = JOptionPane.showConfirmDialog(SoDoBanPanel_NVLT.this, warningMsg, "Cảnh báo xung đột", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    if (isKhachTre && !maDonBiTre.isEmpty()) {
                        donDatBanDAO.updateTrangThaiCuaDon(maDonBiTre, "Đã hủy"); 
                    }
                    if (banDAO.updateTrangThaiBan(ban.getMaBan(), "Đang dùng")) loadData(txtSearch.getText());
                }
            } else {
                int confirm = JOptionPane.showConfirmDialog(SoDoBanPanel_NVLT.this, "Bạn có chắc chắn muốn mở bàn " + ban.getTenBan() + " cho khách vãng lai?", "Xác nhận mở bàn", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    if (banDAO.updateTrangThaiBan(ban.getMaBan(), "Đang dùng")) loadData(txtSearch.getText());
                }
            }
        });

        btnDatTruoc.addActionListener(evt -> {
            String strNgay = null; String strGio = null; String strSL = null;
            if (filterThoiGianDat != null) {
                strNgay = filterThoiGianDat.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                strGio = filterThoiGianDat.format(DateTimeFormatter.ofPattern("HH:mm"));
            }
            if (filterSoLuongKhach != null) strSL = String.valueOf(filterSoLuongKhach);

            DialogTaoDonDat diag = new DialogTaoDonDat(SwingUtilities.getWindowAncestor(this), ban, strNgay, strGio, strSL);
            diag.setVisible(true);
            
            if (diag.isThanhCong()) {
                try {
                    LocalDate ngayMoi = LocalDate.parse(diag.getNgay(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    LocalTime gioMoi = LocalTime.parse(diag.getGio(), DateTimeFormatter.ofPattern("HH:mm"));
                    boolean isHopLe = true;
                    dsDonDatHienTai = donDatBanDAO.getAllDonDat(); 
                    
                    for (DonDatBan donCu : dsDonDatHienTai) {
                        if (donCu.getMaBan().equals(ban.getMaBan()) && donCu.getNgayDat().equals(ngayMoi) && !donCu.getTrangThai().equalsIgnoreCase("Đã hủy")) {
                            if (Math.abs(Duration.between(donCu.getThoiGian(), gioMoi).toMinutes()) < 120) {
                                JOptionPane.showMessageDialog(this, "Bàn này đã có lịch đặt lúc " + donCu.getThoiGian().format(DateTimeFormatter.ofPattern("HH:mm")) + ".\nVui lòng chọn giờ đặt cách ít nhất 2 tiếng!", "Lỗi trùng lịch", JOptionPane.WARNING_MESSAGE);
                                isHopLe = false; break;
                            }
                        }
                    }

                    if (!isHopLe) return; 

                    DonDatBan donMoi = new DonDatBan(diag.getMaDon(), ban.getMaBan(), ngayMoi, gioMoi, Integer.parseInt(diag.getSoLuong()), diag.getGhiChu(), "Đang chờ");
                    donMoi.setTenKhachHang(diag.getHoTen());
                    donMoi.setSoDienThoai(diag.getSdt());
                    
                    if (donDatBanDAO.insertDonDat(donMoi)) {
                        dsDonDatHienTai.add(donMoi); 
                        if (donDatBanDAO.autoUpdateMauBan()) loadData(txtSearch.getText());
                        JOptionPane.showMessageDialog(this, "Đã tạo đơn thành công cho khách: " + diag.getHoTen());
                        if (filterSoLuongKhach != null) {
                            filterSoLuongKhach = null; filterThoiGianDat = null;
                            btnDatBan.setText("Đặt bàn"); btnDatBan.setBackground(BTN_YELLOW);
                        }
                        loadData(txtSearch.getText()); 
                    }
                } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Lỗi định dạng ngày/giờ!"); }
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

    private void xemChiTietDatBan(Component parent, Ban ban) {
        dsDonDatHienTai = donDatBanDAO.getAllDonDat();
        List<DonDatBan> dsCuaBan = dsDonDatHienTai.stream().filter(d -> d.getMaBan().equals(ban.getMaBan())).collect(Collectors.toList());
        DatBanDialog dialog = new DatBanDialog(SwingUtilities.getWindowAncestor(parent), ban, dsCuaBan);
        dialog.setVisible(true);
        loadData(txtSearch.getText());
    }
 // Thêm class này vào cuối file của bạn
    class ScrollablePanel extends JPanel implements Scrollable {
        public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) { return 20; }
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) { return 20; }
        
        // ĐÂY LÀ DÒNG QUAN TRỌNG NHẤT: Ép chiều ngang luôn bằng khung nhìn
        public boolean getScrollableTracksViewportWidth() { return true; }
        public boolean getScrollableTracksViewportHeight() { return false; }
    }
 // =========================================================================
    // TÍNH NĂNG MỚI: XỬ LÝ CHUYỂN BÀN
    // =========================================================================
 // =========================================================================
    // TÍNH NĂNG MỚI: XỬ LÝ CHUYỂN BÀN (CÓ CHUYỂN KÈM ĐƠN HÀNG/HÓA ĐƠN)
    // =========================================================================
    private void xuLyNutChuyenBan() {
        List<Ban> dsAll = banDAO.getAllBan();
        
        // 1. Lấy danh sách bàn ĐANG DÙNG (để làm Bàn Nguồn)
        List<Ban> dsTuBan = dsAll.stream()
            .filter(b -> b.getTrangThai().toLowerCase().contains("dùng") || b.getTrangThai().toLowerCase().contains("sử dụng"))
            .collect(Collectors.toList());

        if (dsTuBan.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Hiện không có bàn nào đang được sử dụng để chuyển!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // 2. Lấy danh sách bàn TRỐNG và KHÔNG BỊ TRÙNG LỊCH ĐẶT (để làm Bàn Đích)
        LocalDateTime now = LocalDateTime.now();
        dsDonDatHienTai = donDatBanDAO.getAllDonDat(); 
        
        List<Ban> dsDenBan = dsAll.stream()
            .filter(b -> b.getTrangThai().toLowerCase().contains("trống"))
            .filter(b -> {
                boolean biTrungLich = dsDonDatHienTai.stream()
                    .filter(d -> d.getMaBan().equals(b.getMaBan()) && !d.getTrangThai().equalsIgnoreCase("Đã hủy"))
                    .anyMatch(d -> {
                        if (d.getNgayDat().equals(LocalDate.now())) {
                            LocalDateTime thoiGianDat = LocalDateTime.of(d.getNgayDat(), d.getThoiGian());
                            long phutConLai = Duration.between(now, thoiGianDat).toMinutes();
                            return phutConLai > -120 && phutConLai <= 120; // Chặn bàn sắp có khách
                        }
                        return false;
                    });
                return !biTrungLich; 
            })
            .sorted((b1, b2) -> Integer.compare(b1.getSoGhe(), b2.getSoGhe())) 
            .collect(Collectors.toList());

        if (dsDenBan.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Hiện không có bàn trống nào phù hợp!\n(Hoặc các bàn trống đều đã có khách đặt trước trong 2 giờ tới)", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 3. Tạo Giao diện chọn bàn
        JComboBox<String> cbTuBan = new JComboBox<>();
        for (Ban b : dsTuBan) cbTuBan.addItem(b.getMaBan() + " - " + b.getTenBan() + " (" + b.getSoGhe() + " ghế)");

        JComboBox<String> cbDenBan = new JComboBox<>();
        for (Ban b : dsDenBan) cbDenBan.addItem(b.getMaBan() + " - " + b.getTenBan() + " (" + b.getSoGhe() + " ghế)");

        JPanel pnl = new JPanel(new GridLayout(2, 2, 10, 15));
        pnl.add(new JLabel("Chuyển TỪ BÀN (Đang dùng):"));
        pnl.add(cbTuBan);
        pnl.add(new JLabel("Đến BÀN MỚI (Trống):"));
        pnl.add(cbDenBan);

        int result = JOptionPane.showConfirmDialog(this, pnl, "Thao Tác Chuyển Bàn", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String maTuBan = cbTuBan.getSelectedItem().toString().split(" - ")[0];
            String maDenBan = cbDenBan.getSelectedItem().toString().split(" - ")[0];

            int confirm = JOptionPane.showConfirmDialog(this, "Xác nhận chuyển khách và TOÀN BỘ ĐƠN HÀNG từ " + maTuBan + " sang " + maDenBan + "?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    // BƯỚC QUAN TRỌNG: Gọi DAO để chuyển mã bàn trong bảng Hóa Đơn/Order
                    // Đảm bảo bạn đã viết hàm chuyenBan trong DonDatMon_DAO (Xem hướng dẫn Bước 2)
                    boolean chuyenBillThanhCong = donDatMonDAO.chuyenBan(maTuBan, maDenBan);
                    
                    if (chuyenBillThanhCong) {
                        // Nếu dời bill thành công thì mới đổi màu (trạng thái) bàn
                        banDAO.updateTrangThaiBan(maTuBan, "Trống");
                        banDAO.updateTrangThaiBan(maDenBan, "Đang dùng");
                        
                        JOptionPane.showMessageDialog(this, "Chuyển bàn và dời đơn hàng thành công!");
                        loadData(txtSearch.getText().trim());
                    } else {
                        JOptionPane.showMessageDialog(this, "Có lỗi xảy ra: Không tìm thấy hóa đơn chưa thanh toán của bàn " + maTuBan, "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Lỗi hệ thống: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}