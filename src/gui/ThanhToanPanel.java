package gui;

import dao.BanDAO;
import dao.DonDatBanDAO;
import dao.DonDatMon_DAO;
import dao.ChiTietDatMon_DAO;
import dao.KhuyenMai_DAO;
import dao.HoaDon_DAO;
import dao.ChiTietHoaDon_DAO;
import entity.Ban;
import entity.DonDatMon;
import entity.ChiTietDatMon;
import entity.KhuyenMai;
import entity.HoaDon;
import connectDB.SQLConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThanhToanPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private final Color BG_APP = new Color(255, 245, 240); 
	private final Color COLOR_TEXT_DARK = new Color(30, 30, 30);
	private final Color COLOR_RED_ACCENT = new Color(220, 53, 69);
	
	private CardLayout cardLayoutDetail;
	private JPanel pnlDetailContainer, pnlGridBan;
	private JLabel lblTimeDate;
	private JTextField txtSearch;
	private JButton btnTim, btnLamMoi, btnGopDon;
	private Timer timer;

	private List<JButton> listBanButtons = new ArrayList<>();
	private JButton btnActiveBan = null;

	private JLabel lblMaDonInfo, lblMaBanInfo, lblMaNVInfo, lblThoiGianInfo, lblGhiChuInfo;
	private JTable tblChiTiet;
	private DefaultTableModel modelChiTiet;

	private JComboBox<String> cbKhuyenMai, cbPhuongThuc;
	private JLabel lblTongTien, lblGiamGia, lblVAT, lblKhachCanTra;
	private JPanel pnlQRCode;
	private JCheckBox chkInHoaDon;
	private JButton btnXacNhan, btnHuy;

	private String maNV;
	private double tongTienHienTai = 0, khachCanTraHienTra = 0;
	private DecimalFormat df = new DecimalFormat("#,### đ");

	private BanDAO banDAO = new BanDAO();
	private DonDatMon_DAO donDatMonDAO = new DonDatMon_DAO();
	private ChiTietDatMon_DAO chiTietDatMonDAO = new ChiTietDatMon_DAO();
	private KhuyenMai_DAO khuyenMaiDAO = new KhuyenMai_DAO();
	private HoaDon_DAO hoaDonDAO = new HoaDon_DAO();
	private ChiTietHoaDon_DAO ctHoaDonDAO = new ChiTietHoaDon_DAO();
	private DonDatBanDAO donDatBanDAO = new DonDatBanDAO();

	private List<KhuyenMai> listKhuyenMaiHienTai = new ArrayList<>();
	private boolean isUpdatingKhuyenMai = false; 

	public ThanhToanPanel(String maNV) {
		this.maNV = maNV;
		setLayout(new BorderLayout(0, 15));
		setBackground(BG_APP);
		setBorder(new EmptyBorder(15, 25, 15, 25));

		add(createTopPanel(), BorderLayout.NORTH);
		
		pnlDetailContainer = new JPanel(cardLayoutDetail = new CardLayout());
		pnlDetailContainer.setOpaque(false);
		pnlDetailContainer.add(createEmptyPanel(), "EMPTY");
		pnlDetailContainer.add(createDetailSplitPanel(), "DETAIL");
		add(pnlDetailContainer, BorderLayout.CENTER);

		startClock();
		loadDuLieuBanTuDB();
		setupListeners();

		this.addComponentListener(new java.awt.event.ComponentAdapter() {
			public void componentShown(java.awt.event.ComponentEvent e) {
				loadDuLieuBanTuDB();
				cardLayoutDetail.show(pnlDetailContainer, "EMPTY");
				btnActiveBan = null;
			}
		});
	}

	private JPanel createTopPanel() {
		JPanel pnl = new JPanel(); 
		pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS)); 
		pnl.setOpaque(false);

		lblTimeDate = new JLabel("Đang tải..."); 
		lblTimeDate.setFont(new Font("Segoe UI", Font.PLAIN, 15));
		JPanel pT = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 8)); 
		pT.setBackground(new Color(255, 235, 235)); 
		pT.putClientProperty("FlatLaf.style", "arc: 10");
		pT.setMaximumSize(new Dimension(500, 40));
		pT.add(lblTimeDate);
		
		JPanel pTimeWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		pTimeWrapper.setOpaque(false);
		pTimeWrapper.add(pT);
		pnl.add(pTimeWrapper);
		pnl.add(Box.createRigidArea(new Dimension(0, 15)));

		JLabel lblT = new JLabel("THANH TOÁN HÓA ĐƠN"); 
		lblT.setFont(new Font("Segoe UI", Font.BOLD, 28));
		lblT.setForeground(COLOR_TEXT_DARK);
		JPanel pTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)); 
		pTitle.setOpaque(false); 
		pTitle.add(lblT);
		pnl.add(pTitle);
		pnl.add(Box.createRigidArea(new Dimension(0, 15)));

		txtSearch = new JTextField(25); 
		txtSearch.setPreferredSize(new Dimension(250, 38));
		txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 15));
		txtSearch.putClientProperty("JTextField.placeholderText", "");
		
		btnTim = new JButton("Tìm"); 
		styleButton(btnTim, new Color(255, 102, 102), Color.WHITE);
		btnTim.setPreferredSize(new Dimension(90, 38));
		
		btnLamMoi = new JButton("Làm mới"); 
		styleButton(btnLamMoi, new Color(140, 226, 163), Color.BLACK);
		btnLamMoi.setPreferredSize(new Dimension(110, 38));
		
		btnGopDon = new JButton("Gộp đơn TT"); 
		styleButton(btnGopDon, new Color(179, 229, 252), Color.BLACK);
		btnGopDon.setPreferredSize(new Dimension(130, 38));

		JPanel pS = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0)); 
		pS.setOpaque(false);
		pS.add(txtSearch); pS.add(btnTim); pS.add(btnLamMoi); pS.add(btnGopDon);
		pnl.add(pS); 
		pnl.add(Box.createRigidArea(new Dimension(0, 20)));

		JPanel pActiveRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
		pActiveRow.setOpaque(false);
		
		JLabel lblS = new JLabel("BÀN ĐANG PHỤC VỤ:"); 
		lblS.setFont(new Font("Segoe UI", Font.BOLD, 15)); 
		lblS.setForeground(COLOR_TEXT_DARK);
		pActiveRow.add(lblS);
		
		pnlGridBan = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0)); 
		pnlGridBan.setOpaque(false);
		
		JScrollPane scrollBan = new JScrollPane(pnlGridBan);
		scrollBan.setOpaque(false);
		scrollBan.getViewport().setOpaque(false);
		scrollBan.setBorder(null);
		scrollBan.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
		scrollBan.setPreferredSize(new Dimension(800, 50));
		
		pActiveRow.add(scrollBan);
		pnl.add(pActiveRow);
		
		JPanel pDivider = new JPanel(new BorderLayout());
		pDivider.setOpaque(false);
		pDivider.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 210, 205)));
		pDivider.add(Box.createRigidArea(new Dimension(0, 15)));
		pnl.add(pDivider);

		return pnl;
	}

	private void addTableButton(String maBan, String ghiChu) {
		JButton btn = new JButton(maBan); 
		btn.setFont(new Font("Segoe UI", Font.BOLD, 15)); 
		btn.setPreferredSize(new Dimension(80, 36));
		btn.putClientProperty("ghiChu", ghiChu); 
		
		btn.setBackground(new Color(255, 225, 225)); 
		btn.setForeground(COLOR_RED_ACCENT);
		btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		btn.putClientProperty("JButton.buttonType", "roundRect");
		btn.putClientProperty("JButton.arc", 20);
		btn.setFocusPainted(false);
		
		btn.addActionListener(e -> {
			if (btnActiveBan != null) { 
				btnActiveBan.setBackground(new Color(255, 225, 225)); 
				btnActiveBan.setForeground(COLOR_RED_ACCENT); 
			}
			btn.setBackground(new Color(255, 180, 180)); 
			btn.setForeground(Color.RED); 
			btnActiveBan = btn;
			
			lblMaBanInfo.setText("Mã bàn: " + maBan); 
			lblMaNVInfo.setText("Mã nhân viên: " + maNV); 
			lblGhiChuInfo.setText("Ghi chú: " + (ghiChu.isEmpty() ? "Không" : ghiChu));
			
			loadChiTietTuDB(maBan);
			cbKhuyenMai.setSelectedIndex(0); cbPhuongThuc.setSelectedIndex(0); 
			cardLayoutDetail.show(pnlDetailContainer, "DETAIL");
		});
		pnlGridBan.add(btn);
	}

	private JPanel createEmptyPanel() {
		JPanel pnl = new JPanel(new GridBagLayout());
		pnl.setOpaque(false);
		JLabel lbl = new JLabel("Vui lòng chọn một bàn đang phục vụ để xem hóa đơn.");
		lbl.setFont(new Font("Segoe UI", Font.ITALIC, 16));
		lbl.setForeground(Color.GRAY);
		pnl.add(lbl);
		return pnl;
	}

	private JPanel createDetailSplitPanel() {
		JPanel pnl = new JPanel(new BorderLayout(20, 0)); 
		pnl.setOpaque(false);
		
		// ==================== TRÁI ====================
		JPanel pL = new JPanel(new BorderLayout(0, 20)); 
		pL.setOpaque(false);
		
		RoundedPanel pInfoCard = new RoundedPanel(20, Color.WHITE);
		pInfoCard.setLayout(new BorderLayout());
		pInfoCard.setBorder(new EmptyBorder(15, 25, 15, 25));
		
		JLabel lblTitleLeft = new JLabel("THÔNG TIN ĐƠN GỌI MÓN");
		lblTitleLeft.setFont(new Font("Segoe UI", Font.BOLD, 17));
		pInfoCard.add(lblTitleLeft, BorderLayout.NORTH);

		JPanel pI = new JPanel(new GridLayout(3, 2, 10, 15)); 
		pI.setOpaque(false);
		pI.setBorder(new EmptyBorder(15, 0, 0, 0));
		
		pI.add(lblMaDonInfo = createNormalLabel("Mã đơn: --")); 
		pI.add(lblMaBanInfo = createNormalLabel("Mã bàn: --"));
		pI.add(lblMaNVInfo = createNormalLabel("Mã nhân viên: --")); 
		pI.add(lblThoiGianInfo = createNormalLabel("Thời gian: --"));
		pI.add(lblGhiChuInfo = createNormalLabel("Ghi chú: --")); 
		
		pInfoCard.add(pI, BorderLayout.CENTER);
		pL.add(pInfoCard, BorderLayout.NORTH);

		RoundedPanel pTableCard = new RoundedPanel(20, Color.WHITE);
		pTableCard.setLayout(new BorderLayout());
		pTableCard.setBorder(new EmptyBorder(15, 0, 15, 0)); 
		
		JLabel lblTableTitle = new JLabel("Danh sách món:");
		lblTableTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
		lblTableTitle.setBorder(new EmptyBorder(0, 20, 0, 0)); 
		pTableCard.add(lblTableTitle, BorderLayout.NORTH);

		modelChiTiet = new DefaultTableModel(new String[]{"Mã món", "Tên món", "Số lượng", "Đơn giá", "Thành tiền"}, 0);
		tblChiTiet = new JTable(modelChiTiet); 
		tblChiTiet.setRowHeight(40);
		tblChiTiet.setFont(new Font("Segoe UI", Font.PLAIN, 15));
		tblChiTiet.setBackground(Color.WHITE);
		tblChiTiet.setShowVerticalLines(false);
		tblChiTiet.setGridColor(new Color(240, 240, 240));
		
		tblChiTiet.getTableHeader().setBackground(new Color(255, 235, 235));
		tblChiTiet.getTableHeader().setForeground(new Color(200, 60, 70));
		tblChiTiet.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));
		tblChiTiet.getTableHeader().setBorder(BorderFactory.createEmptyBorder());
		
		DefaultTableCellRenderer centerRender = new DefaultTableCellRenderer();
		centerRender.setHorizontalAlignment(SwingConstants.CENTER);
		for(int i=0; i<5; i++) tblChiTiet.getColumnModel().getColumn(i).setCellRenderer(centerRender);

		JScrollPane scrollTbl = new JScrollPane(tblChiTiet);
		scrollTbl.getViewport().setBackground(Color.WHITE);
		
		// ĐÃ SỬA: Xóa sạch ranh giới, đường viền của JScrollPane để bảng liền mạch với tiêu đề
		scrollTbl.setBorder(BorderFactory.createEmptyBorder());
		scrollTbl.setViewportBorder(BorderFactory.createEmptyBorder());
		
		pTableCard.add(scrollTbl, BorderLayout.CENTER);
		pL.add(pTableCard, BorderLayout.CENTER);

		// ==================== PHẢI ====================
		RoundedPanel pR = new RoundedPanel(20, Color.WHITE); 
		pR.setLayout(new BorderLayout()); 
		pR.setBorder(new EmptyBorder(25, 25, 25, 25));
		pR.setPreferredSize(new Dimension(420, 0));

		JLabel lblInvTitle = new JLabel("HÓA ĐƠN", SwingConstants.CENTER);
		lblInvTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
		pR.add(lblInvTitle, BorderLayout.NORTH);

		JPanel pInv = new JPanel(); 
		pInv.setLayout(new BoxLayout(pInv, BoxLayout.Y_AXIS)); 
		pInv.setOpaque(false);
		pInv.setBorder(new EmptyBorder(20, 0, 0, 0));
		
		pInv.add(createRow("Tổng thành tiền:", lblTongTien = new JLabel("0 đ", SwingConstants.RIGHT)));
		pInv.add(Box.createRigidArea(new Dimension(0, 15)));
		
		cbKhuyenMai = new JComboBox<>();
		cbKhuyenMai.setBackground(new Color(255, 204, 204));
		pInv.add(createRow("Khuyến mãi:", cbKhuyenMai));
		pInv.add(Box.createRigidArea(new Dimension(0, 15)));
		
		pInv.add(createRow("Áp dụng giảm:", lblGiamGia = new JLabel("-0 đ", SwingConstants.RIGHT)));
		pInv.add(Box.createRigidArea(new Dimension(0, 15)));
		
		pInv.add(createRow("Thuế VAT (8%):", lblVAT = new JLabel("0 đ", SwingConstants.RIGHT)));
		pInv.add(Box.createRigidArea(new Dimension(0, 20)));
		
		JSeparator sep = new JSeparator();
		sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
		pInv.add(sep);
		pInv.add(Box.createRigidArea(new Dimension(0, 20)));
		
		pInv.add(createRow("TỔNG THANH TOÁN:", lblKhachCanTra = new JLabel("0 đ", SwingConstants.RIGHT)));
		lblKhachCanTra.setFont(new Font("Segoe UI", Font.BOLD, 22)); 
		lblKhachCanTra.setForeground(Color.BLACK); 
		
		pInv.add(Box.createRigidArea(new Dimension(0, 30)));
		cbPhuongThuc = new JComboBox<>(new String[]{"Tiền mặt", "Chuyển khoản (Mã QR)"});
		cbPhuongThuc.setBackground(new Color(255, 204, 204));
		pInv.add(createRow("Phương thức TT:", cbPhuongThuc));
		
		pnlQRCode = new JPanel(new BorderLayout());
		pnlQRCode.setOpaque(false);
		pnlQRCode.setPreferredSize(new Dimension(200, 150));
		pnlQRCode.setMaximumSize(new Dimension(200, 150));
		JLabel lblQRImage = new JLabel("MÃ QR", SwingConstants.CENTER);
		lblQRImage.setBorder(new LineBorder(Color.LIGHT_GRAY, 1, true));
		pnlQRCode.add(lblQRImage, BorderLayout.CENTER);
		pnlQRCode.setVisible(false);
		JPanel pnlQRCenter = new JPanel(new FlowLayout(FlowLayout.CENTER));
		pnlQRCenter.setOpaque(false);
		pnlQRCenter.add(pnlQRCode);
		pInv.add(Box.createRigidArea(new Dimension(0, 10)));
		pInv.add(pnlQRCenter);

		// ĐÃ SỬA: Lột sạch viền của thanh cuộn Hóa Đơn bên phải để biến mất khung màu xám
		JScrollPane scrollInv = new JScrollPane(pInv);
		scrollInv.setBorder(BorderFactory.createEmptyBorder());
		scrollInv.setViewportBorder(BorderFactory.createEmptyBorder());
		scrollInv.setOpaque(false);
		scrollInv.getViewport().setOpaque(false);
		
		pR.add(scrollInv, BorderLayout.CENTER);

		JPanel pnlBottomRight = new JPanel(new BorderLayout(0, 10));
		pnlBottomRight.setOpaque(false);
		
		chkInHoaDon = new JCheckBox(" In ra hóa đơn", true);
		chkInHoaDon.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		chkInHoaDon.setOpaque(false);
		pnlBottomRight.add(chkInHoaDon, BorderLayout.NORTH);

		JPanel pAct = new JPanel(new GridLayout(1, 2, 15, 0));
		pAct.setOpaque(false);
		
		btnXacNhan = new JButton("THANH TOÁN");
		styleButton(btnXacNhan, new Color(108, 229, 232), Color.BLACK); 
		
		btnHuy = new JButton("HỦY");
		styleButton(btnHuy, new Color(166, 166, 166), Color.BLACK); 
		
		pAct.add(btnXacNhan); 
		pAct.add(btnHuy); 
		
		pnlBottomRight.add(pAct, BorderLayout.CENTER);
		pR.add(pnlBottomRight, BorderLayout.SOUTH);

		pnl.add(pL, BorderLayout.CENTER); 
		pnl.add(pR, BorderLayout.EAST);
		return pnl;
	}

	private JPanel createRow(String labelText, JComponent comp) {
		JPanel p = new JPanel(new BorderLayout());
		p.setOpaque(false);
		p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

		JLabel lbl = new JLabel(labelText);
		lbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
		p.add(lbl, BorderLayout.WEST);

		JPanel pRight = new JPanel(new BorderLayout());
		pRight.setOpaque(false);
		pRight.setPreferredSize(new Dimension(200, 35));
		pRight.add(comp, BorderLayout.CENTER);
		
		p.add(pRight, BorderLayout.EAST);
		return p;
	}

	private void setupListeners() {
		btnTim.addActionListener(e -> {
			String k = txtSearch.getText().trim().toLowerCase();
			for (Component c : pnlGridBan.getComponents()) {
				if (c instanceof JButton) {
					JButton b = (JButton) c;
					String g = b.getClientProperty("ghiChu") != null ? b.getClientProperty("ghiChu").toString().toLowerCase() : "";
					b.setVisible(b.getText().toLowerCase().contains(k) || g.contains(k));
				}
			}
		});

		btnLamMoi.addActionListener(e -> loadDuLieuBanTuDB());
		
		cbKhuyenMai.addActionListener(e -> {
			if (!isUpdatingKhuyenMai) {
				tinhLaiHoaDon();
			}
		});
		
		cbPhuongThuc.addActionListener(e -> pnlQRCode.setVisible(cbPhuongThuc.getSelectedIndex() == 1));
		btnHuy.addActionListener(e -> cardLayoutDetail.show(pnlDetailContainer, "EMPTY"));
		btnXacNhan.addActionListener(e -> handleXacNhan());
	}

	private void handleXacNhan() {
		if (btnActiveBan == null) return;
		if (JOptionPane.showConfirmDialog(this, "Xác nhận thanh toán?", "Xác nhận", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;

		String maB = btnActiveBan.getText();
		String maH = phatSinhMaHD();
		HoaDon hd = new HoaDon(); hd.setMaHD(maH); hd.setNgayLap(LocalDateTime.now());
		hd.setPhuongThucTT(cbPhuongThuc.getSelectedItem().toString()); hd.setTongTien(khachCanTraHienTra);
		entity.NhanVien n = new entity.NhanVien(); n.setMaNV(this.maNV); hd.setNhanVien(n);
		entity.Ban b = new entity.Ban(); b.setMaBan(maB); hd.setBan(b);
		if (cbKhuyenMai.getSelectedIndex() > 0) hd.setKhuyenMai(listKhuyenMaiHienTai.get(cbKhuyenMai.getSelectedIndex() - 1));

		try {
			if (hoaDonDAO.themHoaDon(hd)) {
				for (int i = 0; i < modelChiTiet.getRowCount(); i++) {
					String mM = modelChiTiet.getValueAt(i, 0).toString();
					int sl = Integer.parseInt(modelChiTiet.getValueAt(i, 2).toString());
					double tt = Double.parseDouble(modelChiTiet.getValueAt(i, 4).toString().replaceAll("[^0-9]", ""));
					ctHoaDonDAO.themChiTietHienTai(maH, mM, sl, tt);
				}
				clearDonTamCuaBan(maB);
                
				XuatHoaDonHelper.xuatHoaDon(maH, hd.getNgayLap().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")), this.maNV, maB, modelChiTiet, lblTongTien.getText(), lblGiamGia.getText(), lblVAT.getText(), lblKhachCanTra.getText(), hd.getPhuongThucTT(), chkInHoaDon.isSelected());
				
				JOptionPane.showMessageDialog(this, "Thanh toán thành công " + maH);
				loadDuLieuBanTuDB(); cardLayoutDetail.show(pnlDetailContainer, "EMPTY");
			}
		} catch (Exception ex) { ex.printStackTrace(); }
	}

	private String phatSinhMaHD() {
		int max = 0;
		try {
			for (HoaDon h : hoaDonDAO.getAll()) {
				if (h.getMaHD() != null && h.getMaHD().length() <= 6) {
					int n = Integer.parseInt(h.getMaHD().substring(2));
					if (n > max) max = n;
				}
			}
		} catch (Exception e) {}
		return String.format("HD%03d", max + 1);
	}

	private void clearDonTamCuaBan(String maBan) {
		try (Connection con = SQLConnection.getConnection()) {
            Ban b = banDAO.getBanByMa(maBan);
            
            con.prepareStatement("DELETE FROM ChiTietDatMon WHERE maDonDat IN (SELECT maDonDat FROM DonDatMon WHERE maBan = '" + maBan + "')").executeUpdate();
            con.prepareStatement("DELETE FROM DonDatMon WHERE maBan = '" + maBan + "'").executeUpdate();

            List<String> listBanCanUpdate = new ArrayList<>();
            listBanCanUpdate.add(maBan); 

            if (b != null && b.getMaKhoi() != null && b.getMaKhoi() > 0) {
                for (Ban phu : banDAO.getAllBan()) {
                    if (phu.getMaKhoi() != null && phu.getMaKhoi().equals(b.getMaKhoi()) && !phu.getMaBan().equalsIgnoreCase(maBan)) {
                        listBanCanUpdate.add(phu.getMaBan());
                    }
                }
                banDAO.giaiTanKhoi(b.getMaKhoi());
            } else {
                banDAO.updateTrangThaiBan(maBan, "Trống");
            }
            
            for (entity.DonDatBan ddb : donDatBanDAO.getAllDonDat()) {
                if (listBanCanUpdate.contains(ddb.getMaBan()) && 
                   (ddb.getTrangThai().equalsIgnoreCase("Checked-in") || ddb.getTrangThai().equalsIgnoreCase("Đang dùng"))) {
                    donDatBanDAO.updateTrangThaiCuaDon(ddb.getMaDon(), "Hoàn thành");
                }
            }
            
		} catch (Exception e) { e.printStackTrace(); }
	}

	private void loadDuLieuBanTuDB() {
		pnlGridBan.removeAll(); listBanButtons.clear();
        
        List<Ban> allBans = banDAO.getAllBan();

		for (Ban b : allBans) {
            String tt = b.getTrangThai().toLowerCase();
			if (tt.contains("đang") || tt.contains("sử dụng")) {
                
                if (b.getMaBanChinh() != null && !b.getMaBanChinh().equalsIgnoreCase(b.getMaBan())) {
                    continue; 
                }

                DonDatMon d = donDatMonDAO.getDonDangMoTheoBan(b.getMaBan());
                if (d == null) continue; 

                String ghiChuHienThi = "";
                if (b.getMaKhoi() != null && b.getMaKhoi() > 0) {
                    List<String> dsPhu = new ArrayList<>();
                    for (Ban phu : allBans) {
                        if (phu.getMaKhoi() != null && phu.getMaKhoi().equals(b.getMaKhoi()) && !phu.getMaBan().equalsIgnoreCase(b.getMaBan())) {
                            dsPhu.add(phu.getMaBan());
                        }
                    }
                    if (!dsPhu.isEmpty()) {
                        ghiChuHienThi = "(Gồm: " + String.join(", ", dsPhu) + ")";
                    }
                }
				
				addTableButton(b.getMaBan(), ghiChuHienThi);
			}
		}
		pnlGridBan.revalidate(); pnlGridBan.repaint();
	}

	private void loadChiTietTuDB(String maBan) {
		modelChiTiet.setRowCount(0); tongTienHienTai = 0;
        Map<String, ChiTietDatMon> mapC = new HashMap<>();
        Map<String, Integer> mapS = new HashMap<>();
        
        List<String> listMaDon = new ArrayList<>();
        String thoiGianHienThi = "N/A";
        
        try (Connection con = SQLConnection.getConnection()) {
            ResultSet rs = con.prepareStatement("SELECT maDonDat, thoiGianDat FROM DonDatMon WHERE maBan = '" + maBan + "' ORDER BY thoiGianDat ASC").executeQuery();
            
            while (rs.next()) {
                String mD = rs.getString("maDonDat");
                listMaDon.add(mD);
                
                if (thoiGianHienThi.equals("N/A")) {
                    java.sql.Timestamp ts = rs.getTimestamp("thoiGianDat");
                    if (ts != null) thoiGianHienThi = ts.toLocalDateTime().format(DateTimeFormatter.ofPattern("HH:mm"));
                }
                
                for (ChiTietDatMon ct : chiTietDatMonDAO.getByMaDon(mD)) {
                    String mM = ct.getMaMon();
                    if (mapS.containsKey(mM)) mapS.put(mM, mapS.get(mM) + ct.getSoLuong());
                    else { mapS.put(mM, ct.getSoLuong()); mapC.put(mM, ct); }
                }
            }
            
            if (listMaDon.isEmpty()) return;
            
            for (String m : mapS.keySet()) {
                ChiTietDatMon ct = mapC.get(m); int sl = mapS.get(m);
                double tt = sl * ct.getDonGia();
                modelChiTiet.addRow(new Object[]{m, ct.getMonAn().getTenMon(), sl, df.format(ct.getDonGia()), df.format(tt)});
                tongTienHienTai += tt;
            }
            
            String strDon = listMaDon.size() > 1 ? listMaDon.get(0) + " (+" + (listMaDon.size() - 1) + " phiếu)" : listMaDon.get(0);
            lblMaDonInfo.setText("Mã đơn: " + strDon);
            lblThoiGianInfo.setText("Thời gian: " + thoiGianHienThi);
            lblTongTien.setText(df.format(tongTienHienTai));
            
            loadKhuyenMaiFromDB(); 
            tinhLaiHoaDon();
        } catch (Exception e) { e.printStackTrace(); }
	}

	private void loadKhuyenMaiFromDB() {
		isUpdatingKhuyenMai = true; 
		cbKhuyenMai.removeAllItems(); 
		cbKhuyenMai.addItem("Không áp dụng"); 
		listKhuyenMaiHienTai.clear();
		
		if (modelChiTiet.getRowCount() == 0) {
			isUpdatingKhuyenMai = false; 
			return;
		}

		LocalTime now = LocalTime.now();
		for (entity.KhuyenMai km : khuyenMaiDAO.getAll()) {
			if (!km.getTrangThai().contains("Đang")) continue;
			String dt = km.getDoiTuongApDung() != null ? km.getDoiTuongApDung().toString().toLowerCase() : "";
			String ten = km.getTenKM().toLowerCase();

			if (dt.contains("trưa") || ten.contains("trưa")) {
				if (now.isBefore(LocalTime.of(12, 0)) || now.isAfter(LocalTime.of(16, 0))) continue;
			}

			if (ten.contains("(") && ten.endsWith(")")) {
				String tM = ten.substring(ten.lastIndexOf("(") + 1, ten.length() - 1).toLowerCase();
				boolean co = false;
				for (int i = 0; i < modelChiTiet.getRowCount(); i++) 
					if (modelChiTiet.getValueAt(i, 1).toString().toLowerCase().contains(tM)) { co = true; break; }
				if (!co) continue;
			}
			
			listKhuyenMaiHienTai.add(km); 
			
			String giaTriHienThi = "";
			if (km.getLoaiKM() != null && km.getLoaiKM().contains("phần trăm")) {
				giaTriHienThi = km.getGiaTri() + "%";
			} else {
				giaTriHienThi = df.format(km.getGiaTri());
			}
			cbKhuyenMai.addItem(giaTriHienThi + " - " + km.getTenKM());
		}

		isUpdatingKhuyenMai = false; 
	}

	private void tinhLaiHoaDon() {
		double giam = 0; int s = cbKhuyenMai.getSelectedIndex();
		if (s > 0) {
			KhuyenMai k = listKhuyenMaiHienTai.get(s - 1);
			giam = k.getLoaiKM().contains("phần trăm") ? (tongTienHienTai * k.getGiaTri() / 100.0) : k.getGiaTri();
		}
		if (giam > tongTienHienTai) giam = tongTienHienTai;
		double sauGiam = tongTienHienTai - giam;
		double vat = sauGiam * 0.08; khachCanTraHienTra = sauGiam + vat;
		lblGiamGia.setText("- " + df.format(giam)); lblVAT.setText(df.format(vat)); lblKhachCanTra.setText(df.format(khachCanTraHienTra));
	}

	private void startClock() {
		timer = new Timer(1000, e -> {
			String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, 'ngày' dd/MM/yyyy - HH:mm:ss", new java.util.Locale("vi", "VN")));
			lblTimeDate.setText(dateTime);
		});
		timer.start();
	}

    private JLabel createNormalLabel(String t) { 
		JLabel l = new JLabel(t); 
		l.setFont(new Font("Segoe UI", Font.BOLD, 15)); 
		return l; 
	}
	
	private void styleButton(JButton btn, Color bg, Color fg) {
		btn.setFont(new Font("Segoe UI", Font.BOLD, 15)); 
        btn.setBackground(bg); 
        btn.setForeground(fg); 
		btn.setFocusPainted(false);
		btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		btn.putClientProperty("JButton.buttonType", "roundRect");
		btn.putClientProperty("JButton.arc", 15);
	}

	class RoundedPanel extends JPanel {
		private int radius;
		public RoundedPanel(int radius, Color bgColor) {
			this.radius = radius;
			setOpaque(false);
			setBackground(bgColor);
		}
		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setColor(getBackground());
			g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
			g2.dispose();
			super.paintComponent(g);
		}
	}
}