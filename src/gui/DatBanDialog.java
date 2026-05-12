package gui;

import dao.BanDAO;
import dao.DonDatBanDAO;
import entity.Ban;
import entity.DonDatBan;
import connectDB.SQLConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DatBanDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private JTable table;
    private DefaultTableModel model;
    private Ban ban;
    private List<DonDatBan> dsDonDat;
    private DonDatBanDAO donDatBanDAO = new DonDatBanDAO();
    private BanDAO banDAO = new BanDAO();
    
    private boolean isUpdatingTable = false; 

    public DatBanDialog(Window parent, Ban ban, List<DonDatBan> dsDonDat) {
        super(parent, "Chi tiết các đơn đặt - Bàn " + ban.getMaBan(), ModalityType.APPLICATION_MODAL);
        this.ban = ban;
        this.dsDonDat = dsDonDat;

        setSize(1000, 500); // Mở rộng form ra một chút để chứa thêm cột Mã NV
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(Color.WHITE);

        // --- 1. HEADER TITLE ---
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(Color.WHITE);
        pnlHeader.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel lblHeader = new JLabel("CHI TIẾT CÁC ĐƠN ĐẶT CHO BÀN " + ban.getMaBan().toUpperCase());
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblHeader.setForeground(new Color(220, 50, 50)); 
        pnlHeader.add(lblHeader, BorderLayout.WEST);
        add(pnlHeader, BorderLayout.NORTH);

        // --- 2. JTABLE DATA ---
        // ĐÃ SỬA: Bổ sung thêm cột "Mã nhân viên" vào vị trí số 2
        String[] cols = {"Mã đơn", "Mã NV", "Họ tên", "Số điện thoại", "Ngày đặt", "Thời gian", "Số lượng khách", "Trạng thái"};
        
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { 
                // ĐÃ SỬA: Chỉ cho phép sửa Họ Tên (2), Số điện thoại (3), và Trạng thái (7)
                // Các cột Mã đơn (0) và Mã NV (1) sẽ bị khóa cứng (Read-only)
                return column == 2 || column == 3 || column == 7; 
            }
        };
        table = new JTable(model);
        
        table.setRowHeight(35); 
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        table.getTableHeader().setBackground(new Color(255, 235, 235)); 
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));
        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(230, 230, 230)));
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setBackground(new Color(255, 235, 235)); 
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
                lbl.setForeground(Color.BLACK);
                return lbl;
            }
        });

        table.setShowVerticalLines(false);
        table.setGridColor(new Color(240, 240, 240));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer); // Mã đơn
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer); // Mã NV
        table.getColumnModel().getColumn(6).setCellRenderer(centerRenderer); // Số lượng khách
        table.getColumnModel().getColumn(7).setCellRenderer(centerRenderer); // Trạng thái

        // --- COMBOBOX TRẠNG THÁI ---
        JComboBox<String> cbTrangThai = new JComboBox<>(new String[]{"Đã đặt", "Đã hủy"});
        cbTrangThai.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbTrangThai.setBackground(Color.WHITE);
        DefaultCellEditor trangThaiEditor = new DefaultCellEditor(cbTrangThai);
        trangThaiEditor.setClickCountToStart(2);
        
        // ĐÃ SỬA: Cập nhật index của cột Trạng thái thành 7
        table.getColumnModel().getColumn(7).setCellEditor(trangThaiEditor);

        loadDataToTable();

        // --- LẮNG NGHE SỰ KIỆN ĐỔI TRẠNG THÁI TRỰC TIẾP TRÊN BẢNG ---
        model.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (isUpdatingTable) return; 

                // ĐÃ SỬA: Bắt sự kiện ở cột số 7 (Trạng thái)
                if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == 7) {
                    int row = e.getFirstRow();
                    String maDon = model.getValueAt(row, 0).toString();
                    String trangThaiMoi = model.getValueAt(row, 7).toString();
                    
                    DonDatBan currentDon = dsDonDat.stream()
                            .filter(d -> d.getMaDon().equals(maDon)).findFirst().orElse(null);

                    if (currentDon != null) {
                        if (trangThaiMoi.equals("Đã đặt")) {
                            boolean biTrung = donDatBanDAO.kiemTraTrungLich(ban.getMaBan(), currentDon.getNgayDat(), currentDon.getThoiGian(), maDon);
                            if (biTrung) {
                                JOptionPane.showMessageDialog(DatBanDialog.this, 
                                    "Không thể đặt được vì có khách đã đặt giờ đó.\nXin đặt lại giờ khác.", 
                                    "Cảnh báo trùng lịch", JOptionPane.WARNING_MESSAGE);
                                
                                isUpdatingTable = true;
                                model.setValueAt("Đã hủy", row, 7); 
                                isUpdatingTable = false;
                                return; 
                            }
                        }

                        donDatBanDAO.updateTrangThaiCuaDon(maDon, trangThaiMoi);

                        if (trangThaiMoi.equals("Đã hủy")) {
                            banDAO.updateTrangThaiBan(ban.getMaBan(), "Trống"); 
                        } else if (trangThaiMoi.equals("Đã đặt")) {
                            if (currentDon.getNgayDat().equals(LocalDate.now())) {
                                banDAO.updateTrangThaiBan(ban.getMaBan(), "Đã đặt");
                            }
                        }
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

        JButton btnCapNhat = createStyledButton("CẬP NHẬT", new Color(54, 92, 245)); 
        JButton btnDong = createStyledButton("Đóng", Color.GRAY);

        btnCapNhat.addActionListener(e -> luuCapNhatThongTin());
        btnDong.addActionListener(e -> dispose());

        pnlBottom.add(btnCapNhat);
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
                d.getMaNV() != null ? d.getMaNV() : "N/A", // ĐÃ SỬA: Thêm Mã NV vào bảng
                d.getTenKhachHang() != null ? d.getTenKhachHang() : "",
                d.getSoDienThoai() != null ? d.getSoDienThoai() : "",
                d.getNgayDat() != null ? d.getNgayDat().format(dfNgay) : "",
                d.getThoiGian() != null ? d.getThoiGian().format(dfGio) : "",
                d.getSoLuongKhach(), 
                d.getTrangThai()
            });
        }
    }

    // --- CẬP NHẬT: Lưu dữ liệu vào 2 bảng riêng biệt (KhachHang và DonDatBan) ---
    private void luuCapNhatThongTin() {
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }

        try (Connection con = SQLConnection.getConnection()) {
            con.setAutoCommit(false); 
            
            String sqlDon = "UPDATE DonDatBan SET trangThai = ? WHERE maDon = ?";
            String sqlKhach = "UPDATE KhachHang SET hoTen = ?, soDienThoai = ? WHERE maKhachHang = (SELECT maKhachHang FROM DonDatBan WHERE maDon = ?)";
            
            try (PreparedStatement psDon = con.prepareStatement(sqlDon);
                 PreparedStatement psKhach = con.prepareStatement(sqlKhach)) {
                 
                for (int i = 0; i < table.getRowCount(); i++) {
                    String maDon = model.getValueAt(i, 0).toString();
                    
                    // ĐÃ SỬA: Đẩy Index lên để bỏ qua cột Mã NV (Index 1) vì nó Read-only
                    String tenKhach = model.getValueAt(i, 2).toString().trim();
                    String sdt = model.getValueAt(i, 3).toString().trim();
                    String trangThai = model.getValueAt(i, 7).toString();

                    psDon.setString(1, trangThai);
                    psDon.setString(2, maDon);
                    psDon.addBatch(); 
                    
                    psKhach.setString(1, tenKhach);
                    psKhach.setString(2, sdt);
                    psKhach.setString(3, maDon);
                    psKhach.addBatch();
                }
                
                psDon.executeBatch();
                psKhach.executeBatch();
                
                con.commit(); 
            } catch (Exception e) {
                con.rollback(); 
                throw e; 
            } finally {
                con.setAutoCommit(true); 
            }
            
            JOptionPane.showMessageDialog(this, "Đã cập nhật thông tin thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi lưu dữ liệu: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
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