package gui;

import dao.HoaDon_DAO;
import dao.ChiTietHoaDon_DAO;
import entity.HoaDon;
import entity.ChiTietHoaDon;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

public class QuanLyHoaDonPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	// BẢNG MÀU UI MỚI THEO THIẾT KẾ
	private final Color BG_APP = new Color(255, 245, 240); // Nền cam sữa nhạt
	private final Color COLOR_TEXT_DARK = new Color(30, 30, 30);
	
	private JLabel lblTimeDate;
	private JTextField txtSearch;
	private JDateChooser dcTuNgay, dcDenNgay;
	private JButton btnXem;
	private JTable tblHoaDon;
	private DefaultTableModel modelHoaDon;

	private Timer timer;
	private DecimalFormat df = new DecimalFormat("#,### đ");
	private DateTimeFormatter fmtDateTime = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");

	private HoaDon_DAO hoaDonDAO = new HoaDon_DAO();
	private ChiTietHoaDon_DAO ctHoaDonDAO = new ChiTietHoaDon_DAO();

	public QuanLyHoaDonPanel() {
		setLayout(new BorderLayout(0, 20));
		setBackground(BG_APP); // Set nền chuẩn
		setBorder(new EmptyBorder(15, 25, 15, 25));

		add(createHeaderPanel(), BorderLayout.NORTH);
		add(createTablePanel(), BorderLayout.CENTER);

		startClock();
		loadAllHoaDon();
		setupListeners();

		this.addComponentListener(new java.awt.event.ComponentAdapter() {
			@Override
			public void componentShown(java.awt.event.ComponentEvent e) {
				loadAllHoaDon();
			}
		});
	}

	private JPanel createHeaderPanel() {
		JPanel pnlHeader = new JPanel();
		pnlHeader.setLayout(new BoxLayout(pnlHeader, BoxLayout.Y_AXIS));
		pnlHeader.setOpaque(false); // Trong suốt để lộ nền cam sữa

		// 1. Time Banner
		lblTimeDate = new JLabel("Đang tải..."); 
		lblTimeDate.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		lblTimeDate.setForeground(Color.BLACK);
		
		JPanel pT = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 8)); 
		pT.setBackground(new Color(255, 235, 235)); 
		pT.putClientProperty("FlatLaf.style", "arc: 10");
		pT.setMaximumSize(new Dimension(1000, 40));
		pT.add(lblTimeDate);
		
		JPanel pTimeWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		pTimeWrapper.setOpaque(false);
		pTimeWrapper.add(pT);
		pnlHeader.add(pTimeWrapper);
		pnlHeader.add(Box.createRigidArea(new Dimension(0, 15)));

		// 2. Title
		JLabel lblTitle = new JLabel("DANH SÁCH HÓA ĐƠN");
		lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
		lblTitle.setForeground(COLOR_TEXT_DARK);
		JPanel pnlTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		pnlTitle.setOpaque(false);
		pnlTitle.add(lblTitle);
		pnlHeader.add(pnlTitle);
		pnlHeader.add(Box.createRigidArea(new Dimension(0, 20)));

		// 3. Filter Bar
		JPanel pnlFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
		pnlFilter.setOpaque(false);

		pnlFilter.add(createNormalLabel("Tìm kiếm:"));
		txtSearch = new JTextField(20);
		txtSearch.setPreferredSize(new Dimension(220, 38));
		txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 15));
		txtSearch.putClientProperty("JTextField.placeholderText", "Mã Hóa Đơn, Mã Bàn...");
		pnlFilter.add(txtSearch);

		pnlFilter.add(createNormalLabel("Từ ngày:"));
		dcTuNgay = new JDateChooser();
		dcTuNgay.setPreferredSize(new Dimension(150, 38));
		((JTextField) dcTuNgay.getDateEditor().getUiComponent()).putClientProperty("JTextField.placeholderText", "dd/MM/yyyy");
		styleDateChooser(dcTuNgay); 
		pnlFilter.add(dcTuNgay);

		pnlFilter.add(createNormalLabel("Đến ngày:"));
		dcDenNgay = new JDateChooser();
		dcDenNgay.setPreferredSize(new Dimension(150, 38));
		((JTextField) dcDenNgay.getDateEditor().getUiComponent()).putClientProperty("JTextField.placeholderText", "dd/MM/yyyy");
		styleDateChooser(dcDenNgay); 
		pnlFilter.add(dcDenNgay);

		btnXem = new JButton("Xem");
		btnXem.setFont(new Font("Segoe UI", Font.BOLD, 15));
		btnXem.setForeground(Color.WHITE);
		btnXem.setBackground(new Color(255, 102, 102));
		btnXem.setPreferredSize(new Dimension(100, 38));
		btnXem.setCursor(new Cursor(Cursor.HAND_CURSOR));
		btnXem.setFocusPainted(false);
		btnXem.putClientProperty("JButton.buttonType", "roundRect");
		btnXem.putClientProperty("JButton.arc", 15);
		btnXem.setBorderPainted(false);
		pnlFilter.add(btnXem);

		pnlHeader.add(pnlFilter);

		return pnlHeader;
	}

	private void styleDateChooser(JDateChooser dc) {
		dc.setDateFormatString("dd/MM/yyyy");
		dc.setBorder(BorderFactory.createCompoundBorder(
			new LineBorder(new Color(200, 200, 200), 1, true),
			new EmptyBorder(2, 5, 2, 5)
		));
		dc.setBackground(Color.WHITE);

		JTextField txt = (JTextField) dc.getDateEditor().getUiComponent();
		txt.setBorder(BorderFactory.createEmptyBorder());
		txt.setBackground(Color.WHITE);
		txt.setFont(new Font("Segoe UI", Font.PLAIN, 15));

		JButton btn = dc.getCalendarButton();
		btn.setBackground(Color.WHITE);
		btn.setContentAreaFilled(false); 
		btn.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
		btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		btn.setIcon(new SimpleCalendarIcon()); 
	}

	class SimpleCalendarIcon implements Icon {
		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setColor(new Color(120, 120, 120)); 
			g2.setStroke(new BasicStroke(1.5f));
			
			g2.drawRoundRect(x + 2, y + 4, 16, 14, 3, 3);
			g2.drawLine(x + 2, y + 8, x + 18, y + 8);
			g2.drawLine(x + 6, y + 2, x + 6, y + 6);
			g2.drawLine(x + 14, y + 2, x + 14, y + 6);
			
			g2.fillRect(x + 5, y + 11, 2, 2);
			g2.fillRect(x + 9, y + 11, 2, 2);
			g2.fillRect(x + 13, y + 11, 2, 2);
			g2.fillRect(x + 5, y + 14, 2, 2);
			g2.fillRect(x + 9, y + 14, 2, 2);
			g2.fillRect(x + 13, y + 14, 2, 2);
			
			g2.dispose();
		}
		@Override public int getIconWidth() { return 20; }
		@Override public int getIconHeight() { return 20; }
	}

	private JPanel createTablePanel() {
		JPanel pnl = new JPanel(new BorderLayout());
		pnl.setOpaque(false); // Trong suốt để hòa vào nền BG_APP

		String[] cols = { "Mã hóa đơn", "Ngày lập", "Mã khuyến mãi", "Mã nhân viên", "Mã bàn", "Phương thức thanh toán",
				"Tổng tiền", "Trạng thái" };
		modelHoaDon = new DefaultTableModel(cols, 0) {
			@Override
			public boolean isCellEditable(int r, int c) {
				return false;
			}
		};
		tblHoaDon = new JTable(modelHoaDon);

		// ĐÃ CẬP NHẬT GIAO DIỆN BẢNG THEO THIẾT KẾ
		tblHoaDon.setRowHeight(45); // Tăng chiều cao dòng cho thoáng
		tblHoaDon.setFont(new Font("Segoe UI", Font.PLAIN, 15));
		tblHoaDon.setBackground(Color.WHITE);
		
		// Ẩn sọc dọc, làm mờ sọc ngang
		tblHoaDon.setShowVerticalLines(false);
		tblHoaDon.setGridColor(new Color(245, 245, 245));
		
		// Định dạng Header: Nền hồng nhạt, chữ đỏ
		tblHoaDon.getTableHeader().setBackground(new Color(255, 235, 235));
		tblHoaDon.getTableHeader().setForeground(new Color(200, 60, 70));
		tblHoaDon.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 15));
		tblHoaDon.getTableHeader().setPreferredSize(new Dimension(0, 45));
		tblHoaDon.getTableHeader().setBorder(BorderFactory.createEmptyBorder());

		tblHoaDon.setSelectionBackground(new Color(255, 235, 235));
		tblHoaDon.setSelectionForeground(Color.BLACK);

		DefaultTableCellRenderer center = new DefaultTableCellRenderer();
		center.setHorizontalAlignment(JLabel.CENTER);
		for (int i = 0; i < tblHoaDon.getColumnCount(); i++) {
			tblHoaDon.getColumnModel().getColumn(i).setCellRenderer(center);
		}

		tblHoaDon.getColumnModel().getColumn(0).setPreferredWidth(100);
		tblHoaDon.getColumnModel().getColumn(1).setPreferredWidth(180);
		tblHoaDon.getColumnModel().getColumn(5).setPreferredWidth(180); // Phương thức TT cho rộng ra
		tblHoaDon.getColumnModel().getColumn(6).setPreferredWidth(120);

		JScrollPane scroll = new JScrollPane(tblHoaDon);
		// Lột sạch viền xám xấu xí của JScrollPane
		scroll.setBorder(BorderFactory.createEmptyBorder());
		scroll.setViewportBorder(BorderFactory.createEmptyBorder());
		
		// Set nền của JScrollPane trùng với nền App để bảng trông như lơ lửng
		scroll.setBackground(BG_APP);
		scroll.getViewport().setBackground(BG_APP);
		
		pnl.add(scroll, BorderLayout.CENTER);

		return pnl;
	}

	private void setupListeners() {
        txtSearch.addActionListener(e -> btnXem.doClick());
        ((JTextField) dcTuNgay.getDateEditor().getUiComponent()).addActionListener(e -> btnXem.doClick());
        ((JTextField) dcDenNgay.getDateEditor().getUiComponent()).addActionListener(e -> btnXem.doClick());

		btnXem.addActionListener(e -> {
			String keyword = txtSearch.getText().trim().toLowerCase();
			LocalDate tuNgay = null;
			LocalDate denNgay = null;

			Date dateTu = dcTuNgay.getDate();
			if (dateTu != null) {
				tuNgay = dateTu.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			}

			Date dateDen = dcDenNgay.getDate();
			if (dateDen != null) {
				denNgay = dateDen.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			}

			modelHoaDon.setRowCount(0);
			List<HoaDon> list = hoaDonDAO.getAll();
			LocalDate today = LocalDate.now();

			boolean isDefaultSearch = keyword.isEmpty() && tuNgay == null && denNgay == null;

			for (HoaDon hd : list) {
				boolean matchKeyword = true;
				boolean matchDate = true;

				if (!keyword.isEmpty()) {
					String maHD = hd.getMaHD() != null ? hd.getMaHD().toLowerCase() : "";
					String maBan = hd.getBan() != null ? hd.getBan().getMaBan().toLowerCase() : "";
					if (!maHD.contains(keyword) && !maBan.contains(keyword)) {
						matchKeyword = false;
					}
				}

				if (hd.getNgayLap() != null) {
					LocalDate ngayLapDate = hd.getNgayLap().toLocalDate();

					if (isDefaultSearch) {
						if (!ngayLapDate.equals(today)) {
							matchDate = false;
						}
					} else {
						if (tuNgay != null && ngayLapDate.isBefore(tuNgay)) {
							matchDate = false;
						}
						if (denNgay != null && ngayLapDate.isAfter(denNgay)) {
							matchDate = false;
						}
					}
				} else {
					if (isDefaultSearch)
						matchDate = false;
				}

				if (matchKeyword && matchDate) {
					String tg = hd.getNgayLap() != null ? hd.getNgayLap().format(fmtDateTime) : "N/A";
					String km = hd.getKhuyenMai() != null ? hd.getKhuyenMai().getMaKM() : "Không";

					modelHoaDon.addRow(new Object[] { hd.getMaHD(), tg, km,
							hd.getNhanVien() != null ? hd.getNhanVien().getMaNV() : "N/A",
							hd.getBan() != null ? hd.getBan().getMaBan() : "N/A", hd.getPhuongThucTT(),
							df.format(hd.getTongTien()), "Đã thanh toán" });
				}
			}
		});

		tblHoaDon.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int row = tblHoaDon.rowAtPoint(e.getPoint());

				if (row != -1) {
					if (e.getClickCount() == 2) {
						String maHD = tblHoaDon.getValueAt(row, 0).toString();
						String thoiGian = tblHoaDon.getValueAt(row, 1).toString();
						String maKM = tblHoaDon.getValueAt(row, 2).toString();
						String maNV = tblHoaDon.getValueAt(row, 3).toString();
						String maBan = tblHoaDon.getValueAt(row, 4).toString();
						String phuongThucTT = tblHoaDon.getValueAt(row, 5).toString();
						String tongTien = tblHoaDon.getValueAt(row, 6).toString();
						String trangThai = tblHoaDon.getValueAt(row, 7).toString();

						showChiTietHoaDonDialog(maHD, thoiGian, maKM, maNV, maBan, phuongThucTT, tongTien, trangThai);
					}
				}
			}
		});
	}

	private void showChiTietHoaDonDialog(String maHD, String thoiGian, String maKM, String maNV, String maBan,
			String phuongThucTT, String tongTien, String trangThai) {
		JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Chi Tiết Hóa Đơn", true);
		dialog.setSize(900, 650);
		dialog.setLocationRelativeTo(this);
		dialog.setLayout(new BorderLayout(15, 15));
		dialog.getContentPane().setBackground(new Color(245, 245, 245));

		JPanel pnlMain = new JPanel(new BorderLayout(0, 15));
		pnlMain.setBackground(new Color(245, 245, 245));
		pnlMain.setBorder(new EmptyBorder(15, 15, 15, 15));

		JPanel pnlInfo = new JPanel(new GridLayout(4, 2, 20, 10));
		pnlInfo.setBackground(new Color(255, 204, 204));
		pnlInfo.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder(new LineBorder(Color.GRAY, 1, true), "Thông tin hóa đơn",
						TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 16), Color.BLACK),
				new EmptyBorder(15, 15, 15, 15)));

		pnlInfo.add(createRowInfo("Mã hóa đơn:", maHD));
		pnlInfo.add(createRowInfo("Thời gian lập:", thoiGian));
		pnlInfo.add(createRowInfo("Mã nhân viên:", maNV));
		pnlInfo.add(createRowInfo("Mã bàn:", maBan));
		pnlInfo.add(createRowInfo("Mã khuyến mãi:", maKM));
		pnlInfo.add(createRowInfo("Phương thức TT:", phuongThucTT));
		pnlInfo.add(createRowInfo("Tổng tiền:", tongTien, Color.RED));
		pnlInfo.add(createRowInfo("Trạng thái:", trangThai, new Color(40, 167, 69)));

		pnlMain.add(pnlInfo, BorderLayout.NORTH);

		JPanel pnlTableWrapper = new JPanel(new BorderLayout());
		pnlTableWrapper.setBackground(Color.WHITE);
		pnlTableWrapper
				.setBorder(BorderFactory.createTitledBorder(new LineBorder(Color.GRAY, 1, true), "Chi tiết món ăn",
						TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 16), Color.BLACK));

		String[] cols = { "Mã Hóa Đơn", "Mã Món", "Tên Món", "Số Lượng", "Thành Tiền" };
		DefaultTableModel modelCT = new DefaultTableModel(cols, 0) {
			@Override
			public boolean isCellEditable(int r, int c) {
				return false;
			}
		};
		JTable tblCT = new JTable(modelCT);

		tblCT.setRowHeight(35);
		tblCT.setFont(new Font("Segoe UI", Font.PLAIN, 14));
		tblCT.getTableHeader().setBackground(new Color(255, 235, 235));
		tblCT.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
		tblCT.setShowVerticalLines(false);
		tblCT.setSelectionBackground(new Color(255, 235, 235));
		tblCT.setSelectionForeground(Color.BLACK);

		DefaultTableCellRenderer center = new DefaultTableCellRenderer();
		center.setHorizontalAlignment(JLabel.CENTER);
		for (int i = 0; i < tblCT.getColumnCount(); i++)
			tblCT.getColumnModel().getColumn(i).setCellRenderer(center);

		tblCT.getColumnModel().getColumn(2).setPreferredWidth(200);

		List<ChiTietHoaDon> listCT = ctHoaDonDAO.getByMaHD(maHD);
		for (ChiTietHoaDon ct : listCT) {
			String maMon = ct.getMaMon();
			String tenMon = ct.getMonAn() != null ? ct.getMonAn().getTenMon() : "N/A";
			int sl = ct.getSoLuong();

			double thTien = sl * ct.getDonGia();
			modelCT.addRow(new Object[] { maHD, maMon, tenMon, sl, df.format(thTien) });
		}

		pnlTableWrapper.add(new JScrollPane(tblCT), BorderLayout.CENTER);
		pnlMain.add(pnlTableWrapper, BorderLayout.CENTER);

		JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		pnlBottom.setBackground(new Color(245, 245, 245));
		JButton btnDong = new JButton("ĐÓNG");
		btnDong.setFont(new Font("Segoe UI", Font.BOLD, 15));
		btnDong.setPreferredSize(new Dimension(100, 40));
		btnDong.setCursor(new Cursor(Cursor.HAND_CURSOR));
		btnDong.addActionListener(e -> dialog.dispose());
		pnlBottom.add(btnDong);

		dialog.add(pnlMain, BorderLayout.CENTER);
		dialog.add(pnlBottom, BorderLayout.SOUTH);
		dialog.setVisible(true);
	}

	private JPanel createRowInfo(String title, String value, Color textColor) {
		JPanel pnl = new JPanel(new BorderLayout(10, 0));
		pnl.setOpaque(false);
		JLabel lblT = new JLabel(title);
		lblT.setFont(new Font("Segoe UI", Font.PLAIN, 15));
		lblT.setPreferredSize(new Dimension(125, 30));

		JTextField txtV = new JTextField(value);
		txtV.setFont(new Font("Segoe UI", Font.BOLD, 15));
		txtV.setForeground(textColor);
		txtV.setEditable(false);
		txtV.setBackground(Color.WHITE);
		txtV.setBorder(BorderFactory.createCompoundBorder(new LineBorder(new Color(200, 200, 200)),
				new EmptyBorder(0, 5, 0, 5)));

		pnl.add(lblT, BorderLayout.WEST);
		pnl.add(txtV, BorderLayout.CENTER);
		return pnl;
	}

	private JPanel createRowInfo(String title, String value) {
		return createRowInfo(title, value, Color.BLACK);
	}

	private void startClock() {
		timer = new Timer(1000, e -> {
			// Cập nhật format thời gian khớp với thiết kế
			String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, 'ngày' dd/MM/yyyy - HH:mm:ss", new java.util.Locale("vi", "VN")));
			lblTimeDate.setText(dateTime);
		});
		timer.start();
	}

	private JLabel createNormalLabel(String text) {
		JLabel l = new JLabel(text);
		l.setFont(new Font("Segoe UI", Font.BOLD, 15));
		l.setForeground(COLOR_TEXT_DARK);
		return l;
	}

	public void loadAllHoaDon() {
		modelHoaDon.setRowCount(0);
		List<HoaDon> list = hoaDonDAO.getAll();
		LocalDate today = LocalDate.now();

		for (HoaDon hd : list) {
			if (hd.getNgayLap() != null && hd.getNgayLap().toLocalDate().equals(today)) {
				String tg = hd.getNgayLap().format(fmtDateTime);
				String km = hd.getKhuyenMai() != null ? hd.getKhuyenMai().getMaKM() : "Không";

				modelHoaDon.addRow(new Object[] { hd.getMaHD(), tg, km,
						hd.getNhanVien() != null ? hd.getNhanVien().getMaNV() : "N/A",
						hd.getBan() != null ? hd.getBan().getMaBan() : "N/A", hd.getPhuongThucTT(),
						df.format(hd.getTongTien()), "Đã thanh toán" });
			}
		}
	}
}