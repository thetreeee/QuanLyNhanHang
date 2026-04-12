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
import javax.swing.table.TableColumn;
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

        setSize(900, 500);
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
        String[] cols = {"Mã đơn", "Họ tên", "Số điện thoại", "Ngày đặt", "Thời gian", "Số lượng khách", "Trạng thái"};
        
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { 
                return column == 1 || column == 2 || column == 6; 
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
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer); 
        table.getColumnModel().getColumn(5).setCellRenderer(centerRenderer); 
        table.getColumnModel().getColumn(6).setCellRenderer(centerRenderer); 

        // --- COMBOBOX TRẠNG THÁI ---
        JComboBox<String> cbTrangThai = new JComboBox<>(new String[]{"Đã đặt", "Đã hủy"});
        cbTrangThai.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbTrangThai.setBackground(Color.WHITE);
        DefaultCellEditor trangThaiEditor = new DefaultCellEditor(cbTrangThai);
        trangThaiEditor.setClickCountToStart(2);
        table.getColumnModel().getColumn(6).setCellEditor(trangThaiEditor);

        loadDataToTable();

        // --- LẮNG NGHE SỰ KIỆN ĐỔI TRẠNG THÁI TRỰC TIẾP TRÊN BẢNG ---
        model.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (isUpdatingTable) return; 

                if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == 6) {
                    int row = e.getFirstRow();
                    String maDon = model.getValueAt(row, 0).toString();
                    String trangThaiMoi = model.getValueAt(row, 6).toString();
                    
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
                                model.setValueAt("Đã hủy", row, 6); 
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
            // Tắt auto-commit để thực hiện giao dịch (Transaction) an toàn cho 2 bảng
            con.setAutoCommit(false); 
            
            // 1. Câu lệnh update Trạng thái cho bảng DonDatBan
            String sqlDon = "UPDATE DonDatBan SET trangThai = ? WHERE maDon = ?";
            
            // 2. Câu lệnh update Họ tên và SĐT cho bảng KhachHang 
            String sqlKhach = "UPDATE KhachHang SET hoTen = ?, soDienThoai = ? WHERE maKhachHang = (SELECT maKhachHang FROM DonDatBan WHERE maDon = ?)";
            
            try (PreparedStatement psDon = con.prepareStatement(sqlDon);
                 PreparedStatement psKhach = con.prepareStatement(sqlKhach)) {
                 
                for (int i = 0; i < table.getRowCount(); i++) {
                    String maDon = model.getValueAt(i, 0).toString();
                    String tenKhach = model.getValueAt(i, 1).toString().trim();
                    String sdt = model.getValueAt(i, 2).toString().trim();
                    String trangThai = model.getValueAt(i, 6).toString();

                    // Nạp dữ liệu Batch cho bảng DonDatBan
                    psDon.setString(1, trangThai);
                    psDon.setString(2, maDon);
                    psDon.addBatch(); 
                    
                    // Nạp dữ liệu Batch cho bảng KhachHang
                    psKhach.setString(1, tenKhach);
                    psKhach.setString(2, sdt);
                    psKhach.setString(3, maDon);
                    psKhach.addBatch();
                }
                
                // Thực thi cả 2 Batch
                psDon.executeBatch();
                psKhach.executeBatch();
                
                // Nếu mọi thứ trơn tru thì Commit (chốt lưu) xuống CSDL
                con.commit(); 
            } catch (Exception e) {
                con.rollback(); // Nếu có bất kỳ lỗi gì xảy ra, hoàn tác không lưu gì cả
                throw e; 
            } finally {
                con.setAutoCommit(true); // Trả lại trạng thái mặc định
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