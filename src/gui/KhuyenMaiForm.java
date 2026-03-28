package gui;

import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import dao.KhuyenMai_DAO;
import entity.DoiTuongKM;
import entity.KhuyenMai;

public class KhuyenMaiForm extends JDialog {

    private static final long serialVersionUID = 1L;
    private final Color BG = new Color(255, 255, 255);
    private final Color PRIMARY = new Color(54, 92, 245);
    private final Color BORDER = new Color(220, 220, 220);
    private final Color RED = new Color(209, 5, 60);

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private JLabel lblErrTen, lblErrGiaTri, lblErrNgayBD, lblErrNgayKT, lblErrDoiTuong;
    private JTextField txtTen, txtGiaTri, txtNgayBD, txtNgayKT;
    private JComboBox<DoiTuongKM> cbDoiTuong;
    private JComboBox<String> cbHinhThuc;
    private JButton btnSave;
    private LocalDate ngayBD;
    
    private KhuyenMaiPanel parentPanel;

    public KhuyenMaiForm(JFrame parent, KhuyenMaiPanel parentPanel) {
        super(parent, "Thêm khuyến mãi", true);
        this.parentPanel = parentPanel;
        
        setSize(500, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel main = new JPanel(new GridLayout(8, 2, 15, 15));
        main.setBackground(BG);
        main.setBorder(new EmptyBorder(20, 25, 20, 25));

        JTextField txtMa = createInput();
        KhuyenMai_DAO dao = new KhuyenMai_DAO();
        txtMa.setText(dao.generateMaKM());
        txtMa.setEditable(false);

        txtTen = createInput();
        lblErrTen = createErrorLabel();

        cbHinhThuc = new JComboBox<>(new String[]{"Giảm phần trăm", "Giảm giá"});

        txtGiaTri = createInput();
        lblErrGiaTri = createErrorLabel();

        txtNgayBD = createInput(); lblErrNgayBD = createErrorLabel();
        setPlaceholder(txtNgayBD, "  dd/MM/yyyy");
        txtNgayKT = createInput(); lblErrNgayKT = createErrorLabel();
        setPlaceholder(txtNgayKT, "  dd/MM/yyyy");

        cbDoiTuong = new JComboBox<>(DoiTuongKM.values());
        lblErrDoiTuong = createErrorLabel();

        main.add(createLabel("Mã khuyến mãi")); main.add(txtMa);

        JPanel pnlTen = new JPanel(new BorderLayout()); pnlTen.setBackground(BG);
        pnlTen.add(txtTen, BorderLayout.CENTER); pnlTen.add(lblErrTen, BorderLayout.SOUTH);
        main.add(createLabel("Tên chương trình")); main.add(pnlTen);

        main.add(createLabel("Hình thức")); main.add(cbHinhThuc);

        JPanel pnlGiaTri = new JPanel(new BorderLayout()); pnlGiaTri.setBackground(BG);
        pnlGiaTri.add(txtGiaTri, BorderLayout.CENTER); pnlGiaTri.add(lblErrGiaTri, BorderLayout.SOUTH);
        main.add(createLabel("Giá trị")); main.add(pnlGiaTri);

        JPanel pnlNgayBD = new JPanel(new BorderLayout()); pnlNgayBD.setBackground(BG);
        pnlNgayBD.add(txtNgayBD, BorderLayout.CENTER); pnlNgayBD.add(lblErrNgayBD, BorderLayout.SOUTH);
        main.add(createLabel("Ngày bắt đầu")); main.add(pnlNgayBD);

        JPanel pnlNgayKT = new JPanel(new BorderLayout()); pnlNgayKT.setBackground(BG);
        pnlNgayKT.add(txtNgayKT, BorderLayout.CENTER); pnlNgayKT.add(lblErrNgayKT, BorderLayout.SOUTH);
        main.add(createLabel("Ngày kết thúc")); main.add(pnlNgayKT);

        JPanel pnlDoiTuong = new JPanel(new BorderLayout()); pnlDoiTuong.setBackground(BG);
        pnlDoiTuong.add(cbDoiTuong, BorderLayout.CENTER); pnlDoiTuong.add(lblErrDoiTuong, BorderLayout.SOUTH);
        main.add(createLabel("Đối tượng áp dụng")); main.add(pnlDoiTuong);

        add(main, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10)); btnPanel.setBackground(BG);
        JButton btnCancel = new JButton("Hủy"); styleBtn(btnCancel, Color.GRAY);
        btnSave = new JButton("Lưu"); styleBtn(btnSave, PRIMARY); btnSave.setEnabled(false);
        btnPanel.add(btnCancel); btnPanel.add(btnSave);
        add(btnPanel, BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> saveKhuyenMai(txtMa.getText()));

        // --- CÀI ĐẶT SỰ KIỆN: Lắng nghe lúc gõ chữ (Chỉ kiểm tra ngầm để bật nút Lưu) ---
        SimpleListener dl = new SimpleListener(this::checkSaveButton);
        txtTen.getDocument().addDocumentListener(dl);
        txtGiaTri.getDocument().addDocumentListener(dl);
        txtNgayBD.getDocument().addDocumentListener(dl);
        txtNgayKT.getDocument().addDocumentListener(dl);
        
        cbHinhThuc.addActionListener(e -> { checkGiaTri(true); checkSaveButton(); });
        cbDoiTuong.addActionListener(e -> { checkDoiTuong(true); checkSaveButton(); });

        // --- CÀI ĐẶT SỰ KIỆN: Khi người dùng rời chuột khỏi ô mới báo lỗi đỏ ---
        txtTen.addFocusListener(new FocusAdapter() { public void focusLost(FocusEvent e) { checkTen(true); } });
        txtGiaTri.addFocusListener(new FocusAdapter() { public void focusLost(FocusEvent e) { checkGiaTri(true); } });
        txtNgayBD.addFocusListener(new FocusAdapter() { public void focusLost(FocusEvent e) { checkNgayBD(true); } });
        txtNgayKT.addFocusListener(new FocusAdapter() { public void focusLost(FocusEvent e) { checkNgayKT(true); } });

        // Gọi 1 lần để nút "Lưu" bị mờ lúc mới mở lên
        checkSaveButton();
    }

