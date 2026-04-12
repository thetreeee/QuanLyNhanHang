package gui;

import dao.*;
import entity.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.ArrayList;

public class DanhSachDonPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private JTable table;
    private DefaultTableModel model;
    private JTextField txtSearch;

    private DonDatMon_DAO donDAO = new DonDatMon_DAO();
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
    DecimalFormat df = new DecimalFormat("#,### VNĐ");

    // ===== STYLE =====
    private final Color BG_WHITE = Color.WHITE;
    private final Color TABLE_HEADER_BG = new Color(255, 235, 235);
    private final Color TABLE_HEADER_TEXT = new Color(180, 50, 60);
    private final Color TEXT_DARK = new Color(44, 56, 74);
    private JLabel lblTimeDate;
    private Timer timer;

    private String maNhanVien;

    public DanhSachDonPanel(String maNV) {
        this.maNhanVien = maNV; 

        setLayout(new BorderLayout(0, 20));
        setBackground(BG_WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel topWrapper = new JPanel();
        topWrapper.setLayout(new BoxLayout(topWrapper, BoxLayout.Y_AXIS));
        topWrapper.setOpaque(false);

        JPanel timeBanner = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        timeBanner.setBackground(new Color(255, 246, 246));
        timeBanner.putClientProperty("FlatLaf.style", "arc: 15");

        lblTimeDate = new JLabel();
        lblTimeDate.setFont(new Font("Segoe UI", Font.PLAIN, 15)); 
        lblTimeDate.setForeground(Color.BLACK);
        
        Locale localeVI = new Locale("vi", "VN");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("EEEE, 'ngày' dd/MM/yyyy - HH:mm:ss", localeVI);
        timer = new Timer(1000, e -> lblTimeDate.setText(LocalDateTime.now().format(dtf)));
        timer.start();
        
        timeBanner.add(lblTimeDate);
        topWrapper.add(timeBanner);
        topWrapper.add(Box.createRigidArea(new Dimension(0, 20)));

        JPanel titleActionPanel = new JPanel(new BorderLayout());
        titleActionPanel.setOpaque(false);
  
        // ===== TITLE =====
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        
        topWrapper.add(Box.createVerticalStrut(15));
        JLabel lblTitle = new JLabel("DANH SÁCH ĐƠN GỌI MÓN HÔM NAY"); 
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(TEXT_DARK);

        titlePanel.add(lblTitle, BorderLayout.WEST);
        topWrapper.add(titlePanel);
        
        // ===== SEARCH =====
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.setOpaque(false);

        searchPanel.add(new JLabel("Tìm theo mã bàn:"));

        txtSearch = new JTextField(20);
        txtSearch.setPreferredSize(new Dimension(250, 35));
        
        txtSearch.addActionListener(e -> loadData(txtSearch.getText().trim()));
        
        searchPanel.add(txtSearch);

        JButton btnReset = createStyledButton("Làm mới", new Color(108, 117, 110), Color.white);
        btnReset.setFocusPainted(false);
        
        btnReset.addActionListener(e -> {
            txtSearch.setText(""); 
            loadData("");          
        });
        
        searchPanel.add(btnReset);

        topWrapper.add(searchPanel);

        add(topWrapper, BorderLayout.NORTH);

        // ===== TABLE =====
        model = new DefaultTableModel(
                new String[]{"", "Mã đơn", "Mã Bàn", "Thời gian", "Ghi chú", "Mã Nhân Viên"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0; 
            }
        };

        table = new JTable(model);
        setupTableStyle();

        table.getColumnModel().getColumn(0).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(0).setCellEditor(new ButtonEditor(new JCheckBox()));

        add(new JScrollPane(table), BorderLayout.CENTER);

        // ===== LOAD =====
        loadData("");

        // ===== DOUBLE CLICK XEM CHI TIẾT =====
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedColumn() != 0) {
                    int row = table.getSelectedRow();
                    String maDon = model.getValueAt(row, 1).toString();
                    showDetail(maDon);
                }
            }
        });

        this.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                if (txtSearch != null) {
                    loadData(txtSearch.getText().trim()); 
                } else {
                    loadData("");
                }
            }
        });
    }

    // ===== LOAD DATA =====
    public void loadData(String keyword) {
        model.setRowCount(0);
        List<DonDatMon> ds = new ArrayList<>();

        try {
            ds = donDAO.getAll();
        } catch (Exception e) {
            e.printStackTrace();
        }

        LocalDate homNay = LocalDate.now();

        for (DonDatMon d : ds) {
            
            // --- LỌC ĐƠN HÔM NAY ---
            LocalDate ngayCuaDon = d.getThoiGianDat().toLocalDate();
            if (!ngayCuaDon.equals(homNay)) {
                continue;
            }

            // --- NÂNG CẤP LỌC TÌM KIẾM: QUÉT CẢ MÃ BÀN VÀ GHI CHÚ ---
            String maBan = d.getMaBan() != null ? d.getMaBan().toLowerCase() : "";
            String ghiChu = (d.getGhiChu() != null) ? d.getGhiChu().toLowerCase() : "";
            String key = keyword.toLowerCase();

            // Nếu ô tìm kiếm có chữ, VÀ chữ đó KHÔNG NẰM TRONG Mã Bàn, VÀ CŨNG KHÔNG NẰM TRONG Ghi Chú -> Bỏ qua
            if (!keyword.isEmpty() && !maBan.contains(key) && !ghiChu.contains(key)) {
                continue;
            }

            String ngayDinhDang = d.getThoiGianDat().format(dtf);
            
            model.addRow(new Object[]{
                    "x", 
                    d.getMaDonDat(), 
                    d.getMaBan(), 
                    ngayDinhDang, 
                    d.getGhiChu(), 
                    d.getMaNV() 
            });
        }
    }

    // ===== SHOW DETAIL =====
    private void showDetail(String maDon) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        DonDatMon don = donDAO.getById(maDon); 
        ChiTietDonDialog dialog = new ChiTietDonDialog((Frame) owner, maDon, don);
        dialog.setVisible(true);
        loadData(txtSearch.getText().trim());
    }

    // ===== STYLE TABLE =====
    private void setupTableStyle() {
        table.setRowHeight(45);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(240, 240, 240));
        table.setSelectionBackground(new Color(232, 240, 254));
        table.setSelectionForeground(Color.BLACK);
        
        table.getColumnModel().getColumn(0).setMaxWidth(35);
        table.getColumnModel().getColumn(0).setMinWidth(35);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(TABLE_HEADER_BG);
        header.setForeground(TABLE_HEADER_TEXT);
        header.setPreferredSize(new Dimension(0, 40));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(JLabel.CENTER);

        for (int i = 1; i < 6; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(center);
        }
    }
    
    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(130, 38));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        btn.putClientProperty("JButton.buttonType", "roundRect");
        btn.putClientProperty("JButton.arc", 15);
        return btn;
    }

    // =========================================================================
    // LỚP HỖ TRỢ VẼ CHỮ "x" ĐỎ ĐỂ XÓA 
    // =========================================================================
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFont(new Font("Arial", Font.BOLD, 15));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setForeground(Color.RED); 
            setHorizontalAlignment(SwingConstants.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText("x");
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private boolean isPushed;
        private int selectedRow;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(false);
            button.setContentAreaFilled(false);
            button.setBorderPainted(false);
            button.setFont(new Font("Arial", Font.BOLD, 15));
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            button.setForeground(Color.RED);
            
            button.addActionListener(e -> {
                fireEditingStopped();
                String maDon = table.getValueAt(selectedRow, 1).toString();
                
                int confirm = JOptionPane.showConfirmDialog(null, 
                        "Bạn có chắc chắn muốn hủy đơn hàng: " + maDon + "?", 
                        "Xác nhận hủy đơn", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    new dao.ChiTietDatMon_DAO().xoaTatCaChiTietTheoMaDon(maDon);
                    donDAO.xoaDon(maDon);
                    loadData(txtSearch.getText().trim());
                    JOptionPane.showMessageDialog(null, "Đã hủy đơn hàng thành công!");
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            selectedRow = row;
            button.setText("x");
            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            isPushed = false;
            return "x";
        }
        
        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }
    }
}