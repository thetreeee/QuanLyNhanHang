package gui;

import dao.BanDAO;
import dao.DonDatBanDAO;
import entity.Ban;
import entity.DonDatBan;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DialogChiTietDonDat extends JDialog {
    private static final long serialVersionUID = 1L;
    
    private JTextField txtMaDon, txtNgayDat, txtThoiGian, txtKhachHang;
    private JComboBox<String> cbTrangThai;
    private JTable tableChiTiet;
    private DefaultTableModel modelChiTiet;
    private JButton btnCapNhat, btnHuy, btnThemNhanh;
    
    private JSpinner spnSoKhachMoi;
    private JComboBox<String> cbBanGoiY;

    private DonDatBanDAO donDAO = new DonDatBanDAO();
    private BanDAO banDAO = new BanDAO();
    private String maDonGoc;

    public DialogChiTietDonDat(Window parent, String maDon, String maBan, String ngayDat, String thoiGian, String trangThai, String khachHang, String soLuong) {
        super(parent, "Chi Tiết Đơn Đặt Bàn", ModalityType.APPLICATION_MODAL);
        this.maDonGoc = maDon;
        setSize(900, 700); 
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Color.WHITE);

        // --- 1. HEADER ---
        JPanel pnlTopWrapper = new JPanel(new BorderLayout());
        pnlTopWrapper.setBackground(Color.WHITE);
        pnlTopWrapper.setBorder(new EmptyBorder(10, 10, 0, 10));
        
        JPanel pnlHeader = new JPanel(new GridBagLayout());
        pnlHeader.setBackground(new Color(255, 250, 205)); 
        pnlHeader.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Thông tin Đơn Đặt Bàn", TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 14), Color.BLACK));
        setupHeaderFields(pnlHeader, maDon, ngayDat, thoiGian, trangThai, khachHang);
        pnlTopWrapper.add(pnlHeader, BorderLayout.CENTER);
        add(pnlTopWrapper, BorderLayout.NORTH);

        // --- 2. BỘ LỌC THÔNG MINH ---
        JPanel pnlCenterWrapper = new JPanel(new BorderLayout(0, 10));
        pnlCenterWrapper.setBackground(Color.WHITE);
        pnlCenterWrapper.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel pnlSmartFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        pnlSmartFilter.setBackground(new Color(245, 245, 245));
        pnlSmartFilter.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

        pnlSmartFilter.add(new JLabel("Số khách dự kiến mới:"));
        spnSoKhachMoi = new JSpinner(new SpinnerNumberModel(Integer.parseInt(soLuong), 1, 100, 1));
        spnSoKhachMoi.setPreferredSize(new Dimension(70, 30));
        pnlSmartFilter.add(spnSoKhachMoi);

        pnlSmartFilter.add(new JLabel("Gợi ý bàn thêm (Khoảng ±2 ghế):"));
        cbBanGoiY = new JComboBox<>();
        cbBanGoiY.setPreferredSize(new Dimension(250, 30));
        pnlSmartFilter.add(cbBanGoiY);

        btnThemNhanh = new JButton("Thêm bàn");
        btnThemNhanh.setBackground(new Color(40, 167, 69));
        btnThemNhanh.setForeground(Color.WHITE);
        btnThemNhanh.setFont(new Font("Segoe UI", Font.BOLD, 13));
        pnlSmartFilter.add(btnThemNhanh);

        pnlCenterWrapper.add(pnlSmartFilter, BorderLayout.NORTH);

        // --- 3. BẢNG CHI TIẾT ---
        setupTableDetail();
        JScrollPane scroll = new JScrollPane(tableChiTiet);
        pnlCenterWrapper.add(scroll, BorderLayout.CENTER);
        add(pnlCenterWrapper, BorderLayout.CENTER);

        setupBottomButtons();

        // Logic điều khiển
        capNhatDanhSachGoiY();
        spnSoKhachMoi.addChangeListener(e -> capNhatDanhSachGoiY());
        
        btnThemNhanh.addActionListener(e -> {
            String selection = (String) cbBanGoiY.getSelectedItem();
            if (selection != null && !selection.contains("Đã đủ") && !selection.contains("Không có") && !selection.contains("Lỗi")) {
                String maBanMoi = selection.split(" ")[0];
                Ban b = banDAO.getBanByMa(maBanMoi);
                modelChiTiet.addRow(new Object[]{maBanMoi, b.getSoGhe(), " [ Xóa ] "});
                capNhatDanhSachGoiY();
            }
        });
    }

    private void capNhatDanhSachGoiY() {
        cbBanGoiY.removeAllItems();
        try {
            int soKhachMoi = (int) spnSoKhachMoi.getValue();
            int sucChuaHienTai = 0;
            
            for (int i = 0; i < modelChiTiet.getRowCount(); i++) {
                sucChuaHienTai += Integer.parseInt(modelChiTiet.getValueAt(i, 1).toString());
            }

            int choConThieu = soKhachMoi - sucChuaHienTai;

            if (choConThieu <= 0) {
                cbBanGoiY.addItem("Đã đủ chỗ ngồi");
                btnThemNhanh.setEnabled(false);
                return;
            }

            // ĐÃ SỬA: Lấy vị trí nếu bảng có bàn, nếu bảng rỗng thì viTri = "" (Tìm toàn nhà hàng)
            String viTri = "";
            if (modelChiTiet.getRowCount() > 0) {
                Ban banGoc = banDAO.getBanByMa(modelChiTiet.getValueAt(0, 0).toString());
                if (banGoc != null) viTri = banGoc.getViTri();
            }
            
            DateTimeFormatter fNgay = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate ngay = LocalDate.parse(txtNgayDat.getText().trim(), fNgay);
            LocalTime gio = LocalTime.parse(txtThoiGian.getText().trim(), DateTimeFormatter.ofPattern("HH:mm"));

            List<Ban> dsBanTrong = banDAO.getAllBan();
            boolean timThay = false;

            for (Ban b : dsBanTrong) {
                // Lọc theo vị trí (hoặc lấy tất cả nếu viTri đang rỗng)
                if ((viTri.isEmpty() || b.getViTri().equals(viTri)) && b.getTrangThai().equals("Trống")) {
                    int ghe = b.getSoGhe();
                    if (ghe >= choConThieu  && ghe <= (choConThieu + 2)) {
                        if (!donDAO.kiemTraTrungLich(b.getMaBan(), ngay, gio, maDonGoc)) {
                            if (!checkBanDaCoTrongBang(b.getMaBan())) {
                                // Nếu bảng đang rỗng (đang tìm toàn nhà hàng), hiển thị thêm tên khu vực cho dễ chọn
                                String hienThiThem = viTri.isEmpty() ? " - " + b.getViTri() : "";
                                cbBanGoiY.addItem(b.getMaBan() + " (" + ghe + " ghế)" + hienThiThem);
                                timThay = true;
                            }
                        }
                    }
                }
            }

            if (!timThay) {
                cbBanGoiY.addItem("Không có bàn phù hợp ±2 ghế");
                btnThemNhanh.setEnabled(false);
            } else {
                btnThemNhanh.setEnabled(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            cbBanGoiY.addItem("Lỗi dữ liệu");
        }
    }

    private boolean checkBanDaCoTrongBang(String ma) {
        for(int i=0; i<modelChiTiet.getRowCount(); i++) {
            if(modelChiTiet.getValueAt(i, 0).equals(ma)) return true;
        }
        return false;
    }

    private void setupTableDetail() {
        String[] cols = {"Mã Bàn", "Số lượng khách dự kiến", "Thao tác"}; 
        modelChiTiet = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tableChiTiet = new JTable(modelChiTiet);
        tableChiTiet.setRowHeight(35);
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        tableChiTiet.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);

        tableChiTiet.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int col = tableChiTiet.columnAtPoint(e.getPoint());
                int row = tableChiTiet.rowAtPoint(e.getPoint());
                
                if (col == 2 && row != -1) { 
                    // ĐÃ SỬA: Cho phép Lễ tân xóa tự do mà không bị chặn lại.
                    // Các cảnh báo thiếu bàn, thiếu chỗ được dời xuống nút Cập Nhật
                    modelChiTiet.removeRow(row);
                    capNhatDanhSachGoiY();
                }
            }
        });

        // Load dữ liệu ban đầu
        List<Object[]> danhSachBan = donDAO.getChiTietBanCuaDon(maDonGoc);
        for (Object[] r : danhSachBan) {
            modelChiTiet.addRow(new Object[]{r[0], r[1], " [ Xóa ] "});
        }
    }

    private void setupBottomButtons() {
        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        pnlBottom.setBackground(Color.WHITE);

        btnCapNhat = new JButton("Cập Nhật Đơn Đặt");
        btnCapNhat.setBackground(new Color(65, 105, 225));
        btnCapNhat.setForeground(Color.WHITE);
        btnCapNhat.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCapNhat.setPreferredSize(new Dimension(180, 40));

        btnCapNhat.addActionListener(e -> {
            // 1. Kiểm tra Lễ tân có để quên bảng trống không
            if (modelChiTiet.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, 
                    "Không thể cập nhật! \nBạn chưa chọn bàn nào cho đơn đặt này.", 
                    "Thiếu bàn", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int tongKhachMoi = (int) spnSoKhachMoi.getValue();
            List<String> dsBanMoi = new ArrayList<>();
            int tongSucChuaThucTe = 0;
            
            for (int i = 0; i < modelChiTiet.getRowCount(); i++) {
                String maBan = modelChiTiet.getValueAt(i, 0).toString();
                dsBanMoi.add(maBan);
                
                Ban b = banDAO.getBanByMa(maBan);
                if (b != null) {
                    tongSucChuaThucTe += b.getSoGhe();
                }
            }

            // 2. Kiểm tra tổng sức chứa thực tế so với khách
            if (tongSucChuaThucTe < tongKhachMoi) {
                JOptionPane.showMessageDialog(this, 
                    "Không thể cập nhật! \n" +
                    "Tổng sức chứa của các bàn đã chọn (" + tongSucChuaThucTe + " chỗ) \n" +
                    "không đủ cho " + tongKhachMoi + " khách.", 
                    "Thiếu chỗ ngồi", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 2.5 Kiểm tra cảnh báo khách vãng lai (1.5 tiếng = 90 phút)
            DateTimeFormatter fNgay = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate ngayMoi;
            LocalTime gioMoi;
            try {
                ngayMoi = LocalDate.parse(txtNgayDat.getText().trim(), fNgay);
                gioMoi = LocalTime.parse(txtThoiGian.getText().trim(), DateTimeFormatter.ofPattern("HH:mm"));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Ngày hoặc giờ không đúng định dạng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (ngayMoi.equals(LocalDate.now())) {
                boolean coBanDangDung = false;
                for (String maBan : dsBanMoi) {
                    Ban b = banDAO.getBanByMa(maBan);
                    if (b != null && "Đang dùng".equalsIgnoreCase(b.getTrangThai())) {
                        coBanDangDung = true;
                        break;
                    }
                }
                
                if (coBanDangDung) {
                    int phutHienTai = LocalTime.now().getHour() * 60 + LocalTime.now().getMinute();
                    int phutMoi = gioMoi.getHour() * 60 + gioMoi.getMinute();
                    
                    if (phutMoi - phutHienTai < 90 && phutMoi - phutHienTai > 0) {
                        int ans = JOptionPane.showConfirmDialog(this, 
                            "Bàn này hiện ĐANG CÓ KHÁCH (khách vãng lai).\n" +
                            "Nếu đổi giờ đặt thành " + gioMoi.toString() + ", khoảng thời gian còn lại \n" +
                            "không đủ 1.5 tiếng để khách vãng lai dùng bữa xong.\n\n" +
                            "Bạn có chắc chắn vẫn muốn ép đổi giờ không?", 
                            "Cảnh báo thời gian", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                        if (ans != JOptionPane.YES_OPTION) {
                            return;
                        }
                    }
                }
            }

            // 3. Tiến hành gọi DAO cập nhật
            if (donDAO.capNhatBanChoDonDat(maDonGoc, dsBanMoi, tongKhachMoi)) {
                donDAO.updateThongTinChiTietDon(maDonGoc, ngayMoi, gioMoi, tongKhachMoi, cbTrangThai.getSelectedItem().toString());
                JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                dispose();
            }
        });

        btnHuy = new JButton("Hủy");
        btnHuy.setPreferredSize(new Dimension(100, 40));
        btnHuy.addActionListener(e -> dispose());

        pnlBottom.add(btnCapNhat);
        pnlBottom.add(btnHuy);
        add(pnlBottom, BorderLayout.SOUTH);
    }

    private void setupHeaderFields(JPanel pnlHeader, String maDon, String ngayDat, String thoiGian, String trangThai, String khachHang) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 15, 10, 15);

        txtMaDon = createStyledTextField(maDon); txtMaDon.setEditable(false);
        txtNgayDat = createStyledTextField(ngayDat);
        txtThoiGian = createStyledTextField(thoiGian);
        txtKhachHang = createStyledTextField(khachHang); txtKhachHang.setEditable(false);

        cbTrangThai = new JComboBox<>(new String[]{"Đã đặt", "Đã hủy", "Đang dùng"});
        cbTrangThai.setSelectedItem(trangThai);

        gbc.gridx = 0; gbc.gridy = 0; pnlHeader.add(new JLabel("Mã đơn:"), gbc);
        gbc.gridx = 1; pnlHeader.add(txtMaDon, gbc);
        gbc.gridx = 2; gbc.gridy = 0; pnlHeader.add(new JLabel("Ngày đặt:"), gbc);
        gbc.gridx = 3; pnlHeader.add(txtNgayDat, gbc);

        gbc.gridx = 0; gbc.gridy = 1; pnlHeader.add(new JLabel("Khách hàng:"), gbc);
        gbc.gridx = 1; pnlHeader.add(txtKhachHang, gbc);
        gbc.gridx = 2; gbc.gridy = 1; pnlHeader.add(new JLabel("Thời gian:"), gbc);
        gbc.gridx = 3; pnlHeader.add(txtThoiGian, gbc);

        gbc.gridx = 2; gbc.gridy = 2; pnlHeader.add(new JLabel("Trạng thái:"), gbc);
        gbc.gridx = 3; pnlHeader.add(cbTrangThai, gbc);
    }

    private JTextField createStyledTextField(String text) {
        JTextField txt = new JTextField(text);
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txt.setPreferredSize(new Dimension(200, 32));
        return txt;
    }
}