package gui;

import dao.NhanVien_Dao;
import entity.NhanVien;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.List;

public class DialogThemNV extends JDialog {
    private JTextField txtMa, txtTen, txtGmail, txtPass;
    private JComboBox<String> cbGioiTinh, cbChucVu;
    private JButton btnConfirm;
    private JLabel lblError;
    private NhanVien nvResult = null;

    private final Color PRIMARY_BLUE = new Color(54, 92, 245);
    private final Color DISABLED_GRAY = new Color(220, 220, 220);

    public DialogThemNV(Frame parent, NhanVien oldNv) {
        super(parent, true);
        setTitle(oldNv == null ? "Thêm nhân viên mới" : "Sửa thông tin nhân viên");
        setSize(450, 580); 
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // --- 1. FORM NHẬP LIỆU ---
        JPanel pnlForm = new JPanel(new GridLayout(6, 2, 10, 25));
        pnlForm.setBorder(new EmptyBorder(30, 40, 10, 40));
        pnlForm.setBackground(Color.WHITE);

        pnlForm.add(new JLabel("Mã nhân viên:"));
        txtMa = new JTextField(); 
        txtMa.setEditable(false); // Khóa ô nhập mã
        txtMa.setBackground(new Color(245, 245, 245)); // Tô màu xám nhạt
        txtMa.setFont(new Font("Segoe UI", Font.BOLD, 14));
        pnlForm.add(txtMa);
        
        pnlForm.add(new JLabel("Họ tên:"));
        txtTen = new JTextField(); pnlForm.add(txtTen);
        
        pnlForm.add(new JLabel("Gmail:"));
        txtGmail = new JTextField(); pnlForm.add(txtGmail);
        
        pnlForm.add(new JLabel("Mật khẩu:"));
        txtPass = new JTextField(); pnlForm.add(txtPass);
        
        pnlForm.add(new JLabel("Giới tính:"));
        cbGioiTinh = new JComboBox<>(new String[]{"Nam", "Nữ"}); pnlForm.add(cbGioiTinh);
        
        pnlForm.add(new JLabel("Chức vụ:"));
        cbChucVu = new JComboBox<>(new String[]{"NHANVIENBEP", "NHANVIENTHUNGAN", "NHANVIENPHUCVU"});
        pnlForm.add(cbChucVu);

        add(pnlForm, BorderLayout.CENTER);

        // --- 2. PHẦN DƯỚI: THÔNG BÁO LỖI & 2 NÚT (HỦY - LƯU) ---
        JPanel pnlBottom = new JPanel(new BorderLayout());
        pnlBottom.setBackground(Color.WHITE);
        pnlBottom.setBorder(new EmptyBorder(0, 40, 20, 40));

        lblError = new JLabel(" "); 
        lblError.setForeground(Color.RED);
        lblError.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblError.setHorizontalAlignment(SwingConstants.CENTER);
        lblError.setBorder(new EmptyBorder(0, 0, 10, 0));
        pnlBottom.add(lblError, BorderLayout.NORTH);

        // Panel chứa 2 nút nằm ngang
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        btnPanel.setBackground(Color.WHITE);

        JButton btnCancel = new JButton("Hủy");
        styleBtn(btnCancel, Color.GRAY);
        btnCancel.addActionListener(e -> dispose());

        btnConfirm = new JButton(oldNv == null ? "THÊM" : "SỬA"); 
        styleBtn(btnConfirm, PRIMARY_BLUE);
        btnConfirm.setEnabled(false);

        btnPanel.add(btnCancel);
        btnPanel.add(btnConfirm);
        
        pnlBottom.add(btnPanel, BorderLayout.CENTER);
        add(pnlBottom, BorderLayout.SOUTH);

        // --- ĐỔ DỮ LIỆU HOẶC TỰ ĐỘNG SINH MÃ ---
        if (oldNv != null) {
            // Chế độ Sửa: Đổ dữ liệu cũ lên
            txtMa.setText(oldNv.getMaNV());
            txtTen.setText(oldNv.getHoTen());
            txtGmail.setText(oldNv.getGmail());
            txtPass.setText(oldNv.getMatKhau());
            cbGioiTinh.setSelectedItem(oldNv.getGioiTinh());
            cbChucVu.setSelectedItem(oldNv.getChucVu());
        } else {
            // Chế độ Thêm: Tự động sinh mã nhân viên mới
            txtMa.setText(generateNewMaNV());
        }

        // --- 3. BẮT SỰ KIỆN LỖI ---
        SimpleListener dl = new SimpleListener(this::checkSaveButton);
        // Bỏ theo dõi txtMa vì nó đã bị khóa tự động sinh
        txtTen.getDocument().addDocumentListener(dl);
        txtGmail.getDocument().addDocumentListener(dl);
        txtPass.getDocument().addDocumentListener(dl);

        txtTen.addFocusListener(new FocusAdapter() { public void focusLost(FocusEvent e) { validData(true); } });
        txtGmail.addFocusListener(new FocusAdapter() { public void focusLost(FocusEvent e) { validData(true); } });
        txtPass.addFocusListener(new FocusAdapter() { public void focusLost(FocusEvent e) { validData(true); } });

        btnConfirm.addActionListener(e -> {
            if (validData(true)) {
                nvResult = new NhanVien(
                    txtMa.getText().trim(),
                    txtTen.getText().trim(),
                    txtGmail.getText().trim(),
                    cbChucVu.getSelectedItem().toString(),
                    5000000.0,
                    txtPass.getText().trim(),
                    cbGioiTinh.getSelectedItem().toString()
                );
                setVisible(false);
            }
        });

        checkSaveButton();
    }

