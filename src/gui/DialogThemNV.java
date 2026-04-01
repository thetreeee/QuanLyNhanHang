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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

public class DialogThemNV extends JDialog {
    private JTextField txtMa, txtTen, txtSDT, txtGmail, txtPass;
    private JComboBox<String> cbGioiTinh, cbChucVu, cbTrangThai;
    private JButton btnConfirm;
    private JLabel lblError;
    private NhanVien nvResult = null;
    private NhanVien_Dao nv_dao = new NhanVien_Dao();

    private final Color PRIMARY_BLUE = new Color(54, 92, 245);
    private final Color DISABLED_GRAY = new Color(220, 220, 220);

    public DialogThemNV(Frame parent, NhanVien oldNv, String currentStatus) {
        super(parent, true);
        setTitle(oldNv == null ? "Thêm nhân viên mới" : "Sửa thông tin nhân viên");
        
        setSize(450, oldNv == null ? 620 : 660); 
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // --- 1. FORM NHẬP LIỆU ---
        int rowCount = (oldNv == null) ? 7 : 8; 
        JPanel pnlForm = new JPanel(new GridLayout(rowCount, 2, 10, 20));
        pnlForm.setBorder(new EmptyBorder(30, 40, 10, 40));
        pnlForm.setBackground(Color.WHITE);

        pnlForm.add(new JLabel("Mã nhân viên:"));
        txtMa = new JTextField(); 
        txtMa.setEditable(false);
        txtMa.setBackground(new Color(240, 240, 240)); 
        txtMa.setFont(new Font("Segoe UI", Font.BOLD, 14));
        pnlForm.add(txtMa);
        
        pnlForm.add(new JLabel("Họ tên:"));
        txtTen = new JTextField(); pnlForm.add(txtTen);

        pnlForm.add(new JLabel("Số điện thoại:"));
        txtSDT = new JTextField(); pnlForm.add(txtSDT);
        
        pnlForm.add(new JLabel("Gmail:"));
        txtGmail = new JTextField(); pnlForm.add(txtGmail);
        
        pnlForm.add(new JLabel("Mật khẩu:"));
        txtPass = new JTextField(); pnlForm.add(txtPass);
        
        pnlForm.add(new JLabel("Giới tính:"));
        cbGioiTinh = new JComboBox<>(new String[]{"Nam", "Nữ"}); pnlForm.add(cbGioiTinh);
        
        pnlForm.add(new JLabel("Chức vụ:"));
        cbChucVu = new JComboBox<>(new String[]{"Nhân viên lễ tân", "Nhân viên thu ngân", "Nhân viên phục vụ"});
        pnlForm.add(cbChucVu);

        if (oldNv != null) {
            pnlForm.add(new JLabel("Trạng thái:"));
            cbTrangThai = new JComboBox<>(new String[]{"Đang làm", "Nghỉ việc"});
            pnlForm.add(cbTrangThai);
        }

        add(pnlForm, BorderLayout.CENTER);

        // --- 2. PHẦN DƯỚI: THÔNG BÁO LỖI & 2 NÚT ---
        JPanel pnlBottom = new JPanel(new BorderLayout());
        pnlBottom.setBackground(Color.WHITE);
        pnlBottom.setBorder(new EmptyBorder(0, 40, 20, 40));

        lblError = new JLabel(" "); 
        lblError.setForeground(Color.RED);
        lblError.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblError.setHorizontalAlignment(SwingConstants.CENTER);
        lblError.setBorder(new EmptyBorder(0, 0, 10, 0));
        pnlBottom.add(lblError, BorderLayout.NORTH);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        btnPanel.setBackground(Color.WHITE);

        JButton btnCancel = new JButton("Hủy");
        styleBtn(btnCancel, Color.GRAY);
        btnCancel.addActionListener(e -> dispose());

        btnConfirm = new JButton(oldNv == null ? "Thêm" : "Sửa"); 
        styleBtn(btnConfirm, PRIMARY_BLUE);
        btnConfirm.setEnabled(false);

        btnPanel.add(btnCancel);
        btnPanel.add(btnConfirm);
        
        pnlBottom.add(btnPanel, BorderLayout.CENTER);
        add(pnlBottom, BorderLayout.SOUTH);

        // --- ĐỔ DỮ LIỆU ---
        if (oldNv != null) {
            txtMa.setText(oldNv.getMaNV());
            txtTen.setText(oldNv.getHoTen());
            txtSDT.setText(oldNv.getSoDienThoai()); 
            txtGmail.setText(oldNv.getGmail());
            txtPass.setText(oldNv.getMatKhau());
            cbGioiTinh.setSelectedItem(oldNv.getGioiTinh());
            cbChucVu.setSelectedItem(oldNv.getChucVu());
            cbTrangThai.setSelectedItem(currentStatus); 
        } else {
            txtMa.setText(generateNewMaNV());
        }

        // --- 3. BẮT SỰ KIỆN LỖI ---
        
        // CẬP NHẬT: Chặn gõ phím sai ngay từ đầu cho ô Số điện thoại
        txtSDT.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                // 1. Nếu không phải là số -> Hủy bỏ phím vừa gõ
                if (!Character.isDigit(c)) {
                    e.consume();
                }
                // 2. Nếu đã gõ đủ 10 số -> Khóa không cho gõ thêm
                if (txtSDT.getText().length() >= 10) {
                    e.consume();
                }
            }
        });

        SimpleListener dl = new SimpleListener(() -> checkSaveButton());
        txtTen.getDocument().addDocumentListener(dl);
        txtSDT.getDocument().addDocumentListener(dl); 
        txtGmail.getDocument().addDocumentListener(dl);
        txtPass.getDocument().addDocumentListener(dl);

        txtTen.addFocusListener(new FocusAdapter() { public void focusLost(FocusEvent e) { validData(true); } });
        txtSDT.addFocusListener(new FocusAdapter() { public void focusLost(FocusEvent e) { validData(true); } }); 
        txtGmail.addFocusListener(new FocusAdapter() { public void focusLost(FocusEvent e) { validData(true); } });
        txtPass.addFocusListener(new FocusAdapter() { public void focusLost(FocusEvent e) { validData(true); } });

        btnConfirm.addActionListener(e -> {
            if (validData(true)) {
                String finalStatus = (oldNv == null) ? "Đang làm" : cbTrangThai.getSelectedItem().toString();

                nvResult = new NhanVien(
                    txtMa.getText().trim(),
                    txtTen.getText().trim(),
                    txtSDT.getText().trim(), 
                    txtGmail.getText().trim(),
                    cbChucVu.getSelectedItem().toString(),
                    5000000.0,
                    txtPass.getText().trim(),
                    cbGioiTinh.getSelectedItem().toString(),
                    finalStatus 
                );
                setVisible(false);
            }
        });

        checkSaveButton();
    }

    private String generateNewMaNV() {
        List<NhanVien> dsNhanVien = nv_dao.getAllNhanVien();
        int maxId = 0;
        for (NhanVien nv : dsNhanVien) {
            String ma = nv.getMaNV();
            if (ma != null && ma.startsWith("NV")) {
                try {
                    int currentId = Integer.parseInt(ma.substring(2));
                    if (currentId > maxId) {
                        maxId = currentId;
                    }
                } catch (NumberFormatException e) {
                }
            }
        }
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
                          !txtSDT.getText().trim().isEmpty() &&
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
        String sdt = txtSDT.getText().trim();
        String gmail = txtGmail.getText().trim();
        String pass = txtPass.getText().trim();

        String errorMsg = "";
        
        // --- 1. Kiểm tra Họ tên ---
        if (!ten.isEmpty()) {
            if (ten.matches(".*\\d.*")) {
                errorMsg = "Họ tên không được chứa chữ số";
            } else {
                boolean hasUpperCase = !ten.equals(ten.toLowerCase());
                boolean hasLowerCase = !ten.equals(ten.toUpperCase());
                if (!hasUpperCase || !hasLowerCase) {
                    errorMsg = "Họ tên phải chứa cả chữ in hoa và chữ thường";
                }
            }
        }

        // --- 2. Kiểm tra Số điện thoại ---
        if (errorMsg.isEmpty() && !sdt.isEmpty()) {
            // Regex: Đảm bảo đúng 10 số VÀ ký tự đầu tiên bắt buộc là số 0
            if (!sdt.matches("^0\\d{9}$")) {
                errorMsg = "Số điện thoại phải gồm 10 chữ số và bắt đầu bằng số 0";
            }
        }

        // --- 3. Kiểm tra Gmail ---
        if (errorMsg.isEmpty() && !gmail.isEmpty()) {
            if (!gmail.endsWith("@gmail.com")) {
                errorMsg = "Gmail bắt buộc phải có đuôi là '@gmail.com'";
            }
        }

        // --- 4. Kiểm tra Mật khẩu ---
        if (errorMsg.isEmpty() && !pass.isEmpty()) {
            if (pass.length() < 5) {
                errorMsg = "Mật khẩu phải từ 5 ký tự trở lên";
            } else {
                boolean hasLetter = pass.matches(".*[a-zA-Z].*");
                boolean hasDigit = pass.matches(".*\\d.*");
                if (!hasLetter || !hasDigit) {
                    errorMsg = "Mật khẩu phải bao gồm cả chữ và số";
                }
            }
        }

        // --- 5. XỬ LÝ GIAO DIỆN LỖI (UI) ---
        if (showError) {
            lblError.setText(errorMsg.isEmpty() ? " " : errorMsg);
        } else if (errorMsg.isEmpty()) {
            lblError.setText(" ");
        }

        boolean isFullData = !ten.isEmpty() && !sdt.isEmpty() && !gmail.isEmpty() && !pass.isEmpty();
        return isFullData && errorMsg.isEmpty();
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