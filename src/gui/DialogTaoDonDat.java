package gui;

import dao.DonDatBanDAO;
import entity.Ban;
import entity.DonDatBan;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DialogTaoDonDat extends JDialog {
    private static final long serialVersionUID = 1L;
    private JTextField txtMaDon, txtMaBan, txtHoTen, txtSdt, txtNgay, txtThoiGian, txtSoLuong, txtGhiChu;
    private JLabel lblErrorSdt, lblErrorHoTen; 
    private JButton btnLuu, btnHuy;
    private boolean isThanhCong = false;
    private final Color BTN_YELLOW = new Color(255, 209, 102);

    public DialogTaoDonDat(Window parent, Ban ban, String ngayLoc, String gioLoc, String soLuongLoc) {
        super(parent, "Tạo Đơn Đặt Bàn", ModalityType.APPLICATION_MODAL);
        setSize(450, 700); 
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // --- TIÊU ĐỀ ---
        JLabel lblHeader = new JLabel("THÔNG TIN ĐẶT BÀN", SwingConstants.CENTER);
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblHeader.setBorder(new EmptyBorder(20, 0, 10, 0));
        add(lblHeader, BorderLayout.NORTH);

        // --- NỘI DUNG NHẬP LIỆU ---
        JPanel pnlContent = new JPanel(new GridLayout(8, 2, 15, 15)); 
        pnlContent.setBackground(Color.WHITE);
        pnlContent.setBorder(new EmptyBorder(10, 30, 10, 30));

        // 1. Mã đơn
        txtMaDon = createStyledTextField(phatSinhMaDonTuDong());
        txtMaDon.setEditable(false);
        txtMaDon.setFocusable(false);

        // 2. Mã bàn
        txtMaBan = createStyledTextField(ban.getMaBan());
        txtMaBan.setEditable(false);
        txtMaBan.setFocusable(false);

        // =============================================================
        // 3. Họ tên khách hàng (ĐÃ THÊM BẮT LỖI KHI RỜI CHUỘT)
        // =============================================================
        txtHoTen = createStyledTextField("");
        lblErrorHoTen = new JLabel("* Phải nhập đủ họ tên");
        lblErrorHoTen.setForeground(Color.RED);
        lblErrorHoTen.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblErrorHoTen.setVisible(false);

        JPanel pnlHoTenWrapper = new JPanel(new BorderLayout(0, 2));
        pnlHoTenWrapper.setOpaque(false);
        pnlHoTenWrapper.add(txtHoTen, BorderLayout.CENTER);
        pnlHoTenWrapper.add(lblErrorHoTen, BorderLayout.SOUTH);

        txtHoTen.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                validateHoTen(); // Rời chuột đi thì kiểm tra xem có rỗng không
            }

            @Override
            public void focusGained(FocusEvent e) {
                // Trả lại viền xám bình thường khi bấm vào để gõ tiếp
                txtHoTen.putClientProperty("JComponent.outline", null);
                txtHoTen.setBorder(UIManager.getBorder("TextField.border"));
                lblErrorHoTen.setVisible(false);
            }
        });

        // 4. Số điện thoại (CÓ BẮT LỖI KHI RỜI CHUỘT)
        txtSdt = createStyledTextField("");
        lblErrorSdt = new JLabel("* SĐT phải bắt đầu bằng 0 và có 10 số");
        lblErrorSdt.setForeground(Color.RED);
        lblErrorSdt.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblErrorSdt.setVisible(false);

        JPanel pnlSdtWrapper = new JPanel(new BorderLayout(0, 2));
        pnlSdtWrapper.setOpaque(false);
        pnlSdtWrapper.add(txtSdt, BorderLayout.CENTER);
        pnlSdtWrapper.add(lblErrorSdt, BorderLayout.SOUTH);

        txtSdt.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                validateSDT(); 
            }

            @Override
            public void focusGained(FocusEvent e) {
                txtSdt.putClientProperty("JComponent.outline", null);
                txtSdt.setBorder(UIManager.getBorder("TextField.border"));
                lblErrorSdt.setVisible(false);
            }
        });

        // 5. Ngày đặt
        String ngayGoc = (ngayLoc != null && !ngayLoc.trim().isEmpty()) ? ngayLoc : LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        txtNgay = createStyledTextField(ngayGoc);

        // 6. Thời gian
        String gioGoc = (gioLoc != null && !gioLoc.trim().isEmpty()) ? gioLoc : LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        txtThoiGian = createStyledTextField(gioGoc);
        
        // 7. Số lượng khách
        String slGoc = (soLuongLoc != null && !soLuongLoc.trim().isEmpty()) ? soLuongLoc : "1";
        txtSoLuong = createStyledTextField(slGoc);
        
        // 8. Ghi chú
        txtGhiChu = createStyledTextField("");

        // Add vào panel
        pnlContent.add(new JLabel("Mã đơn:"));        pnlContent.add(txtMaDon);
        pnlContent.add(new JLabel("Mã bàn:"));        pnlContent.add(txtMaBan);
        pnlContent.add(new JLabel("Họ tên:"));         pnlContent.add(pnlHoTenWrapper); 
        pnlContent.add(new JLabel("Số điện thoại:"));  pnlContent.add(pnlSdtWrapper); 
        pnlContent.add(new JLabel("Ngày:"));           pnlContent.add(txtNgay);
        pnlContent.add(new JLabel("Thời gian:"));      pnlContent.add(txtThoiGian);
        pnlContent.add(new JLabel("Số lượng khách:")); pnlContent.add(txtSoLuong);
        pnlContent.add(new JLabel("Ghi chú:"));        pnlContent.add(txtGhiChu);

        add(pnlContent, BorderLayout.CENTER);

        // --- CỤM NÚT BẤM ---
        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        pnlBtns.setBackground(Color.WHITE);

        btnLuu = new JButton("Xác nhận");
        styleButton(btnLuu, BTN_YELLOW);
        
        btnHuy = new JButton("Hủy bỏ");
        styleButton(btnHuy, new Color(240, 240, 240));

        btnLuu.addActionListener(e -> {
            
            // 1. Kiểm tra rỗng
            if(txtHoTen.getText().trim().isEmpty() || txtSdt.getText().trim().isEmpty()) {
                validateHoTen(); // Chủ động gọi để nháy đỏ các ô rỗng
                validateSDT();
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ Họ tên và Số điện thoại khách!");
                return;
            }
            
            // 2. Chặn lưu nếu SĐT gõ sai định dạng
            if (!isValidSDT(txtSdt.getText().trim())) {
                JOptionPane.showMessageDialog(this, "Số điện thoại không hợp lệ!\nVui lòng nhập đúng 10 số và bắt đầu bằng số 0.", "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
                validateSDT(); 
                txtSdt.requestFocus();
                return;
            }

            try {
                // 3. Lấy thời gian từ text
                LocalDate ngay = LocalDate.parse(txtNgay.getText().trim(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                LocalTime gio = LocalTime.parse(txtThoiGian.getText().trim(), DateTimeFormatter.ofPattern("HH:mm"));
                
                // ==========================================================
                // 4. VALIDATE: KIỂM TRA THỜI GIAN TRONG QUÁ KHỨ NGAY TẠI FORM
                // ==========================================================
                LocalDateTime thoiGianChon = LocalDateTime.of(ngay, gio);
                if (thoiGianChon.isBefore(LocalDateTime.now())) {
                    JOptionPane.showMessageDialog(this, 
                        "Thời gian đặt bàn không hợp lệ!", 
                        "Cảnh báo", 
                        JOptionPane.WARNING_MESSAGE);
                    return; // Chặn lại, không thực hiện tiếp
                }

                isThanhCong = true;
                dispose();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập ngày giờ đúng định dạng (dd/MM/yyyy và HH:mm)!");
            }
        });
        
        btnHuy.addActionListener(e -> dispose());

        pnlBtns.add(btnLuu); 
        pnlBtns.add(btnHuy);
        add(pnlBtns, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(btnLuu);
    }

    // Hàm kiểm tra rỗng của Họ tên
    private void validateHoTen() {
        String hoten = txtHoTen.getText().trim();
        if (hoten.isEmpty()) {
            txtHoTen.putClientProperty("JComponent.outline", "error");
            txtHoTen.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
            lblErrorHoTen.setVisible(true);
        } else {
            txtHoTen.putClientProperty("JComponent.outline", null);
            txtHoTen.setBorder(UIManager.getBorder("TextField.border"));
            lblErrorHoTen.setVisible(false);
        }
    }

    // Hàm kiểm tra Regex của SĐT Việt Nam
    private boolean isValidSDT(String sdt) {
        return sdt.matches("^0\\d{9}$");
    }

    // Hàm đổi màu viền khi SĐT gõ sai
    private void validateSDT() {
        String sdt = txtSdt.getText().trim();
        if (sdt.isEmpty() || isValidSDT(sdt)) {
            txtSdt.putClientProperty("JComponent.outline", null);
            txtSdt.setBorder(UIManager.getBorder("TextField.border"));
            lblErrorSdt.setVisible(false);
        } else {
            txtSdt.putClientProperty("JComponent.outline", "error");
            txtSdt.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
            lblErrorSdt.setVisible(true);
        }
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
            return dao.getMaDonTiepTheo(); 
        } catch (Exception e) { 
            return "D001"; 
        }
    }

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