package gui;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GUIDashBoard extends JFrame {

    private final Color SIDEBAR_BG = new Color(255, 246, 246); 
    private final Color MAIN_BG = Color.WHITE;
    private final Color ACTIVE_PINK = new Color(255, 182, 182); 
    private final Color TEXT_DARK = new Color(70, 70, 70);
    private final Color TEXT_MUTED = new Color(120, 120, 120);
    
    private JPanel mainContentPanel; 
    private CardLayout cardLayout;
    private List<JButton> menuButtons = new ArrayList<>();

    // Lưu tham chiếu các Panel để gọi hàm load dữ liệu Real-time
    private ThucDonPanel pnlThucDon;
    private QuanLyGiaBanPanel pnlGiaBan;
    private SoDoBanPanel pnlSoDoBan; // Thêm tham chiếu cho Sơ đồ bàn

    public GUIDashBoard() {
        try { UIManager.setLookAndFeel(new FlatLightLaf()); } catch (Exception e) {}

        setTitle("Tuấn Trường Restaurant System - Admin");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1350, 850);
        setLocationRelativeTo(null);

        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBackground(MAIN_BG);
        setContentPane(contentPane);

        // --- 1. SIDEBAR ---
        JPanel sidebar = new JPanel(new BorderLayout()); 
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(280, 850));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(230, 230, 230)));
        contentPane.add(sidebar, BorderLayout.WEST);

        JPanel menuContainer = new JPanel();
        menuContainer.setOpaque(false);
        menuContainer.setLayout(new BoxLayout(menuContainer, BoxLayout.Y_AXIS));
        
        menuContainer.add(createBrandPanel());

        // Thêm Menu và định danh CardName (Lưu ý: "SoDoBan" phải khớp ở cả 2 nơi)
        addMenu(menuContainer, "Nhân viên", "icons/staff.png", "NhanVien");
        addMenu(menuContainer, "Thực đơn", "icons/menu (1).png", "ThucDon"); 
        addMenu(menuContainer, "Bảng giá", "icons/list.png", "BangGia");
        addMenu(menuContainer, "Sơ đồ bàn", "icons/seating.png", "SoDoBan");
        addMenu(menuContainer, "Khuyến mãi", "icons/promotion.png", "KhuyenMai");
        addMenu(menuContainer, "Thống kê", "icons/analysis.png", "ThongKe");

        sidebar.add(menuContainer, BorderLayout.NORTH);
        sidebar.add(createLogoutPanel(), BorderLayout.SOUTH);

        // --- 2. MAIN CONTENT AREA (Sử dụng CardLayout) ---
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setOpaque(false);

        // Khởi tạo các Panel
        pnlThucDon = new ThucDonPanel();
        pnlGiaBan = new QuanLyGiaBanPanel();
        pnlSoDoBan = new SoDoBanPanel(); // Khởi tạo Sơ đồ bàn

        // Thêm vào CardLayout với định danh CardName khớp với nút Menu
        mainContentPanel.add(new NhanVienPanel(), "NhanVien");
        mainContentPanel.add(new ThongKePanel(), "ThongKe");   
        mainContentPanel.add(pnlThucDon, "ThucDon"); 
        mainContentPanel.add(new KhuyenMaiPanel(), "KhuyenMai");
        mainContentPanel.add(pnlGiaBan, "BangGia");
        mainContentPanel.add(pnlSoDoBan, "SoDoBan"); // Đã sửa từ "Ban" thành "SoDoBan"
        
        contentPane.add(mainContentPanel, BorderLayout.CENTER);
        
        // Mặc định hiện Thực đơn
        cardLayout.show(mainContentPanel, "ThucDon");
        setActiveMenu("Thực đơn");
    }

    private void addMenu(JPanel container, String text, String iconPath, String cardName) {
        JButton btn = new JButton(text);
        btn.setIcon(getIconFromFile(iconPath, 20));
        btn.setMaximumSize(new Dimension(240, 45));
        btn.setPreferredSize(new Dimension(240, 45));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btn.setForeground(TEXT_MUTED);
        btn.setBackground(SIDEBAR_BG);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setIconTextGap(15);
        btn.setMargin(new Insets(0, 20, 0, 0));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        menuButtons.add(btn);
        
        // SỰ KIỆN CLICK MENU: CHUYỂN TRANG & CẬP NHẬT DỮ LIỆU
        btn.addActionListener(e -> {
            cardLayout.show(mainContentPanel, cardName);
            setActiveMenu(text);

            // Tự động làm mới dữ liệu Real-time dựa trên CardName
            if (cardName.equals("ThucDon")) {
                pnlThucDon.loadDataFromDatabase(); 
            } else if (cardName.equals("BangGia")) {
                pnlGiaBan.loadDataToTable();
//            } else if (cardName.equals("SoDoBan")) {
//                pnlSoDoBan.loadData(""); // Làm mới sơ đồ bàn khi vừa nhấn vào
            }
        });
        
        container.add(btn);
        container.add(Box.createRigidArea(new Dimension(0, 8)));
    }

    private void setActiveMenu(String activeText) {
        for (JButton btn : menuButtons) {
            if (btn.getText().equals(activeText)) {
                btn.setBackground(ACTIVE_PINK);
                btn.setForeground(TEXT_DARK);
                btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
            } else {
                btn.setBackground(SIDEBAR_BG);
                btn.setForeground(TEXT_MUTED);
                btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            }
        }
    }

    private JPanel createBrandPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(40, 10, 40, 10));
        GridBagConstraints g = new GridBagConstraints();
        
        g.gridx = 0; g.gridy = 0; g.gridheight = 2; g.insets = new Insets(0, 0, 0, 12);
        p.add(new JLabel(getIconFromFile("icons/logoNHTT.png", 55)), g);
        
        g.gridx = 1; g.gridheight = 1; g.insets = new Insets(0, 0, 0, 0);
        JLabel l1 = new JLabel("Nhà hàng");
        l1.setForeground(new Color(220, 100, 100));
        l1.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        p.add(l1, g);
        
        g.gridy = 1;
        JLabel l2 = new JLabel("TUẤN TRƯỜNG");
        l2.setForeground(new Color(220, 50, 50));
        l2.setFont(new Font("Segoe UI", Font.BOLD, 17));
        p.add(l2, g);
        return p;
    }

    private JPanel createLogoutPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(15, 25, 30, 25));
        
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(230, 1));
        sep.setForeground(new Color(220, 220, 220));
        
        JLabel name = new JLabel("QUẢN LÝ: TUẤN TRƯỜNG");
        name.setIcon(getIconFromFile("icons/user.png", 18));
        name.setForeground(TEXT_DARK);
        name.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        name.setBorder(new EmptyBorder(15, 0, 15, 0));
        
        JButton btnLogout = new JButton(" Đăng xuất");
        btnLogout.setIcon(getIconFromFile("icons/logouttt.png", 18));
        btnLogout.setBackground(SIDEBAR_BG);
        btnLogout.setContentAreaFilled(false);
        btnLogout.setHorizontalAlignment(SwingConstants.LEFT);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnLogout.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(this, "Xác nhận đăng xuất?", "Thoát", 0) == 0) {
                this.dispose(); 
                new GUITaiKhoan().setVisible(true); // Mở lại màn hình đăng nhập
            }
        });
        
        p.add(sep);
        p.add(name);
        p.add(btnLogout);
        return p;
    }

    public ThucDonPanel getPnlThucDon() {
        return this.pnlThucDon;
    }

    private ImageIcon getIconFromFile(String path, int size) {
        File f = new File(path);
        if (f.exists()) {
            Image img = new ImageIcon(path).getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        }
        return null;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GUIDashBoard().setVisible(true));
    }
}