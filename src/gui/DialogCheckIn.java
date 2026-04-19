package gui;

import dao.BanDAO;
import dao.DonDatBanDAO;
import entity.Ban;
import entity.DonDatBan;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;

public class DialogCheckIn extends JDialog {
    private static final long serialVersionUID = 1L;
    private JTable table;
    private DefaultTableModel model;
    private JButton btnCheckIn, btnHuy;
    private boolean isThanhCong = false;

    private DonDatBanDAO donDAO = new DonDatBanDAO();
    private BanDAO banDAO = new BanDAO();

    public DialogCheckIn(Window parent) {
        super(parent, "Danh Sách Khách Đợi Check-in", ModalityType.APPLICATION_MODAL);
        setSize(850, 500);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // --- HEADER ---
        JLabel lblTitle = new JLabel("DANH SÁCH ĐƠN ĐẶT HỢP LỆ ĐỂ CHECK-IN", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(220, 50, 50));
        lblTitle.setBorder(new EmptyBorder(20, 0, 15, 0));
        add(lblTitle, BorderLayout.NORTH);

        // --- BẢNG DANH SÁCH ---
        String[] cols = {"Mã đơn", "Mã bàn", "Khách hàng", "Số ĐT", "Giờ đặt", "Tình trạng"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(35);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.getTableHeader().setBackground(new Color(255, 235, 235));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));

        DefaultTableCellRenderer centerRender = new DefaultTableCellRenderer();
        centerRender.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            if (i != 2) table.getColumnModel().getColumn(i).setCellRenderer(centerRender);
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
        JPanel pnlTable = new JPanel(new BorderLayout());
        pnlTable.setBorder(new EmptyBorder(0, 20, 0, 20));
        pnlTable.setBackground(Color.WHITE);
        pnlTable.add(scroll, BorderLayout.CENTER);
        add(pnlTable, BorderLayout.CENTER);

        // --- NÚT BẤM ---
        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        pnlBottom.setBackground(Color.WHITE);

        btnCheckIn = new JButton("Xác nhận Check-in");
        btnCheckIn.setBackground(new Color(40, 167, 69)); // Màu xanh lá
        btnCheckIn.setForeground(Color.WHITE);
        btnCheckIn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCheckIn.setPreferredSize(new Dimension(180, 40));

        btnHuy = new JButton("Đóng");
        btnHuy.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnHuy.setPreferredSize(new Dimension(100, 40));

        btnHuy.addActionListener(e -> dispose());
        
        btnCheckIn.addActionListener(e -> xuLyCheckIn());

        pnlBottom.add(btnCheckIn);
        pnlBottom.add(btnHuy);
        add(pnlBottom, BorderLayout.SOUTH);

