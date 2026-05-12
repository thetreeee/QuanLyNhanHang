package gui;

import dao.KhachHang_DAO;
import entity.KhachHang;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class ThemKhachHangDialog extends JDialog {
    private JTextField txtMaKH, txtHoTen, txtSdt;
    private JLabel lblErrorSdt, lblErrorHoTen;
    private JButton btnLuu, btnHuy;
    private KhachHang_DAO khDAO = new KhachHang_DAO();
    private QuanLyKhachHang_Panel parentPanel;
    private final Color BTN_YELLOW = new Color(255, 209, 102);

    public ThemKhachHangDialog(Frame owner, QuanLyKhachHang_Panel parentPanel) {
        super(owner, "Thêm Khách Hàng Mới", true);
        this.parentPanel = parentPanel;
        
        setSize(450, 450);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // --- HEADER ---
        JLabel lblHeader = new JLabel("THÔNG TIN KHÁCH HÀNG", SwingConstants.CENTER);
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblHeader.setBorder(new EmptyBorder(25, 0, 10, 0));
        add(lblHeader, BorderLayout.NORTH);

        // --- CONTENT ---
        JPanel pnlContent = new JPanel(new GridLayout(3, 2, 15, 30));
        pnlContent.setBackground(Color.WHITE);
        pnlContent.setBorder(new EmptyBorder(20, 40, 20, 40));

        // 1. Mã khách hàng
        txtMaKH = createStyledTextField(khDAO.tuDongPhatSinhMa());
        txtMaKH.setEditable(false);
        txtMaKH.setFocusable(false);
        txtMaKH.setBackground(new Color(245, 245, 245));

        // 2. Số điện thoại
        txtSdt = createStyledTextField("");
        lblErrorSdt = new JLabel("* SĐT 10 số, bắt đầu bằng 0");
        lblErrorSdt.setForeground(Color.RED);
        lblErrorSdt.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblErrorSdt.setVisible(false);
        
        JPanel pnlSdtWrapper = new JPanel(new BorderLayout(0, 2));
        pnlSdtWrapper.setOpaque(false);
        pnlSdtWrapper.add(txtSdt, BorderLayout.CENTER);
        pnlSdtWrapper.add(lblErrorSdt, BorderLayout.SOUTH);

        // 3. Họ tên
        txtHoTen = createStyledTextField("");
        lblErrorHoTen = new JLabel("* Họ tên không chứa số");
        lblErrorHoTen.setForeground(Color.RED);
        lblErrorHoTen.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblErrorHoTen.setVisible(false);

        JPanel pnlHoTenWrapper = new JPanel(new BorderLayout(0, 2));
        pnlHoTenWrapper.setOpaque(false);
        pnlHoTenWrapper.add(txtHoTen, BorderLayout.CENTER);
        pnlHoTenWrapper.add(lblErrorHoTen, BorderLayout.SOUTH);

        pnlContent.add(new JLabel("Mã khách hàng:")); pnlContent.add(txtMaKH);
        pnlContent.add(new JLabel("Số điện thoại:"));  pnlContent.add(pnlSdtWrapper);
        pnlContent.add(new JLabel("Họ tên:"));         pnlContent.add(pnlHoTenWrapper);

        add(pnlContent, BorderLayout.CENTER);

        // --- BUTTONS ---
        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        pnlBtns.setBackground(Color.WHITE);

        btnLuu = new JButton("Lưu khách hàng");
        styleButton(btnLuu, BTN_YELLOW);
        // ĐÃ SỬA: Mặc định khóa nút Lưu khi vừa mở Form lên
        btnLuu.setEnabled(false); 
        
        btnHuy = new JButton("Hủy bỏ");
        styleButton(btnHuy, new Color(240, 240, 240));

        btnLuu.addActionListener(e -> thucHienLuu());
        btnHuy.addActionListener(e -> dispose());

        pnlBtns.add(btnLuu);
        pnlBtns.add(btnHuy);
        add(pnlBtns, BorderLayout.SOUTH);

        // Kích hoạt bộ kiểm tra tự động
        setupValidation();
    }

    private void setupValidation() {
        // 1. Kiểm tra Real-time từng ký tự gõ vào để MỞ/KHÓA nút Lưu
        DocumentListener docListener = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { checkEnableSaveButton(); }
            @Override public void removeUpdate(DocumentEvent e) { checkEnableSaveButton(); }
            @Override public void changedUpdate(DocumentEvent e) { checkEnableSaveButton(); }
        };
        
        txtSdt.getDocument().addDocumentListener(docListener);
        txtHoTen.getDocument().addDocumentListener(docListener);

        // 2. Chỉ hiện cảnh báo viền đỏ khi người dùng bấm chuột ra chỗ khác (Focus Lost)
        txtSdt.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) { validateSdtUI(); }
            
            @Override
            public void focusGained(FocusEvent e) { hideError(txtSdt, lblErrorSdt); }
        });
        
        txtHoTen.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) { validateHoTenUI(); }
            
            @Override
            public void focusGained(FocusEvent e) { hideError(txtHoTen, lblErrorHoTen); }
        });
    }

    // --- HÀM KIỂM TRA ĐỂ BẬT TẮT NÚT LƯU ---
    private void checkEnableSaveButton() {
        String sdt = txtSdt.getText().trim();
        String hoten = txtHoTen.getText().trim();
        
        boolean isSdtFormatValid = sdt.matches("^0\\d{9}$");
        boolean isHoTenValid = !hoten.isEmpty() && !hoten.matches(".*\\d.*");
        
        if (isSdtFormatValid && isHoTenValid) {
            // Chỉ khi gõ đủ 10 số mới chui xuống Database kiểm tra trùng (giúp phần mềm chạy nhẹ hơn)
            boolean isTrung = khDAO.kiemTraTrungSDT(sdt, null);
            
            // Bật nút Lưu nếu mọi thứ hợp lệ và SĐT không trùng
            btnLuu.setEnabled(!isTrung);
        } else {
            // Sai 1 điều kiện bất kỳ -> Khóa nút Lưu lập tức
            btnLuu.setEnabled(false);
        }
    }

    // --- HÀM KIỂM TRA HIỂN THỊ VIỀN ĐỎ CHO UI ---
    private boolean validateSdtUI() {
        String sdt = txtSdt.getText().trim();
        if (sdt.isEmpty()) return false; // Để trống thì không chửi, kệ nó (vì nút Lưu đã khóa rồi)
        
        if (!sdt.matches("^0\\d{9}$")) {
            showError(txtSdt, lblErrorSdt, "* SĐT phải đủ 10 số và bắt đầu bằng 0");
            return false;
        }
        if (khDAO.kiemTraTrungSDT(sdt, null)) {
            showError(txtSdt, lblErrorSdt, "* Số điện thoại này đã tồn tại!");
            return false;
        }
        hideError(txtSdt, lblErrorSdt);
        return true;
    }

    private boolean validateHoTenUI() {
        String hoten = txtHoTen.getText().trim();
        if (hoten.isEmpty()) return false; 
        
        if (hoten.matches(".*\\d.*")) {
            showError(txtHoTen, lblErrorHoTen, "* Họ tên không được chứa số");
            return false;
        }
        hideError(txtHoTen, lblErrorHoTen);
        return true;
    }

    private void thucHienLuu() {
        // Nút lưu đã được bảo vệ nên không cần check rườm rà nữa
        KhachHang kh = new KhachHang(
            txtMaKH.getText(),
            txtHoTen.getText().trim(),
            txtSdt.getText().trim(),
            0 // Tổng chi tiêu mặc định là 0
        );

        if (khDAO.insertKhachHang(kh)) {
            JOptionPane.showMessageDialog(this, "Thêm khách hàng thành công!");
            parentPanel.loadDataToTable(); // Refresh bảng ở màn hình chính
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Lỗi khi lưu vào Database!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showError(JTextField txt, JLabel lbl, String msg) {
        txt.putClientProperty("JComponent.outline", "error");
        lbl.setText(msg);
        lbl.setVisible(true);
    }

    private void hideError(JTextField txt, JLabel lbl) {
        txt.putClientProperty("JComponent.outline", null);
        lbl.setVisible(false);
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
        btn.setPreferredSize(new Dimension(150, 40));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.putClientProperty("JButton.buttonType", "roundRect");
        btn.putClientProperty("JButton.arc", 15);
    }
}