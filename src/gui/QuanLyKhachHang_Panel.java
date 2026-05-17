package gui;

import dao.KhachHang_DAO;
import entity.KhachHang;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class QuanLyKhachHang_Panel extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private JTextField txtSearch;
    private JButton btnAdd;

    private final Color BG_WHITE = Color.WHITE;
    private final Color TIME_BANNER_BG = new Color(255, 246, 246);
    private final Color TABLE_HEADER_BG = new Color(255, 235, 235);
    private final Color TABLE_HEADER_TEXT = new Color(180, 50, 60);
    private final Color TEXT_DARK = new Color(44, 56, 74);
    private final Color BTN_ADD_YELLOW = new Color(255, 209, 102); 

    private KhachHang_DAO khDAO = new KhachHang_DAO();
    private String vaiTro;
    private DecimalFormat df = new DecimalFormat("#,###");

    public QuanLyKhachHang_Panel(String vaiTro) {
        this.vaiTro = vaiTro;
        setLayout(new BorderLayout(0, 20));
        setBackground(BG_WHITE);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // --- 1. BANNER THỜI GIAN ---
        JPanel topWrapper = new JPanel();
        topWrapper.setLayout(new BoxLayout(topWrapper, BoxLayout.Y_AXIS));
        topWrapper.setOpaque(false);

        JPanel timeBanner = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        timeBanner.setBackground(TIME_BANNER_BG);
        timeBanner.putClientProperty("FlatLaf.style", "arc: 15"); 

        JLabel lblTime = new JLabel();
        lblTime.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblTime.setForeground(Color.BLACK);
        
        Locale localeVI = new Locale("vi", "VN");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("EEEE, 'ngày' dd/MM/yyyy - HH:mm:ss", localeVI);
        new Timer(1000, e -> lblTime.setText(LocalDateTime.now().format(dtf))).start();
        timeBanner.add(lblTime);
        
        topWrapper.add(timeBanner);
        topWrapper.add(Box.createRigidArea(new Dimension(0, 20)));

        // --- 2. TIÊU ĐỀ & NÚT THÊM ---
        JPanel titleActionPanel = new JPanel(new BorderLayout());
        titleActionPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("QUẢN LÝ KHÁCH HÀNG");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(TEXT_DARK);
        titleActionPanel.add(lblTitle, BorderLayout.WEST);

        btnAdd = createStyledButton("+ Thêm Khách Hàng", BTN_ADD_YELLOW, Color.BLACK);
        btnAdd.setPreferredSize(new Dimension(180, 42)); 
        
        btnAdd.addActionListener(e -> {
            Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
            new ThemKhachHangDialog(owner, this).setVisible(true);
        });

        JPanel btnActions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnActions.setOpaque(false);
        btnActions.add(btnAdd);
        titleActionPanel.add(btnActions, BorderLayout.EAST);

        topWrapper.add(titleActionPanel);
        add(topWrapper, BorderLayout.NORTH);

        // --- 3. PHẦN CENTER (TÌM KIẾM & BẢNG) ---
        JPanel centerPanel = new JPanel(new BorderLayout(0, 20));
        centerPanel.setOpaque(false);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        searchPanel.setOpaque(false);
        searchPanel.add(createLabel("Tìm theo SĐT hoặc Tên:")); 
        
        txtSearch = new JTextField(20);
        txtSearch.setPreferredSize(new Dimension(300, 38)); 
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm Kiếm"); 
        
        // =========================================================
        // TÌM KIẾM REAL-TIME 
        // =========================================================
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { locTimKiem(); }
            @Override public void removeUpdate(DocumentEvent e) { locTimKiem(); }
            @Override public void changedUpdate(DocumentEvent e) { locTimKiem(); }
        });
        
        searchPanel.add(txtSearch);
        centerPanel.add(searchPanel, BorderLayout.NORTH);

        // BẢNG DỮ LIỆU ĐÃ XÓA CỘT ĐIỂM TÍCH LŨY
        String[] columnNames = {"Mã KH", "Họ Tên", "Số Điện Thoại", "Tổng Chi Tiêu (VNĐ)", "Hạng"};
        model = new DefaultTableModel(null, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0 && vaiTro.equalsIgnoreCase("Quản lý"); 
            }
        };

        table = new JTable(model);
        setupTableStyle();
        thietLapPhanQuyen(); 

        // SỰ KIỆN DOUBLE CLICK ĐỂ MỞ CỬA SỔ SỬA
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (vaiTro.equalsIgnoreCase("Thu ngân")) return; 

                int row = table.getSelectedRow();
                int col = table.getSelectedColumn();
                if (row == -1) return;
                
                if (e.getClickCount() == 2 && col != 0) {
                    String maKH = model.getValueAt(row, 0).toString();
                    
                    Frame owner = (Frame) SwingUtilities.getWindowAncestor(QuanLyKhachHang_Panel.this);
                    
                    // TODO: Mở comment dòng này khi bạn thiết kế xong SuaKhachHangDialog
                    // new SuaKhachHangDialog(owner, QuanLyKhachHang_Panel.this, maKH).setVisible(true);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        loadDataToTable();
    }

    private void thietLapPhanQuyen() {
        if (vaiTro.equalsIgnoreCase("Lễ tân")) {
            // Lễ tân không được xem Tổng chi tiêu (cột 3)
            table.getColumnModel().getColumn(3).setMinWidth(0);
            table.getColumnModel().getColumn(3).setMaxWidth(0);
        } else if (vaiTro.equalsIgnoreCase("Thu ngân")) {
            // Thu ngân không được Thêm khách hàng mới
            btnAdd.setVisible(false);
        }
    }

    private void locTimKiem() {
        String keyword = txtSearch.getText().trim().toLowerCase();
        model.setRowCount(0);
        List<KhachHang> ds = khDAO.getAllKhachHang();
        for (KhachHang kh : ds) {
            if (kh.getHoTen().toLowerCase().contains(keyword) || kh.getSoDienThoai().contains(keyword)) {
                model.addRow(new Object[]{
                    kh.getMaKH(), kh.getHoTen(), kh.getSoDienThoai(),
                    df.format(kh.getTongChiTieu()), kh.getHangThanhVien()
                });
            }
        }
    }

    public void loadDataToTable() {
        model.setRowCount(0);
        List<KhachHang> ds = khDAO.getAllKhachHang(); 
        for (KhachHang kh : ds) {
            model.addRow(new Object[]{
                kh.getMaKH(), kh.getHoTen(), kh.getSoDienThoai(),
                df.format(kh.getTongChiTieu()), kh.getHangThanhVien()
            });
        }
    }

    private void thucHienXoaKhachHang(int row) {
        String maKH = model.getValueAt(row, 0).toString();
        String tenKH = model.getValueAt(row, 1).toString();

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Xóa khách hàng [" + tenKH + "] khỏi hệ thống?\n(Dữ liệu sẽ được ẩn đi để không ảnh hưởng hóa đơn cũ)", 
            "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (khDAO.xoaMemKhachHang(maKH)) {
                JOptionPane.showMessageDialog(this, "Xóa thành công!");
                loadDataToTable();
            } else {
                JOptionPane.showMessageDialog(this, "Có lỗi xảy ra khi xóa!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void setupTableStyle() {
        table.setRowHeight(50); 
        table.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(240, 240, 240));
        table.setSelectionBackground(new Color(232, 240, 254)); 
        table.setSelectionForeground(Color.BLACK);
        table.setFocusable(false);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 15));
        header.setBackground(TABLE_HEADER_BG); 
        header.setForeground(TABLE_HEADER_TEXT);
        header.setPreferredSize(new Dimension(0, 45));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);

        for (int i = 0; i < table.getColumnCount(); i++) {
            if (i == 0) {
                boolean isQuanLy = vaiTro.equalsIgnoreCase("Quản lý");
                table.getColumnModel().getColumn(i).setCellRenderer(new ItemWithDeleteRenderer(isQuanLy));
                if (isQuanLy) {
                    table.getColumnModel().getColumn(i).setCellEditor(new ItemWithDeleteEditor(new JCheckBox()));
                }
                table.getColumnModel().getColumn(i).setPreferredWidth(120);
            } else {
                table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }
    }

    class ItemWithDeleteRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
        private final JLabel lblDelete = new JLabel("x");
        private final JLabel lblText = new JLabel();

        public ItemWithDeleteRenderer(boolean showDelete) {
            setLayout(new BorderLayout(5, 0));
            setBorder(new EmptyBorder(0, 10, 0, 0));
            
            lblDelete.setForeground(Color.RED);
            lblDelete.setFont(new Font("Arial", Font.BOLD, 15)); 
            
            lblText.setFont(new Font("Segoe UI", Font.BOLD, 15)); 
            
            if (showDelete) add(lblDelete, BorderLayout.WEST); 
            add(lblText, BorderLayout.CENTER);  
            setOpaque(true);
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            lblText.setText(value != null ? value.toString() : "");
            setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            lblText.setForeground(isSelected ? Color.BLACK : TEXT_DARK);
            return this;
        }
    }

    class ItemWithDeleteEditor extends DefaultCellEditor {
        private final JPanel panel = new JPanel(new BorderLayout(5, 0));
        private final JButton btnDelete = new JButton("x");
        private final JLabel lblText = new JLabel();
        private int currentRow;

        public ItemWithDeleteEditor(JCheckBox checkBox) {
            super(checkBox);
            panel.setBorder(new EmptyBorder(0, 10, 0, 0));
            btnDelete.setForeground(Color.RED);
            btnDelete.setFont(new Font("Arial", Font.BOLD, 15));
            btnDelete.setBorderPainted(false);
            btnDelete.setContentAreaFilled(false);
            btnDelete.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnDelete.setMargin(new Insets(0, 0, 0, 0));
            
            btnDelete.addActionListener(e -> {
                fireEditingStopped();
                thucHienXoaKhachHang(currentRow);
            });
            lblText.setFont(new Font("Segoe UI", Font.BOLD, 15));
            panel.add(btnDelete, BorderLayout.WEST);
            panel.add(lblText, BorderLayout.CENTER);
        }
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.currentRow = row;
            lblText.setText(value != null ? value.toString() : "");
            panel.setBackground(table.getSelectionBackground());
            lblText.setForeground(Color.BLACK);
            return panel;
        }
        @Override
        public Object getCellEditorValue() { return lblText.getText(); }
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

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(TEXT_DARK);
        return lbl;
    }
}