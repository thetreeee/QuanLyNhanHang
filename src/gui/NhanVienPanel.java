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
    
    // Biến để theo dõi trạng thái đang hiển thị
    private boolean isShowingNghiViec = false;
    private JButton btnToggleView;

    public NhanVienPanel() {
        setLayout(new BorderLayout(0, 20));
        setBackground(BG_WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        initUI();
        loadDataFromDatabase();
    }

    /**
     * Load dữ liệu có lọc theo trạng thái
     */
    private void loadDataFromDatabase() {
        model.setRowCount(0);
        List<NhanVien> list = nv_dao.getAllNhanVien();
        
        String filterStatus = isShowingNghiViec ? "Nghỉ việc" : "Đang làm";
        
        for (NhanVien nv : list) {
            String trangThai = nv.getTrangThai();
            if (trangThai == null || trangThai.trim().isEmpty()) {
                trangThai = "Đang làm";
            }
            
            // Chỉ thêm vào bảng nếu khớp với bộ lọc hiện tại
            if (trangThai.equalsIgnoreCase(filterStatus)) {
                model.addRow(new Object[]{
                    nv.getMaNV(), 
                    nv.getHoTen(), 
                    nv.getGmail(), 
                    nv.getMatKhau(), 
                    nv.getGioiTinh(), 
                    nv.getChucVu(), 
                    trangThai
                });
            }
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
        txtSearch.setPreferredSize(new Dimension(200, 40));
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm tên...");
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { xuLyTimKiem(); }
            public void removeUpdate(DocumentEvent e) { xuLyTimKiem(); }
            public void changedUpdate(DocumentEvent e) { xuLyTimKiem(); }
        });

        // NÚT CHUYỂN ĐỔI CHẾ ĐỘ XEM
        btnToggleView = new JButton("Xem NV nghỉ việc");
        styleSecondaryBtn(btnToggleView);
        btnToggleView.addActionListener(e -> {
            isShowingNghiViec = !isShowingNghiViec;
            if (isShowingNghiViec) {
                btnToggleView.setText("Quay lại DS đang làm");
                btnToggleView.setBackground(new Color(200, 200, 200));
            } else {
                btnToggleView.setText("Xem NV nghỉ việc");
                btnToggleView.setBackground(BG_WHITE);
            }
            loadDataFromDatabase();
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
            DialogThemNV dialog = new DialogThemNV((Frame) SwingUtilities.getWindowAncestor(this), null, "Đang làm");
            dialog.setVisible(true);
            if (dialog.getNewEmployee() != null && nv_dao.insertNhanVien(dialog.getNewEmployee())) {
                loadDataFromDatabase();
            }
        });

        btnActions.add(txtSearch);
        btnActions.add(btnToggleView);
        btnActions.add(btnAdd);
        titleActionPanel.add(btnActions, BorderLayout.EAST);

        topWrapper.add(titleActionPanel);
        add(topWrapper, BorderLayout.NORTH);

        // --- 3. CENTER (BẢNG DANH SÁCH) ---
        String[] columns = {"Mã nhân viên", "Tên nhân viên", "Gmail", "Mật khẩu", "Giới tính", "Chức vụ", "Trạng thái"};
        model = new DefaultTableModel(null, columns) { 
            @Override public boolean isCellEditable(int r, int c) { return false; } 
        };
        
        table = new JTable(model); 
        table.setRowHeight(50); 
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(240, 240, 240));
        table.setSelectionBackground(new Color(232, 240, 254));
        table.setSelectionForeground(Color.BLACK);
        table.setFocusable(false);

        JTableHeader h = table.getTableHeader();
        h.setPreferredSize(new Dimension(0, 45));
        h.setFont(new Font("Segoe UI", Font.BOLD, 14));
        h.setBackground(TABLE_HEADER_BG);
        h.setForeground(TABLE_HEADER_TEXT);

        DefaultTableCellRenderer cr = new DefaultTableCellRenderer();
        cr.setHorizontalAlignment(JLabel.CENTER);
        
        for (int i = 0; i < table.getColumnCount(); i++) {
            if (i == 6) { 
                table.getColumnModel().getColumn(i).setCellRenderer(new DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                        setHorizontalAlignment(JLabel.CENTER);
                        if (value != null) {
                            if (value.toString().equalsIgnoreCase("Đang làm")) {
                                setForeground(new Color(0, 153, 0)); 
                            } else {
                                setForeground(Color.RED); 
                            }
                            setFont(getFont().deriveFont(Font.BOLD));
                        }
                        return c;
                    }
                });
            } else {
                table.getColumnModel().getColumn(i).setCellRenderer(cr);
            }
        }
        
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int r = table.getSelectedRow();
                    if (r != -1) {
                        try {
                            // Lấy chức vụ trước để kiểm tra
                            String chuc = model.getValueAt(r, 5).toString();
                            
                            // CHỐT CHẶN: Nếu là Quản lý thì không cho phép sửa
                            if (chuc.equalsIgnoreCase("Quản lý") || chuc.equalsIgnoreCase("QUANLY")) {
                                JOptionPane.showMessageDialog(NhanVienPanel.this, 
                                    "Không thể chỉnh sửa thông tin của Quản lý", 
                                    "Từ chối quyền truy cập", 
                                    JOptionPane.WARNING_MESSAGE);
                                return; // Lập tức dừng lại, không mở form nữa
                            }

                            // Nếu không phải quản lý thì lấy các dữ liệu còn lại và mở form bình thường
                            String ma = model.getValueAt(r, 0).toString();
                            String ten = model.getValueAt(r, 1).toString();
                            String gmail = model.getValueAt(r, 2).toString();
                            String mk = model.getValueAt(r, 3).toString();
                            String gioi = model.getValueAt(r, 4).toString();
                            String trangThai = model.getValueAt(r, 6).toString();

                            NhanVien nv = new NhanVien(ma, ten, gmail, chuc, 5000000.0, mk, gioi, trangThai);
                            
                            DialogThemNV d = new DialogThemNV((Frame) SwingUtilities.getWindowAncestor(NhanVienPanel.this), nv, trangThai);
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

    private void styleSecondaryBtn(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(180, 42));
        btn.setBackground(BG_WHITE);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.putClientProperty("JButton.buttonType", "roundRect");
        btn.putClientProperty("JButton.arc", 15);
    }

    private void xuLyTimKiem() {
        String k = txtSearch.getText().trim().toLowerCase();
        model.setRowCount(0);
        String filterStatus = isShowingNghiViec ? "Nghỉ việc" : "Đang làm";
        
        for (NhanVien nv : nv_dao.getAllNhanVien()) {
            String trangThai = nv.getTrangThai() != null ? nv.getTrangThai() : "Đang làm";
            
            if (trangThai.equalsIgnoreCase(filterStatus) && nv.getHoTen().toLowerCase().contains(k)) {
                model.addRow(new Object[]{
                    nv.getMaNV(), 
                    nv.getHoTen(), 
                    nv.getGmail(), 
                    nv.getMatKhau(), 
                    nv.getGioiTinh(), 
                    nv.getChucVu(), 
                    trangThai
                });
            }
        }
    }
}