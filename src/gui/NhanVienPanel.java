package gui;

import dao.NhanVien_Dao;
import entity.NhanVien;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class NhanVienPanel extends JPanel {
    private final Color BG_WHITE = Color.WHITE;
    private final Color TIME_BANNER_BG = new Color(255, 246, 246);
    private final Color TABLE_HEADER_BG = new Color(255, 235, 235);
    private final Color TABLE_HEADER_TEXT = new Color(180, 50, 60);
    private final Color TEXT_DARK = new Color(44, 56, 74);
    
    private DefaultTableModel model;
    private JTable table;
    private NhanVien_Dao nv_dao = new NhanVien_Dao();
    private JTextField txtSearch;

    public NhanVienPanel() {
        setLayout(new BorderLayout(0, 20));
        setBackground(BG_WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        initUI();
        loadDataFromDatabase();
    }

    private void loadDataFromDatabase() {
        model.setRowCount(0);
        List<NhanVien> list = nv_dao.getAllNhanVien();
        for (NhanVien nv : list) {
            model.addRow(new Object[]{nv.getMaNV(), nv.getHoTen(), nv.getGmail(), nv.getMatKhau(), nv.getGioiTinh(), nv.getChucVu(), "X"});
        }
    }

    private void initUI() {
        JPanel topWrapper = new JPanel();
        topWrapper.setLayout(new BoxLayout(topWrapper, BoxLayout.Y_AXIS));
        topWrapper.setOpaque(false);

        // --- 1. BANNER THỜI GIAN ---
        JPanel timeBanner = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        timeBanner.setBackground(TIME_BANNER_BG);
        timeBanner.putClientProperty("FlatLaf.style", "arc: 15");

        JLabel lblTimeDate = new JLabel();
        lblTimeDate.setFont(new Font("Segoe UI", Font.PLAIN, 14)); 
        lblTimeDate.setForeground(Color.BLACK);
        
        Locale localeVI = new Locale("vi", "VN");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("EEEE, 'ngày' dd/MM/yyyy - HH:mm:ss", localeVI);
        new Timer(1000, e -> lblTimeDate.setText(LocalDateTime.now().format(dtf))).start();
        
        timeBanner.add(lblTimeDate);
        topWrapper.add(timeBanner);
        topWrapper.add(Box.createRigidArea(new Dimension(0, 20)));

        // --- 2. TIÊU ĐỀ & THANH HÀNH ĐỘNG ---
        JPanel titleActionPanel = new JPanel(new BorderLayout());
        titleActionPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("QUẢN LÝ NHÂN VIÊN");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(TEXT_DARK);
        titleActionPanel.add(lblTitle, BorderLayout.WEST);

        JPanel btnActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        btnActions.setOpaque(false);

        txtSearch = new JTextField(15);
        txtSearch.setPreferredSize(new Dimension(250, 40));
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.putClientProperty("JTextField.placeholderText", "🔍 Tìm tên nhân viên...");
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { xuLyTimKiem(); }
            public void removeUpdate(DocumentEvent e) { xuLyTimKiem(); }
            public void changedUpdate(DocumentEvent e) { xuLyTimKiem(); }
        });

        JButton btnAdd = new JButton("+ Thêm nhân viên");
        btnAdd.setBackground(new Color(255, 209, 102)); 
        btnAdd.setForeground(Color.BLACK);
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAdd.setPreferredSize(new Dimension(170, 42));
        btnAdd.setFocusPainted(false);
        btnAdd.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAdd.putClientProperty("JButton.buttonType", "roundRect");
        btnAdd.putClientProperty("JButton.arc", 15);
        
        btnAdd.addActionListener(e -> {
            DialogThemNV dialog = new DialogThemNV((Frame) SwingUtilities.getWindowAncestor(this), null);
            dialog.setVisible(true);
            if (dialog.getNewEmployee() != null && nv_dao.insertNhanVien(dialog.getNewEmployee())) {
                loadDataFromDatabase();
            }
        });

        btnActions.add(txtSearch);
        btnActions.add(btnAdd);
        titleActionPanel.add(btnActions, BorderLayout.EAST);

        topWrapper.add(titleActionPanel);
        add(topWrapper, BorderLayout.NORTH);

        // --- 3. CENTER (BẢNG DANH SÁCH) ---
        String[] columns = {"Mã nhân viên", "Tên nhân viên", "Gmail", "Mật khẩu", "Giới tính", "Chức vụ", "Xóa"};
        model = new DefaultTableModel(null, columns) { 
            @Override public boolean isCellEditable(int r, int c) { return c == 6; } 
        };
        
        table = new JTable(model); 
        table.setRowHeight(50); 
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(240, 240, 240));
        table.setSelectionBackground(new Color(232, 240, 254));
        table.setSelectionForeground(Color.BLACK);
        table.setFocusable(false);

        table.getColumnModel().getColumn(6).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(6).setCellEditor(new ButtonEditor(new JCheckBox()));
        table.getColumnModel().getColumn(6).setMaxWidth(60);

        JTableHeader h = table.getTableHeader();
        h.setPreferredSize(new Dimension(0, 45));
        h.setFont(new Font("Segoe UI", Font.BOLD, 14));
        h.setBackground(TABLE_HEADER_BG);
        h.setForeground(TABLE_HEADER_TEXT);

        DefaultTableCellRenderer cr = new DefaultTableCellRenderer();
        cr.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount() - 1; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(cr);
        }
        
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int r = table.getSelectedRow();
                    if (r != -1) {
                        try {
                            String ma = model.getValueAt(r, 0).toString();
                            String ten = model.getValueAt(r, 1).toString();
                            String gmail = model.getValueAt(r, 2).toString();
                            String mk = model.getValueAt(r, 3).toString();
                            String gioi = model.getValueAt(r, 4).toString();
                            String chuc = model.getValueAt(r, 5).toString();

                            NhanVien nv = new NhanVien(ma, ten, gmail, chuc, 5000000.0, mk, gioi);
                            DialogThemNV d = new DialogThemNV((Frame) SwingUtilities.getWindowAncestor(NhanVienPanel.this), nv);
                            d.setVisible(true);
                            if (d.getNewEmployee() != null && nv_dao.updateNhanVien(d.getNewEmployee())) {
                                loadDataFromDatabase();
                            }
                        } catch (Exception ex) { ex.printStackTrace(); }
                    }
                }
            }
        });

        JScrollPane sp = new JScrollPane(table); 
        sp.getViewport().setBackground(Color.WHITE);
        sp.setBorder(BorderFactory.createEmptyBorder());
        
        add(sp, BorderLayout.CENTER);
    }

    private void xuLyTimKiem() {
        String k = txtSearch.getText().trim().toLowerCase();
        model.setRowCount(0);
        for (NhanVien nv : nv_dao.getAllNhanVien()) {
            if (nv.getHoTen().toLowerCase().contains(k)) 
                model.addRow(new Object[]{nv.getMaNV(), nv.getHoTen(), nv.getGmail(), nv.getMatKhau(), nv.getGioiTinh(), nv.getChucVu(), "X"});
        }
    }

    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setText("X"); setForeground(Color.RED);
            setFont(new Font("Segoe UI", Font.BOLD, 16));
            setContentAreaFilled(false); setBorderPainted(false);
        }
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private boolean isPushed;
        private int selectedRow;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton("X");
            button.setOpaque(true); button.setForeground(Color.RED);
            button.setFont(new Font("Segoe UI", Font.BOLD, 16));
            button.addActionListener(e -> fireEditingStopped());
        }
        @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            selectedRow = row; isPushed = true; return button;
        }
        @Override public Object getCellEditorValue() {
            if (isPushed) {
                String maNV = model.getValueAt(selectedRow, 0).toString();
                if (JOptionPane.showConfirmDialog(button, "Xác nhận xóa nhân viên " + maNV + "?", "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    if (nv_dao.deleteNhanVien(maNV)) {
                        loadDataFromDatabase();
                        JOptionPane.showMessageDialog(button, "Đã xóa thành công!");
                    }
                }
            }
            isPushed = false; return "X";
        }
        @Override public boolean stopCellEditing() { isPushed = false; return super.stopCellEditing(); }
    }

    // =======================================================================
    // --- LỚP DIALOG THÊM/SỬA NHÂN VIÊN ĐÃ CHỈNH SỬA GIAO DIỆN NÚT MỚI
    // =======================================================================
    class DialogThemNV extends JDialog {
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
            txtMa = new JTextField(); pnlForm.add(txtMa);
            
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

            // Đổ dữ liệu nếu là sửa
            if (oldNv != null) {
                txtMa.setText(oldNv.getMaNV());
                txtMa.setEditable(false);
                txtTen.setText(oldNv.getHoTen());
                txtGmail.setText(oldNv.getGmail());
                txtPass.setText(oldNv.getMatKhau());
                cbGioiTinh.setSelectedItem(oldNv.getGioiTinh());
                cbChucVu.setSelectedItem(oldNv.getChucVu());
            }

            // --- 3. BẮT SỰ KIỆN LỖI ---
            SimpleListener dl = new SimpleListener(this::checkSaveButton);
            txtMa.getDocument().addDocumentListener(dl);
            txtTen.getDocument().addDocumentListener(dl);
            txtGmail.getDocument().addDocumentListener(dl);
            txtPass.getDocument().addDocumentListener(dl);

            txtMa.addFocusListener(new FocusAdapter() { public void focusLost(FocusEvent e) { validData(true); } });
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
            boolean hasData = !txtMa.getText().trim().isEmpty() && 
                              !txtTen.getText().trim().isEmpty() && 
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
            String ma = txtMa.getText().trim();
            String ten = txtTen.getText().trim();
            String gmail = txtGmail.getText().trim();
            String pass = txtPass.getText().trim();

            if (ma.isEmpty() || ten.isEmpty() || gmail.isEmpty() || pass.isEmpty()) {
                return false;
            }

            if (!ma.matches("^NV\\d{1,3}$")) {
                if (showError) lblError.setText("Mã sai cú pháp 'NVXXX' (Tối đa 3 chữ số)");
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
}