package gui;

import dao.MonAn_DAO;
import entity.MonAn;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

public class SuaMonAnDialog extends JDialog {

    private JTextField txtMaMon, txtTenMon, txtDonViTinh;
    private JComboBox<String> cbLoaiMon, cbTrangThai;
    private JLabel lblImagePreview;
    private String selectedImagePath = ""; 
    private JButton btnCapNhat, btnHuy, btnChonAnh;
    private ThucDonPanel parentPanel;
    
    // Đối tượng Entity lưu thông tin món đang sửa
    private MonAn currentFood; 
    private MonAn_DAO monAn_dao = new MonAn_DAO();

    private final Color BLUE_ACCENT = new Color(54, 92, 245);

    public SuaMonAnDialog(Frame owner, ThucDonPanel parentPanel, MonAn food) {
        super(owner, "Cập Nhật Món Ăn", true);
        this.parentPanel = parentPanel;
        this.currentFood = food;
        initComponents();
        loadDataToForm(); 
        setupValidation(); 
    }

    private void initComponents() {
        setSize(550, 450);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());

        JPanel pnlContent = new JPanel(new GridBagLayout());
        pnlContent.setBorder(new EmptyBorder(20, 20, 20, 20));
        pnlContent.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtMaMon = new JTextField();
        txtMaMon.setEditable(false); 
        txtMaMon.setBackground(new Color(240, 240, 240));

        txtTenMon = new JTextField();
        txtDonViTinh = new JTextField();
        cbLoaiMon = new JComboBox<>(new String[]{"Khai vị", "Món chính", "Món nước", "Tráng miệng"});
        cbTrangThai = new JComboBox<>(new String[]{"Đang bán", "Tạm ngưng"});

        JPanel pnlImage = new JPanel(new BorderLayout(10, 10));
        pnlImage.setOpaque(false);
        lblImagePreview = new JLabel("Chưa có ảnh", SwingConstants.CENTER);
        lblImagePreview.setPreferredSize(new Dimension(100, 100));
        lblImagePreview.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        btnChonAnh = new JButton("Chọn Ảnh...");
        btnChonAnh.addActionListener(e -> chonAnh());

        pnlImage.add(lblImagePreview, BorderLayout.CENTER);
        pnlImage.add(btnChonAnh, BorderLayout.SOUTH);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3; pnlContent.add(new JLabel("Mã món ăn:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7; pnlContent.add(txtMaMon, gbc);
        gbc.gridx = 0; gbc.gridy = 1; pnlContent.add(new JLabel("Tên món:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7; pnlContent.add(txtTenMon, gbc);
        gbc.gridx = 0; gbc.gridy = 2; pnlContent.add(new JLabel("Đơn vị tính:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7; pnlContent.add(txtDonViTinh, gbc);
        gbc.gridx = 0; gbc.gridy = 3; pnlContent.add(new JLabel("Phân loại:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7; pnlContent.add(cbLoaiMon, gbc);
        gbc.gridx = 0; gbc.gridy = 4; pnlContent.add(new JLabel("Trạng thái:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7; pnlContent.add(cbTrangThai, gbc);
        gbc.gridx = 2; gbc.gridy = 0; gbc.gridheight = 4; gbc.weightx = 0; pnlContent.add(pnlImage, gbc);

        add(pnlContent, BorderLayout.CENTER);

        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        btnCapNhat = new JButton("Cập Nhật");
        btnCapNhat.setBackground(BLUE_ACCENT);
        btnCapNhat.setForeground(Color.WHITE);
        btnHuy = new JButton("Hủy");

        btnHuy.addActionListener(e -> dispose());
        btnCapNhat.addActionListener(e -> capNhatDatabase());

        pnlButtons.add(btnCapNhat);
        pnlButtons.add(btnHuy);
        add(pnlButtons, BorderLayout.SOUTH);
    }

    // --- BƯỚC 1: ĐỔ DỮ LIỆU AN TOÀN CHỐNG NULL ---
    private void loadDataToForm() {
        txtMaMon.setText(currentFood.getMaMon());
        txtTenMon.setText(currentFood.getTenMon());
        
        // Bọc an toàn cho Đơn vị tính
        txtDonViTinh.setText(currentFood.getDonViTinh() != null ? currentFood.getDonViTinh() : "");
        
        // Bọc an toàn cho Phân loại
        if (currentFood.getLoaiMon() != null && !currentFood.getLoaiMon().isEmpty()) {
            cbLoaiMon.setSelectedItem(currentFood.getLoaiMon());
        } else {
            cbLoaiMon.setSelectedIndex(0); // Chọn mặc định nếu bị null
        }

        // Bọc an toàn cho Trạng thái
        if (currentFood.getTrangThai() != null && !currentFood.getTrangThai().isEmpty()) {
            cbTrangThai.setSelectedItem(currentFood.getTrangThai());
        } else {
            cbTrangThai.setSelectedIndex(0); // Chọn mặc định nếu bị null
        }
        
        selectedImagePath = currentFood.getHinhAnh();
        if (selectedImagePath != null && !selectedImagePath.trim().isEmpty()) {
            File f = new File(selectedImagePath);
            if (f.exists()) {
                ImageIcon icon = new ImageIcon(new ImageIcon(selectedImagePath).getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH));
                lblImagePreview.setIcon(icon);
                lblImagePreview.setText("");
            } else {
                lblImagePreview.setText("Ảnh không tồn tại");
            }
        }
    }

    private void setupValidation() {
        DocumentListener docListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { checkFields(); }
            public void removeUpdate(DocumentEvent e) { checkFields(); }
            public void changedUpdate(DocumentEvent e) { checkFields(); }
        };
        txtTenMon.getDocument().addDocumentListener(docListener);
        txtDonViTinh.getDocument().addDocumentListener(docListener);
        checkFields();
    }

    private void checkFields() {
        boolean isReady = !txtTenMon.getText().trim().isEmpty()
                && !txtDonViTinh.getText().trim().isEmpty()
                && selectedImagePath != null && !selectedImagePath.trim().isEmpty();
        btnCapNhat.setEnabled(isReady);
    }

    private void chonAnh() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Hình ảnh (JPG, PNG)", "jpg", "png", "jpeg"));
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedImagePath = fileChooser.getSelectedFile().getAbsolutePath(); 
            ImageIcon icon = new ImageIcon(new ImageIcon(selectedImagePath).getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH));
            lblImagePreview.setIcon(icon);
            lblImagePreview.setText("");
            checkFields(); 
        }
    }

    // --- BƯỚC 2: CẬP NHẬT AN TOÀN CHỐNG NULL ---
    private void capNhatDatabase() {
        try {
            currentFood.setTenMon(txtTenMon.getText().trim());
            currentFood.setDonViTinh(txtDonViTinh.getText().trim());
            
            // Lấy dữ liệu từ ComboBox một cách an toàn
            Object trangThaiObj = cbTrangThai.getSelectedItem();
            currentFood.setTrangThai(trangThaiObj != null ? trangThaiObj.toString() : "Đang bán");
            
            Object loaiMonObj = cbLoaiMon.getSelectedItem();
            currentFood.setLoaiMon(loaiMonObj != null ? loaiMonObj.toString() : "Khác");
            
            currentFood.setHinhAnh(selectedImagePath);

            if (monAn_dao.updateMonAn(currentFood)) {
                JOptionPane.showMessageDialog(this, "Cập nhật món ăn thành công!");
                if (parentPanel != null) {
                    parentPanel.loadDataFromDatabase(); 
                }
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Cập nhật thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi hệ thống: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}