package gui;

import dao.BanDAO;
import entity.Ban;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class DialogThemSuaBan extends JDialog {

    private Ban banHienTai; 
    private BanDAO banDAO;
    private boolean isThanhCong = false; 

    private JTextField txtMaBan;
    private JTextField txtTenBan;
    private JTextField txtSoGhe;
    private JComboBox<String> cbTrangThai;
    private JComboBox<String> cbViTri;

    private JLabel lblErrTenBan;
    private JLabel lblErrSoGhe;

    private JButton btnLuu;
    private JButton btnHuy;

    public DialogThemSuaBan(Frame parent, Ban ban) {
        super(parent, ban == null ? "Thêm Bàn Mới" : "Cập Nhật Thông Tin Bàn", true);
        this.banHienTai = ban; 
        this.banDAO = new BanDAO();

        initUI();
        
        loadData();
        setupListeners();
        
        validateRealTime(false); 
    }

    private void initUI() {
        setSize(450, 420);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBackground(Color.WHITE);
        pnlForm.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 2, 5);

        Font fontLabel = new Font("Segoe UI", Font.BOLD, 13);
        Font fontInput = new Font("Segoe UI", Font.PLAIN, 14);
        Font fontErr = new Font("Segoe UI", Font.ITALIC, 11);
        Color colorErr = new Color(220, 53, 69);

        // 1. Mã Bàn
        gbc.gridx = 0; gbc.gridy = 0;
        JLabel lblMaBan = new JLabel("Mã bàn:");
        lblMaBan.setFont(fontLabel);
        pnlForm.add(lblMaBan, gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        txtMaBan = new JTextField();
        txtMaBan.setFont(fontInput);
        txtMaBan.setEditable(false);
        txtMaBan.setBackground(new Color(240, 240, 240));
        pnlForm.add(txtMaBan, gbc);

        // 2. Tên Bàn
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        JLabel lblTenBan = new JLabel("Tên bàn (*):");
        lblTenBan.setFont(fontLabel);
        pnlForm.add(lblTenBan, gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        txtTenBan = new JTextField();
        txtTenBan.setFont(fontInput);
        pnlForm.add(txtTenBan, gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        lblErrTenBan = new JLabel(" ");
        lblErrTenBan.setFont(fontErr);
        lblErrTenBan.setForeground(colorErr);
        pnlForm.add(lblErrTenBan, gbc);

        // 3. Số Ghế
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        JLabel lblSoGhe = new JLabel("Số ghế (*):");
        lblSoGhe.setFont(fontLabel);
        pnlForm.add(lblSoGhe, gbc);

        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 1.0;
        txtSoGhe = new JTextField();
        txtSoGhe.setFont(fontInput);
        pnlForm.add(txtSoGhe, gbc);

        gbc.gridx = 1; gbc.gridy = 4;
        lblErrSoGhe = new JLabel(" ");
        lblErrSoGhe.setFont(fontErr);
        lblErrSoGhe.setForeground(colorErr);
        pnlForm.add(lblErrSoGhe, gbc);

        // 4. Trạng Thái - CHỈ HIỂN THỊ KHI ĐANG CẬP NHẬT BÀN (SỬA)
        JLabel lblTrangThai = new JLabel("Trạng thái:");
        lblTrangThai.setFont(fontLabel);
        String[] arrTrangThai = {"Trống", "Đang sử dụng", "Đã đặt trước"};
        cbTrangThai = new JComboBox<>(arrTrangThai);
        cbTrangThai.setFont(fontInput);
        cbTrangThai.setBackground(Color.WHITE);

        if (banHienTai != null) {
            gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0;
            pnlForm.add(lblTrangThai, gbc);

            gbc.gridx = 1; gbc.gridy = 5; gbc.weightx = 1.0;
            pnlForm.add(cbTrangThai, gbc);
        }

        // 5. Vị Trí
        gbc.gridx = 0; gbc.gridy = 7; gbc.weightx = 0;
        JLabel lblViTri = new JLabel("Vị trí:");
        lblViTri.setFont(fontLabel);
        pnlForm.add(lblViTri, gbc);

        gbc.gridx = 1; gbc.gridy = 7; gbc.weightx = 1.0;
        String[] arrViTri = {"Tầng 1", "Tầng 2", "Khu ngoài trời", "Phòng VIP"};
        cbViTri = new JComboBox<>(arrViTri);
        cbViTri.setFont(fontInput);
        cbViTri.setBackground(Color.WHITE);
        pnlForm.add(cbViTri, gbc);

        add(pnlForm, BorderLayout.CENTER);

        // PANEL NÚT BẤM
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        pnlButtons.setBackground(Color.WHITE);

        btnHuy = new JButton("Hủy bỏ");
        btnHuy.setFont(fontLabel);
        btnHuy.setCursor(new Cursor(Cursor.HAND_CURSOR));

        String textNutLuu = (banHienTai == null) ? "Thêm Bàn" : "Cập Nhật";
        btnLuu = new JButton(textNutLuu);
        
        btnLuu.setFont(fontLabel);
        btnLuu.setBackground(new Color(56, 118, 243)); 
        btnLuu.setForeground(Color.WHITE);
        btnLuu.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLuu.setEnabled(false); 

        pnlButtons.add(btnHuy);
        pnlButtons.add(btnLuu);
        add(pnlButtons, BorderLayout.SOUTH);
    }

    private void loadData() {
        if (banHienTai == null) {
            txtMaBan.setText(banDAO.phatSinhMaBanTuDong());
            txtSoGhe.setText("2"); 
        } else {
            txtMaBan.setText(banHienTai.getMaBan());
            txtTenBan.setText(banHienTai.getTenBan());
            txtSoGhe.setText(String.valueOf(banHienTai.getSoGhe()));
            cbTrangThai.setSelectedItem(banHienTai.getTrangThai());
            cbViTri.setSelectedItem(banHienTai.getViTri());
        }
    }

    private void setupListeners() {
        DocumentListener docListener = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { validateRealTime(true); }
            @Override public void removeUpdate(DocumentEvent e) { validateRealTime(true); }
            @Override public void changedUpdate(DocumentEvent e) { validateRealTime(true); }
        };

        txtTenBan.getDocument().addDocumentListener(docListener);
        txtSoGhe.getDocument().addDocumentListener(docListener);

        txtTenBan.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                validateRealTime(true);
            }
        });

        txtSoGhe.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                validateRealTime(true);
            }
        });

        btnHuy.addActionListener(e -> dispose());
        btnLuu.addActionListener(e -> luuDuLieu());
    }

    private void validateRealTime(boolean showError) {
        boolean hopLe = true;

        if (txtTenBan.getText().trim().isEmpty()) {
            if (showError) lblErrTenBan.setText("Tên bàn không được để rỗng");
            hopLe = false;
        } else {
            lblErrTenBan.setText(" "); 
        }

        String strSoGhe = txtSoGhe.getText().trim();
        if (strSoGhe.isEmpty()) {
            if (showError) lblErrSoGhe.setText("Vui lòng nhập số ghế");
            hopLe = false;
        } else {
            try {
                int soGhe = Integer.parseInt(strSoGhe);
                if (soGhe != 2 && soGhe != 4 && soGhe != 6 && soGhe != 8 && soGhe != 10) {
                    if (showError) lblErrSoGhe.setText("Số ghế chỉ được là 2, 4, 6, 8, hoặc 10");
                    hopLe = false;
                } else {
                    lblErrSoGhe.setText(" "); 
                }
            } catch (NumberFormatException ex) {
                if (showError) lblErrSoGhe.setText("Số ghế chỉ được nhập chữ số");
                hopLe = false;
            }
        }

        btnLuu.setEnabled(hopLe); 
    }

    private void luuDuLieu() {
        try {
            String maBan = txtMaBan.getText().trim();
            String tenBan = txtTenBan.getText().trim();
            int soGhe = Integer.parseInt(txtSoGhe.getText().trim());
            
            String trangThai = (banHienTai == null) ? "Trống" : cbTrangThai.getSelectedItem().toString();
            String viTri = cbViTri.getSelectedItem().toString();

            // ĐÃ CẬP NHẬT: Xử lý bảo toàn mã khối khi Thêm mới / Cập nhật
            Integer maKhoi = null;
            String maBanChinh = null;
            
            if (banHienTai != null) { // Nếu đang sửa bàn, lấy lại thông tin khối cũ để bảo toàn
                maKhoi = banHienTai.getMaKhoi();
                maBanChinh = banHienTai.getMaBanChinh();
            }

            Ban banMoi = new Ban(maBan, tenBan, soGhe, trangThai, viTri, maKhoi, maBanChinh);

            if (banHienTai == null) {
                if (banDAO.insertBan(banMoi)) {
                    isThanhCong = true;
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Lỗi khi thêm!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                if (banDAO.updateBan(banMoi)) {
                    isThanhCong = true;
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Dữ liệu không hợp lệ!", "Lỗi", JOptionPane.WARNING_MESSAGE);
        }
    }

    public boolean isThanhCong() {
        return isThanhCong;
    }
}