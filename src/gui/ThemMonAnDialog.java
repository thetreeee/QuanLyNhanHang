package gui;

import dao.MonAn_DAO;
import entity.MonAn;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.util.List;

public class ThemMonAnDialog extends JDialog {

    private JTextField txtMaMon, txtTenMon, txtDonViTinh;
    private JComboBox<String> cbLoaiMon, cbTrangThai;
    private JLabel lblImagePreview;
    private String selectedImagePath = ""; 
    private JButton btnThem, btnHuy, btnChonAnh;
    private ThucDonPanel parentPanel;
    
    private MonAn_DAO monAn_dao = new MonAn_DAO();
    private final Color BLUE_ACCENT = new Color(54, 92, 245);

    public ThemMonAnDialog(Frame owner, ThucDonPanel parentPanel) {
        super(owner, "Thêm Món Ăn Mới", true);
        this.parentPanel = parentPanel;
        initComponents();
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
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- CỘT 1: MÃ MÓN ĂN ---
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        pnlContent.add(new JLabel("Mã món ăn:"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.7;
        txtMaMon = new JTextField(generateNewMaMon()); // Gọi hàm sinh mã tự động
        txtMaMon.setEditable(false); // Khóa ô nhập liệu
        txtMaMon.setBackground(new Color(240, 240, 240)); // Tô nền xám nhạt
        txtMaMon.setFont(new Font("Segoe UI", Font.BOLD, 14));
        pnlContent.add(txtMaMon, gbc);

        // --- CÁC TRƯỜNG TIẾP THEO ---
        gbc.gridx = 0; gbc.gridy = 1;
        pnlContent.add(new JLabel("Tên món:"), gbc);
        gbc.gridx = 1;
        txtTenMon = new JTextField();
        pnlContent.add(txtTenMon, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        pnlContent.add(new JLabel("Đơn vị tính:"), gbc);
        gbc.gridx = 1;
        txtDonViTinh = new JTextField("Phần");
        pnlContent.add(txtDonViTinh, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        pnlContent.add(new JLabel("Phân loại:"), gbc);
        gbc.gridx = 1;
        cbLoaiMon = new JComboBox<>(new String[]{"Khai vị", "Món chính", "Món nước", "Tráng miệng"});
        pnlContent.add(cbLoaiMon, gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        pnlContent.add(new JLabel("Trạng thái:"), gbc);
        gbc.gridx = 1;
        cbTrangThai = new JComboBox<>(new String[]{"Đang bán", "Tạm ngưng"});
        pnlContent.add(cbTrangThai, gbc);

        // --- KHU VỰC HÌNH ẢNH (Bên phải) ---
        JPanel pnlImage = new JPanel(new BorderLayout(10, 10));
        pnlImage.setOpaque(false);
        lblImagePreview = new JLabel("Chưa có ảnh", SwingConstants.CENTER);
        lblImagePreview.setPreferredSize(new Dimension(120, 120));
        lblImagePreview.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        btnChonAnh = new JButton("Chọn Ảnh...");
        btnChonAnh.addActionListener(e -> chonAnh());
        pnlImage.add(lblImagePreview, BorderLayout.CENTER);
        pnlImage.add(btnChonAnh, BorderLayout.SOUTH);

        gbc.gridx = 2; gbc.gridy = 0; gbc.gridheight = 5; gbc.weightx = 0;
        gbc.insets = new Insets(5, 20, 5, 10);
        pnlContent.add(pnlImage, gbc);

        add(pnlContent, BorderLayout.CENTER);

        // --- NÚT CHỨC NĂNG ---
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        btnThem = new JButton("Lưu Món Ăn");
        btnThem.setBackground(BLUE_ACCENT);
        btnThem.setForeground(Color.WHITE);
        btnThem.setEnabled(false); 
        
        btnHuy = new JButton("Hủy");
        btnHuy.addActionListener(e -> dispose());
        btnThem.addActionListener(e -> luuVaoDatabase());

        pnlButtons.add(btnThem);
        pnlButtons.add(btnHuy);
        add(pnlButtons, BorderLayout.SOUTH);
    }

    // --- HÀM TỰ ĐỘNG SINH MÃ MÓN ĂN ---
    private String generateNewMaMon() {
        int maxId = 0;
        try {
            // Lấy toàn bộ món ăn bằng cách truyền chuỗi rỗng
            List<MonAn> dsMonAn = monAn_dao.searchMonAn(""); 
            for (MonAn mon : dsMonAn) {
                String ma = mon.getMaMon();
                if (ma != null && ma.startsWith("M")) {
                    try {
                        // Cắt bỏ chữ "M" và chuyển phần số thành Integer
                        int currentId = Integer.parseInt(ma.substring(1));
                        if (currentId > maxId) {
                            maxId = currentId;
                        }
                    } catch (NumberFormatException e) {
                        // Bỏ qua các mã không đúng định dạng M + số
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // Tăng mã lớn nhất lên 1, định dạng 3 chữ số (VD: M001, M002)
        return String.format("M%03d", maxId + 1);
    }

    private void setupValidation() {
        DocumentListener docListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { checkFields(); }
            public void removeUpdate(DocumentEvent e) { checkFields(); }
            public void changedUpdate(DocumentEvent e) { checkFields(); }
        };
        // Bỏ theo dõi txtMaMon vì nó tự sinh
        txtTenMon.getDocument().addDocumentListener(docListener);
        txtDonViTinh.getDocument().addDocumentListener(docListener);
        checkFields();
    }

    private void checkFields() {
        String tenMon = txtTenMon.getText().trim();
        String donVi = txtDonViTinh.getText().trim();
        
        // Điều kiện: Tên món, đơn vị tính không được trống và phải có chọn ảnh
        boolean isReady = !tenMon.isEmpty()
                && !donVi.isEmpty()
                && !selectedImagePath.trim().isEmpty();
        
        btnThem.setEnabled(isReady);
    }

    private void chonAnh() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Hình ảnh (JPG, PNG)", "jpg", "png", "jpeg"));
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedImagePath = fileChooser.getSelectedFile().getAbsolutePath(); 
            ImageIcon icon = new ImageIcon(new ImageIcon(selectedImagePath).getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH));
            lblImagePreview.setIcon(icon);
            lblImagePreview.setText("");
            checkFields(); 
        }
    }

    private void luuVaoDatabase() {
        try {
            MonAn mon = new MonAn();
            mon.setMaMon(txtMaMon.getText().trim());
            mon.setTenMon(txtTenMon.getText().trim());
            mon.setDonViTinh(txtDonViTinh.getText().trim());
            mon.setTrangThai(cbTrangThai.getSelectedItem().toString());
            mon.setLoaiMon(cbLoaiMon.getSelectedItem().toString());
            mon.setHinhAnh(selectedImagePath);

            if (monAn_dao.insertMonAn(mon)) {
                JOptionPane.showMessageDialog(this, "Đã thêm món ăn thành công!");
                if (parentPanel != null) parentPanel.loadDataFromDatabase(); 
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi lưu dữ liệu!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}