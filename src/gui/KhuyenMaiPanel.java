package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import dao.KhuyenMai_DAO;
import entity.KhuyenMai;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class KhuyenMaiPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private final Color BG_WHITE = Color.WHITE;
    private final Color TIME_BANNER_BG = new Color(255, 246, 246);
    private final Color TABLE_HEADER_BG = new Color(255, 235, 235);
    private final Color TABLE_HEADER_TEXT = new Color(180, 50, 60);
    private final Color TEXT_DARK = new Color(44, 56, 74);
    private DefaultTableModel model;
    private JTable table;
    private KhuyenMai_DAO dao = new KhuyenMai_DAO();

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public KhuyenMaiPanel() {
        setLayout(new BorderLayout(0, 20));
        setBackground(BG_WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel topWrapper = new JPanel();
        topWrapper.setLayout(new BoxLayout(topWrapper, BoxLayout.Y_AXIS));
        topWrapper.setOpaque(false);

        JPanel timeBanner = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        timeBanner.setBackground(TIME_BANNER_BG);
        timeBanner.putClientProperty("FlatLaf.style", "arc: 15");

        JLabel lblTime = new JLabel();
        lblTime.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        Locale localeVI = new Locale("vi", "VN");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("EEEE, 'ngày' dd/MM/yyyy - HH:mm:ss", localeVI);
        new Timer(1000, e -> lblTime.setText(LocalDateTime.now().format(dtf))).start();
        timeBanner.add(lblTime);
        topWrapper.add(timeBanner);
        topWrapper.add(Box.createRigidArea(new Dimension(0, 20)));

        JPanel titleActionPanel = new JPanel(new BorderLayout());
        titleActionPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("QUẢN LÝ KHUYẾN MÃI");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(TEXT_DARK);
        titleActionPanel.add(lblTitle, BorderLayout.WEST);

        JPanel btnActions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnActions.setOpaque(false);

        JButton btnAdd = new JButton("+ Thêm khuyến mãi");
        btnAdd.setBackground(new Color(255, 209, 102)); 
        btnAdd.setForeground(Color.BLACK);
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAdd.setPreferredSize(new Dimension(180, 42));
        btnAdd.setFocusPainted(false);
        btnAdd.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // --- SỬA Ở ĐÂY: TRUYỀN 'this' VÀO FORM ---
        btnAdd.addActionListener(e -> {
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            // Truyền KhuyenMaiPanel (this) vào để form thêm biết đường gọi lại
            new KhuyenMaiForm(parentFrame, this).setVisible(true); 
        });
        
        btnAdd.putClientProperty("JButton.buttonType", "roundRect");
        btnAdd.putClientProperty("JButton.arc", 15);
        btnActions.add(btnAdd);

        String[] filters = {"Tất cả chương trình", "Lọc theo số tiền", "Lọc theo phần trăm (%)"};
        JComboBox<String> cbFilter = new JComboBox<>(filters);
        cbFilter.setPreferredSize(new Dimension(220, 35));
        cbFilter.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbFilter.addActionListener(e -> loadDataByFilter(cbFilter.getSelectedItem().toString()));
        btnActions.add(cbFilter);

        titleActionPanel.add(btnActions, BorderLayout.EAST);
        topWrapper.add(titleActionPanel);

        add(topWrapper, BorderLayout.NORTH);

        String[] columnNames = {"Mã KM", "Tên chương trình", "Hình thức", "Giá trị", "Ngày bắt đầu", "Ngày kết thúc", "Trạng thái"};
        model = new DefaultTableModel(null, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        setupTableStyle();

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    String ma = table.getValueAt(row, 0).toString();
                    KhuyenMai km = dao.getAll().stream()
                            .filter(x -> x.getMaKM().equals(ma))
                            .findFirst()
                            .orElse(null);
                    if (km != null) {
                        // --- SỬA Ở ĐÂY: TRUYỀN 'this' VÀO FORM CHI TIẾT ---
                        new ChiTietKhuyenMaiForm((Window) SwingUtilities.getWindowAncestor(table), km, KhuyenMaiPanel.this).setVisible(true);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(BG_WHITE);
        add(scrollPane, BorderLayout.CENTER);

        loadData();
    }

    private void setupTableStyle() {
        table.setRowHeight(65);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(240, 240, 240));
        table.setSelectionBackground(new Color(232, 240, 254));
        table.setSelectionForeground(Color.BLACK);
        table.setFocusable(false);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(TABLE_HEADER_BG);
        header.setForeground(TABLE_HEADER_TEXT);
        header.setPreferredSize(new Dimension(0, 45));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        for (int i = 0; i < table.getColumnCount(); i++) {
            if (i == 3) { 
                table.getColumnModel().getColumn(i).setCellRenderer(new DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(JTable table, Object value,
                            boolean isSelected, boolean hasFocus, int row, int column) {
                        JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                                table, value, isSelected, hasFocus, row, column);
                        lbl.setHorizontalAlignment(JLabel.CENTER);
                        return lbl;
                    }
                });
            } else if (i == 6) { 
                table.getColumnModel().getColumn(i).setCellRenderer(new DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(JTable table, Object value,
                            boolean isSelected, boolean hasFocus, int row, int column) {
                        JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                                table, value, isSelected, hasFocus, row, column);
                        lbl.setHorizontalAlignment(JLabel.CENTER);
                        if (value != null) {
                            switch (value.toString()) {
                                case "Sắp diễn ra":
                                    lbl.setForeground(isSelected ? new Color(255, 200, 50) : new Color(255, 200, 50)); 
                                    lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
                                    break;
                                case "Đang chạy":
                                    lbl.setForeground(isSelected ? new Color(0, 153, 0) : new Color(0, 153, 0)); 
                                    lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
                                    break;
                                case "Đã kết thúc":
                                    lbl.setForeground(isSelected ? new Color(255, 102, 102) : new Color(255, 102, 102)); 
                                    break;
                                case "Ngừng áp dụng":
                                    lbl.setForeground(isSelected ? new Color(180, 50, 60) : new Color(180, 50, 60)); 
                                    lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
                                    break;
                                default:
                                    lbl.setForeground(isSelected ? Color.BLACK : Color.DARK_GRAY);
                                    lbl.setFont(lbl.getFont().deriveFont(Font.PLAIN));
                            }
                        }
                        return lbl;
                    }
                });
            } else {
                table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }
    }

    // --- SỬA Ở ĐÂY: DÙNG HÀM PUBLIC ĐỂ CÁC FORM GỌI VÀO ---
    public void loadData() {
        model.setRowCount(0);
        for (KhuyenMai km : dao.getAll()) {
            String giaTriStr = (km.getLoaiKM() != null && km.getLoaiKM().equals("Giảm phần trăm")) ?
                    km.getGiaTri() + "%" : String.format("%,.0f VNĐ", km.getGiaTri());

            // Bọc an toàn NullPointer cho ngày tháng
            String ngayBD = km.getNgayBatDau() != null ? km.getNgayBatDau().format(formatter) : "Chưa cập nhật";
            String ngayKT = km.getNgayKetThuc() != null ? km.getNgayKetThuc().format(formatter) : "Không giới hạn";

            model.addRow(new Object[]{
                    km.getMaKM(),
                    km.getTenKM(),
                    km.getLoaiKM(),
                    giaTriStr,
                    ngayBD,
                    ngayKT,
                    km.getTrangThai()
            });
        }
    }

    public void loadDataByFilter(String filter) {
        if (filter.equals("Tất cả chương trình")) {
            loadData();
            return;
        }
        
        model.setRowCount(0);
        for (KhuyenMai km : dao.getAll()) {
            if (filter.equals("Lọc theo phần trăm (%)") && !km.getLoaiKM().equals("Giảm phần trăm"))
                continue;
            if (filter.equals("Lọc theo số tiền") && !km.getLoaiKM().equals("Giảm giá"))
                continue;

            String giaTriStr = km.getLoaiKM().equals("Giảm phần trăm") ?
                    km.getGiaTri() + "%" : String.format("%,.0f VNĐ", km.getGiaTri());

            String ngayBD = km.getNgayBatDau() != null ? km.getNgayBatDau().format(formatter) : "Chưa cập nhật";
            String ngayKT = km.getNgayKetThuc() != null ? km.getNgayKetThuc().format(formatter) : "Không giới hạn";

            model.addRow(new Object[]{
                    km.getMaKM(),
                    km.getTenKM(),
                    km.getLoaiKM(),
                    giaTriStr,
                    ngayBD,
                    ngayKT,
                    km.getTrangThai()
            });
        }
    }
}