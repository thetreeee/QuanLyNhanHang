package gui;

import com.toedter.calendar.JDateChooser;
import dao.GiaBan_DAO;
import dao.MonAn_DAO;
import entity.MonAn;
import connectDB.SQLConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SuaGiaBanDialog extends JDialog {

    // --- PHẦN HEADER (BẢNG GIÁ) ---
    private JTextField txtMaBangGia, txtMoTa;
    private JDateChooser dateBatDau, dateKetThuc;
    private JComboBox<String> cbTrangThai;

    // --- PHẦN DETAIL (CHI TIẾT MÓN ĂN & GIÁ) ---
    private JTable tableChiTiet;
    private DefaultTableModel modelChiTiet;
    
    // --- THÊM MỚI: CÔNG CỤ THÊM MÓN VÀO BẢNG GIÁ ---
    private JComboBox<String> cbMonAnMoi;
    private JButton btnThemMonMoi;

    private JButton btnLuu, btnHuy;
    private QuanLyGiaBanPanel parentPanel;
    private ThucDonPanel pnlThucDon;
    private String maBGToEdit;
    
    private MonAn_DAO monAn_dao = new MonAn_DAO();
    private GiaBan_DAO giaBan_dao = new GiaBan_DAO();
    
    private final Color BTN_BLUE = new Color(54, 92, 245);
    private final Color HEADER_BG = new Color(255, 245, 205);
    private final Color BTN_YELLOW = new Color(255, 209, 102);

    public SuaGiaBanDialog(Frame owner, QuanLyGiaBanPanel parentPanel, String maBangGia, ThucDonPanel pnlThucDon) {
        super(owner, "Cập Nhật Bảng Giá", true);
        this.parentPanel = parentPanel;
        this.pnlThucDon = pnlThucDon;
        this.maBGToEdit = maBangGia;
        
        initComponents();
        loadDataToForm(); 
    }

    private void initComponents() {
        setSize(850, 650);
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

        txtMaBangGia = new JTextField(maBGToEdit);
        txtMaBangGia.setEditable(false);
        txtMaBangGia.setBackground(new Color(240, 240, 240));
        
        txtMoTa = new JTextField();
        dateBatDau = new JDateChooser();
        dateBatDau.setDateFormatString("dd/MM/yyyy");
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
        // 2. PANEL DETAIL (Lưới nhập giá & Thêm món)
        // ==========================================
        JPanel pnlDetail = new JPanel(new BorderLayout(0, 10));
        pnlDetail.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY), "Chi tiết giá bán", TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 14)
        ));

        // --- THANH CÔNG CỤ THÊM MÓN VÀO BẢNG GIÁ ---
        JPanel pnlAddMon = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlAddMon.add(new JLabel("Bổ sung món vào bảng giá: "));
        cbMonAnMoi = new JComboBox<>();
        cbMonAnMoi.setPreferredSize(new Dimension(250, 32));
        pnlAddMon.add(cbMonAnMoi);
        
        btnThemMonMoi = new JButton("+ Thêm món này");
        btnThemMonMoi.setBackground(BTN_YELLOW);
        btnThemMonMoi.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnThemMonMoi.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnThemMonMoi.addActionListener(e -> themMonVaoBang());
        pnlAddMon.add(btnThemMonMoi);
        
        pnlDetail.add(pnlAddMon, BorderLayout.NORTH);

        // --- BẢNG DỮ LIỆU ---
        String[] cols = {"Mã Sản Phẩm", "Tên Sản Phẩm", "Giá Bán Thiết Lập (VNĐ)"};
        modelChiTiet = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2; // Chỉ cho gõ vào cột giá
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
        btnLuu = new JButton("Cập Nhật Bảng Giá");
        btnLuu.setBackground(BTN_BLUE);
        btnLuu.setForeground(Color.WHITE);
        btnLuu.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLuu.setPreferredSize(new Dimension(170, 40));
        
        btnHuy = new JButton("Hủy");
        btnHuy.setPreferredSize(new Dimension(100, 40));

        btnHuy.addActionListener(e -> dispose());
        btnLuu.addActionListener(e -> capNhatDatabase());

        pnlButtons.add(btnLuu);
        pnlButtons.add(btnHuy);
        add(pnlButtons, BorderLayout.SOUTH);
    }

    private void loadDataToForm() {
        try (Connection con = SQLConnection.getConnection()) {
            // 1. Tải thông tin Header
            String sqlBG = "SELECT * FROM BangGia WHERE maBangGia = ?";
            PreparedStatement pst1 = con.prepareStatement(sqlBG);
            pst1.setString(1, maBGToEdit);
            ResultSet rs1 = pst1.executeQuery();
            if (rs1.next()) {
                txtMoTa.setText(rs1.getString("moTa"));
                dateBatDau.setDate(rs1.getDate("ngayBatDau"));
                dateKetThuc.setDate(rs1.getDate("ngayKetThuc"));
                cbTrangThai.setSelectedItem(rs1.getString("trangThai"));
            }

            // 2. Tải giá của các món ăn trong bảng giá này vào một Map
            Map<String, String> giaMap = new HashMap<>();
            String sqlCT = "SELECT maMon, giaBan FROM ChiTietBangGia WHERE maBangGia = ?";
            PreparedStatement pst2 = con.prepareStatement(sqlCT);
            pst2.setString(1, maBGToEdit);
            ResultSet rs2 = pst2.executeQuery();
            while (rs2.next()) {
                giaMap.put(rs2.getString("maMon"), String.format("%.0f", rs2.getDouble("giaBan")));
            }

            // 3. Phân loại món ăn
            cbMonAnMoi.removeAllItems();
            List<MonAn> tatCaMon = monAn_dao.searchMonAn(""); 
            
            for (MonAn m : tatCaMon) {
                if (giaMap.containsKey(m.getMaMon())) {
                    // Món đã có giá -> Hiện lên Bảng
                    String giaDaSet = giaMap.get(m.getMaMon());
                    modelChiTiet.addRow(new Object[]{m.getMaMon(), m.getTenMon(), giaDaSet});
                } else {
                    // Món chưa có giá -> Đưa vào danh sách ComboBox chờ thêm
                    cbMonAnMoi.addItem(m.getMaMon() + " - " + m.getTenMon());
                }
            }

        } catch (Exception e) { e.printStackTrace(); }
    }

    // --- XỬ LÝ SỰ KIỆN THÊM MÓN TỪ COMBOBOX XUỐNG BẢNG ---
    private void themMonVaoBang() {
        if (cbMonAnMoi.getSelectedItem() != null) {
            String selected = cbMonAnMoi.getSelectedItem().toString();
            String[] parts = selected.split(" - ", 2);
            if (parts.length == 2) {
                String maMon = parts[0];
                String tenMon = parts[1];
                
                // Thêm vào bảng (Giá để trống)
                modelChiTiet.addRow(new Object[]{maMon, tenMon, ""});
                
                // Xóa khỏi danh sách chọn để tránh trùng
                cbMonAnMoi.removeItem(selected);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Không còn món ăn nào để thêm!");
        }
    }

    private void capNhatDatabase() {
        if (tableChiTiet.isEditing()) {
            tableChiTiet.getCellEditor().stopCellEditing();
        }

        String moTa = txtMoTa.getText().trim();
        Date dBD = dateBatDau.getDate();
        Date dKT = dateKetThuc.getDate();
        String trangThai = cbTrangThai.getSelectedItem().toString();
        
        if (dBD == null || (dKT != null && dKT.before(dBD))) {
            JOptionPane.showMessageDialog(this, "Lỗi ngày bắt đầu / kết thúc!"); return;
        }

        // Lấy danh sách các món CÓ NHẬP GIÁ để lưu
        List<String> dsMaMonDaNhapGia = new ArrayList<>();
        Map<String, Double> dsGiaMoi = new HashMap<>();

        for (int i = 0; i < tableChiTiet.getRowCount(); i++) {
            String maMon = tableChiTiet.getValueAt(i, 0).toString();
            Object giaObj = tableChiTiet.getValueAt(i, 2);
            String giaStr = (giaObj != null) ? giaObj.toString().trim() : "";
            
            // Chỉ lưu những món mà người dùng có gõ giá > 0
            if (!giaStr.isEmpty()) {
                try {
                    double giaBan = Double.parseDouble(giaStr);
                    if (giaBan > 0) {
                        dsMaMonDaNhapGia.add(maMon);
                        dsGiaMoi.put(maMon, giaBan);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Lỗi: Giá của món " + maMon + " không hợp lệ!");
                    return;
                }
            }
        }

        if (dsMaMonDaNhapGia.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Bạn phải thiết lập giá cho ít nhất một món ăn!");
            return;
        }

        // Kiểm tra Ràng buộc chống trùng lấp thời gian (Overlap)
        if (trangThai.equals("Đang áp dụng")) {
            String overlapMsg = giaBan_dao.checkMonDaCoGia(dsMaMonDaNhapGia, maBGToEdit, dBD, dKT);
            if (overlapMsg != null) {
                JOptionPane.showMessageDialog(this, overlapMsg + "\nVui lòng điều chỉnh lại ngày hoặc tạm ngưng bảng giá cũ!", 
                                              "Cảnh báo trùng lấp giá", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        // Bắt đầu cập nhật Database (Sử dụng Transaction)
        Connection con = null;
        try {
            con = SQLConnection.getConnection();
            con.setAutoCommit(false);

            // 1. Cập nhật Header
            String sqlBG = "UPDATE BangGia SET moTa=?, ngayBatDau=?, ngayKetThuc=?, trangThai=? WHERE maBangGia=?";
            PreparedStatement pst1 = con.prepareStatement(sqlBG);
            pst1.setString(1, moTa);
            pst1.setDate(2, new java.sql.Date(dBD.getTime()));
            pst1.setDate(3, dKT != null ? new java.sql.Date(dKT.getTime()) : null);
            pst1.setString(4, trangThai);
            pst1.setString(5, maBGToEdit);
            pst1.executeUpdate();

            // 2. Xóa toàn bộ giá cũ của bảng này
            String sqlDel = "DELETE FROM ChiTietBangGia WHERE maBangGia=?";
            PreparedStatement pstDel = con.prepareStatement(sqlDel);
            pstDel.setString(1, maBGToEdit);
            pstDel.executeUpdate();

            // 3. Chèn lại các giá mới
            String sqlIns = "INSERT INTO ChiTietBangGia (maBangGia, maMon, giaBan) VALUES (?, ?, ?)";
            PreparedStatement pstIns = con.prepareStatement(sqlIns);
            for (String maMon : dsMaMonDaNhapGia) {
                pstIns.setString(1, maBGToEdit);
                pstIns.setString(2, maMon);
                pstIns.setDouble(3, dsGiaMoi.get(maMon));
                pstIns.addBatch();
            }
            pstIns.executeBatch();

            con.commit();
            JOptionPane.showMessageDialog(this, "Cập nhật bảng giá thành công!");
            
            if (parentPanel != null) parentPanel.loadDataToTable();
            if (pnlThucDon != null) pnlThucDon.loadDataFromDatabase();
            
            this.dispose();

        } catch (Exception ex) {
            try { if (con != null) con.rollback(); } catch (SQLException e) {}
            JOptionPane.showMessageDialog(this, "Lỗi DB: " + ex.getMessage());
        } finally {
            try { if (con != null) con.close(); } catch (SQLException e) {}
        }
    }
}