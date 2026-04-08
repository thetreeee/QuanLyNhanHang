package gui;

import dao.ChiTietDatMon_DAO;
import dao.DonDatMon_DAO;
import dao.MonAn_DAO;
import entity.ChiTietDatMon;
import entity.DonDatMon;
import entity.MonAn;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ChiTietDonDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private String maDon;
    private JTable table;
    private DefaultTableModel model;
    private DonDatMon don;

    private JComboBox<String> cbMonAnMoi;
    private JButton btnThemMonMoi, btnCapNhat, btnDong, btnXoaMon;
    
    private ChiTietDatMon_DAO ctDAO = new ChiTietDatMon_DAO();
    private MonAn_DAO monAnDAO = new MonAn_DAO();
    
    private JTextField txtNote;
    
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
    DecimalFormat df = new DecimalFormat("#,###");

    private final Color HEADER_BG = new Color(244, 182, 169); 
    private final Color BTN_BLUE = new Color(54, 92, 245);
    private final Color BTN_YELLOW = new Color(255, 209, 102);

    public ChiTietDonDialog(Frame owner, String maDon, DonDatMon don) {
        super(owner, "Cập Nhật Chi Tiết Đơn Hàng", true);
        this.maDon = maDon;
        this.don = don;

        initComponents();
        loadData();
    }

    private void initComponents() {
        setSize(900, 650);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout(0, 10));
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(15, 15, 15, 15));

        // 1. PANEL HEADER
        JPanel pnlHeader = new JPanel(new GridBagLayout());
        pnlHeader.setBackground(HEADER_BG);
        pnlHeader.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                "Thông tin đơn gọi món", 
                TitledBorder.LEFT, TitledBorder.TOP, 
                new Font("Segoe UI", Font.BOLD, 14)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Dòng 1: Mã đơn & Thời gian
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.1;
        pnlHeader.add(new JLabel("Mã đơn đặt:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.4;
        JTextField txtMa = new JTextField(don.getMaDonDat());
        txtMa.setEditable(false);
        pnlHeader.add(txtMa, gbc);

        gbc.gridx = 2; gbc.weightx = 0.1;
        pnlHeader.add(new JLabel("Thời gian:"), gbc);
        gbc.gridx = 3; gbc.weightx = 0.4;
        String thoiGianStr = don.getThoiGianDat().format(dtf);
        JTextField txtTime = new JTextField(thoiGianStr); 
        txtTime.setEditable(false);
        pnlHeader.add(txtTime, gbc);

        // Dòng 2: Mã NV & Mã bàn
        gbc.gridx = 0; gbc.gridy = 1;
        pnlHeader.add(new JLabel("Mã nhân viên:"), gbc);
        gbc.gridx = 1;
        JTextField txtNV = new JTextField(don.getMaNV());
        txtNV.setEditable(false);
        pnlHeader.add(txtNV, gbc);

        gbc.gridx = 2;
        pnlHeader.add(new JLabel("Mã bàn:"), gbc);
        gbc.gridx = 3;
        JTextField txtBan = new JTextField(don.getMaBan());
        txtBan.setEditable(false);
        pnlHeader.add(txtBan, gbc);

        // Dòng 3: Ghi chú
        gbc.gridx = 0; gbc.gridy = 2;
        pnlHeader.add(new JLabel("Ghi chú:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        txtNote = new JTextField(don.getGhiChu());
        txtNote.setEditable(true);
        pnlHeader.add(txtNote, gbc);

        add(pnlHeader, BorderLayout.NORTH);

        // 2. PANEL DETAIL
        JPanel pnlDetail = new JPanel(new BorderLayout(0, 10));
        pnlDetail.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Chi tiết đơn gọi món", TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 14)));

        JPanel pnlTool = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        pnlTool.add(new JLabel("Bổ sung món: "));
        cbMonAnMoi = new JComboBox<>(); cbMonAnMoi.setPreferredSize(new Dimension(250, 32));
        loadComboBoxMonAn(); pnlTool.add(cbMonAnMoi);

        btnThemMonMoi = new JButton("+ Thêm món");
        btnThemMonMoi.setBackground(BTN_YELLOW);
        btnThemMonMoi.addActionListener(e -> themMonVaoTable());
        pnlTool.add(btnThemMonMoi);

        btnXoaMon = new JButton("Xóa");
        btnXoaMon.setBackground(new Color(255, 102, 102)); btnXoaMon.setForeground(Color.WHITE);
        btnXoaMon.addActionListener(e -> xoaMon());
        pnlTool.add(btnXoaMon);
        pnlDetail.add(pnlTool, BorderLayout.NORTH);

        // BẢNG VÀ LOGIC TÍNH TẠM
        String[] cols = {"MÃ MÓN", "TÊN MÓN", "SỐ LƯỢNG", "ĐƠN GIÁ (vnđ)", "THÀNH TIỀN (vnđ)"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return col == 2; }
        };

        model.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == 2) {
                    int row = e.getFirstRow();
                    tinhLaiThanhTien(row);
                }
            }
        });

        table = new JTable(model);
        table.setRowHeight(32);
        JScrollPane scroll = new JScrollPane(table);
        pnlDetail.add(scroll, BorderLayout.CENTER);
        add(pnlDetail, BorderLayout.CENTER);
        table.setSelectionBackground(new Color(241, 213, 208));
        table.setSelectionForeground(Color.BLACK);

        // 3. PANEL SOUTH
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        btnCapNhat = new JButton("CẬP NHẬT ĐƠN");
        btnCapNhat.setBackground(BTN_BLUE); btnCapNhat.setForeground(Color.WHITE);
        btnCapNhat.setPreferredSize(new Dimension(180, 40));
        btnCapNhat.addActionListener(e -> capNhatDatabase());

        btnDong = new JButton("HỦY");
        btnDong.setPreferredSize(new Dimension(100, 40));
        btnDong.addActionListener(e -> dispose());

        pnlButtons.add(btnCapNhat); pnlButtons.add(btnDong);
        add(pnlButtons, BorderLayout.SOUTH);
    }

    private void loadComboBoxMonAn() {
        cbMonAnMoi.removeAllItems();
        List<MonAn> list = monAnDAO.getAllMonAnWithActivePrice(); 
        for (MonAn m : list) { 
            if (m.getGiaBan() > 0) {
                cbMonAnMoi.addItem(m.getMaMon() + " - " + m.getTenMon());
            }
        }
    }

    private void loadData() {
        model.setRowCount(0);
        List<ChiTietDatMon> ds = ctDAO.getByMaDon(maDon);
        for (ChiTietDatMon ct : ds) {
            double thanhTien = ct.getSoLuong() * ct.getDonGia();

            model.addRow(new Object[]{ 
                ct.getMaMon(), 
                ct.getMonAn().getTenMon(), 
                ct.getSoLuong(), 
                df.format(ct.getDonGia()), 
                df.format(thanhTien) 
            });
        }
    }

    private void themMonVaoTable() {
        String selected = (String) cbMonAnMoi.getSelectedItem();
        if (selected == null) return;
        String maMon = selected.split(" - ")[0];
        String tenMon = selected.split(" - ")[1];

        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0).toString().equals(maMon)) {
                int sl = Integer.parseInt(model.getValueAt(i, 2).toString()) + 1;
                model.setValueAt(sl, i, 2); 
                return;
            }
        }
        MonAn m = monAnDAO.getMonAnById(maMon);
        // ĐÃ FIX: Format chuỗi tiền tệ cho đồng bộ với lúc LoadData
        model.addRow(new Object[]{maMon, tenMon, 1, df.format(m.getGiaBan()), df.format(m.getGiaBan())});
    }

    private void tinhLaiThanhTien(int row) {
        try {
            int sl = Integer.parseInt(model.getValueAt(row, 2).toString());
            String maMon = model.getValueAt(row, 0).toString();
            MonAn m = monAnDAO.getMonAnById(maMon);
            
            double gia = m.getGiaBan();
            SwingUtilities.invokeLater(() -> model.setValueAt(df.format(sl * gia), row, 4));
        } catch (Exception ex) {}
    }

    private void xoaMon() {
        int row = table.getSelectedRow();
        if (row != -1) { model.removeRow(row); }
        else { JOptionPane.showMessageDialog(this, "Chọn món muốn xóa khỏi bảng!"); }
    }

    private void capNhatDatabase() {
        if (table.isEditing()) table.getCellEditor().stopCellEditing();
        
        int confirm = JOptionPane.showConfirmDialog(this, "Xác nhận lưu mọi thay đổi?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            String ghiChuMoi = txtNote.getText().trim();
            DonDatMon_DAO donDAO = new DonDatMon_DAO();
            donDAO.updateGhiChu(maDon, ghiChuMoi);

            // 1. Dọn dẹp Database cũ
            ctDAO.xoaTatCaChiTietTheoMaDon(maDon); 

            // 2. ĐÃ FIX: Chèn lại Database mới từ dữ liệu trên bảng
            for (int i = 0; i < table.getRowCount(); i++) {
                String maM = table.getValueAt(i, 0).toString();
                int sl = Integer.parseInt(table.getValueAt(i, 2).toString());
                
                // Loại bỏ tất cả ký tự không phải là số (như dấu phẩy, dấu chấm) để chuyển về Double
                String giaStr = table.getValueAt(i, 3).toString().replaceAll("[^0-9]", "");
                double donGia = Double.parseDouble(giaStr);
                
                ChiTietDatMon ct = new ChiTietDatMon(maDon, maM, sl, (float)donGia);
                ctDAO.insertCt(maDon, ct);
            }
            
            JOptionPane.showMessageDialog(this, "Hệ thống cập nhật đơn thành công!");
            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi lưu dữ liệu: " + e.getMessage());
        }
    }
}