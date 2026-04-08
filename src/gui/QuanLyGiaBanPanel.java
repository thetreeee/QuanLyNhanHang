package gui;

import dao.GiaBan_DAO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Vector;

public class QuanLyGiaBanPanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private JTextField txtSearch;

    private final Color BG_WHITE = Color.WHITE;
    private final Color TIME_BANNER_BG = new Color(255, 246, 246);
    private final Color TABLE_HEADER_BG = new Color(255, 235, 235);
    private final Color TABLE_HEADER_TEXT = new Color(180, 50, 60);
    private final Color TEXT_DARK = new Color(44, 56, 74);
    private final Color BTN_ADD_YELLOW = new Color(255, 209, 102); 
    private final Color BTN_SEARCH_PINK = new Color(255, 102, 102);

    private GiaBan_DAO giaBan_dao = new GiaBan_DAO();

    public QuanLyGiaBanPanel() {
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
        
        // Cập nhật đồng hồ sang tiếng Việt chuẩn
        Locale localeVI = new Locale("vi", "VN");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("EEEE, 'ngày' dd/MM/yyyy - HH:mm:ss", localeVI);
        new Timer(1000, e -> lblTime.setText(LocalDateTime.now().format(dtf))).start();
        timeBanner.add(lblTime);
        
        topWrapper.add(timeBanner);
        topWrapper.add(Box.createRigidArea(new Dimension(0, 20)));

        // --- 2. TIÊU ĐỀ & NÚT THÊM ---
        JPanel titleActionPanel = new JPanel(new BorderLayout());
        titleActionPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("QUẢN LÝ BẢNG GIÁ");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(TEXT_DARK);
        titleActionPanel.add(lblTitle, BorderLayout.WEST);

        JButton btnAdd = createStyledButton("+ Thêm Bảng Giá", BTN_ADD_YELLOW, Color.BLACK);
        btnAdd.setPreferredSize(new Dimension(160, 42)); 
        btnAdd.addActionListener(e -> {
            GUIDashBoard dashBoard = (GUIDashBoard) SwingUtilities.getWindowAncestor(this);
            new ThemGiaBanDialog(dashBoard, this).setVisible(true);
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
        searchPanel.add(createLabel("Tìm theo Mã hoặc Mô tả:")); 
        
        txtSearch = new JTextField(20);
        txtSearch.setPreferredSize(new Dimension(250, 38));
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchPanel.add(txtSearch);

        JButton btnXem = createStyledButton("Lọc dữ liệu", BTN_SEARCH_PINK, Color.WHITE);
        
        // SỰ KIỆN NÚT LỌC DỮ LIỆU
        btnXem.addActionListener(e -> {
            String keyword = txtSearch.getText().trim();
            if (keyword.isEmpty()) {
                loadDataToTable(); 
            } else {
                thucHienTimKiem(keyword);
            }
        });
        
        searchPanel.add(btnXem);
        centerPanel.add(searchPanel, BorderLayout.NORTH);

        // ĐỔI CỘT CHO PHÙ HỢP MÔ HÌNH MASTER-DETAIL
        String[] columnNames = {"Mã Bảng Giá", "Mô tả (Tên đợt giá)", "Ngày bắt đầu", "Ngày kết thúc", "Trạng thái"};
        model = new DefaultTableModel(null, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0; // Chỉ cột 0 có nút Xóa mới được "click"
            }
        };

        table = new JTable(model);
        setupTableStyle();

        // SỰ KIỆN DOUBLE CLICK ĐỂ MỞ CỬA SỔ SỬA CHI TIẾT
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                int col = table.getSelectedColumn();
                if (row == -1) return;
                
                // Tránh nhầm với click vào nút Xóa ở cột 0
                if (e.getClickCount() == 2 && col != -1) {
                    thucHienSuaGia(row);
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

    private void thucHienTimKiem(String keyword) {
        model.setRowCount(0);
        Vector<Vector<Object>> data = giaBan_dao.searchBangGiaHeader(keyword);
        
        if (data.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy bảng giá nào phù hợp!");
            loadDataToTable(); 
        } else {
            for (Vector<Object> row : data) {
                model.addRow(row);
            }
        }
    }

    public void loadDataToTable() {
        model.setRowCount(0);
        Vector<Vector<Object>> data = giaBan_dao.searchBangGiaHeader(""); 
        for (Vector<Object> row : data) {
            model.addRow(row);
        }
    }

    private void thucHienSuaGia(int row) {
        String maBG = model.getValueAt(row, 0).toString();
        
        GUIDashBoard dashBoard = (GUIDashBoard) SwingUtilities.getWindowAncestor(this);
        ThucDonPanel pnlThucDon = dashBoard.getPnlThucDon();
        
        new SuaGiaBanDialog(dashBoard, this, maBG, pnlThucDon).setVisible(true);
    }

    private void thucHienXoaGiaBan(int row) {
        String maBG = model.getValueAt(row, 0).toString();
        String moTa = model.getValueAt(row, 1).toString();

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Bạn có chắc chắn muốn xóa Bảng giá [" + maBG + " - " + moTa + "]?\nThao tác này sẽ xóa toàn bộ chi tiết giá bên trong!", 
            "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (giaBan_dao.deleteGiaBan(maBG)) {
                JOptionPane.showMessageDialog(this, "Xóa thành công!");
                loadDataToTable();
                GUIDashBoard dashBoard = (GUIDashBoard) SwingUtilities.getWindowAncestor(this);
                dashBoard.getPnlThucDon().loadDataFromDatabase();
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
                table.getColumnModel().getColumn(i).setCellRenderer(new ItemWithDeleteRenderer());
                table.getColumnModel().getColumn(i).setCellEditor(new ItemWithDeleteEditor(new JCheckBox()));
                table.getColumnModel().getColumn(i).setPreferredWidth(120);
            } else if (i == 1) {
                table.getColumnModel().getColumn(i).setPreferredWidth(250);
            } else if (i == 4) { // Trạng thái
                table.getColumnModel().getColumn(i).setCellRenderer(new DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                        
                        String originalStatus = value != null ? value.toString() : "";
                        String displayStatus = originalStatus;

                        // --- THUẬT TOÁN ĐỔI TRẠNG THÁI ẢO DỰA VÀO NGÀY KẾT THÚC ---
                        Object endDateObj = table.getValueAt(row, 3);
                        if (endDateObj != null && !endDateObj.toString().isEmpty()) {
                            try {
                                String endDateStr = endDateObj.toString();
                                java.time.LocalDate endDate = null;
                                
                                // Hỗ trợ cả 2 định dạng (có xuyệt hoặc có gạch ngang)
                                if (endDateStr.contains("-")) {
                                    endDate = java.time.LocalDate.parse(endDateStr, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                                } else if (endDateStr.contains("/")) {
                                    endDate = java.time.LocalDate.parse(endDateStr, java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                                }

                                // Nếu ngày kết thúc đã qua so với ngày hôm nay -> Đổi ảo thành "Quá Ngày"
                                if (endDate != null && endDate.isBefore(java.time.LocalDate.now())) {
                                    displayStatus = "Quá Ngày";
                                }
                            } catch (Exception ex) {
                                // Bỏ qua lỗi ép kiểu
                            }
                        }

                        // Đổ chữ mới (displayStatus) ra màn hình thay vì chữ cũ
                        Component c = super.getTableCellRendererComponent(table, displayStatus, isSelected, hasFocus, row, column);
                        setHorizontalAlignment(JLabel.CENTER);
                        
                        // Cấu hình màu sắc
                        if (displayStatus.equals("Quá Ngày")) {
                            setForeground(Color.RED);
                            setFont(getFont().deriveFont(Font.BOLD));
                        } else if (displayStatus.equals("Đang áp dụng")) {
                            setForeground(isSelected ? new Color(0, 100, 0) : new Color(0, 153, 0)); 
                            setFont(getFont().deriveFont(Font.BOLD));
                        } else {
                            setForeground(Color.RED);
                        }
                        
                        return c;
                    }
                });
            } else {
                table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }
    }

    // --- RENDERER & EDITOR (Dấu x đỏ nhỏ góc trái trên) ---
    class ItemWithDeleteRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
        private final JLabel lblDelete = new JLabel("x");
        private final JLabel lblText = new JLabel();

        public ItemWithDeleteRenderer() {
            setLayout(new BorderLayout(5, 0));
            setBorder(new EmptyBorder(0, 10, 0, 0));
            lblDelete.setForeground(Color.RED);
            lblDelete.setFont(new Font("Arial", Font.BOLD, 15)); 
            lblDelete.setCursor(new Cursor(Cursor.HAND_CURSOR));
            lblText.setFont(new Font("Segoe UI", Font.BOLD, 15)); 
            add(lblDelete, BorderLayout.WEST); 
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
                thucHienXoaGiaBan(currentRow);
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