    // --- HÀM TỰ ĐỘNG SINH MÃ NHÂN VIÊN KẾ TIẾP ---
    private String generateNewMaNV() {
        NhanVien_Dao dao = new NhanVien_Dao();
        List<NhanVien> dsNhanVien = dao.getAllNhanVien();
        
        int maxId = 0;
        for (NhanVien nv : dsNhanVien) {
            String ma = nv.getMaNV();
            if (ma != null && ma.startsWith("NV")) {
                try {
                    // Cắt bỏ chữ "NV" và chuyển phần số thành Integer
                    int currentId = Integer.parseInt(ma.substring(2));
                    if (currentId > maxId) {
                        maxId = currentId;
                    }
                } catch (NumberFormatException e) {
                    // Bỏ qua nếu có nhân viên mã không đúng chuẩn NV + số
                }
            }
        }
        
        // Tăng maxId lên 1 và format về chuỗi có 3 chữ số (VD: NV007)
        return String.format("NV%03d", maxId + 1);
    }

    private void styleBtn(JButton btn, Color color) {
        btn.setBackground(color); 
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false); 
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(100, 35)); 
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.putClientProperty("JButton.buttonType", "roundRect");
        btn.putClientProperty("JButton.arc", 15);
    }

    private void checkSaveButton() {
        boolean hasData = !txtTen.getText().trim().isEmpty() && 
                          !txtGmail.getText().trim().isEmpty() && 
                          !txtPass.getText().trim().isEmpty();
                          
        if (hasData) {
            boolean isValid = validData(false); 
            btnConfirm.setEnabled(isValid);
            btnConfirm.setBackground(isValid ? PRIMARY_BLUE : DISABLED_GRAY);
        } else {
            btnConfirm.setEnabled(false);
            btnConfirm.setBackground(DISABLED_GRAY);
            lblError.setText(" "); 
        }
    }

    private boolean validData(boolean showError) {
        String ten = txtTen.getText().trim();
        String gmail = txtGmail.getText().trim();
        String pass = txtPass.getText().trim();

        if (ten.isEmpty() || gmail.isEmpty() || pass.isEmpty()) {
            return false;
        }

        if (!gmail.endsWith("@gmail.com")) {
            if (showError) lblError.setText("Gmail bắt buộc phải có đuôi là '@gmail.com'");
            return false;
        }

        if (pass.length() < 5) {
            if (showError) lblError.setText("Mật khẩu phải từ 5 ký tự trở lên");
            return false;
        }

        boolean hasLetter = pass.matches(".*[a-zA-Z].*");
        boolean hasDigit = pass.matches(".*\\d.*");
        if (!hasLetter || !hasDigit) {
            if (showError) lblError.setText("Mật khẩu phải bao gồm cả chữ và số");
            return false;
        }

        lblError.setText(" "); 
        return true;
    }

    public NhanVien getNewEmployee() {
        return nvResult;
    }

    private class SimpleListener implements DocumentListener {
        private final Runnable r;
        public SimpleListener(Runnable r){ this.r=r; }
        public void insertUpdate(DocumentEvent e){ r.run(); }
        public void removeUpdate(DocumentEvent e){ r.run(); }
        public void changedUpdate(DocumentEvent e){ r.run(); }
    }
}