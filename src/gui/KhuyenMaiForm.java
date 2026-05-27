package gui;

import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.ZoneId;
import java.util.Date;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.toedter.calendar.JDateChooser;
import dao.KhuyenMai_DAO;
import dao.MonAn_DAO;
import entity.DoiTuongKM;
import entity.KhuyenMai;
import entity.MonAn;

public class KhuyenMaiForm extends JDialog {

    private static final long serialVersionUID = 1L;
    private final Color BG = new Color(255, 255, 255);
    private final Color PRIMARY = new Color(54, 92, 245);
    private final Color BORDER = new Color(220, 220, 220);
    private final Color RED = new Color(209, 5, 60);

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private JLabel lblErrTen, lblErrGiaTri, lblErrNgayBD, lblErrNgayKT, lblErrDoiTuong, lblErrMonAn;
    private JTextField txtTen, txtGiaTri;
    private JDateChooser dcNgayBD, dcNgayKT;
    private JComboBox<DoiTuongKM> cbDoiTuong;
    private JComboBox<String> cbHinhThuc;
    private JComboBox<String> cbMonAn; // ĐÃ THÊM: ComboBox chọn món ăn
    private JButton btnSave;
    private LocalDate ngayBD;
    
    private JLabel lblMonAnTitle; // Nãn tiêu đề của món ăn
    private JPanel pnlMonAn; // Panel chứa ô chọn món
    
    private JComboBox<String> cbHangKhach; // ComboBox chọn hạng
    private JLabel lblHangKhachTitle;
    private JPanel pnlHangKhach;
    
    private JPanel pnlDynamicLabel;
    private JPanel pnlDynamicInput;
    
    private KhuyenMaiPanel parentPanel;

