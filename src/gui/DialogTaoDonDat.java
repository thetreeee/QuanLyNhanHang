package gui;

import dao.DonDatBanDAO;
import entity.Ban;
import entity.DonDatBan;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DialogTaoDonDat extends JDialog {
    private static final long serialVersionUID = 1L;
    private JTextField txtMaDon, txtMaBan, txtHoTen, txtSdt, txtNgay, txtThoiGian, txtSoLuong, txtGhiChu;
    private JButton btnLuu, btnHuy;
    private boolean isThanhCong = false;
    private final Color BTN_YELLOW = new Color(255, 209, 102);

    public DialogTaoDonDat(Window parent, Ban ban) {
        super(parent, "Tạo Đơn Đặt Bàn", ModalityType.APPLICATION_MODAL);
        setSize(450, 650); // Tăng chiều cao một chút để chứa thêm 2 hàng mới
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // --- TIÊU ĐỀ ---
        JLabel lblHeader = new JLabel("THÔNG TIN ĐẶT BÀN", SwingConstants.CENTER);
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblHeader.setBorder(new EmptyBorder(20, 0, 10, 0));
        add(lblHeader, BorderLayout.NORTH);

        // --- NỘI DUNG NHẬP LIỆU (8 hàng, 2 cột) ---
        JPanel pnlContent = new JPanel(new GridLayout(8, 2, 15, 20)); 
        pnlContent.setBackground(Color.WHITE);
        pnlContent.setBorder(new EmptyBorder(10, 30, 10, 30));

        // 1. Mã đơn
        txtMaDon = createStyledTextField(phatSinhMaDonTuDong());
        txtMaDon.setEditable(false);
        txtMaDon.setFocusable(false);
        txtMaDon.setBackground(new Color(240, 240, 240));

        // 2. Mã bàn
        txtMaBan = createStyledTextField(ban.getMaBan());
        txtMaBan.setEditable(false);
        txtMaBan.setFocusable(false);
        txtMaBan.setBackground(new Color(240, 240, 240));

        // 3. Họ tên khách hàng (MỚI)
        txtHoTen = createStyledTextField("");

        // 4. Số điện thoại (MỚI)
        txtSdt = createStyledTextField("");

        // 5. Ngày đặt
        txtNgay = createStyledTextField(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        txtNgay.setEditable(true); 

        // 6. Thời gian
        txtThoiGian = createStyledTextField(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        
        // 7. Số lượng khách
        txtSoLuong = createStyledTextField("1");
        
        // 8. Ghi chú
        txtGhiChu = createStyledTextField("");

        // Add vào panel theo đúng thứ tự anh yêu cầu
        pnlContent.add(new JLabel("Mã đơn:"));        pnlContent.add(txtMaDon);
        pnlContent.add(new JLabel("Mã bàn:"));        pnlContent.add(txtMaBan);
        pnlContent.add(new JLabel("Họ tên:"));         pnlContent.add(txtHoTen);
        pnlContent.add(new JLabel("Số điện thoại:"));  pnlContent.add(txtSdt);
        pnlContent.add(new JLabel("Ngày:"));          pnlContent.add(txtNgay);
        pnlContent.add(new JLabel("Thời gian:"));     pnlContent.add(txtThoiGian);
        pnlContent.add(new JLabel("Số lượng khách:")); pnlContent.add(txtSoLuong);
        pnlContent.add(new JLabel("Ghi chú:"));       pnlContent.add(txtGhiChu);

        add(pnlContent, BorderLayout.CENTER);

        // --- CỤM NÚT BẤM ---
        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        pnlBtns.setBackground(Color.WHITE);

        btnLuu = new JButton("Xác nhận");
        styleButton(btnLuu, BTN_YELLOW);
        
        btnHuy = new JButton("Hủy bỏ");
        styleButton(btnHuy, new Color(240, 240, 240));

        btnLuu.addActionListener(e -> {
            if(txtHoTen.getText().trim().isEmpty() || txtSdt.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ Họ tên và Số điện thoại khách!");
                return;
            }
            // Validate định dạng SDT cơ bản
            if(!txtSdt.getText().trim().matches("^\\d{10}$")) {
                JOptionPane.showMessageDialog(this, "Số điện thoại phải gồm 10 chữ số!");
                return;
            }
            isThanhCong = true;
            dispose();
        });
        
        btnHuy.addActionListener(e -> dispose());

        pnlBtns.add(btnLuu); 
        pnlBtns.add(btnHuy);
        add(pnlBtns, BorderLayout.SOUTH);
    }

    private JTextField createStyledTextField(String text) {
        JTextField txt = new JTextField(text);
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txt.putClientProperty("JTextField.arc", 10);
        return txt;
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(Color.BLACK);
        btn.setPreferredSize(new Dimension(120, 40));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.putClientProperty("JButton.buttonType", "roundRect");
        btn.putClientProperty("JButton.arc", 15);
    }

    private String phatSinhMaDonTuDong() {
        try {
            DonDatBanDAO dao = new DonDatBanDAO();
            List<DonDatBan> ds = dao.getAllDonDat();
            if (ds == null || ds.isEmpty()) return "D001";
            int maxSo = 0;
            for (DonDatBan d : ds) {
                String ma = d.getMaDon();
                if (ma != null && ma.length() > 1) {
                    try {
                        int so = Integer.parseInt(ma.substring(1));
                        if (so > maxSo) maxSo = so;
                    } catch (NumberFormatException e) {
                        // Bỏ qua nếu mã không đúng chuẩn Dxxx
                    }
                }
            }
            return String.format("D%03d", maxSo + 1);
        } catch (Exception e) { return "D001"; }
    }

    // =================================================================
    // HÀM NHẬN DỮ LIỆU TỪ BỘ LỌC BÊN SƠ ĐỒ BÀN CHUYỀN SANG (MỚI THÊM)
    // =================================================================
    public void setDuLieuTuBoLoc(String ngay, String gio, String soLuong) {
        txtNgay.setText(ngay);
        txtThoiGian.setText(gio); 
        txtSoLuong.setText(soLuong); 
    }

    // Các hàm getter
    public boolean isThanhCong() { return isThanhCong; }
    public String getMaDon() { return txtMaDon.getText().trim(); }
    public String getMaBan() { return txtMaBan.getText().trim(); }
    public String getHoTen() { return txtHoTen.getText().trim(); }
    public String getSdt() { return txtSdt.getText().trim(); }
    public String getNgay() { return txtNgay.getText().trim(); }
    public String getGio() { return txtThoiGian.getText().trim(); }
    public String getSoLuong() { return txtSoLuong.getText().trim(); }
    public String getGhiChu() { return txtGhiChu.getText().trim(); }
}