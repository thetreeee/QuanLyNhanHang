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
import com.toedter.calendar.JDateChooser;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;

public class DialogThemNV extends JDialog {
    private JTextField txtMa, txtTen, txtSDT, txtGmail, txtPass;
    private JComboBox<String> cbGioiTinh, cbChucVu, cbTrangThai;
    private JLabel errTen, errDate, errSDT, errGmail, errPass;
    private JDateChooser dateSinh;
    private JButton btnConfirm;
    private NhanVien nvResult = null;
    private NhanVien_Dao nv_dao = new NhanVien_Dao();

    private final Color PRIMARY_BLUE = new Color(54, 92, 245);
    private final Color DISABLED_GRAY = new Color(220, 220, 220);

    public DialogThemNV(Frame parent, NhanVien oldNv, String currentStatus) {
        super(parent, true);
        setTitle(oldNv == null ? "Thêm nhân viên mới" : "Sửa thông tin nhân viên");
        
        setSize(450, oldNv == null ? 670 : 710); 
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // --- 1. FORM NHẬP LIỆU ---
        int rowCount = (oldNv == null) ? 8 : 9; 
        JPanel pnlForm = new JPanel(new GridLayout(rowCount, 2, 10, 10));
        pnlForm.setBorder(new EmptyBorder(30, 40, 10, 40));
        pnlForm.setBackground(Color.WHITE);

        errTen = new JLabel(" "); errDate = new JLabel(" "); errSDT = new JLabel(" ");
        errGmail = new JLabel(" "); errPass = new JLabel(" ");

        pnlForm.add(new JLabel("Mã nhân viên:"));
        txtMa = new JTextField(); 
        txtMa.setEditable(false);
        txtMa.setBackground(new Color(240, 240, 240)); 
        txtMa.setFont(new Font("Segoe UI", Font.BOLD, 14));
        pnlForm.add(txtMa);
        
        pnlForm.add(new JLabel("Họ tên:"));
        txtTen = new JTextField(); 
        pnlForm.add(createInputPanel(txtTen, errTen));

        pnlForm.add(new JLabel("Ngày sinh:"));
        dateSinh = createModernDateChooser(); 
        pnlForm.add(createInputPanel(dateSinh, errDate));

        pnlForm.add(new JLabel("Số điện thoại:"));
        txtSDT = new JTextField(); 
        pnlForm.add(createInputPanel(txtSDT, errSDT));
        
        pnlForm.add(new JLabel("Gmail:"));
        txtGmail = new JTextField(); 
        pnlForm.add(createInputPanel(txtGmail, errGmail));
        
        pnlForm.add(new JLabel("Mật khẩu:"));
        txtPass = new JTextField(); 
        pnlForm.add(createInputPanel(txtPass, errPass));
        
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

        // --- 2. PHẦN DƯỚI: 2 NÚT ---
        JPanel pnlBottom = new JPanel(new BorderLayout());
        pnlBottom.setBackground(Color.WHITE);
        pnlBottom.setBorder(new EmptyBorder(10, 40, 20, 40));

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
            if (oldNv.getNgaySinh() != null) {
                dateSinh.setDate(Date.from(oldNv.getNgaySinh().atStartOfDay(ZoneId.systemDefault()).toInstant()));
            }
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
        
        dateSinh.getDateEditor().addPropertyChangeListener(e -> {
            if ("date".equals(e.getPropertyName())) {
                checkSaveButton();
                validData(true);
            }
        });

        txtTen.addFocusListener(new FocusAdapter() { public void focusLost(FocusEvent e) { validData(true); } });
        txtSDT.addFocusListener(new FocusAdapter() { public void focusLost(FocusEvent e) { validData(true); } }); 
        txtGmail.addFocusListener(new FocusAdapter() { public void focusLost(FocusEvent e) { validData(true); } });
        txtPass.addFocusListener(new FocusAdapter() { public void focusLost(FocusEvent e) { validData(true); } });

        btnConfirm.addActionListener(e -> {
            if (validData(true)) {
                String finalStatus = (oldNv == null) ? "Đang làm" : cbTrangThai.getSelectedItem().toString();

                // RÀNG BUỘC THỜI GIAN KHI CHO NGHỈ VIỆC
                if (oldNv != null && !"Nghỉ việc".equals(oldNv.getTrangThai()) && "Nghỉ việc".equals(finalStatus)) {
                    LocalTime now = LocalTime.now();
                    if (now.isAfter(LocalTime.of(5, 0)) && now.isBefore(LocalTime.of(23, 0))) {
                        JOptionPane.showMessageDialog(this, 
                            "Chỉ được phép cho nhân viên nghỉ việc ngoài giờ làm (từ 23:00 đêm đến 05:00 sáng).", 
                            "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                        return; // Chặn lưu
                    }
                }

                LocalDate ns = null;
                if (dateSinh.getDate() != null) {
                    ns = dateSinh.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                }

                nvResult = new NhanVien(
                    txtMa.getText().trim(),
                    txtTen.getText().trim(),
                    ns,
                    txtSDT.getText().trim(), 
                    txtGmail.getText().trim(),
                    cbChucVu.getSelectedItem().toString(),
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

    private JPanel createInputPanel(JComponent input, JLabel errLabel) {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setOpaque(false);
        pnl.add(input, BorderLayout.CENTER);
        
        errLabel.setForeground(Color.RED);
        errLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        errLabel.setBorder(new EmptyBorder(2, 2, 0, 0));
        pnl.add(errLabel, BorderLayout.SOUTH);
        
        return pnl;
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

    private JDateChooser createModernDateChooser() {
        com.toedter.calendar.JTextFieldDateEditor editor = new com.toedter.calendar.JTextFieldDateEditor("dd/MM/yyyy", "##/##/####", '_') {
            @Override
            protected void paintComponent(Graphics g) {
                String currentText = getText();
                if (!hasFocus() && (currentText == null || currentText.isEmpty() || currentText.equals("__/__/____"))) {
                    // Ẩn chữ mask "__/__/____" đi bằng cách đổi màu chữ cùng màu nền
                    Color oldColor = getForeground();
                    setForeground(Color.WHITE);
                    super.paintComponent(g);
                    setForeground(oldColor);
                    
                    // Vẽ đè placeholder "dd/mm/yyyy" màu xám lên trên
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(Color.GRAY);
                    g2.setFont(getFont());
                    FontMetrics fm = g2.getFontMetrics();
                    int x = getInsets().left;
                    int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                    g2.drawString("dd/mm/yyyy", x, y);
                    g2.dispose();
                } else {
                    super.paintComponent(g);
                }
            }
        };
        JDateChooser dateChooser = new JDateChooser(editor);
        dateChooser.setPreferredSize(new Dimension(140, 32));
        dateChooser.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        // Tùy chỉnh editor (ô nhập text)
        editor.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        editor.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        editor.setBackground(Color.WHITE);
        
        // Tùy chỉnh nút bấm (calendar icon)
        JButton btn = dateChooser.getCalendarButton();
        btn.setIcon(null);
        btn.setText("📅"); 
        btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        btn.setBackground(Color.WHITE);
        btn.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        
        // Cập nhật viền cho toàn bộ JDateChooser
        dateChooser.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(2, 2, 2, 2)
        ));
        dateChooser.setBackground(Color.WHITE);
        
        return dateChooser;
    }

    private void checkSaveButton() {
        boolean hasData = !txtTen.getText().trim().isEmpty() && 
                          !txtSDT.getText().trim().isEmpty() &&
                          !txtGmail.getText().trim().isEmpty() && 
                          !txtPass.getText().trim().isEmpty() &&
                          dateSinh.getDate() != null;
                          
        if (hasData) {
            boolean isValid = validData(false); 
            btnConfirm.setEnabled(isValid);
            btnConfirm.setBackground(isValid ? PRIMARY_BLUE : DISABLED_GRAY);
        } else {
            btnConfirm.setEnabled(false);
            btnConfirm.setBackground(DISABLED_GRAY);
        }
    }

    private boolean validData(boolean showError) {
        String ten = txtTen.getText().trim();
        String sdt = txtSDT.getText().trim();
        String gmail = txtGmail.getText().trim();
        String pass = txtPass.getText().trim();

        String errorMsg = "";
        JComponent errorComponent = null;
        
        // --- 1. Kiểm tra Họ tên ---
        if (!ten.isEmpty()) {
            if (ten.matches(".*\\d.*")) {
                errorMsg = "Họ tên không được chứa chữ số";
                errorComponent = txtTen;
            } else {
                boolean hasUpperCase = !ten.equals(ten.toLowerCase());
                boolean hasLowerCase = !ten.equals(ten.toUpperCase());
                if (!hasUpperCase || !hasLowerCase) {
                    errorMsg = "Phải có chũ hoa và chữ thường";
                    errorComponent = txtTen;
                }
            }
        }

        // --- 2. Kiểm tra Số điện thoại ---
        if (errorMsg.isEmpty() && !sdt.isEmpty()) {
            // Regex: Đảm bảo đúng 10 số VÀ ký tự đầu tiên bắt buộc là số 0
            if (!sdt.matches("^0\\d{9}$")) {
                errorMsg = "Số điện thoại phải gồm 10 chữ số và bắt đầu bằng số 0";
                errorComponent = txtSDT;
            } else {
                // Kiểm tra trùng số điện thoại với nhân viên "Đang làm"
                List<NhanVien> listNV = nv_dao.getAllNhanVien();
                for (NhanVien nv : listNV) {
                    if ("Đang làm".equals(nv.getTrangThai()) 
                        && sdt.equals(nv.getSoDienThoai()) 
                        && !txtMa.getText().trim().equals(nv.getMaNV())) {
                        errorMsg = "Bị trùng số điện thoại với " + nv.getHoTen();
                        errorComponent = txtSDT;
                        break;
                    }
                }
            }
        }

        // --- 3. Kiểm tra Gmail ---
        if (errorMsg.isEmpty() && !gmail.isEmpty()) {
            if (!gmail.contains("@") || !gmail.endsWith(".com")) {
                errorMsg = "Gmail bắt buộc phải chứa '@' và có đuôi là '.com'";
                errorComponent = txtGmail;
            }
        }

        // --- 4. Kiểm tra Mật khẩu ---
        if (errorMsg.isEmpty() && !pass.isEmpty()) {
            if (pass.length() < 5) {
                errorMsg = "Mật khẩu phải từ 5 ký tự trở lên";
                errorComponent = txtPass;
            } else {
                boolean hasLetter = pass.matches(".*[a-zA-Z].*");
                boolean hasDigit = pass.matches(".*\\d.*");
                if (!hasLetter || !hasDigit) {
                    errorMsg = "Mật khẩu phải bao gồm cả chữ và số";
                    errorComponent = txtPass;
                }
            }
        }

        // --- 5. Kiểm tra Ngày sinh ---
        if (errorMsg.isEmpty() && dateSinh.getDate() != null) {
            LocalDate ns = dateSinh.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            int age = Period.between(ns, LocalDate.now()).getYears();
            if (age < 18 || age > 40) {
                errorMsg = "Nhân viên phải có độ tuổi từ 18 đến 40 tuổi!";
                errorComponent = dateSinh;
            }
        }

        // --- 6. XỬ LÝ GIAO DIỆN LỖI (UI) ---
        // Reset border về mặc định
        txtTen.setBorder(UIManager.getBorder("TextField.border"));
        txtSDT.setBorder(UIManager.getBorder("TextField.border"));
        txtGmail.setBorder(UIManager.getBorder("TextField.border"));
        txtPass.setBorder(UIManager.getBorder("TextField.border"));
        dateSinh.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(2, 2, 2, 2)
        ));

        // Reset text lỗi
        errTen.setText(" ");
        errSDT.setText(" ");
        errGmail.setText(" ");
        errPass.setText(" ");
        errDate.setText(" ");

        // Bôi đỏ ô bị lỗi nếu có
        if (showError && errorComponent != null) {
            errorComponent.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
            // Đặt text lỗi tương ứng
            if (errorComponent == txtTen) errTen.setText(errorMsg);
            else if (errorComponent == txtSDT) errSDT.setText(errorMsg);
            else if (errorComponent == txtGmail) errGmail.setText(errorMsg);
            else if (errorComponent == txtPass) errPass.setText(errorMsg);
            else if (errorComponent == dateSinh) errDate.setText(errorMsg);
        }

        boolean isFullData = !ten.isEmpty() && !sdt.isEmpty() && !gmail.isEmpty() && !pass.isEmpty() && dateSinh.getDate() != null;
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