    public KhuyenMaiForm(JFrame parent, KhuyenMaiPanel parentPanel) {
        super(parent, "Thêm khuyến mãi", true);
        this.parentPanel = parentPanel;
        
        setSize(500, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        
        // Gộp các tuỳ chọn mở rộng vào 1 dòng cuối cùng bằng CardLayout để tránh bị giãn cách trống
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

        dcNgayBD = createDateChooser(); lblErrNgayBD = createErrorLabel();
        dcNgayKT = createDateChooser(); lblErrNgayKT = createErrorLabel();

        cbDoiTuong = new JComboBox<>(DoiTuongKM.values());
        lblErrDoiTuong = createErrorLabel();

        // --- ĐÃ THÊM: KHỞI TẠO Ô CHỌN MÓN ĂN ---
        cbMonAn = new JComboBox<>();
        cbMonAn.addItem("-- Chọn món ăn --");
        try {
            List<MonAn> listMon = new MonAn_DAO().getAllMonAnWithActivePrice();
            for (MonAn m : listMon) {
                cbMonAn.addItem(m.getMaMon() + " - " + m.getTenMon());
            }
        } catch (Exception e) {}
        
        lblErrMonAn = createErrorLabel();
        pnlMonAn = new JPanel(new BorderLayout());
        pnlMonAn.setBackground(BG);
        pnlMonAn.add(cbMonAn, BorderLayout.CENTER);
        pnlMonAn.add(lblErrMonAn, BorderLayout.SOUTH);
        lblMonAnTitle = createLabel("Món ăn áp dụng");

        // ----------------------------------------
        
        // --- KHỞI TẠO Ô CHỌN HẠNG KHÁCH HÀNG ---
        cbHangKhach = new JComboBox<>(new String[]{"-- Chọn hạng --", "Kim Cương", "Vàng", "Bạc"});
        pnlHangKhach = new JPanel(new BorderLayout());
        pnlHangKhach.setBackground(BG);
        pnlHangKhach.add(cbHangKhach, BorderLayout.CENTER);
        lblHangKhachTitle = createLabel("Hạng áp dụng");
        // ----------------------------------------
        
        // --- KHỞI TẠO PANEL DYNAMIC ĐỂ CHỨA CÁC INPUT MỞ RỘNG (CARD LAYOUT) ---
        pnlDynamicLabel = new JPanel(new CardLayout());
        pnlDynamicLabel.setBackground(BG);
        pnlDynamicLabel.add(new JLabel(""), "EMPTY");
        pnlDynamicLabel.add(lblMonAnTitle, "MON");
        pnlDynamicLabel.add(lblHangKhachTitle, "HANG");
        
        pnlDynamicInput = new JPanel(new CardLayout());
        pnlDynamicInput.setBackground(BG);
        JPanel pnlEmpty = new JPanel(); pnlEmpty.setBackground(BG);
        pnlDynamicInput.add(pnlEmpty, "EMPTY");
        pnlDynamicInput.add(pnlMonAn, "MON");
        pnlDynamicInput.add(pnlHangKhach, "HANG");
        // ----------------------------------------

        main.add(createLabel("Mã khuyến mãi")); main.add(txtMa);

        JPanel pnlTen = new JPanel(new BorderLayout()); pnlTen.setBackground(BG);
        pnlTen.add(txtTen, BorderLayout.CENTER); pnlTen.add(lblErrTen, BorderLayout.SOUTH);
        main.add(createLabel("Tên chương trình")); main.add(pnlTen);

        main.add(createLabel("Hình thức")); main.add(cbHinhThuc);

        JPanel pnlGiaTri = new JPanel(new BorderLayout()); pnlGiaTri.setBackground(BG);
        pnlGiaTri.add(txtGiaTri, BorderLayout.CENTER); pnlGiaTri.add(lblErrGiaTri, BorderLayout.SOUTH);
        main.add(createLabel("Giá trị")); main.add(pnlGiaTri);

        JPanel pnlNgayBD = new JPanel(new BorderLayout()); pnlNgayBD.setBackground(BG);
        pnlNgayBD.add(dcNgayBD, BorderLayout.CENTER); pnlNgayBD.add(lblErrNgayBD, BorderLayout.SOUTH);
        main.add(createLabel("Ngày bắt đầu")); main.add(pnlNgayBD);

        JPanel pnlNgayKT = new JPanel(new BorderLayout()); pnlNgayKT.setBackground(BG);
        pnlNgayKT.add(dcNgayKT, BorderLayout.CENTER); pnlNgayKT.add(lblErrNgayKT, BorderLayout.SOUTH);
        main.add(createLabel("Ngày kết thúc")); main.add(pnlNgayKT);

        JPanel pnlDoiTuong = new JPanel(new BorderLayout()); pnlDoiTuong.setBackground(BG);
        pnlDoiTuong.add(cbDoiTuong, BorderLayout.CENTER); pnlDoiTuong.add(lblErrDoiTuong, BorderLayout.SOUTH);
        main.add(createLabel("Đối tượng áp dụng")); main.add(pnlDoiTuong);

        // Đưa Label và Input Động (CardLayout) vào hàng cuối cùng của Grid
        main.add(pnlDynamicLabel); main.add(pnlDynamicInput);

        add(main, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10)); btnPanel.setBackground(BG);
        JButton btnCancel = new JButton("Hủy"); styleBtn(btnCancel, Color.GRAY);
        btnSave = new JButton("Lưu"); styleBtn(btnSave, PRIMARY); btnSave.setEnabled(false);
        btnPanel.add(btnCancel); btnPanel.add(btnSave);
        add(btnPanel, BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> saveKhuyenMai(txtMa.getText()));

        // --- LẮNG NGHE SỰ KIỆN ---
        SimpleListener dl = new SimpleListener(this::checkSaveButton);
        txtTen.getDocument().addDocumentListener(dl);
        txtGiaTri.getDocument().addDocumentListener(dl);
        
        PropertyChangeListener dateListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("date".equals(evt.getPropertyName())) {
                    checkSaveButton();
                }
            }
        };
        dcNgayBD.addPropertyChangeListener(dateListener);
        dcNgayKT.addPropertyChangeListener(dateListener);
        
        cbHinhThuc.addActionListener(e -> { checkGiaTri(true); checkSaveButton(); });
        
        // BẮT SỰ KIỆN HIỂN THỊ ĐỘNG Ô CHỌN MÓN ĂN / CHỌN HẠNG
        cbDoiTuong.addActionListener(e -> { 
            boolean isKhachDungMon = cbDoiTuong.getSelectedItem() != null && 
                                     cbDoiTuong.getSelectedItem().toString().toLowerCase().contains("món");
            boolean isTheoHang = cbDoiTuong.getSelectedItem() != null && 
                                 cbDoiTuong.getSelectedItem().toString().toLowerCase().contains("hạng");
            
            CardLayout clLabel = (CardLayout) pnlDynamicLabel.getLayout();
            CardLayout clInput = (CardLayout) pnlDynamicInput.getLayout();
            
            if (isKhachDungMon) {
                clLabel.show(pnlDynamicLabel, "MON");
                clInput.show(pnlDynamicInput, "MON");
            } else if (isTheoHang) {
                clLabel.show(pnlDynamicLabel, "HANG");
                clInput.show(pnlDynamicInput, "HANG");
            } else {
                clLabel.show(pnlDynamicLabel, "EMPTY");
                clInput.show(pnlDynamicInput, "EMPTY");
            }
            
            checkDoiTuong(true); 
            checkSaveButton(); 
        });

        cbMonAn.addActionListener(e -> checkSaveButton());
        cbHangKhach.addActionListener(e -> checkSaveButton());

        txtTen.addFocusListener(new FocusAdapter() { public void focusLost(FocusEvent e) { checkTen(true); } });
        txtGiaTri.addFocusListener(new FocusAdapter() { public void focusLost(FocusEvent e) { checkGiaTri(true); } });

        checkSaveButton();
    }

    private void checkSaveButton() {
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
        lblErrTen.setText(" "); 
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
            lblErrGiaTri.setText(" "); 
            return true;
        } catch (Exception e) { 
            if (showError) lblErrGiaTri.setText("Giá trị phải là số!"); 
            return false; 
        }
    }

    private boolean checkNgayBD(boolean showError) {
        Date dateBD = dcNgayBD.getDate();
        if (dateBD == null) {
            if (showError) lblErrNgayBD.setText("Vui lòng chọn ngày!");
            ngayBD = null;
            return false;
        }
        
        ngayBD = dateBD.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        if (ngayBD.isBefore(LocalDate.now())) {
            if (showError) lblErrNgayBD.setText("Ngày bắt đầu phải từ hôm nay trở đi!");
            return false;
        }
        lblErrNgayBD.setText(" ");
        return true;
    }

    private boolean checkNgayKT(boolean showError) {
        Date dateKT = dcNgayKT.getDate();
        if (dateKT == null) {
            if (showError) lblErrNgayKT.setText("Vui lòng chọn ngày!");
            return false;
        }
        
        LocalDate ngayKT = dateKT.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        if (ngayBD != null && ngayKT.isBefore(ngayBD)) {
            if (showError) lblErrNgayKT.setText("Ngày kết thúc phải >= ngày bắt đầu!");
            return false;
        }
        lblErrNgayKT.setText(" ");
        return true;
    }

    private boolean checkDoiTuong(boolean showError) {
        if (cbDoiTuong.getSelectedItem() == null) {
            if (showError) lblErrDoiTuong.setText("Phải chọn đối tượng!");
            return false;
        }
        
        // Kiểm tra ràng buộc: Nếu là "Khách dùng món" thì BẮT BUỘC phải chọn Món ăn
        boolean isKhachDungMon = cbDoiTuong.getSelectedItem().toString().toLowerCase().contains("món");
        if (isKhachDungMon && cbMonAn.getSelectedIndex() == 0) {
            if (showError) lblErrMonAn.setText("Vui lòng chọn món ăn áp dụng!");
            lblErrDoiTuong.setText(" ");
            return false;
        }
        
        // Kiểm tra ràng buộc: Nếu là "Khách hàng theo hạng" thì BẮT BUỘC phải chọn Hạng
        boolean isTheoHang = cbDoiTuong.getSelectedItem().toString().toLowerCase().contains("hạng");
        if (isTheoHang && cbHangKhach.getSelectedIndex() == 0) {
            if (showError) lblErrDoiTuong.setText("Vui lòng chọn hạng áp dụng!");
            lblErrMonAn.setText(" ");
            return false;
        }

        lblErrDoiTuong.setText(" ");
        lblErrMonAn.setText(" ");
        return true;
    }

    private void saveKhuyenMai(String maKM) {
        try {
            KhuyenMai km = new KhuyenMai();
            km.setMaKM(maKM);
            
            // Xử lý thủ thuật: Nếu là Khuyến mãi theo món, tự động gắn tên Món vào tên Khuyến Mãi
            String tenKM = txtTen.getText().trim();
            boolean isKhachDungMon = cbDoiTuong.getSelectedItem() != null && 
                                     cbDoiTuong.getSelectedItem().toString().toLowerCase().contains("món");
            boolean isTheoHang = cbDoiTuong.getSelectedItem() != null && 
                                 cbDoiTuong.getSelectedItem().toString().toLowerCase().contains("hạng");
                                 
            if (isKhachDungMon && cbMonAn.getSelectedIndex() > 0) {
                // Cắt lấy tên món (Bỏ cái mã món ở đầu đi)
                String tenMon = cbMonAn.getSelectedItem().toString().split(" - ")[1];
                tenKM += " (" + tenMon + ")"; // VD: Sale 50% (Bánh mì)
            } else if (isTheoHang && cbHangKhach.getSelectedIndex() > 0) {
                String hang = cbHangKhach.getSelectedItem().toString();
                tenKM += " (Hạng " + hang + ")"; // VD: Sale 50% (Hạng Vàng)
            }
            km.setTenKM(tenKM);
            
            km.setLoaiKM(cbHinhThuc.getSelectedItem().toString());
            km.setGiaTri(Double.parseDouble(txtGiaTri.getText().trim()));
            km.setNgayBatDau(dcNgayBD.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            km.setNgayKetThuc(dcNgayKT.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
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
    
    private JDateChooser createDateChooser() {
        JDateChooser dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("dd/MM/yyyy");
        dateChooser.setPreferredSize(new Dimension(200, 35));
        dateChooser.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JTextField editor = (JTextField) dateChooser.getDateEditor().getUiComponent();
        editor.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        editor.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        editor.setBackground(Color.WHITE);
        
        JButton btn = dateChooser.getCalendarButton();
        btn.setIcon(null);
        btn.setText("📅"); 
        btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        btn.setBackground(Color.WHITE);
        btn.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        
        dateChooser.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        return dateChooser;
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