package gui;

import entity.Ban;
import entity.DonDatBan;
import dao.BanDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DatBanDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private JTable table;
    private DefaultTableModel model;
    private Ban ban;
    private List<DonDatBan> dsDonDat;
    private BanDAO banDAO = new BanDAO(); // Dùng để cập nhật trạng thái bàn

    public DatBanDialog(Window parent, Ban ban, List<DonDatBan> dsDonDat) {
        super(parent, "Chi tiết các đơn đặt - " + ban.getTenBan(), ModalityType.APPLICATION_MODAL);
        this.ban = ban;
        this.dsDonDat = dsDonDat;

        setSize(900, 500);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(Color.WHITE);

        // --- 1. HEADER TITLE ---
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(Color.WHITE);
        pnlHeader.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel lblHeader = new JLabel("CHI TIẾT CÁC ĐƠN ĐẶT CHO " + ban.getTenBan().toUpperCase());
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 18));
        // Đã đổi sang màu hồng/đỏ của logo thương hiệu Tuấn Trường
        lblHeader.setForeground(new Color(220, 50, 50)); 
        pnlHeader.add(lblHeader, BorderLayout.WEST);
        
        add(pnlHeader, BorderLayout.NORTH);

        // --- 2. JTABLE DATA ---
        String[] cols = {
            "Mã đơn", "Họ tên", "Số điện thoại", "Ngày đặt", "Thời gian", "Số lượng khách", "Trạng thái"
        };
        
        // CẬP NHẬT: Cho phép sửa (Double-click) ở cột 6 (Trạng thái)
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { 
                return column == 6; // Chỉ cho phép sửa cột Trạng thái
            }
        };
        table = new JTable(model);
        
        // --- STYLING CHO TABLE ---
        table.setRowHeight(35); 
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        // CẬP NHẬT: Ép in đậm chữ Header
        table.getTableHeader().setBackground(new Color(255, 235, 235)); 
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));
        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(230, 230, 230)));
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setBackground(new Color(255, 235, 235)); 
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 14)); // IN ĐẬM CHỮ CỘT
                lbl.setForeground(Color.BLACK);
                return lbl;
            }
        });

        table.setShowVerticalLines(false);
        table.setGridColor(new Color(240, 240, 240));

        // Căn giữa dữ liệu
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer); 
        table.getColumnModel().getColumn(5).setCellRenderer(centerRenderer); 
        table.getColumnModel().getColumn(6).setCellRenderer(centerRenderer); 

        // --- CẬP NHẬT: THÊM COMBOBOX VÀO CỘT TRẠNG THÁI ---
        JComboBox<String> cbTrangThai = new JComboBox<>(new String[]{"Đã đặt", "Đã hủy"});
        cbTrangThai.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbTrangThai.setBackground(Color.WHITE);
        
        DefaultCellEditor trangThaiEditor = new DefaultCellEditor(cbTrangThai);
        trangThaiEditor.setClickCountToStart(2); // Cài đặt click 2 lần mới hiện ComboBox
        
        TableColumn trangThaiCol = table.getColumnModel().getColumn(6);
        trangThaiCol.setCellEditor(trangThaiEditor);

        loadDataToTable();

        // --- CẬP NHẬT: LẮNG NGHE SỰ KIỆN KHI ĐỔI TRẠNG THÁI BẰNG COMBOBOX ---
        model.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == 6) {
                    int row = e.getFirstRow();
                    String trangThaiMoi = model.getValueAt(row, 6).toString();
                    
                    // Cập nhật trạng thái Bàn trong Database để Panel bên ngoài tự đổi màu
                    if (trangThaiMoi.equals("Đã hủy")) {
                        banDAO.updateTrangThaiBan(ban.getMaBan(), "Trống"); // Trống -> Màu xanh
                    } else if (trangThaiMoi.equals("Đã đặt")) {
                        banDAO.updateTrangThaiBan(ban.getMaBan(), "Đã đặt"); // Đã đặt -> Màu vàng
                    }
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, new Color(230, 230, 230)));
        add(scroll, BorderLayout.CENTER);

        // --- 3. BOTTOM BUTTONS ---
        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        pnlBottom.setBackground(new Color(248, 249, 250));
        pnlBottom.setBorder(new EmptyBorder(5, 20, 5, 20));

        JButton btnHuyDon = createStyledButton("Hủy đơn chọn", new Color(220, 53, 69));
        JButton btnDong = createStyledButton("Đóng", Color.GRAY);

        btnHuyDon.addActionListener(e -> huyDonDat());
        btnDong.addActionListener(e -> dispose());

        pnlBottom.add(btnHuyDon);
        pnlBottom.add(btnDong);
        add(pnlBottom, BorderLayout.SOUTH);
    }

    private void loadDataToTable() {
        model.setRowCount(0);
        DateTimeFormatter dfNgay = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter dfGio = DateTimeFormatter.ofPattern("HH:mm");

        for (DonDatBan d : dsDonDat) {
            model.addRow(new Object[]{
                d.getMaDon(),
                d.getTenKhachHang(),
                d.getSoDienThoai(),
                d.getNgayDat() != null ? d.getNgayDat().format(dfNgay) : "",
                d.getThoiGian() != null ? d.getThoiGian().format(dfGio) : "",
                d.getSoLuongKhach(), 
                d.getTrangThai()
            });
        }
    }

    private void huyDonDat() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn đơn cần hủy!", "Nhắc nhở", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String trangThai = model.getValueAt(row, 6).toString(); 
        if (trangThai.equalsIgnoreCase("Đã hủy")) {
            JOptionPane.showMessageDialog(this, "Đơn này đã được hủy trước đó!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String maDon = model.getValueAt(row, 0).toString();
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn hủy đơn " + maDon + "?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            // Lệnh setValueAt này sẽ kích hoạt TableModelListener ở trên để tự động đổi màu thẻ bàn bên ngoài
            model.setValueAt("Đã hủy", row, 6); 
            JOptionPane.showMessageDialog(this, "Đã hủy đơn " + maDon + " thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(130, 38));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        btn.putClientProperty("JButton.buttonType", "roundRect");
        btn.putClientProperty("JButton.arc", 15); 
        return btn;
    }
}