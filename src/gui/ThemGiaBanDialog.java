package gui;

import com.toedter.calendar.JDateChooser;
import dao.GiaBan_DAO;
import dao.MonAn_DAO;
import entity.MonAn;
import connectDB.SQLConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThemGiaBanDialog extends JDialog {

    // --- PHẦN HEADER (BẢNG GIÁ) ---
    private JTextField txtMaBangGia, txtMoTa;
    private JDateChooser dateBatDau, dateKetThuc;
    private JComboBox<String> cbTrangThai;

    // --- PHẦN DETAIL (CHI TIẾT MÓN ĂN & GIÁ) ---
    private JTable tableChiTiet;
    private DefaultTableModel modelChiTiet;
    private JTextField txtTimKiem; 

    private JButton btnLuu, btnHuy;
    private QuanLyGiaBanPanel parentPanel;
    
    private MonAn_DAO monAn_dao = new MonAn_DAO();
    private GiaBan_DAO giaBan_dao = new GiaBan_DAO();
    
    private final Color BTN_BLUE = new Color(54, 92, 245);
    private final Color HEADER_BG = new Color(255, 245, 205); 
    
    private Map<String, String> giaDaNhap = new HashMap<>();
    private List<MonAn> tatCaMonAn = new ArrayList<>(); 

    public ThemGiaBanDialog(Frame owner, QuanLyGiaBanPanel parentPanel) {
        super(owner, "Thiết Lập Bảng Giá Mới", true);
        this.parentPanel = parentPanel;
        
        layDanhSachGoc();
        
        initComponents();
        loadDanhSachMonAnVaoBang(""); 
        
        txtMaBangGia.setText(giaBan_dao.tuDongPhatSinhMa());
    }

    private void layDanhSachGoc() {
        List<MonAn> dsMonRaw = monAn_dao.searchMonAn(""); 
        for (MonAn m : dsMonRaw) {
            String trangThai = m.getTrangThai();
            if (trangThai != null && trangThai.equalsIgnoreCase("Tạm ngưng")) {
                continue; 
            }
            tatCaMonAn.add(m);
        }
    }

    private void initComponents() {
        setSize(800, 650); 
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout(0, 10));
        ((JPanel)getContentPane()).setBorder(new EmptyBorder(15, 15, 15, 15));

        // ==========================================
        // 1. PANEL HEADER
        // ==========================================
        JPanel pnlHeader = new JPanel(new GridBagLayout());
        pnlHeader.setBackground(HEADER_BG);
        pnlHeader.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY), "Thông tin Bảng Giá (Header)", TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 14)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtMaBangGia = new JTextField();
        txtMaBangGia.setEditable(false); 
        txtMoTa = new JTextField();
        dateBatDau = new JDateChooser();
        dateBatDau.setDateFormatString("dd/MM/yyyy");
        dateBatDau.setDate(new Date()); 
        
        dateKetThuc = new JDateChooser();
        dateKetThuc.setDateFormatString("dd/MM/yyyy");
        
        cbTrangThai = new JComboBox<>(new String[]{"Đang áp dụng", "Tạm ngưng"});

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.1;
        pnlHeader.add(new JLabel("Mã bảng giá:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.4;
        pnlHeader.add(txtMaBangGia, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.1;
        pnlHeader.add(new JLabel("Mô tả (Tên BG):"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.4;
        pnlHeader.add(txtMoTa, gbc);

        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0.1;
        pnlHeader.add(new JLabel("Ngày bắt đầu:"), gbc);
        gbc.gridx = 3; gbc.weightx = 0.4;
        pnlHeader.add(dateBatDau, gbc);

        gbc.gridx = 2; gbc.gridy = 1; gbc.weightx = 0.1;
        pnlHeader.add(new JLabel("Ngày kết thúc:"), gbc);
        gbc.gridx = 3; gbc.weightx = 0.4;
        pnlHeader.add(dateKetThuc, gbc);

        gbc.gridx = 2; gbc.gridy = 2; gbc.weightx = 0.1;
        pnlHeader.add(new JLabel("Trạng thái:"), gbc);
        gbc.gridx = 3; gbc.weightx = 0.4;
        pnlHeader.add(cbTrangThai, gbc);

        add(pnlHeader, BorderLayout.NORTH);

        // ==========================================
        // 2. PANEL DETAIL 
        // ==========================================
        JPanel pnlDetail = new JPanel(new BorderLayout(0, 10));
        pnlDetail.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY), "Chi tiết giá bán (Detail)", TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 14)
        ));

        // --- THANH TÌM KIẾM MỚI ---
        JPanel pnlTimKiem = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblSearch = new JLabel("Tìm kiếm món ăn: ");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 13));
        txtTimKiem = new JTextField(25);
        txtTimKiem.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtTimKiem.putClientProperty("JTextField.placeholderText", "Nhập tên món...");
        
        pnlTimKiem.add(lblSearch);
        pnlTimKiem.add(txtTimKiem);
        pnlDetail.add(pnlTimKiem, BorderLayout.NORTH);

        // --- SỰ KIỆN GÕ TÌM KIẾM ---
        txtTimKiem.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { locTimKiem(); }
            @Override public void removeUpdate(DocumentEvent e) { locTimKiem(); }
            @Override public void changedUpdate(DocumentEvent e) { locTimKiem(); }
        });

        // Bảng
        String[] cols = {"Mã Sản Phẩm", "Tên Sản Phẩm", "Giá Bán Thiết Lập (VNĐ)"};
        modelChiTiet = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2; 
            }
        };
        tableChiTiet = new JTable(modelChiTiet);
        tableChiTiet.setRowHeight(30);
        tableChiTiet.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tableChiTiet.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

        JScrollPane scrollTable = new JScrollPane(tableChiTiet);
        pnlDetail.add(scrollTable, BorderLayout.CENTER);

        add(pnlDetail, BorderLayout.CENTER);

        // ==========================================
        // 3. PANEL BUTTONS
        // ==========================================
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        btnLuu = new JButton("Lưu Bảng Giá");
        btnLuu.setBackground(BTN_BLUE);
        btnLuu.setForeground(Color.WHITE);
        btnLuu.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLuu.setPreferredSize(new Dimension(150, 40));
        
        btnHuy = new JButton("Hủy");
        btnHuy.setPreferredSize(new Dimension(100, 40));

        btnHuy.addActionListener(e -> dispose());
        btnLuu.addActionListener(e -> luuVaoDatabase());

        pnlButtons.add(btnLuu);
        pnlButtons.add(btnHuy);
        add(pnlButtons, BorderLayout.SOUTH);
    }

    private void luuTamGiaDangNhap() {
        if (tableChiTiet.isEditing()) {
            tableChiTiet.getCellEditor().stopCellEditing();
        }
        for (int i = 0; i < tableChiTiet.getRowCount(); i++) {
            String maMon = tableChiTiet.getValueAt(i, 0).toString();
            Object giaObj = tableChiTiet.getValueAt(i, 2);
            if (giaObj != null && !giaObj.toString().trim().isEmpty()) {
                giaDaNhap.put(maMon, giaObj.toString().trim());
            }
        }
    }

    private void locTimKiem() {
        luuTamGiaDangNhap(); 
        String keyword = txtTimKiem.getText().trim().toLowerCase();
        loadDanhSachMonAnVaoBang(keyword);
    }

    private void loadDanhSachMonAnVaoBang(String keyword) {
        modelChiTiet.setRowCount(0); 
        for (MonAn m : tatCaMonAn) {
            if (m.getTenMon().toLowerCase().contains(keyword)) {
                String giaCu = giaDaNhap.getOrDefault(m.getMaMon(), "");
                modelChiTiet.addRow(new Object[]{m.getMaMon(), m.getTenMon(), giaCu});
            }
        }
    }

    private void luuVaoDatabase() {
        luuTamGiaDangNhap(); 

        String maBG = txtMaBangGia.getText().trim();
        String moTa = txtMoTa.getText().trim();
        Date dBD = dateBatDau.getDate();
        Date dKT = dateKetThuc.getDate();
        String trangThai = cbTrangThai.getSelectedItem().toString();
        
        // --- ĐÃ NÂNG CẤP BỘ LỌC ĐIỀU KIỆN ---
        if (maBG.isEmpty()) { 
            JOptionPane.showMessageDialog(this, "Mã bảng giá không được rỗng!"); 
            return; 
        }
        if (dBD == null) { 
            JOptionPane.showMessageDialog(this, "Vui lòng chọn Ngày bắt đầu!"); 
            return; 
        }
        if (dKT == null) { 
            JOptionPane.showMessageDialog(this, "Vui lòng chọn Ngày kết thúc để tránh lỗi áp dụng giá vĩnh viễn!"); 
            return; 
        }
        if (dKT.before(dBD)) { 
            JOptionPane.showMessageDialog(this, "Lỗi: Ngày kết thúc không được nhỏ hơn Ngày bắt đầu!"); 
            return; 
        }
        if (giaBan_dao.kiemTraTrungMa(maBG)) { 
            JOptionPane.showMessageDialog(this, "Mã bảng giá đã tồn tại!"); 
            return; 
        }

        List<String> dsMaMonDaNhapGia = new ArrayList<>();
        Map<String, Double> dsGiaMoi = new HashMap<>();

        for (Map.Entry<String, String> entry : giaDaNhap.entrySet()) {
            String maMon = entry.getKey();
            String giaStr = entry.getValue();
            
            try {
                double giaBan = Double.parseDouble(giaStr);
                if (giaBan > 0) {
                    dsMaMonDaNhapGia.add(maMon);
                    dsGiaMoi.put(maMon, giaBan);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Lỗi: Giá nhập không hợp lệ ở một số món!");
                return; 
            }
        }

        if (dsMaMonDaNhapGia.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Bạn phải thiết lập giá cho ít nhất một món ăn!");
            return;
        }

        if (trangThai.equals("Đang áp dụng")) {
            String overlapMsg = giaBan_dao.checkMonDaCoGia(dsMaMonDaNhapGia, maBG, dBD, dKT);
            if (overlapMsg != null) {
                JOptionPane.showMessageDialog(this, overlapMsg + "\nVui lòng điều chỉnh lại ngày hoặc tạm ngưng bảng giá cũ!", 
                                              "Cảnh báo trùng lấp giá", JOptionPane.WARNING_MESSAGE);
                return; 
            }
        }

        Connection con = null;
        try {
            con = SQLConnection.getConnection();
            con.setAutoCommit(false); 

            String sqlBG = "INSERT INTO BangGia (maBangGia, moTa, ngayBatDau, ngayKetThuc, trangThai) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pst1 = con.prepareStatement(sqlBG);
            pst1.setString(1, maBG);
            pst1.setString(2, moTa);
            pst1.setDate(3, new java.sql.Date(dBD.getTime()));
            pst1.setDate(4, new java.sql.Date(dKT.getTime()));
            pst1.setString(5, trangThai);
            pst1.executeUpdate();

            String sqlCT = "INSERT INTO ChiTietBangGia (maBangGia, maMon, giaBan) VALUES (?, ?, ?)";
            PreparedStatement pst2 = con.prepareStatement(sqlCT);

            for (String maMon : dsMaMonDaNhapGia) {
                pst2.setString(1, maBG);
                pst2.setString(2, maMon);
                pst2.setDouble(3, dsGiaMoi.get(maMon));
                pst2.addBatch();
            }

            pst2.executeBatch();
            con.commit();
            
            JOptionPane.showMessageDialog(this, "Thiết lập bảng giá thành công!");
            if (parentPanel != null) {
                parentPanel.loadDataToTable();
                
                GUIDashBoard dashBoard = (GUIDashBoard) SwingUtilities.getWindowAncestor(this.parentPanel);
                if(dashBoard != null && dashBoard.getPnlThucDon() != null) {
                    dashBoard.getPnlThucDon().loadDataFromDatabase();
                }
            }
            this.dispose();

        } catch (Exception ex) {
            try { if (con != null) con.rollback(); } catch (SQLException e) {}
            JOptionPane.showMessageDialog(this, "Lỗi khi lưu DB: " + ex.getMessage());
        } finally {
            try { if (con != null) con.close(); } catch (SQLException e) {}
        }
    }
}