    private void checkSaveButton() {
        // Tham số false: Chỉ kiểm tra đúng sai, KHÔNG in ra chữ đỏ
        boolean vTen = checkTen(false);
        boolean vGiaTri = checkGiaTri(false);
        boolean vNgayBD = checkNgayBD(false);
        boolean vNgayKT = checkNgayKT(false);
        boolean vDoiTuong = checkDoiTuong(false);
        
        btnSave.setEnabled(vTen && vGiaTri && vNgayBD && vNgayKT && vDoiTuong);
    }

    private boolean checkTen(boolean showError) {
        if (txtTen.getText().trim().isEmpty()) {
            if (showError) lblErrTen.setText("Tên không được để trống!");
            return false;
        }
        lblErrTen.setText(" "); // Luôn xóa lỗi nếu đã nhập đúng
        return true;
    }

    private boolean checkGiaTri(boolean showError) {
        try {
            double val = Double.parseDouble(txtGiaTri.getText().trim());
            if (cbHinhThuc.getSelectedItem().toString().equals("Giảm phần trăm")) {
                if (val < 1 || val > 100) { 
                    if (showError) lblErrGiaTri.setText("Phải từ 1 đến 100!"); 
                    return false; 
                }
            } else {
                if (val <= 5000) { 
                    if (showError) lblErrGiaTri.setText("Phải lớn hơn 5000!"); 
                    return false; 
                }
            }
            lblErrGiaTri.setText(" "); // Xóa lỗi ngay lập tức khi đang gõ mà đúng
            return true;
        } catch (Exception e) { 
            if (showError) lblErrGiaTri.setText("Giá trị phải là số!"); 
            return false; 
        }
    }

