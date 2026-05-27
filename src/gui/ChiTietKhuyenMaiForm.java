package gui;

import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.ZoneId;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import com.toedter.calendar.JDateChooser;

import dao.KhuyenMai_DAO;
import entity.DoiTuongKM;
import entity.KhuyenMai;

public class ChiTietKhuyenMaiForm extends JDialog {

    private static final long serialVersionUID = 1L;
    private final Color BG = new Color(255, 255, 255);
    private final Color PRIMARY = new Color(54, 92, 245);
    private final Color DANGER = new Color(209, 5, 60);
    private final Color BORDER = new Color(220, 220, 220);

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private JTextField txtMa, txtTen, txtGiaTri;
    private JDateChooser dcNgayBD, dcNgayKT;
    private JComboBox<DoiTuongKM> cbDoiTuong;
    private JComboBox<String> cbHinhThuc;
    private JComboBox<String> cbTrangThai;

    private KhuyenMai kmCurrent;
    private KhuyenMaiPanel parentPanel;
    private KhuyenMai_DAO dao = new KhuyenMai_DAO();

    public ChiTietKhuyenMaiForm(Window parent, KhuyenMai km, KhuyenMaiPanel parentPanel) {
        super(parent, "Chi tiết Khuyến mãi", ModalityType.APPLICATION_MODAL);
        this.kmCurrent = km;
        this.parentPanel = parentPanel;
        
        setSize(500, 650);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        initComponents();
        loadDataToForm();
    }

    private void initComponents() {
        JPanel main = new JPanel(new GridLayout(9, 2, 15, 15));
        main.setBackground(BG);
        main.setBorder(new EmptyBorder(20, 25, 20, 25));

        txtMa = createInput(); txtMa.setEditable(false); txtMa.setBackground(new Color(240, 240, 240));
        txtTen = createInput();
        cbHinhThuc = new JComboBox<>(new String[]{"Giảm phần trăm", "Giảm giá"});
        txtGiaTri = createInput();
        dcNgayBD = createDateChooser();
        dcNgayKT = createDateChooser();
        cbDoiTuong = new JComboBox<>(DoiTuongKM.values());
        cbTrangThai = new JComboBox<>(new String[]{"Sắp diễn ra", "Đang chạy", "Đã kết thúc", "Ngừng áp dụng"});

        main.add(createLabel("Mã khuyến mãi")); main.add(txtMa);
        main.add(createLabel("Tên chương trình")); main.add(txtTen);
        main.add(createLabel("Hình thức")); main.add(cbHinhThuc);
        main.add(createLabel("Giá trị")); main.add(txtGiaTri);
        main.add(createLabel("Ngày bắt đầu")); main.add(dcNgayBD);
        main.add(createLabel("Ngày kết thúc")); main.add(dcNgayKT);
        main.add(createLabel("Đối tượng áp dụng")); main.add(cbDoiTuong);
        main.add(createLabel("Trạng thái")); main.add(cbTrangThai);

        add(main, BorderLayout.CENTER);

        // --- BUTTONS ---
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10)); 
        btnPanel.setBackground(BG);
        
        JButton btnCancel = new JButton("Đóng"); styleBtn(btnCancel, Color.GRAY);
        JButton btnStop = new JButton("Ngừng áp dụng"); styleBtn(btnStop, DANGER);
        JButton btnSave = new JButton("Cập nhật"); styleBtn(btnSave, PRIMARY);

        btnPanel.add(btnStop);
        btnPanel.add(btnSave);
        btnPanel.add(btnCancel);
        add(btnPanel, BorderLayout.SOUTH);

        // --- SỰ KIỆN ---
        btnCancel.addActionListener(e -> dispose());
        
        btnStop.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn ngừng áp dụng khuyến mãi này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dao.stop(kmCurrent.getMaKM());
                JOptionPane.showMessageDialog(this, "Đã ngừng khuyến mãi!");
                if (parentPanel != null) parentPanel.loadData();
                dispose();
            }
        });

        btnSave.addActionListener(e -> updateKhuyenMai());
    }

    private void loadDataToForm() {
        txtMa.setText(kmCurrent.getMaKM());
        txtTen.setText(kmCurrent.getTenKM());
        cbHinhThuc.setSelectedItem(kmCurrent.getLoaiKM());
        
        txtGiaTri.setText(String.valueOf(kmCurrent.getGiaTri()));
        
        if (kmCurrent.getNgayBatDau() != null) {
            dcNgayBD.setDate(Date.from(kmCurrent.getNgayBatDau().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }
        if (kmCurrent.getNgayKetThuc() != null) {
            dcNgayKT.setDate(Date.from(kmCurrent.getNgayKetThuc().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }
        
        cbDoiTuong.setSelectedItem(kmCurrent.getDoiTuongApDung());
        cbTrangThai.setSelectedItem(kmCurrent.getTrangThai());
    }

    private void updateKhuyenMai() {
        try {
            if (txtTen.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Tên không được để trống!"); return;
            }

            double val = Double.parseDouble(txtGiaTri.getText().trim());
            
            if (dcNgayBD.getDate() == null || dcNgayKT.getDate() == null) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn ngày!"); return;
            }
            
            LocalDate ngayBD = dcNgayBD.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate ngayKT = dcNgayKT.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            if (ngayKT.isBefore(ngayBD)) {
                JOptionPane.showMessageDialog(this, "Ngày kết thúc phải sau ngày bắt đầu!"); return;
            }

            String newTrangThai = cbTrangThai.getSelectedItem().toString();

            kmCurrent.setTenKM(txtTen.getText().trim());
            kmCurrent.setLoaiKM(cbHinhThuc.getSelectedItem().toString());
            kmCurrent.setGiaTri(val);
            kmCurrent.setNgayBatDau(ngayBD);
            kmCurrent.setNgayKetThuc(ngayKT);
            kmCurrent.setDoiTuongApDung((DoiTuongKM) cbDoiTuong.getSelectedItem());
            kmCurrent.setTrangThai(newTrangThai);

            // Gọi DAO
            dao.update(kmCurrent);
            JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
            
            // Lệnh làm mới bảng bên ngoài
            if (parentPanel != null) {
                parentPanel.loadData();
            }
            dispose();

        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Ngày tháng không hợp lệ (Nhập chuẩn: dd/MM/yyyy)!");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Giá trị phải là con số!");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage());
        }
    }

    private JTextField createInput() {
        JTextField txt = new JTextField();
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txt.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        return txt;
    }
    
    private JDateChooser createDateChooser() {
        JDateChooser dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("dd/MM/yyyy");
        dateChooser.setPreferredSize(new Dimension(200, 35));
        dateChooser.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JTextField editor = (JTextField) dateChooser.getDateEditor().getUiComponent();
        editor.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        editor.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        editor.setBackground(Color.WHITE);
        
        JButton btn = dateChooser.getCalendarButton();
        btn.setIcon(null);
        btn.setText("📅"); 
        btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        btn.setBackground(Color.WHITE);
        btn.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setFocusPainted(false);
        
        dateChooser.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        return dateChooser;
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return lbl;
    }

    private void styleBtn(JButton btn, Color color) {
        btn.setBackground(color); btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false); btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(130, 35));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}