package gui;

import dao.BanDAO;
import entity.Ban;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class SoDoBanPanel_NVPV extends JPanel {

    private static final long serialVersionUID = 1L;

    private final Color COLOR_TRONG = new Color(40, 167, 69);   
    private final Color COLOR_DUNG = new Color(220, 53, 69);     
    private final Color COLOR_DAT = new Color(255, 193, 7);      
    private final Color COLOR_PINK = new Color(255, 182, 193);   
    private final Color TEXT_DARK = new Color(44, 56, 74);       
    private BanDAO banDAO;
    private JPanel pnlDanhSachBan;
    private JPanel pnlFloorFilter;
    private JTextField txtSearch;
    private JLabel lblTimeDate;
    private Timer timer;

    private String floorFilter = "Tất cả";
    private String statusFilter = "Tất cả";

    public SoDoBanPanel_NVPV() {
        banDAO = new BanDAO();
        initUI();
        loadData("");
    }

    private void initUI() {

        setLayout(new BorderLayout(0, 20)); 
        setBackground(Color.WHITE);
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

        // ĐÃ FIX XUNG ĐỘT GIT Ở ĐÂY: Giữ lại tiêu đề "Gọi món"
        JLabel lblTitle = new JLabel("Gọi món");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(TEXT_DARK);
        titleActionPanel.add(lblTitle, BorderLayout.WEST);

        JPanel btnActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        btnActions.setOpaque(false);

        txtSearch = new JTextField(15);
        txtSearch.setPreferredSize(new Dimension(220, 40));
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm tên bàn...");
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { loadData(txtSearch.getText()); }
            public void removeUpdate(DocumentEvent e) { loadData(txtSearch.getText()); }
            public void changedUpdate(DocumentEvent e) { loadData(txtSearch.getText()); }
        });

        btnActions.add(txtSearch);
        titleActionPanel.add(btnActions, BorderLayout.EAST);

        topWrapper.add(titleActionPanel);
        topWrapper.add(Box.createRigidArea(new Dimension(0, 15)));

        JPanel pnlFilterBar = new JPanel(new BorderLayout());
        pnlFilterBar.setOpaque(false);

        pnlFloorFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlFloorFilter.setOpaque(false);
        String[] floors = {"Tất cả", "Ngoài trời", "Phòng VIP", "Tầng 1", "Tầng 2"};
        for (String f : floors) {
            pnlFloorFilter.add(createFloorButton(f));
        }

        JPanel pnlStatus = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlStatus.setOpaque(false);
        pnlStatus.add(createStatusButton("Tất cả", Color.GRAY));
        pnlStatus.add(createStatusButton("Trống", COLOR_TRONG));
        pnlStatus.add(createStatusButton("Đã đặt", COLOR_DAT));
        pnlStatus.add(createStatusButton("Đang dùng", COLOR_DUNG));

        pnlFilterBar.add(pnlFloorFilter, BorderLayout.WEST);
        pnlFilterBar.add(pnlStatus, BorderLayout.EAST);

        topWrapper.add(pnlFilterBar);

        add(topWrapper, BorderLayout.NORTH);

        pnlDanhSachBan = new JPanel();
        pnlDanhSachBan.setLayout(new BoxLayout(pnlDanhSachBan, BoxLayout.Y_AXIS));
        pnlDanhSachBan.setOpaque(false);
        JScrollPane scrollPane = new JScrollPane(pnlDanhSachBan);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JButton createFloorButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        boolean isActive = text.equals(floorFilter);
        btn.setBackground(isActive ? COLOR_PINK : Color.WHITE);
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.putClientProperty("JButton.buttonType", "roundRect");
        btn.putClientProperty("JButton.arc", 15);
        
        btn.addActionListener(e -> {
            floorFilter = text;
            for (Component c : pnlFloorFilter.getComponents()) {
                if (c instanceof JButton) {
                    JButton otherBtn = (JButton) c;
                    boolean active = otherBtn.getText().equals(floorFilter);
                    otherBtn.setBackground(active ? COLOR_PINK : Color.WHITE);
                }
            }
            loadData(txtSearch.getText());
        });
        return btn;
    }

    private JButton createStatusButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(Color.WHITE);
        btn.setForeground(color); 
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.putClientProperty("JButton.buttonType", "roundRect");
        btn.putClientProperty("JButton.arc", 15);
        btn.putClientProperty("FlatLaf.style", "borderColor: " + colorToHex(color));

        btn.addActionListener(e -> {
            statusFilter = text;
            loadData(txtSearch.getText());
        });
        return btn;
    }
    
    private String colorToHex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    public void loadData(String query) {
        pnlDanhSachBan.removeAll();
        List<Ban> dsAll = banDAO.getAllBan();
        String[] dsKhuVuc = {"Ngoài trời", "Phòng VIP", "Tầng 1", "Tầng 2"};
        for (String khuVuc : dsKhuVuc) {
            if (!floorFilter.equals("Tất cả") && !floorFilter.equalsIgnoreCase(khuVuc)) continue;
            List<Ban> dsTheoKV = dsAll.stream()
                .filter(b -> {
                    String viTriDB = b.getViTri().trim().toLowerCase();
                    if (khuVuc.equals("Ngoài trời")) return viTriDB.contains("ngoài trời");
                    return viTriDB.equalsIgnoreCase(khuVuc);
                })
                .filter(b -> {
                    String q = query.toLowerCase();
                    return b.getTenBan().toLowerCase().contains(q)
                        || b.getMaBan().toLowerCase().contains(q);
                })
                .filter(b -> {
                    if (statusFilter.equals("Tất cả")) return true;
                    String tt = b.getTrangThai().toLowerCase();
                    if (statusFilter.equals("Đang dùng")) return tt.contains("dùng") || tt.contains("vụ") || tt.contains("sử dụng");
                    if (statusFilter.equals("Đã đặt")) return tt.contains("đặt");
                    if (statusFilter.equals("Trống")) return tt.contains("trống");
                    return tt.contains(statusFilter.toLowerCase());
                }).collect(Collectors.toList());
            if (!dsTheoKV.isEmpty()) {
                JLabel lblKV = new JLabel(khuVuc.toUpperCase());
                lblKV.setFont(new Font("Segoe UI", Font.BOLD, 16));
                lblKV.setBorder(new EmptyBorder(10, 5, 10, 0));
                pnlDanhSachBan.add(lblKV);
                
                JPanel pnlGrid = new JPanel(new WrapLayout(FlowLayout.LEFT, 20, 20));
                pnlGrid.setOpaque(false);
                for (Ban b : dsTheoKV) pnlGrid.add(taoTheBan(b));
                pnlDanhSachBan.add(pnlGrid);
            }
        }
        pnlDanhSachBan.revalidate(); 
        pnlDanhSachBan.repaint();
    }

    private JPanel taoTheBan(Ban ban) {
        int width = (ban.getSoGhe() <= 4) ? 165 : (ban.getSoGhe() <= 8) ? 260 : 360;
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25); 
                g2.dispose();
                super.paintComponent(g); 
            }
            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(3f)); 
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 25, 25); 
                g2.dispose();
            }
        };
        card.setOpaque(false); 
        card.setPreferredSize(new Dimension(width, 140));
        card.setBorder(new EmptyBorder(5, 5, 5, 5)); 
        Color bg = COLOR_TRONG; 
        String tt = ban.getTrangThai().toLowerCase();
        if (tt.contains("dùng") || tt.contains("vụ") || tt.contains("sử dụng")) bg = COLOR_DUNG;
        else if (tt.contains("đặt")) bg = COLOR_DAT;
        card.setBackground(bg);
        
        JLabel lblX = new JLabel("X"); 
        lblX.setFont(new Font("Arial", Font.BOLD, 15));
        lblX.setForeground(new Color(255, 255, 255, 180));
        lblX.setHorizontalAlignment(SwingConstants.RIGHT);
        lblX.setBorder(new EmptyBorder(5, 0, 0, 10));
        lblX.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblX.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (JOptionPane.showConfirmDialog(null, "Xóa " + ban.getTenBan() + "?", "Xác nhận", 0) == 0) {
                    if (banDAO.deleteBan(ban.getMaBan())) loadData(txtSearch.getText());
                }
            }
            @Override public void mouseEntered(MouseEvent e) { lblX.setForeground(Color.WHITE); }
            @Override public void mouseExited(MouseEvent e) { lblX.setForeground(new Color(255, 255, 255, 180)); }
        });
        
        // Ghi chú: Tôi vẫn để lại dấu "X" xóa bàn theo code cũ của bạn, 
        // nếu bạn muốn Nhân viên không được quyền xóa bàn thì có thể comment đoạn add(lblX) lại nhé!
        card.add(lblX, BorderLayout.NORTH);
        
        JPanel pnlCenter = new JPanel(new GridLayout(2, 1)); 
        pnlCenter.setOpaque(false);
        JLabel lblTen = new JLabel(ban.getTenBan(), SwingConstants.CENTER); 
        lblTen.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTen.setForeground(bg == COLOR_DAT ? Color.BLACK : Color.WHITE);
        JLabel lblSub = new JLabel(ban.getSoGhe() + " ghế - " + ban.getMaBan(), SwingConstants.CENTER);
        lblSub.setForeground(bg == COLOR_DAT ? new Color(0,0,0,150) : new Color(255,255,255,180));
        pnlCenter.add(lblTen); 
        pnlCenter.add(lblSub);
        card.add(pnlCenter, BorderLayout.CENTER);
        
        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {

                    GoiMonDialog dialog = new GoiMonDialog(
                        SwingUtilities.getWindowAncestor(card),
                        ban
                    );
                    dialog.setVisible(true);
                }
            }
        });
        return card;
    }
}