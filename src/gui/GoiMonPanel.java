package gui;

import dao.MonAn_DAO;
import entity.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.File;
import java.text.NumberFormat;
import java.util.*;
import java.util.List;

public class GoiMonPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private Ban ban;

    private JPanel pnlMenu;
    private JTextField txtSearch;

    private JPanel pnlOrder;
    private JLabel lblTongTien = new JLabel("0 đ");

    private List<DonGoiMon> dsOrder = new ArrayList<>();
    private List<MonAn> dsMon = new ArrayList<>();

    private MonAn_DAO monAnDAO = new MonAn_DAO();

    public GoiMonPanel(Ban ban) {
        this.ban = ban;

        setLayout(new BorderLayout(10,10));
        setBorder(BorderFactory.createEmptyBorder(15,15,15,15));

        // ===== SEARCH =====
        txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(200, 40));
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm theo tên hoặc mã món...");

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { loadMenu(); }
            public void removeUpdate(DocumentEvent e) { loadMenu(); }
            public void changedUpdate(DocumentEvent e) { loadMenu(); }
        });

        add(txtSearch, BorderLayout.NORTH);

        // ===== MENU LEFT =====
        pnlMenu = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        JScrollPane scrollMenu = new JScrollPane(pnlMenu);
        scrollMenu.getVerticalScrollBar().setUnitIncrement(16); // Lăn chuột mượt hơn

        // ===== ORDER RIGHT =====
        pnlOrder = new JPanel();
        pnlOrder.setLayout(new BoxLayout(pnlOrder, BoxLayout.Y_AXIS));

        JScrollPane scrollOrder = new JScrollPane(pnlOrder);

        JButton btnTang = new JButton("+");
        JButton btnGiam = new JButton("-");
        JButton btnXoa = new JButton("Xóa");
        btnXoa.setBackground(new Color(220, 53, 69)); 
        btnXoa.setForeground(Color.WHITE);
        JButton btnSave = new JButton("Lưu");
        btnSave.setBackground(new Color(0, 123, 255));
        btnSave.setForeground(Color.WHITE);
        JButton btnHuy = new JButton("Hủy");
        btnHuy.setBackground(Color.GRAY);
        btnHuy.setForeground(Color.WHITE);

        btnSave.addActionListener(e -> saveToDB());
        btnHuy.addActionListener(e -> {
            dsOrder.clear();
            refresh();
        });

        JPanel pnlControl = new JPanel();
        pnlControl.add(btnTang);
        pnlControl.add(btnGiam);
        pnlControl.add(btnXoa);
        pnlControl.add(btnSave);
        pnlControl.add(btnHuy);

        JPanel pnlRight = new JPanel(new BorderLayout());
        pnlRight.add(new JLabel("Đơn bàn: " + ban.getTenBan()), BorderLayout.NORTH);
        pnlRight.add(scrollOrder, BorderLayout.CENTER);
        pnlRight.add(pnlControl, BorderLayout.SOUTH);
        pnlRight.add(lblTongTien, BorderLayout.SOUTH);

        // ===== SPLIT =====
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollMenu, pnlRight);
        split.setDividerLocation(650);

        add(split, BorderLayout.CENTER);

        loadMenu();
    }

    // ===== LOAD MENU =====
    private void loadMenu() {
        pnlMenu.removeAll();

        dsMon = monAnDAO.getAllMonAnWithActivePrice();
        String keyword = txtSearch.getText().trim().toLowerCase();

        for (MonAn m : dsMon) {
            // filter theo tên hoặc mã
            if (!m.getTenMon().toLowerCase().contains(keyword)
                    && !m.getMaMon().toLowerCase().contains(keyword)) continue;
            if (m.getGiaBan() <= 0) continue;
            
            pnlMenu.add(createMonCard(m));
        }

        pnlMenu.revalidate();
        pnlMenu.repaint();
    }

    // ===== CARD =====
    private JPanel createMonCard(MonAn m) {
        JPanel card = new JPanel(new BorderLayout());
        card.setPreferredSize(new Dimension(160, 170));
        card.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // ===== IMAGE CẬP NHẬT MỚI =====
        JLabel lblImg = new JLabel();
        lblImg.setHorizontalAlignment(SwingConstants.CENTER);

        String imgName = m.getHinhAnh();
        boolean loaded = false;

        if (imgName != null && !imgName.trim().isEmpty()) {
            // 1. Thử tìm trong thư mục images/ của project
            File f = new File("images/" + imgName);
            
            // 2. Nếu không thấy, thử tìm theo đường dẫn tuyệt đối (nếu DB lưu nguyên C:/...)
            if (!f.exists()) {
                f = new File(imgName);
            }
            
            if (f.exists()) {
                ImageIcon icon = new ImageIcon(f.getAbsolutePath());
                Image img = icon.getImage().getScaledInstance(90, 90, Image.SCALE_SMOOTH);
                lblImg.setIcon(new ImageIcon(img));
                loaded = true;
            }
        }
        
        if (!loaded) {
            lblImg.setText("No Image");
            lblImg.setForeground(Color.GRAY);
        }

        // ===== TEXT =====
        JLabel lblName = new JLabel(m.getTenMon(), SwingConstants.CENTER);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JLabel lblPrice = new JLabel(formatPrice(m.getGiaBan()), SwingConstants.CENTER);
        lblPrice.setForeground(new Color(220, 50, 50));

        JPanel pnlBottom = new JPanel(new GridLayout(2,1));
        pnlBottom.setBackground(Color.WHITE);
        card.setBackground(Color.WHITE);
        
        pnlBottom.add(lblName);
        pnlBottom.add(lblPrice);

        card.add(lblImg, BorderLayout.CENTER);
        card.add(pnlBottom, BorderLayout.SOUTH);

        // CLICK ADD
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                addMon(m);
            }
        });

        return card;
    }

    // ===== FORMAT GIÁ =====
    private String formatPrice(double price) {
        if (price <= 0) return "Chưa có giá";
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        return nf.format(price) + " đ";
    }

    // ===== ADD =====
    private void addMon(MonAn m) {
        for (DonGoiMon item : dsOrder) {
            if (item.getTenMon().equals(m.getTenMon())) {
                item.tangSL();
                refresh();
                return;
            }
        }
        dsOrder.add(new DonGoiMon(m.getTenMon(), 1, m.getGiaBan()));
        refresh();
    }

    private void refresh() {
        pnlOrder.removeAll();

        for (DonGoiMon item : dsOrder) {
            pnlOrder.add(createOrderItem(item));
        }

        pnlOrder.revalidate();
        pnlOrder.repaint();
        updateTotal();
    }

    private JPanel createOrderItem(DonGoiMon item) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        JLabel lblName = new JLabel(item.getTenMon());

        JButton btnMinus = new JButton("-");
        JButton btnPlus = new JButton("+");
        JButton btnDelete = new JButton("X");

        JLabel lblSL = new JLabel(String.valueOf(item.getSoLuong()));
        lblSL.setHorizontalAlignment(SwingConstants.CENTER);
        lblSL.setPreferredSize(new Dimension(30, 30));

        // STYLE
        btnMinus.setBackground(Color.LIGHT_GRAY);
        btnPlus.setBackground(new Color(40,167,69));
        btnPlus.setForeground(Color.WHITE);

        btnDelete.setBackground(new Color(220,53,69));
        btnDelete.setForeground(Color.WHITE);

        // EVENT
        btnPlus.addActionListener(e -> {
            item.tangSL();
            refresh();
        });

        btnMinus.addActionListener(e -> {
            item.giamSL();
            if (item.getSoLuong() <= 0) dsOrder.remove(item);
            refresh();
        });

        btnDelete.addActionListener(e -> {
            dsOrder.remove(item);
            refresh();
        });

        JPanel pnlRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        pnlRight.add(btnMinus);
        pnlRight.add(lblSL);
        pnlRight.add(btnPlus);
        pnlRight.add(btnDelete);

        p.add(lblName, BorderLayout.WEST);
        p.add(pnlRight, BorderLayout.EAST);

        return p;
    }

    private void updateTotal() {
        double sum = 0;
        for (DonGoiMon d : dsOrder) {
            sum += d.getSoLuong() * d.getGia();
        }
        lblTongTien.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTongTien.setForeground(new Color(220, 53, 69));
        lblTongTien.setBorder(new EmptyBorder(10, 0, 0, 0));
        lblTongTien.setText("Tổng: " + formatPrice(sum));
    }
    
    // ===== SAVE =====
    private void saveToDB() {
        if (dsOrder.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chưa có món!");
            return;
        }

        System.out.println("Lưu đơn bàn: " + ban.getMaBan());
        for (DonGoiMon d : dsOrder) {
            System.out.println(d);
        }

        JOptionPane.showMessageDialog(this, "Đã lưu đơn gọi món!");
    }
}