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
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class ThemGiaBanDialog extends JDialog {

    // --- PHẦN HEADER (BẢNG GIÁ) ---
    private JTextField txtMaBangGia, txtMoTa;
    private JDateChooser dateBatDau, dateKetThuc;
    private JComboBox<String> cbTrangThai;

    // --- PHẦN DETAIL (CHI TIẾT MÓN ĂN & GIÁ) ---
    private JTable tableChiTiet;
    private DefaultTableModel modelChiTiet;

    private JButton btnLuu, btnHuy;
    private QuanLyGiaBanPanel parentPanel;
    
    private MonAn_DAO monAn_dao = new MonAn_DAO();
    private GiaBan_DAO giaBan_dao = new GiaBan_DAO();
    
    private final Color BTN_BLUE = new Color(54, 92, 245);
    private final Color HEADER_BG = new Color(255, 245, 205); // Màu vàng nhạt giống hình của thầy

    public ThemGiaBanDialog(Frame owner, QuanLyGiaBanPanel parentPanel) {
        super(owner, "Thiết Lập Bảng Giá Mới", true);
        this.parentPanel = parentPanel;
        initComponents();
        loadDanhSachMonAnVaoBang(); 
        
        // Tự động phát sinh mã BG mới nhất
        txtMaBangGia.setText(giaBan_dao.tuDongPhatSinhMa());
    }

    private void initComponents() {
        setSize(800, 600); // Mở rộng chiều ngang để chứa bảng Table
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout(0, 10));
        ((JPanel)getContentPane()).setBorder(new EmptyBorder(15, 15, 15, 15));

        // ==========================================
        // 1. PANEL HEADER (Thông tin chung của Bảng giá)
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
        txtMoTa = new JTextField();
        dateBatDau = new JDateChooser();
        dateBatDau.setDateFormatString("dd/MM/yyyy");
        dateBatDau.setDate(new Date()); // Mặc định hôm nay
        
        dateKetThuc = new JDateChooser();
        dateKetThuc.setDateFormatString("dd/MM/yyyy");
        
        cbTrangThai = new JComboBox<>(new String[]{"Đang áp dụng", "Tạm ngưng"});

        // Cột 1: Mã BG và Mô tả
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.1;
        pnlHeader.add(new JLabel("Mã bảng giá:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.4;
        pnlHeader.add(txtMaBangGia, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.1;
        pnlHeader.add(new JLabel("Mô tả (Tên BG):"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.4;
        pnlHeader.add(txtMoTa, gbc);

        // Cột 2: Ngày và Trạng thái
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
        // 2. PANEL DETAIL (Bảng thiết lập giá cho từng món)
        // ==========================================
        JPanel pnlDetail = new JPanel(new BorderLayout());
        pnlDetail.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY), "Chi tiết giá bán (Detail) - Ghi giá vào ô tương ứng", TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 14)
        ));

        // Bảng gồm 3 cột. Cột 2 (Giá bán) cho phép Edit
        String[] cols = {"Mã Sản Phẩm", "Tên Sản Phẩm", "Giá Bán Thiết Lập (VNĐ)"};
        modelChiTiet = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2; // CHỈ CHO PHÉP SỬA CỘT GIÁ BÁN
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

    /**
     * Load toàn bộ danh sách món ăn vào bảng. Cột giá để trống.
     */
    private void loadDanhSachMonAnVaoBang() {
        // Cần viết thêm 1 hàm getAllMonAn() cơ bản trong MonAn_DAO nếu chưa có
        // Tạm dùng hàm search với chuỗi rỗng để lấy hết
        List<MonAn> dsMon = monAn_dao.searchMonAn(""); 
        for (MonAn m : dsMon) {
            // Mặc định ô giá để trống (null hoặc chuỗi rỗng)
            modelChiTiet.addRow(new Object[]{m.getMaMon(), m.getTenMon(), ""});
        }
    }

    private void luuVaoDatabase() {
        // Dừng việc Edit cell trên Table nếu người dùng đang gõ dở giá tiền
        if (tableChiTiet.isEditing()) {
            tableChiTiet.getCellEditor().stopCellEditing();
        }

        String maBG = txtMaBangGia.getText().trim();
        String moTa = txtMoTa.getText().trim();
        Date dBD = dateBatDau.getDate();
        Date dKT = dateKetThuc.getDate();
        String trangThai = cbTrangThai.getSelectedItem().toString();
        
        // 1. Validate Header
        if (maBG.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Mã bảng giá không được rỗng!"); return;
        }
        if (dBD == null || (dKT != null && dKT.before(dBD))) {
            JOptionPane.showMessageDialog(this, "Lỗi ngày bắt đầu / kết thúc!"); return;
        }
        if (giaBan_dao.kiemTraTrungMa(maBG)) {
            JOptionPane.showMessageDialog(this, "Mã bảng giá đã tồn tại!"); return;
        }

        // 2. Thu thập dữ liệu từ Detail (JTable)
        Connection con = null;
        try {
            con = SQLConnection.getConnection();
            con.setAutoCommit(false); // Dùng Transaction

            // Bước A: Lưu Header (Bảng giá)
            String sqlBG = "INSERT INTO BangGia (maBangGia, moTa, ngayBatDau, ngayKetThuc, trangThai) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pst1 = con.prepareStatement(sqlBG);
            pst1.setString(1, maBG);
            pst1.setString(2, moTa);
            pst1.setDate(3, new java.sql.Date(dBD.getTime()));
            pst1.setDate(4, dKT != null ? new java.sql.Date(dKT.getTime()) : null);
            pst1.setString(5, trangThai);
            pst1.executeUpdate();

            // Bước B: Lưu Detail (Chi tiết bảng giá)
            boolean hasAtLeastOnePrice = false;
            String sqlCT = "INSERT INTO ChiTietBangGia (maBangGia, maMon, giaBan) VALUES (?, ?, ?)";
            PreparedStatement pst2 = con.prepareStatement(sqlCT);

            // Quét từng dòng trong bảng JTable
            for (int i = 0; i < tableChiTiet.getRowCount(); i++) {
                String maMon = tableChiTiet.getValueAt(i, 0).toString();
                Object giaObj = tableChiTiet.getValueAt(i, 2);
                
                String giaStr = (giaObj != null) ? giaObj.toString().trim() : "";
                
                // CHỈ THÊM VÀO CHI TIẾT NẾU NGƯỜI DÙNG CÓ NHẬP GIÁ (> 0)
                if (!giaStr.isEmpty()) {
                    try {
                        double giaBan = Double.parseDouble(giaStr);
                        if (giaBan > 0) {
                            pst2.setString(1, maBG);
                            pst2.setString(2, maMon);
                            pst2.setDouble(3, giaBan);
                            pst2.addBatch();
                            hasAtLeastOnePrice = true;
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "Lỗi: Giá của món " + maMon + " không hợp lệ!");
                        con.rollback();
                        return;
                    }
                }
            }

            if (!hasAtLeastOnePrice) {
                JOptionPane.showMessageDialog(this, "Bạn phải thiết lập giá cho ít nhất một món ăn!");
                con.rollback();
                return;
            }

            // Chạy Batch Insert chi tiết
            pst2.executeBatch();
            con.commit();
            
            JOptionPane.showMessageDialog(this, "Thiết lập bảng giá thành công!");
            if (parentPanel != null) parentPanel.loadDataToTable();
            this.dispose();

        } catch (Exception ex) {
            try { if (con != null) con.rollback(); } catch (SQLException e) {}
            JOptionPane.showMessageDialog(this, "Lỗi khi lưu DB: " + ex.getMessage());
        } finally {
            try { if (con != null) con.close(); } catch (SQLException e) {}
        }
    }
}