        loadData();
    }

    private void loadData() {
        model.setRowCount(0);
        List<DonDatBan> dsDon = donDAO.getAllDonDat();
        List<Ban> dsBan = banDAO.getAllBan();
        LocalDateTime now = LocalDateTime.now();

        for (DonDatBan d : dsDon) {
            if (d.getNgayDat().equals(LocalDate.now()) && 
               (d.getTrangThai().equalsIgnoreCase("Đang chờ") || d.getTrangThai().equalsIgnoreCase("Đã đặt"))) {
                
                LocalDateTime thoiGianDat = LocalDateTime.of(d.getNgayDat(), d.getThoiGian());
                long phutCachNhau = Duration.between(now, thoiGianDat).toMinutes();

                if (phutCachNhau <= 15) {
                    Ban banCuaDon = dsBan.stream().filter(b -> b.getMaBan().equals(d.getMaBan())).findFirst().orElse(null);
                    
                    if (banCuaDon != null && !banCuaDon.getTrangThai().equalsIgnoreCase("Đang dùng")) {
                        
                        String tinhTrang = "Đúng giờ";
                        if (phutCachNhau > 0 && phutCachNhau <= 15) tinhTrang = "Sớm " + phutCachNhau + " phút";
                        else if (phutCachNhau < 0) tinhTrang = "Trễ " + Math.abs(phutCachNhau) + " phút";

                        model.addRow(new Object[]{
                            d.getMaDon(),
                            d.getMaBan(),
                            d.getTenKhachHang() != null ? d.getTenKhachHang() : "Khách vãng lai",
                            d.getSoDienThoai(),
                            d.getThoiGian().format(DateTimeFormatter.ofPattern("HH:mm")),
                            tinhTrang
                        });
                    }
                }
            }
        }
    }

    // =========================================================================
    // ĐÃ NÂNG CẤP: AUTO CHECK-IN ĐỒNG LOẠT VÀ AUTO GỘP KHỐI CHO ĐƠN NHÓM
    // =========================================================================
    private void xuLyCheckIn() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một đơn để Check-in!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maDonFull = table.getValueAt(row, 0).toString();
        String tenKhach = table.getValueAt(row, 2).toString();

        // 1. Phân tích mã đơn để xem có phải đơn đặt theo khối không (Tách lấy phần gốc trước dấu '-')
        String baseMaDon = maDonFull.contains("-") ? maDonFull.substring(0, maDonFull.indexOf("-")) : maDonFull;

        // 2. Gom tất cả các bàn thuộc cùng mã đơn gốc này
        List<String> listMaBanGop = new ArrayList<>();
        List<String> listMaDonGop = new ArrayList<>();

        List<DonDatBan> dsDon = donDAO.getAllDonDat();
        for (DonDatBan d : dsDon) {
            if (d.getNgayDat().equals(LocalDate.now()) && 
               (d.getTrangThai().equalsIgnoreCase("Đang chờ") || d.getTrangThai().equalsIgnoreCase("Đã đặt"))) {
                
                String md = d.getMaDon();
                // Bắt đúng các đơn có mã giống hệt gốc, hoặc bắt đầu bằng "Gốc-"
                if (md.equals(baseMaDon) || md.startsWith(baseMaDon + "-")) {
                    listMaBanGop.add(d.getMaBan());
                    listMaDonGop.add(d.getMaDon());
                }
            }
        }

        // --- TRƯỜNG HỢP 1: ĐÂY LÀ ĐƠN ĐẶT KHỐI (Có từ 2 bàn trở lên) ---
        if (listMaBanGop.size() > 1) {
            String strBan = String.join(", ", listMaBanGop);
            String maBanChinh = listMaBanGop.get(0);
            
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Khách hàng '" + tenKhach + "' đã đặt Khối gồm " + listMaBanGop.size() + " bàn: " + strBan + ".\n\n" +
                "Hệ thống sẽ Check-in ĐỒNG LOẠT và TỰ ĐỘNG GỘP KHỐI các bàn này lại.\n" +
                "Bàn chính đại diện hóa đơn sẽ là: " + maBanChinh + ".\n\n" +
                "Bạn có muốn tiếp tục?", 
                "Xác nhận Check-in Khối", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                boolean successDon = true;
                // Cập nhật trạng thái Checked-in cho tất cả các phiếu
                for (String md : listMaDonGop) {
                    if (!donDAO.updateTrangThaiCuaDon(md, "Checked-in")) successDon = false;
                }

                // GỌI HÀM DAO ĐỂ KHÓA TẤT CẢ LẠI THÀNH 1 KHỐI TRONG DB
                boolean successBan = banDAO.taoKhoiBan(listMaBanGop, maBanChinh);

                if (successDon && successBan) {
                    JOptionPane.showMessageDialog(this, "Check-in Khối thành công! Các bàn đã được gộp số Nhóm.");
                    isThanhCong = true;
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Có lỗi xảy ra khi Check-in khối!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        } 
        // --- TRƯỜNG HỢP 2: ĐÂY CHỈ LÀ ĐƠN BÀN ĐƠN LẺ ---
        else {
            String maBan = listMaBanGop.get(0);
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Xác nhận Check-in cho khách: " + tenKhach + " (Bàn " + maBan + ")?", 
                "Xác nhận", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                boolean updateDon = donDAO.updateTrangThaiCuaDon(maDonFull, "Checked-in");
                boolean updateBan = banDAO.updateTrangThaiBan(maBan, "Đang dùng");

                if (updateDon && updateBan) {
                    JOptionPane.showMessageDialog(this, "Check-in thành công! Bàn đã sẵn sàng gọi món.");
                    isThanhCong = true;
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Lỗi hệ thống khi Check-in!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    public boolean isThanhCong() {
        return isThanhCong;
    }
}