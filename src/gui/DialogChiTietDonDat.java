package gui;

import dao.BanDAO;
import dao.DonDatBanDAO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DialogChiTietDonDat extends JDialog {
    private static final long serialVersionUID = 1L;
    private JTextField txtMaDon, txtNgayDat, txtThoiGian, txtKhachHang;
    private JComboBox<String> cbTrangThai;
    private JTable tableChiTiet;
    private DefaultTableModel modelChiTiet;
    private JButton btnCapNhat, btnHuy;

    // ĐÃ NÂNG CẤP: Bổ sung tham số 'maBan' vào đây để bảng lấy dữ liệu thật
    public DialogChiTietDonDat(Window parent, String maDon, String maBan, String ngayDat, String thoiGian, String trangThai, String khachHang) {
        super(parent, "Chi Tiết Đơn Đặt Bàn", ModalityType.APPLICATION_MODAL);
        setSize(800, 600); 
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Color.WHITE);

        // ==========================================
        // 1. PHẦN HEADER
        // ==========================================
        JPanel pnlHeader = new JPanel(new GridBagLayout());
        pnlHeader.setBackground(new Color(255, 250, 205)); 
        pnlHeader.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY), 
            "Thông tin Đơn Đặt Bàn (Header)", 
            TitledBorder.LEFT, TitledBorder.TOP, 
            new Font("Segoe UI", Font.BOLD, 14), Color.BLACK
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 15, 10, 15); 

        txtMaDon = createStyledTextField(maDon);
        txtMaDon.setEditable(false);
        txtMaDon.setBackground(new Color(240, 240, 240)); 
        
        txtKhachHang = createStyledTextField(khachHang);
        txtNgayDat = createStyledTextField(ngayDat);
        txtThoiGian = createStyledTextField(thoiGian);
        
        cbTrangThai = new JComboBox<>(new String[]{"Đã đặt","Đã hủy"});
        cbTrangThai.setSelectedItem(trangThai);
        cbTrangThai.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbTrangThai.setPreferredSize(new Dimension(200, 32));

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.05;
        pnlHeader.add(new JLabel("Mã đơn:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.45;
        pnlHeader.add(txtMaDon, gbc);

        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0.05;
        pnlHeader.add(new JLabel("Ngày đặt:"), gbc);
        gbc.gridx = 3; gbc.gridy = 0; gbc.weightx = 0.45;
        pnlHeader.add(txtNgayDat, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.05;
        pnlHeader.add(new JLabel("Khách hàng:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0.45;
        pnlHeader.add(txtKhachHang, gbc);

        gbc.gridx = 2; gbc.gridy = 1; gbc.weightx = 0.05;
        pnlHeader.add(new JLabel("Thời gian:"), gbc);
        gbc.gridx = 3; gbc.gridy = 1; gbc.weightx = 0.45;
        pnlHeader.add(txtThoiGian, gbc);

        gbc.gridx = 2; gbc.gridy = 2; gbc.weightx = 0.05;
        pnlHeader.add(new JLabel("Trạng thái:"), gbc);
        gbc.gridx = 3; gbc.gridy = 2; gbc.weightx = 0.45;
        pnlHeader.add(cbTrangThai, gbc);

        JPanel pnlTopWrapper = new JPanel(new BorderLayout());
        pnlTopWrapper.setBackground(Color.WHITE);
        pnlTopWrapper.setBorder(new EmptyBorder(10, 10, 0, 10));
        pnlTopWrapper.add(pnlHeader, BorderLayout.CENTER);
        add(pnlTopWrapper, BorderLayout.NORTH);

        // ==========================================
        // 2. PHẦN DETAIL (CHI TIẾT)
        // ==========================================
        JPanel pnlDetail = new JPanel(new BorderLayout(0, 10));
        pnlDetail.setBackground(Color.WHITE);
        pnlDetail.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY), 
            "Chi tiết Bàn đặt", 
            TitledBorder.LEFT, TitledBorder.TOP, 
            new Font("Segoe UI", Font.BOLD, 14), Color.BLACK
        ));

        String[] cols = {"Mã Bàn", "Số lượng khách dự kiến"}; 
        
        // --- YÊU CẦU 2: KHÓA BẢNG KHÔNG CHO CHỈNH SỬA ---
        modelChiTiet = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Tuyệt đối không cho sửa trực tiếp trên ô
            }
        };
        
        tableChiTiet = new JTable(modelChiTiet);
        tableChiTiet.setRowHeight(35);
        tableChiTiet.getTableHeader().setBackground(new Color(255, 235, 235));
        tableChiTiet.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        DefaultTableCellRenderer centerRender = new DefaultTableCellRenderer();
        centerRender.setHorizontalAlignment(SwingConstants.CENTER);
        for(int i=0; i<tableChiTiet.getColumnCount(); i++){
            tableChiTiet.getColumnModel().getColumn(i).setCellRenderer(centerRender);
        }

        // Add dữ liệu bàn lấy từ tham số truyền vào
        modelChiTiet.addRow(new Object[]{maBan, "Chưa xác định"});

        JScrollPane scroll = new JScrollPane(tableChiTiet);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); 
        pnlDetail.add(scroll, BorderLayout.CENTER);

        JPanel pnlCenterWrapper = new JPanel(new BorderLayout());
        pnlCenterWrapper.setBackground(Color.WHITE);
        pnlCenterWrapper.setBorder(new EmptyBorder(10, 10, 10, 10));
        pnlCenterWrapper.add(pnlDetail, BorderLayout.CENTER);
        add(pnlCenterWrapper, BorderLayout.CENTER);

        // ==========================================
        // 3. PHẦN NÚT BẤM DƯỚI CÙNG
        // ==========================================
        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        pnlBottom.setBackground(Color.WHITE);

        btnCapNhat = new JButton("Cập Nhật Đơn Đặt");
        btnCapNhat.setBackground(new Color(65, 105, 225)); 
        btnCapNhat.setForeground(Color.WHITE);
        btnCapNhat.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCapNhat.setPreferredSize(new Dimension(180, 40));

        btnHuy = new JButton("Hủy");
        btnHuy.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnHuy.setPreferredSize(new Dimension(100, 40));

        btnHuy.addActionListener(e -> dispose());

        // --- YÊU CẦU 1 & 3: XỬ LÝ NÚT CẬP NHẬT ---
        btnCapNhat.addActionListener(e -> {
            String trangThaiMoi = cbTrangThai.getSelectedItem().toString();
            String maDonHienTai = txtMaDon.getText().trim();
            
            DonDatBanDAO donDAO = new DonDatBanDAO();
            BanDAO banDao = new BanDAO();
            
            // 1. Lưu trạng thái mới của đơn xuống DB
            if (donDAO.updateTrangThaiCuaDon(maDonHienTai, trangThaiMoi)) {
                
                // 2. Chỉnh màu/trạng thái của Bàn tương ứng
                for (int i = 0; i < modelChiTiet.getRowCount(); i++) {
                    String banTrongDon = modelChiTiet.getValueAt(i, 0).toString();
                    
                    if (trangThaiMoi.equals("Đã hủy")) {
                        banDao.updateTrangThaiBan(banTrongDon, "Trống"); // Trả về màu Xanh
                    } else if (trangThaiMoi.equals("Đang dùng")) {
                        banDao.updateTrangThaiBan(banTrongDon, "Đang dùng"); // Trả về màu Đỏ
                    } else if (trangThaiMoi.equals("Đã đặt")) {
                        DateTimeFormatter fNgay = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                        // Nếu là đơn đặt cho hôm nay thì mới khóa màu Vàng
                        if (txtNgayDat.getText().trim().equals(LocalDate.now().format(fNgay))) {
                            banDao.updateTrangThaiBan(banTrongDon, "Đã đặt");
                        }
                    }
                }
                
                JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                dispose(); // Đóng form
            } else {
                JOptionPane.showMessageDialog(this, "Có lỗi xảy ra khi cập nhật!");
            }
        });

        pnlBottom.add(btnCapNhat);
        pnlBottom.add(btnHuy);
        add(pnlBottom, BorderLayout.SOUTH);
    }

    private JTextField createStyledTextField(String text) {
        JTextField txt = new JTextField(text);
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txt.setPreferredSize(new Dimension(200, 32));
        return txt;
    }
}