package gui;

import dao.MonAn_DAO;
import entity.MonAn;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ThucDonPanel extends JPanel {

    // --- HỆ MÀU CHUẨN GIAO DIỆN ---
    private final Color BG_COLOR = new Color(250, 248, 248);
    private final Color TIME_BANNER_BG = new Color(253, 242, 242); 
    private final Color RED_ACCENT = new Color(255, 102, 102); 
    private final Color TEXT_DARK = new Color(50, 50, 50);
    private final Color DIM_BG = new Color(235, 235, 235);
    private final Color DIM_TEXT = new Color(150, 150, 150);
    private final Color BTN_ADD_YELLOW = new Color(255, 209, 102); 

    private JPanel productGrid;
    private List<MonAn> allFoods = new ArrayList<>(); 
    private List<JButton> categoryButtons = new ArrayList<>();
    private JTextField txtSearch;
    private JComboBox<String> cbPriceFilter; // Thêm biến Combobox lọc giá
    
    private MonAn_DAO monAn_dao = new MonAn_DAO();

    public ThucDonPanel() {
        setLayout(new BorderLayout());
        setBackground(BG_COLOR);
        setBorder(new EmptyBorder(20, 25, 20, 25));

        JPanel topWrapper = new JPanel();
        topWrapper.setLayout(new BoxLayout(topWrapper, BoxLayout.Y_AXIS));
        topWrapper.setOpaque(false);

        // --- TẦNG 1: BANNER THỜI GIAN ---
        JPanel timeBanner = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
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
        topWrapper.add(Box.createRigidArea(new Dimension(0, 15)));

        // --- TẦNG 2: TIÊU ĐỀ & NÚT THÊM MÓN ---
        JPanel titleActionPanel = new JPanel(new BorderLayout());
        titleActionPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("QUẢN LÝ THỰC ĐƠN");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(TEXT_DARK);
        titleActionPanel.add(lblTitle, BorderLayout.WEST);

        JButton btnAdd = new JButton("+ Thêm món");
        btnAdd.setBackground(BTN_ADD_YELLOW);
        btnAdd.setForeground(Color.BLACK);
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAdd.setPreferredSize(new Dimension(150, 42)); 
        btnAdd.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAdd.setFocusPainted(false);
        btnAdd.putClientProperty("JButton.buttonType", "roundRect");
        btnAdd.putClientProperty("JButton.arc", 15);
        
        btnAdd.addActionListener(e -> {
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            new ThemMonAnDialog(parentFrame, this).setVisible(true);
        });

        titleActionPanel.add(btnAdd, BorderLayout.EAST);
        topWrapper.add(titleActionPanel);
        topWrapper.add(Box.createRigidArea(new Dimension(0, 15)));

        // --- TẦNG 3: BỘ LỌC & TÌM KIẾM ---
        JPanel filterSearchPanel = new JPanel(new BorderLayout());
        filterSearchPanel.setOpaque(false);

        JPanel filterGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterGroup.setOpaque(false);

        String[] categories = {"Tất cả", "Khai vị", "Món chính", "Món nước", "Tráng miệng"};
        for (String cat : categories) {
            JButton btnCat = new JButton(cat);
            btnCat.setPreferredSize(new Dimension(110, 38));
            btnCat.setFont(new Font("Segoe UI", Font.BOLD, 13));
            btnCat.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnCat.setFocusPainted(false);
            
            btnCat.putClientProperty("JButton.buttonType", "roundRect");
            btnCat.putClientProperty("JButton.arc", 15);
            
            updateButtonStyle(btnCat, cat.equals("Tất cả"));
            
            btnCat.addActionListener(e -> {
                updateCategoryButtons(btnCat);
                thucHienLocVaTimKiem();
            });
            categoryButtons.add(btnCat);
            filterGroup.add(btnCat);
        }

        // --- MỚI: THÊM COMBOBOX LỌC GIÁ KẾ BÊN NÚT TRÁNG MIỆNG ---
        filterGroup.add(Box.createRigidArea(new Dimension(5, 0))); // Khoảng cách nhỏ
        cbPriceFilter = new JComboBox<>(new String[]{"Tất cả giá", "Đã thiết lập", "Chờ thiết lập"});
        cbPriceFilter.setPreferredSize(new Dimension(140, 38));
        cbPriceFilter.setFont(new Font("Segoe UI", Font.BOLD, 13));
        cbPriceFilter.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cbPriceFilter.addActionListener(e -> thucHienLocVaTimKiem()); // Khi chọn thì tự động lọc
        filterGroup.add(cbPriceFilter);

        JPanel searchGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        searchGroup.setOpaque(false);
        txtSearch = new JTextField(15);
        txtSearch.setPreferredSize(new Dimension(220, 40));
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        // --- ĐÃ SỬA: Xóa icon kính lúp để tránh lỗi ô vuông ---
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm tên món...");
        
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { thucHienLocVaTimKiem(); }
            @Override public void removeUpdate(DocumentEvent e) { thucHienLocVaTimKiem(); }
            @Override public void changedUpdate(DocumentEvent e) { thucHienLocVaTimKiem(); }
        });

        searchGroup.add(txtSearch); 
        filterSearchPanel.add(filterGroup, BorderLayout.WEST);
        filterSearchPanel.add(searchGroup, BorderLayout.EAST);

        topWrapper.add(filterSearchPanel);
        topWrapper.add(Box.createRigidArea(new Dimension(0, 15)));
        add(topWrapper, BorderLayout.NORTH);

        // --- CENTER: LƯỚI DANH SÁCH MÓN ---
        productGrid = new JPanel(new GridLayout(0, 3, 25, 25)); 
        productGrid.setOpaque(false);
        
        JPanel gridWrapper = new JPanel(new BorderLayout());
        gridWrapper.setOpaque(false);
        gridWrapper.add(productGrid, BorderLayout.NORTH); 

        JScrollPane scrollPane = new JScrollPane(gridWrapper); 
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        loadDataFromDatabase();
    }

    public void loadDataFromDatabase() {
        allFoods = monAn_dao.getAllMonAnWithActivePrice(); 
        thucHienLocVaTimKiem();
    }

    // --- CẬP NHẬT LOGIC LỌC ĐỂ KẾT HỢP CẢ LOẠI MÓN, TÌM KIẾM VÀ TRẠNG THÁI GIÁ ---
    private void thucHienLocVaTimKiem() {
        String keyword = txtSearch.getText().trim().toLowerCase();
        
        // Lấy loại món đang được chọn
        String selectedCat = "Tất cả";
        for (JButton btn : categoryButtons) {
            if (btn.getBackground().equals(RED_ACCENT)) {
                selectedCat = btn.getText();
                break;
            }
        }

        // Lấy trạng thái giá đang được chọn
        String priceStatus = cbPriceFilter.getSelectedItem().toString();

        productGrid.removeAll(); 
        for (MonAn food : allFoods) {
            boolean matchesKey = food.getTenMon().toLowerCase().contains(keyword);
            boolean matchesCat = selectedCat.equals("Tất cả") || (food.getLoaiMon() != null && food.getLoaiMon().equalsIgnoreCase(selectedCat));
            
            // Xử lý bộ lọc giá
            boolean matchesPrice = true;
            if (priceStatus.equals("Đã thiết lập")) {
                matchesPrice = food.getGiaBan() > 0;
            } else if (priceStatus.equals("Chờ thiết lập")) {
                matchesPrice = food.getGiaBan() <= 0;
            }

            // Nếu thỏa mãn cả 3 điều kiện (Tên + Loại + Giá) thì mới hiển thị
            if (matchesKey && matchesCat && matchesPrice) {
                productGrid.add(createFoodCard(food));
            }
        }
        productGrid.revalidate();
        productGrid.repaint();
    }

    private JPanel createFoodCard(MonAn food) {
        double gia = food.getGiaBan(); 
        boolean isDimmed = (gia <= 0); 
        String giaStr = (gia > 0) ? String.format("%,.0f VNĐ", gia) : "Chờ thiết lập";

        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isDimmed ? DIM_BG : Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.setColor(new Color(225, 225, 225));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.dispose();
            }
        };
        card.setLayout(new BorderLayout());
        card.setPreferredSize(new Dimension(320, 155)); 
        card.setOpaque(false);
        
        MouseAdapter cardAdapter = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(ThucDonPanel.this);
                    new SuaMonAnDialog(parentFrame, ThucDonPanel.this, food).setVisible(true);
                }
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!isDimmed) card.setBorder(BorderFactory.createLineBorder(RED_ACCENT, 1));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                card.setBorder(null);
            }
        };
        card.addMouseListener(cardAdapter);
        if (!isDimmed) card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS)); 
        textPanel.setOpaque(false);
        textPanel.setBorder(new EmptyBorder(15, 20, 15, 5)); 

        JLabel lblName = new JLabel("<html><div style='width:125px;'>" + food.getTenMon() + "</div></html>");
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblName.setForeground(isDimmed ? DIM_TEXT : TEXT_DARK);
        
        JLabel lblCat = new JLabel("Loại: " + (food.getLoaiMon() != null ? food.getLoaiMon() : "N/A"));
        lblCat.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        lblCat.setForeground(isDimmed ? DIM_TEXT : new Color(120, 120, 120));

        JLabel lblPrice = new JLabel(giaStr);
        lblPrice.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblPrice.setForeground(isDimmed ? DIM_TEXT : new Color(220, 50, 50)); 

        textPanel.add(lblName);
        textPanel.add(Box.createVerticalStrut(6));
        textPanel.add(lblCat);
        textPanel.add(Box.createVerticalGlue());
        textPanel.add(lblPrice);
        card.add(textPanel, BorderLayout.CENTER);

        JPanel imgWrapper = new JPanel(new BorderLayout());
        imgWrapper.setOpaque(false);
        imgWrapper.setBorder(new EmptyBorder(8, 0, 15, 15)); 

        JLabel btnX = new JLabel("x");
        btnX.setForeground(new Color(200, 50, 50));
        btnX.setFont(new Font("Arial", Font.BOLD, 18));
        btnX.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnX.setHorizontalAlignment(SwingConstants.RIGHT);
        btnX.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { xoaMonAn(food.getMaMon(), food.getTenMon()); }
        });

        JLabel lblImg = new JLabel();
        lblImg.setPreferredSize(new Dimension(115, 105));
        lblImg.setHorizontalAlignment(SwingConstants.CENTER);
        ImageIcon icon = getIconFromFile(food.getHinhAnh(), 115, 105);
        if (icon != null) lblImg.setIcon(icon);
        else lblImg.setText("No Image");

        imgWrapper.add(btnX, BorderLayout.NORTH);
        imgWrapper.add(lblImg, BorderLayout.CENTER);
        card.add(imgWrapper, BorderLayout.EAST);

        return card;
    }

    private void xoaMonAn(String id, String name) {
        int confirm = JOptionPane.showConfirmDialog(this, "Xác nhận xóa món '" + name + "'?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (monAn_dao.deleteMonAn(id)) {
                    loadDataFromDatabase(); 
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi xóa: " + ex.getMessage());
            }
        }
    }

    private void updateCategoryButtons(JButton selectedBtn) {
        for (JButton btn : categoryButtons) {
            updateButtonStyle(btn, btn == selectedBtn);
        }
    }

    private void updateButtonStyle(JButton btn, boolean isActive) {
        btn.setBackground(isActive ? RED_ACCENT : Color.WHITE);
        btn.setForeground(isActive ? Color.WHITE : TEXT_DARK);
        
        if (isActive) {
            btn.putClientProperty("FlatLaf.style", "borderColor: " + colorToHex(RED_ACCENT));
        } else {
            btn.putClientProperty("FlatLaf.style", "borderColor: #D7D7D7");
        }
    }
    
    private String colorToHex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    private ImageIcon getIconFromFile(String path, int width, int height) {
        if (path == null || path.isEmpty()) return null;
        File f = new File(path);
        if (f.exists()) {
            Image img = new ImageIcon(path).getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        }
        return null;
    }
}