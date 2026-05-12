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

public class SuaKhachHangDialog extends JDialog {
    private JTextField txtMaKH, txtHoTen, txtSdt;
    private JLabel lblErrorSdt, lblErrorHoTen;
    private JButton btnCapNhat, btnHuy;
    private KhachHang_DAO khDAO = new KhachHang_DAO();
    private QuanLyKhachHang_Panel parentPanel;
    private String maKH;
    private final Color BTN_BLUE = new Color(54, 92, 245);

    public SuaKhachHangDialog(Frame owner, QuanLyKhachHang_Panel parentPanel, String maKH) {
        super(owner, "Cập Nhật Thông Tin Khách Hàng", true);
        this.parentPanel = parentPanel;
        this.maKH = maKH;
        
        setSize(450, 450);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // --- HEADER ---
        JLabel lblHeader = new JLabel("CẬP NHẬT KHÁCH HÀNG", SwingConstants.CENTER);
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblHeader.setBorder(new EmptyBorder(25, 0, 10, 0));
        add(lblHeader, BorderLayout.NORTH);

        // --- CONTENT ---
        JPanel pnlContent = new JPanel(new GridLayout(3, 2, 15, 30));
        pnlContent.setBackground(Color.WHITE);
        pnlContent.setBorder(new EmptyBorder(20, 40, 20, 40));

        // 1. Mã khách hàng (KHÓA)
        txtMaKH = createStyledTextField(maKH);
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

        btnCapNhat = new JButton("Lưu thay đổi");
        styleButton(btnCapNhat, BTN_BLUE);
        btnCapNhat.setForeground(Color.WHITE);
        
        btnHuy = new JButton("Hủy bỏ");
        styleButton(btnHuy, new Color(240, 240, 240));

        btnCapNhat.addActionListener(e -> thucHienCapNhat());
        btnHuy.addActionListener(e -> dispose());

        pnlBtns.add(btnCapNhat);
        pnlBtns.add(btnHuy);
        add(pnlBtns, BorderLayout.SOUTH);

        // Đổ dữ liệu cũ vào form và kích hoạt validation
        loadDataToForm();
        setupValidation();
    }

    private void loadDataToForm() {
        KhachHang kh = khDAO.getKhachHangByMa(maKH);
        if (kh != null) {
            txtHoTen.setText(kh.getHoTen());
            txtSdt.setText(kh.getSoDienThoai());
        }
    }

    private void setupValidation() {
        DocumentListener docListener = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { checkEnableUpdateButton(); }
            @Override public void removeUpdate(DocumentEvent e) { checkEnableUpdateButton(); }
            @Override public void changedUpdate(DocumentEvent e) { checkEnableUpdateButton(); }
        };
        
        txtSdt.getDocument().addDocumentListener(docListener);
        txtHoTen.getDocument().addDocumentListener(docListener);

        txtSdt.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) { validateSdtUI(); }
            @Override public void focusGained(FocusEvent e) { hideError(txtSdt, lblErrorSdt); }
        });
        
        txtHoTen.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) { validateHoTenUI(); }
            @Override public void focusGained(FocusEvent e) { hideError(txtHoTen, lblErrorHoTen); }
        });
    }

    private void checkEnableUpdateButton() {
        String sdt = txtSdt.getText().trim();
        String hoten = txtHoTen.getText().trim();
        
        boolean isSdtFormatValid = sdt.matches("^0\\d{9}$");
        boolean isHoTenValid = !hoten.isEmpty() && !hoten.matches(".*\\d.*");
        
        if (isSdtFormatValid && isHoTenValid) {
            // Khi sửa, cho phép giữ nguyên SĐT cũ nhưng không được trùng với SĐT của người khác
            boolean isTrung = khDAO.kiemTraTrungSDT(sdt, maKH);
            btnCapNhat.setEnabled(!isTrung);
        } else {
            btnCapNhat.setEnabled(false);
        }
    }

    private boolean validateSdtUI() {
        String sdt = txtSdt.getText().trim();
        if (sdt.isEmpty()) return false;
        
        if (!sdt.matches("^0\\d{9}$")) {
            showError(txtSdt, lblErrorSdt, "* SĐT phải đủ 10 số và bắt đầu bằng 0");
            return false;
        }
        if (khDAO.kiemTraTrungSDT(sdt, maKH)) {
            showError(txtSdt, lblErrorSdt, "* Số điện thoại này đã thuộc về khách khác!");
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

    private void thucHienCapNhat() {
        KhachHang kh = new KhachHang();
        kh.setMaKH(maKH);
        kh.setHoTen(txtHoTen.getText().trim());
        kh.setSoDienThoai(txtSdt.getText().trim());

        if (khDAO.updateKhachHang(kh)) {
            JOptionPane.showMessageDialog(this, "Cập nhật thông tin thành công!");
            parentPanel.loadDataToTable();
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật dữ liệu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
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