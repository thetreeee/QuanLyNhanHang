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
import java.util.Arrays;

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
        LocalDateTime now = LocalDateTime.now();

        for (DonDatBan d : dsDon) {
            if (d.getNgayDat().equals(LocalDate.now()) && 
               (d.getTrangThai().equalsIgnoreCase("Đang chờ") || d.getTrangThai().equalsIgnoreCase("Đã đặt"))) {
                
                LocalDateTime thoiGianDat = LocalDateTime.of(d.getNgayDat(), d.getThoiGian());
                long phutCachNhau = Duration.between(now, thoiGianDat).toMinutes();

                // Cho phép Check-in sớm 30 phút
                if (phutCachNhau <= 30) {
                    String tinhTrang = "Đúng giờ";
                    if (phutCachNhau > 0 && phutCachNhau <= 30) tinhTrang = "Sớm " + phutCachNhau + " phút";
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

    // =========================================================================
    // XỬ LÝ CHECK-IN VÀ CHẶN NGHIÊM NGẶT "TẤT CẢ HOẶC KHÔNG CÓ GÌ"
    // =========================================================================
    private void xuLyCheckIn() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một đơn để Check-in!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String maDon = table.getValueAt(row, 0).toString();
        String chuoiMaBan = table.getValueAt(row, 1).toString(); // VD: "B01" hoặc "B01, B02, B03"
        String tenKhach = table.getValueAt(row, 2).toString();

        String[] arrBan = chuoiMaBan.contains(",") ? chuoiMaBan.split(", ") : new String[]{chuoiMaBan};
        List<String> listMaBanGop = new ArrayList<>(Arrays.asList(arrBan));

        // =====================================================================
        // LỚP KHIÊN: GOM TẤT CẢ CÁC BÀN ĐANG CÓ KHÁCH ĐỂ BÁO CÁO 1 LẦN
        // =====================================================================
        List<Ban> dsBanHienTai = banDAO.getAllBan();
        List<String> cacBanDangBan = new ArrayList<>();
        
        for (String mb : listMaBanGop) {
            Ban banCheck = dsBanHienTai.stream().filter(b -> b.getMaBan().equals(mb)).findFirst().orElse(null);
            if (banCheck != null && (banCheck.getTrangThai().equalsIgnoreCase("Đang dùng") || banCheck.getTrangThai().equalsIgnoreCase("Checked-in"))) {
                cacBanDangBan.add(mb);
            }
        }

        if (!cacBanDangBan.isEmpty()) {
            String strBanBan = String.join(", ", cacBanDangBan);
            JOptionPane.showMessageDialog(this, 
                "KHOAN ĐÃ! Không thể Check-in vì trong nhóm bàn đặt, bàn: " + strBanBan + " hiện đang có khách ngồi (chưa thanh toán).\n\n" +
                "HƯỚNG XỬ LÝ:\n" +
                "1. Mời khách đoàn '" + tenKhach + "' ra khu vực chờ.\n" +
                "2. Hối thúc khách ở bàn " + strBanBan + " thanh toán.\n" +
                "3. Hoặc vào [Danh sách đặt bàn] -> Đổi sang các bàn trống khác.\n\n" +
                "Hệ thống TỪ CHỐI Check-in để đảm bảo đủ chỗ cho khách!", 
                "Xung đột bàn", JOptionPane.ERROR_MESSAGE);
            return; // Đẩy ra ngay lập tức, hủy bỏ thao tác Check-in
        }
        // =====================================================================

        // Vượt qua khiên bảo vệ, tiến hành Check-in bình thường
        if (listMaBanGop.size() > 1) {
            String maBanChinh = listMaBanGop.get(0); 
            
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Khách đoàn '" + tenKhach + "' đã đặt Nhóm gồm " + listMaBanGop.size() + " bàn: " + chuoiMaBan + ".\n\n" +
                "Hệ thống sẽ Check-in ĐỒNG LOẠT và TỰ ĐỘNG GỘP KHỐI.\n" +
                "Bàn chính đại diện hóa đơn sẽ là: " + maBanChinh + ".\n\n" +
                "Tất cả các bàn đều đã trống. Bạn có muốn Check-in ngay?", 
                "Xác nhận Check-in Khối", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                boolean successDon = donDAO.updateTrangThaiCuaDon(maDon, "Checked-in");
                boolean successBan = banDAO.taoKhoiBan(listMaBanGop, maBanChinh);

                if (successDon && successBan) {
                    for(String b : listMaBanGop) banDAO.updateTrangThaiBan(b, "Đang dùng");
                    JOptionPane.showMessageDialog(this, "Check-in Nhóm thành công! Các bàn đã được liên kết Khối.");
                    isThanhCong = true;
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Có lỗi xảy ra khi Check-in nhóm!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        } 
        else {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Xác nhận Check-in cho khách: " + tenKhach + " (Bàn " + chuoiMaBan + ")?", 
                "Xác nhận", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                boolean updateDon = donDAO.updateTrangThaiCuaDon(maDon, "Checked-in");
                boolean updateBan = banDAO.updateTrangThaiBan(chuoiMaBan, "Đang dùng");

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