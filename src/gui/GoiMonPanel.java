package gui;

import dao.ChiTietDatMon_DAO;
import dao.DonDatMon_DAO;
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
    private NhanVien nv;

    private JPanel pnlMenu;
    private JTextField txtSearch;

    private JPanel pnlOrder;
    private JLabel lblTongTien = new JLabel("0 đ");

    private JTextArea txtGhiChu; 

    private List<DonGoiMon> dsOrder = new ArrayList<>();
    private List<MonAn> dsMon = new ArrayList<>();

    private MonAn_DAO monAnDAO = new MonAn_DAO();

    public GoiMonPanel(Ban ban, NhanVien nv) {
        this.ban = ban;
        this.nv = nv; 
        
        setLayout(new BorderLayout(10,10));
        setBorder(BorderFactory.createEmptyBorder(15,15,15,15));

        // ===== SEARCH =====
        txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(200, 40));
        txtSearch.setToolTipText("Tìm theo tên hoặc mã món...");

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { loadMenu(); }
            public void removeUpdate(DocumentEvent e) { loadMenu(); }
            public void changedUpdate(DocumentEvent e) { loadMenu(); }
        });

        add(txtSearch, BorderLayout.NORTH);

        // ===== MENU =====
        pnlMenu = new JPanel(new GridLayout(0, 4, 10, 15)); 
        pnlMenu.setBackground(Color.WHITE);
        
        JPanel pnlMenuWrapper = new JPanel(new BorderLayout());
        pnlMenuWrapper.setBackground(Color.WHITE);
        pnlMenuWrapper.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        pnlMenuWrapper.add(pnlMenu, BorderLayout.NORTH);

        JScrollPane scrollMenu = new JScrollPane(pnlMenuWrapper);
        scrollMenu.setBorder(null);
        scrollMenu.getVerticalScrollBar().setUnitIncrement(20);
        scrollMenu.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // ===== ORDER =====
        pnlOrder = new JPanel();
        pnlOrder.setLayout(new BoxLayout(pnlOrder, BoxLayout.Y_AXIS));
        JScrollPane scrollOrder = new JScrollPane(pnlOrder);

        // ===== BUTTON =====
        JButton btnConfirm = new JButton("Xác nhận");
        btnConfirm.setBackground(new Color(40,167,69));
        btnConfirm.setForeground(Color.WHITE);

        JButton btnHuy = new JButton("Hủy");
        btnHuy.setBackground(Color.GRAY);
        btnHuy.setForeground(Color.WHITE);

        // ===== EVENT =====
        btnConfirm.addActionListener(e -> {
            if (dsOrder.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Chưa có món!");
                return;
            }

            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Xác nhận đẩy đơn xuống bếp?",
                    "Confirm",
                    JOptionPane.YES_NO_OPTION
            );

            if (choice == JOptionPane.YES_OPTION) {
                saveToDB();
                dsOrder.clear();
                txtGhiChu.setText(""); 
                refresh();
            }
        });

        btnHuy.addActionListener(e -> {

            if (dsOrder.isEmpty()) {
                Window window = SwingUtilities.getWindowAncestor(this);
                if (window != null) window.dispose();
                return;
            }

            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Hủy toàn bộ món?",
                    "Xác nhận",
                    JOptionPane.YES_NO_OPTION
            );

            if (choice == JOptionPane.YES_OPTION) {
                dsOrder.clear();
                txtGhiChu.setText(""); 
                refresh();
            }
        });

        JPanel pnlControl = new JPanel();
        pnlControl.add(btnConfirm);
        pnlControl.add(btnHuy);

        // ===== GHI CHÚ =====
        txtGhiChu = new JTextArea(3, 20);
        txtGhiChu.setLineWrap(true);
        txtGhiChu.setWrapStyleWord(true);
        txtGhiChu.setBorder(BorderFactory.createTitledBorder("Ghi chú phiếu bếp"));

        JScrollPane scrollNote = new JScrollPane(txtGhiChu);
        scrollNote.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        // ===== RIGHT PANEL =====
        JPanel pnlRight = new JPanel(new BorderLayout());
        
        JLabel lblOrderHeader = new JLabel("  Mã đơn: [Tạo tự động]    |    Mã bàn: " + ban.getMaBan());
        lblOrderHeader.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblOrderHeader.setOpaque(true);
        lblOrderHeader.setBackground(new Color(244, 182, 169)); 
        lblOrderHeader.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 5));
        
        pnlRight.add(lblOrderHeader, BorderLayout.NORTH);
        pnlRight.add(scrollOrder, BorderLayout.CENTER);
        pnlRight.setBackground(new Color(243, 194, 190));

        JPanel pnlBottomRight = new JPanel();
        pnlBottomRight.setLayout(new BoxLayout(pnlBottomRight, BoxLayout.Y_AXIS));

        lblTongTien.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTongTien.setForeground(new Color(220, 53, 69));

        lblTongTien.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollNote.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlControl.setAlignmentX(Component.LEFT_ALIGNMENT);

        pnlBottomRight.add(lblTongTien);
        pnlBottomRight.add(Box.createVerticalStrut(5));
        pnlBottomRight.add(scrollNote);
        pnlBottomRight.add(Box.createVerticalStrut(5));
        pnlBottomRight.add(pnlControl);

        pnlRight.add(pnlBottomRight, BorderLayout.SOUTH);

        // ===== SPLIT =====
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollMenu, pnlRight);
        split.setDividerLocation(680);

        add(split, BorderLayout.CENTER);

        loadMenu();
    }

    // ===== LOAD MENU =====
    private void loadMenu() {
        pnlMenu.removeAll();

        dsMon = monAnDAO.getAllMonAnWithActivePrice();
        String keyword = txtSearch.getText().trim().toLowerCase();

        for (MonAn m : dsMon) {
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
        card.setPreferredSize(new Dimension(145, 170));
        card.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        // --- ĐÃ SỬA: Gộp thành 1 dòng duy nhất và thêm chữ 'final' ---
        final boolean isTamNgung = (m.getTrangThai() != null && m.getTrangThai().equalsIgnoreCase("Tạm ngưng"));

        if (isTamNgung) {
            card.setBackground(new Color(240, 240, 240)); 
        } else {
            card.setCursor(new Cursor(Cursor.HAND_CURSOR));
            card.setBackground(Color.WHITE);
        }

        JLabel lblImg = new JLabel("", SwingConstants.CENTER);

        String imgName = m.getHinhAnh();
        if (imgName != null) {
            File f = new File("images/" + imgName);
            if (!f.exists()) f = new File(imgName);

            if (f.exists()) {
                ImageIcon icon = new ImageIcon(f.getAbsolutePath());
                Image img = icon.getImage().getScaledInstance(90, 90, Image.SCALE_SMOOTH);
                lblImg.setIcon(new ImageIcon(img));
            } else {
                lblImg.setText("No Image");
            }
        }

        JLabel lblName = new JLabel(m.getTenMon(), SwingConstants.CENTER);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JLabel lblPrice = new JLabel("", SwingConstants.CENTER);
        
        if (isTamNgung) {
            lblPrice.setText("Tạm ngưng");
            lblPrice.setForeground(Color.RED);
            lblPrice.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblName.setForeground(Color.GRAY); 
            lblImg.setEnabled(false); 
        } else {
            lblPrice.setText(formatPrice(m.getGiaBan()));
            lblPrice.setForeground(new Color(220,50,50));
        }

        JPanel pnlBottom = new JPanel(new GridLayout(2,1));
        pnlBottom.setBackground(isTamNgung ? new Color(240, 240, 240) : Color.WHITE);
        pnlBottom.add(lblName);
        pnlBottom.add(lblPrice);

        card.add(lblImg, BorderLayout.CENTER);
        card.add(pnlBottom, BorderLayout.SOUTH);

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (isTamNgung) {
                    JOptionPane.showMessageDialog(card, "Món này hiện đang tạm ngưng phục vụ (Hết nguyên liệu)!", "Hết hàng", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                addMon(m);
            }
        });

        return card;
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
        dsOrder.add(new DonGoiMon(
                m.getMaMon(),
                m.getTenMon(),
                1,
                m.getGiaBan()
        ));
        refresh();
    }

    // ===== REFRESH =====
    private void refresh() {
        pnlOrder.removeAll();

        for (DonGoiMon item : dsOrder) {
            pnlOrder.add(createOrderItem(item));
        }

        pnlOrder.revalidate();
        pnlOrder.repaint();
        updateTotal();
    }

    // ===== ORDER ITEM =====
    private JPanel createOrderItem(DonGoiMon item) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel lblName = new JLabel(item.getTenMon());

        JButton btnMinus = new JButton("-");
        JButton btnPlus = new JButton("+");
        JButton btnDelete = new JButton("X");

        JLabel lblSL = new JLabel(String.valueOf(item.getSoLuong()), SwingConstants.CENTER);
        lblSL.setPreferredSize(new Dimension(30, 30));


        btnPlus.setBackground(new Color(40,167,69));
        btnPlus.setForeground(Color.WHITE);

        btnDelete.setBackground(new Color(220,53,69));
        btnDelete.setForeground(Color.WHITE);

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

    // ===== TOTAL =====
    private void updateTotal() {
        double sum = 0;
        for (DonGoiMon d : dsOrder) {
            sum += d.getSoLuong() * d.getGia();
        }

        lblTongTien.setText("Tổng: " + formatPrice(sum));
    }

    private String formatPrice(double price) {
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        return nf.format(price) + " đ";
    }

    // ===== SAVE =====
    private void saveToDB() {
        String ghiChu = txtGhiChu.getText().trim();

        DonDatMon_DAO donDAO = new DonDatMon_DAO();
        ChiTietDatMon_DAO ctDAO = new ChiTietDatMon_DAO();
        
        if (this.nv == null || this.nv.getMaNV() == null) {
            JOptionPane.showMessageDialog(this, "Lỗi mất Session! Không tìm thấy mã nhân viên.");
            return;
        }

        String maNV = this.nv.getMaNV();
        String maDon = donDAO.createDon(ban.getMaBan(), maNV ,ghiChu);

        for (DonGoiMon d : dsOrder) {
            ctDAO.insertChiTiet(maDon, d);
        }

        JOptionPane.showMessageDialog(this, "Đã tách phiếu mới gửi xuống bếp!");

        Window w = SwingUtilities.getWindowAncestor(this);
        if (w != null) w.dispose();
    }
}