    private boolean checkNgayBD(boolean showError) {
        String strNgayBD = txtNgayBD.getText().trim();
        if (strNgayBD.isEmpty() || strNgayBD.contains("dd/MM/yyyy")) {
            if (showError) lblErrNgayBD.setText("Vui lòng nhập ngày!");
            ngayBD = null;
            return false;
        }
        try {
            ngayBD = LocalDate.parse(strNgayBD, formatter);
            if (ngayBD.isBefore(LocalDate.now())) {
                if (showError) lblErrNgayBD.setText("Ngày bắt đầu phải từ hôm nay trở đi!");
                return false;
            }
            lblErrNgayBD.setText(" ");
            return true;
        } catch (DateTimeParseException e) {
            if (showError) lblErrNgayBD.setText("Ngày không hợp lệ!");
            ngayBD = null;
            return false;
        }
    }

    private boolean checkNgayKT(boolean showError) {
        String strNgayKT = txtNgayKT.getText().trim();
        if (strNgayKT.isEmpty() || strNgayKT.contains("dd/MM/yyyy")) {
            if (showError) lblErrNgayKT.setText("Vui lòng nhập ngày!");
            return false;
        }
        try {
            LocalDate ngayKT = LocalDate.parse(strNgayKT, formatter);
            if (ngayBD != null && ngayKT.isBefore(ngayBD)) {
                if (showError) lblErrNgayKT.setText("Ngày kết thúc phải >= ngày bắt đầu!");
                return false;
            }
            lblErrNgayKT.setText(" ");
            return true;
        } catch (DateTimeParseException e) {
            if (showError) lblErrNgayKT.setText("Ngày không hợp lệ!");
            return false;
        }
    }

    private boolean checkDoiTuong(boolean showError) {
        if (cbDoiTuong.getSelectedItem() == null) {
            if (showError) lblErrDoiTuong.setText("Phải chọn đối tượng!");
            return false;
        }
        lblErrDoiTuong.setText(" ");
        return true;
    }

    private void saveKhuyenMai(String maKM) {
        try {
            KhuyenMai km = new KhuyenMai();
            km.setMaKM(maKM);
            km.setTenKM(txtTen.getText().trim());
            km.setLoaiKM(cbHinhThuc.getSelectedItem().toString());
            km.setGiaTri(Double.parseDouble(txtGiaTri.getText().trim()));
            km.setNgayBatDau(LocalDate.parse(txtNgayBD.getText().trim(), formatter));
            km.setNgayKetThuc(LocalDate.parse(txtNgayKT.getText().trim(), formatter));
            km.setDoiTuongApDung((DoiTuongKM) cbDoiTuong.getSelectedItem());

            LocalDate today = LocalDate.now();
            if (today.isBefore(km.getNgayBatDau())) km.setTrangThai("Sắp diễn ra");
            else if (today.isAfter(km.getNgayKetThuc())) km.setTrangThai("Đã kết thúc");
            else km.setTrangThai("Đang chạy");

            new KhuyenMai_DAO().insert(km);
            JOptionPane.showMessageDialog(this, "Thêm thành công!");
            
            if (parentPanel != null) {
                parentPanel.loadData();
            }
            
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Có lỗi xảy ra: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JTextField createInput() {
        JTextField txt = new JTextField();
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txt.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        txt.setPreferredSize(new Dimension(200, 35));
        return txt;
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return lbl;
    }

    private void styleBtn(JButton btn, Color color) {
        btn.setBackground(color); btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false); btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(100, 35));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private JLabel createErrorLabel() {
        JLabel lbl = new JLabel(" ");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(RED);
        return lbl;
    }

    private void setPlaceholder(JTextField txt, String placeholder) {
        txt.setText(placeholder); txt.setForeground(Color.GRAY);
        txt.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (txt.getText().equals(placeholder)) { txt.setText(""); txt.setForeground(Color.BLACK); }
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (txt.getText().trim().isEmpty()) { txt.setText(placeholder); txt.setForeground(Color.GRAY); }
            }
        });
    }

    private static class SimpleListener implements DocumentListener {
        private final Runnable r;
        public SimpleListener(Runnable r){ this.r=r; }
        public void insertUpdate(DocumentEvent e){ r.run(); }
        public void removeUpdate(DocumentEvent e){ r.run(); }
        public void changedUpdate(DocumentEvent e){ r.run(); }
    }
}