package gui;

import dao.BanDAO;
import dao.DonDatBanDAO;
import entity.DonDatBan;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

public class DanhSachDatBanPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private JTable table;
    private DefaultTableModel model;
    private JLabel lblTime;
    private JTextField txtSearch;
    
    private DonDatBanDAO donDatBanDAO = new DonDatBanDAO();
    private BanDAO banDAO = new BanDAO(); 
    
    private List<DonDatBan> dsDonDatHienTai;
    private boolean isUpdatingTable = false;

    public DanhSachDatBanPanel() {
        setLayout(new BorderLayout(20, 20));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(20, 25, 20, 25));

        // --- 1. HEADER ---
        JPanel pnlHeader = new JPanel(new BorderLayout(0, 15)); 
        pnlHeader.setBackground(Color.WHITE);
        
        JPanel pnlTimeBanner = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        pnlTimeBanner.setBackground(new Color(255, 246, 246)); 
        pnlTimeBanner.putClientProperty("FlatLaf.style", "arc: 10");
        
        lblTime = new JLabel();
        lblTime.setFont(new Font("Segoe UI", Font.PLAIN, 14)); 
        lblTime.setForeground(Color.BLACK); 
        pnlTimeBanner.add(lblTime);
        
        JPanel pnlTitleAndSearch = new JPanel(new BorderLayout());
        pnlTitleAndSearch.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("DANH SÁCH ĐẶT BÀN");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(220, 50, 50)); 
        pnlTitleAndSearch.add(lblTitle, BorderLayout.WEST);

        txtSearch = new JTextField(20);
        txtSearch.setPreferredSize(new Dimension(250, 40));
        txtSearch.putClientProperty("JTextField.placeholderText", "Tìm tên khách, mã đơn, SĐT...");
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { loadData(txtSearch.getText().trim()); }
            public void removeUpdate(DocumentEvent e) { loadData(txtSearch.getText().trim()); }
            public void changedUpdate(DocumentEvent e) { loadData(txtSearch.getText().trim()); }
        });

        JPanel pnlSearchWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pnlSearchWrapper.setBackground(Color.WHITE);
        pnlSearchWrapper.add(txtSearch);
        pnlTitleAndSearch.add(pnlSearchWrapper, BorderLayout.EAST);
        
        pnlHeader.add(pnlTimeBanner, BorderLayout.NORTH); 
        pnlHeader.add(pnlTitleAndSearch, BorderLayout.CENTER);     
        add(pnlHeader, BorderLayout.NORTH);

        Timer timer = new Timer(1000, e -> {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss");
            lblTime.setText("Thời gian hiện tại: " + sdf.format(new Date()));
        });
        timer.start();

        // --- 2. BẢNG DỮ LIỆU ---
        String[] cols = {"Mã đơn", "Mã bàn", "Họ tên", "Số điện thoại", "Trạng thái"};
        
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { 
                return column == 4; 
            } 
        };
        table = new JTable(model);
        
        table.setRowHeight(38);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(240, 240, 240));
        
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowSelectionAllowed(true);
        table.putClientProperty("JTable.showCellFocusIndicator", false);
        
        table.getTableHeader().setBackground(new Color(255, 235, 235));
        table.getTableHeader().setPreferredSize(new Dimension(0, 45));
        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(230, 230, 230)));
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setBackground(new Color(255, 235, 235));
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 14)); 
                lbl.setForeground(Color.BLACK);
                return lbl;
            }
        });

        DefaultTableCellRenderer noFocusRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
                if (column == 2) {
                    setHorizontalAlignment(SwingConstants.LEFT);
                    setBorder(new EmptyBorder(0, 15, 0, 0));
                } else {
                    setHorizontalAlignment(SwingConstants.CENTER);
                    setBorder(new EmptyBorder(0, 0, 0, 0));
                }
                return this;
            }
        };

        for (int i = 0; i < 4; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(noFocusRenderer);
        }

        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                setBorder(new EmptyBorder(0, 0, 0, 0));

                if (value != null) {
                    String stt = value.toString();
                    if (stt.equalsIgnoreCase("Checked-in")) {
                        setForeground(new Color(40, 167, 69)); // Xanh lá
                        setFont(new Font("Segoe UI", Font.BOLD, 14));
                    } else if (stt.equalsIgnoreCase("Hoàn thành")) {
                        setForeground(new Color(0, 122, 255)); // Xanh dương
                        setFont(new Font("Segoe UI", Font.BOLD, 14));
                    } else if (stt.equalsIgnoreCase("Đã hủy")) {
                        setForeground(Color.GRAY);
                        setFont(new Font("Segoe UI", Font.ITALIC, 14));
                    } else {
                        setForeground(Color.BLACK);
                        setFont(new Font("Segoe UI", Font.PLAIN, 14));
                    }
                }

                if (isSelected) {
                    setBackground(table.getSelectionBackground());
                    setForeground(Color.WHITE);
                } else {
                    setBackground(Color.WHITE);
                }
                return this;
            }
        });

        // ==============================================================
        // ĐÃ SỬA: COMBOBOX THÔNG MINH DÀNH CHO CỘT TRẠNG THÁI
        // ==============================================================
        JComboBox<String> cbTrangThai = new JComboBox<>();
        cbTrangThai.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbTrangThai.setBackground(Color.WHITE);

        DefaultCellEditor trangThaiEditor = new DefaultCellEditor(cbTrangThai) {
            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                String currentStatus = (value != null) ? value.toString() : "";
                cbTrangThai.removeAllItems(); // Xóa sạch danh sách cũ

                // 1. Phân quyền chốt cứng: Khách đã ăn xong hoặc đã hủy thì KHÔNG cho đổi nữa
                if (currentStatus.equalsIgnoreCase("Hoàn thành") || currentStatus.equalsIgnoreCase("Đã hủy")) {
                    cbTrangThai.addItem(currentStatus); 
                } 
                // 2. Nếu khách đang ngồi ở bàn (Checked-in / Đang dùng) -> Chờ Thanh Toán, không cho Lễ tân sửa
                else if (currentStatus.equalsIgnoreCase("Checked-in") || currentStatus.equalsIgnoreCase("Đang dùng")) {
                    cbTrangThai.addItem(currentStatus); 
                } 
                // 3. Nếu khách chưa tới (Đang chờ / Đã đặt) -> Chỉ cho phép Lễ tân đổi qua lại hoặc báo Hủy
                else {
                    cbTrangThai.addItem("Đã đặt");
                    cbTrangThai.addItem("Đã hủy"); 
                }
                
                cbTrangThai.setSelectedItem(currentStatus);
                return super.getTableCellEditorComponent(table, value, isSelected, row, column);
            }
        };
        trangThaiEditor.setClickCountToStart(2); 
        table.getColumnModel().getColumn(4).setCellEditor(trangThaiEditor);

        loadData("");

        model.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (isUpdatingTable) return; 

                if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == 4) {
                    int row = e.getFirstRow();
                    String maDon = model.getValueAt(row, 0).toString();
                    String maBan = model.getValueAt(row, 1).toString(); 
                    String trangThaiMoi = model.getValueAt(row, 4).toString(); 
                    
                    DonDatBan currentDon = dsDonDatHienTai.stream()
                            .filter(d -> d.getMaDon().equals(maDon)).findFirst().orElse(null);

                    if (currentDon != null) {
                        
                        if (trangThaiMoi.equals("Đã đặt")) {
                            boolean biTrung = donDatBanDAO.kiemTraTrungLich(maBan, currentDon.getNgayDat(), currentDon.getThoiGian(), maDon);
                            if (biTrung) {
                                JOptionPane.showMessageDialog(DanhSachDatBanPanel.this, 
                                    "Không thể đặt được vì có khách đã đặt giờ đó.\nXin đặt lại giờ khác.", 
                                    "Cảnh báo trùng lịch", JOptionPane.WARNING_MESSAGE);
                                
                                isUpdatingTable = true;
                                model.setValueAt(currentDon.getTrangThai(), row, 4); // Trả về trạng thái cũ
                                isUpdatingTable = false;
                                return; 
                            }
                        }

                        // Cập nhật CSDL
                        donDatBanDAO.updateTrangThaiCuaDon(maDon, trangThaiMoi);

                        // Cập nhật trạng thái mặt bàn trên sơ đồ
                        if (trangThaiMoi.equals("Đã hủy") || trangThaiMoi.equals("Hoàn thành")) {
                            banDAO.updateTrangThaiBan(maBan, "Trống"); 
                        } else if (trangThaiMoi.equals("Đang dùng") || trangThaiMoi.equals("Checked-in")) {
                            banDAO.updateTrangThaiBan(maBan, "Đang dùng"); 
                        } else if (trangThaiMoi.equals("Đã đặt")) {
                            if (currentDon.getNgayDat().equals(LocalDate.now())) {
                                banDAO.updateTrangThaiBan(maBan, "Đã đặt"); 
                            }
                        }
                    }
                }
            }
        });

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1) {
                    if (table.getSelectedColumn() == 4) return;

                    int row = table.getSelectedRow();
                    String maDon = table.getValueAt(row, 0).toString();
                    String maBan = table.getValueAt(row, 1).toString();
                    String khachHang = table.getValueAt(row, 2).toString();
                    String trangThai = table.getValueAt(row, 4).toString();

                    DonDatBan donDuocChon = dsDonDatHienTai.stream()
                        .filter(d -> d.getMaDon().equals(maDon)).findFirst().orElse(null);

                    if (donDuocChon != null) {
                        DateTimeFormatter fNgay = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                        DateTimeFormatter fGio = DateTimeFormatter.ofPattern("HH:mm");
                        String ngayDat = donDuocChon.getNgayDat().format(fNgay);
                        String gioDat = donDuocChon.getThoiGian().format(fGio);

                        DialogChiTietDonDat dialog = new DialogChiTietDonDat(
                            SwingUtilities.getWindowAncestor(DanhSachDatBanPanel.this),
                            maDon, maBan, ngayDat, gioDat, trangThai, khachHang
                        );
                        dialog.setVisible(true);
                        
                        loadData(txtSearch.getText().trim()); 
                    }
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, new Color(230, 230, 230)));
        add(scroll, BorderLayout.CENTER);
        
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

    public void loadData(String query) {
        model.setRowCount(0);
        dsDonDatHienTai = donDatBanDAO.getAllDonDat();
        
        String keyword = query.toLowerCase();

        for (DonDatBan d : dsDonDatHienTai) {
            String tenKH = d.getTenKhachHang() != null ? d.getTenKhachHang() : "Khách vãng lai";
            String sdt = d.getSoDienThoai() != null ? d.getSoDienThoai() : "";
            String maDon = d.getMaDon();
            
            if (tenKH.toLowerCase().contains(keyword) || maDon.toLowerCase().contains(keyword) || sdt.contains(keyword)) {
                model.addRow(new Object[]{
                    maDon,
                    d.getMaBan(),
                    tenKH,
                    sdt,
                    d.getTrangThai() 
                });
            }
        }
    }
}