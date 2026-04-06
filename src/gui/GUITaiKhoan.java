package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.net.URL;
import dao.taiKhoanDao;

public class GUITaiKhoan extends JFrame {
    private JTextField txtUser;
    private JPasswordField txtPass;
    private JCheckBox chkShowPass; 
    private taiKhoanDao taiKhoanDAO = new taiKhoanDao();

    public GUITaiKhoan() {
        setTitle("Đăng Nhập Hệ Thống");
        setSize(450, 600); 
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        BackgroundPanel mainPanel = new BackgroundPanel("/bg.jpg");
        mainPanel.setLayout(new GridBagLayout());
        setContentPane(mainPanel);

        JPanel loginForm = new JPanel();
        loginForm.setLayout(new BoxLayout(loginForm, BoxLayout.Y_AXIS));
        loginForm.setBackground(new Color(255, 255, 255, 225)); 
        loginForm.setBorder(new EmptyBorder(30, 35, 30, 35));
        loginForm.putClientProperty("FlatLaf.style", "arc: 30");

        // Logo
        JLabel lblLogo = new JLabel();
        URL logoURL = getClass().getResource("/logoNHTT.png");
        if (logoURL != null) {
            ImageIcon icon = new ImageIcon(logoURL);
            Image img = icon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
            lblLogo.setIcon(new ImageIcon(img));
        }
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblWelcome = new JLabel("WELCOME");
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblWelcome.setForeground(new Color(41, 128, 185));
        lblWelcome.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Input Panel
        JPanel pnlInput = new JPanel(new GridBagLayout());
        pnlInput.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5); 
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // User
        JLabel lblUser = new JLabel("Tên đăng nhập:");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        pnlInput.add(lblUser, gbc);

        txtUser = new JTextField();
        txtUser.putClientProperty("JTextField.placeholderText", "Nhập tài khoản...");
        txtUser.setPreferredSize(new Dimension(200, 35));
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        pnlInput.add(txtUser, gbc);

        // Password
        JLabel lblPass = new JLabel("Mật khẩu:");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        pnlInput.add(lblPass, gbc);

        txtPass = new JPasswordField();
        txtPass.putClientProperty("JTextField.placeholderText", "Nhập mật khẩu...");
        txtPass.setPreferredSize(new Dimension(200, 35));
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        pnlInput.add(txtPass, gbc);

        // --- HIỆN MẬT KHẨU ---
        chkShowPass = new JCheckBox("Hiện mật khẩu");
        chkShowPass.setOpaque(false);
        chkShowPass.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridx = 1; gbc.gridy = 2; 
        pnlInput.add(chkShowPass, gbc);

        chkShowPass.addActionListener(e -> {
            if (chkShowPass.isSelected()) {
                txtPass.setEchoChar((char) 0); 
            } else {
                txtPass.setEchoChar('•'); 
            }
        });

        // Nút Đăng nhập
        JButton btnLogin = new JButton("ĐĂNG NHẬP");
        btnLogin.setBackground(new Color(41, 128, 185));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));

        getRootPane().setDefaultButton(btnLogin); // Gắn phím Enter

        loginForm.add(lblLogo);
        loginForm.add(Box.createRigidArea(new Dimension(0, 10)));
        loginForm.add(lblWelcome);
        loginForm.add(Box.createRigidArea(new Dimension(0, 20)));
        loginForm.add(pnlInput); 
        loginForm.add(Box.createRigidArea(new Dimension(0, 20)));
        loginForm.add(btnLogin);

        mainPanel.add(loginForm);

        btnLogin.addActionListener(e -> xuLyDangNhap());
    }

    private void xuLyDangNhap() {
        String user = txtUser.getText().trim();
        String pass = new String(txtPass.getPassword()).trim();

        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng điền đủ thông tin!", "Nhắc nhở", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (pass.length() < 5) {
            JOptionPane.showMessageDialog(this, "Mật khẩu phải từ 5 ký tự trở lên!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        } else if (pass.matches("^[0-9]+$")) {
            JOptionPane.showMessageDialog(this, "Mật khẩu phải chứa thêm chữ cái (không được chỉ có số)!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        } else if (pass.matches("^[a-zA-Z]+$")) {
            JOptionPane.showMessageDialog(this, "Mật khẩu phải chứa thêm chữ số (không được chỉ có chữ)!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        } else {
            // --- XỬ LÝ ĐĂNG NHẬP THEO QUYỀN (RBAC) ---
            if (taiKhoanDAO.kiemTraDangNhap(user, pass)) {
                
                // Lấy chức vụ từ DB
                String chucVu = taiKhoanDAO.getRole(user, pass);
                
                if (chucVu != null) {
                    // ĐIỀU HƯỚNG GIAO DIỆN
                    if (chucVu.equalsIgnoreCase("Quản lý") || chucVu.equalsIgnoreCase("QUANLY")) {
                        new GUIDashBoard().setVisible(true); // Mở Dashboard Quản lý
                    } 
                    else if (chucVu.equalsIgnoreCase("Nhân viên phục vụ") || chucVu.equalsIgnoreCase("NHANVIENPHUCVU")) {
                        new GUIDashBoardNVPV().setVisible(true); // Mở Dashboard Nhân viên Phục vụ
                    }
                    else if (chucVu.equalsIgnoreCase("Nhân viên lễ tân") || chucVu.equalsIgnoreCase("NHANVIENLETAN")) {
                        new GUIDashBoardNVLT().setVisible(true); // Mở Dashboard Nhân viên Lễ tân
                    }
                    else {
                        // Trường hợp các chức vụ khác chưa code xong giao diện (như Thu ngân, v.v...)
                        JOptionPane.showMessageDialog(this, "Chưa có giao diện cho chức vụ: " + chucVu, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                        return; // Dừng lại, không đóng form đăng nhập
                    }
                    this.dispose(); // Đóng form đăng nhập khi vào được dashboard tương ứng
                } else {
                    JOptionPane.showMessageDialog(this, "Lỗi lấy thông tin phân quyền!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }

            } else {
                JOptionPane.showMessageDialog(this, "Sai tài khoản hoặc mật khẩu (hoặc tài khoản đã bị khóa)!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    class BackgroundPanel extends JPanel {
        private Image backgroundImage;
        public BackgroundPanel(String path) {
            URL url = getClass().getResource(path);
            if (url != null) backgroundImage = new ImageIcon(url).getImage();
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }
}