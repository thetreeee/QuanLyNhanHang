package gui;

import javax.swing.table.DefaultTableModel;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import connectDB.SQLConnection;

public class XuatHoaDonHelper {

    public static void xuatHoaDon(String maHD, String ngayLap, String maNV, String maBan,
                                  DefaultTableModel modelChiTiet,
                                  String tongTienHang, String khuyenMai, String thueVAT, String tongThanhToan, String phuongThuc, boolean isPrint) {
        
        // =====================================================================
        // ĐÃ NÂNG CẤP: Truy vấn Database để tự động dịch maNV thành Tên Nhân Viên
        // =====================================================================
        String tenNhanVien = maNV; // Mặc định nếu lỗi DB thì vẫn in mã (VD: NV009)
        try (Connection con = SQLConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM NhanVien WHERE maNV = ?")) {
            ps.setString(1, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Dùng try-catch lồng nhau phòng hờ bạn đặt tên cột trong SQL khác nhau
                    try { tenNhanVien = rs.getString("tenNV"); } 
                    catch (Exception e1) {
                        try { tenNhanVien = rs.getString("tenNhanVien"); } 
                        catch (Exception e2) {
                            try { tenNhanVien = rs.getString("hoTen"); } catch(Exception e3) {}
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Không thể lấy tên nhân viên: " + e.getMessage());
        }

        try {
            // 1. TỰ ĐỘNG TẠO THƯ MỤC LƯU TRỮ TẠI THƯ MỤC GỐC CỦA PROJECT
            File folder = new File("HoaDon");
            if (!folder.exists()) {
                folder.mkdir();
            }

            // 2. TẠO FILE HTML VỚI TÊN LÀ MÃ HÓA ĐƠN
            File file = new File(folder, "HoaDon_" + maHD + ".html");
            Writer out = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");

            // 3. XÂY DỰNG GIAO DIỆN HTML
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Hóa Đơn ").append(maHD).append("</title>");
            html.append("<style>");
            html.append("body { font-family: 'Times New Roman', serif; margin: 40px auto; max-width: 800px; color: #000; }");
            html.append(".header { text-align: left; margin-bottom: 20px; }");
            html.append(".header h2 { margin: 0 0 10px 0; font-size: 26px; text-transform: uppercase; }");
            html.append(".header p { margin: 5px 0; font-size: 16px; }");
            html.append(".divider { border-top: 1px dashed #000; margin: 20px 0; }");
            html.append(".title { text-align: center; margin: 30px 0; font-size: 28px; font-weight: bold; text-transform: uppercase; }");
            
            html.append(".info { margin-bottom: 30px; font-size: 17px; line-height: 2.0; }");
            html.append(".info-row { display: flex; justify-content: space-between; }");
            html.append(".info-col { width: 48%; }");
            html.append(".info strong { font-weight: bold; }");
            
            html.append("table { width: 100%; border-collapse: collapse; margin-bottom: 30px; font-size: 16px; table-layout: fixed; }");
            html.append("th, td { border: 1px solid #000; padding: 10px; word-wrap: break-word; }"); 
            html.append("th { background-color: #e0e0e0; font-weight: bold; }");
            
            html.append(".footer { display: flex; justify-content: flex-end; font-size: 17px; line-height: 2.0; }");
            html.append(".footer-right { width: 380px; }");
            html.append(".footer-row { display: flex; justify-content: space-between; }");
            html.append(".total { font-size: 22px; font-weight: bold; margin-top: 10px; border-top: 2px solid #000; padding-top: 10px; }");
            html.append("</style></head><body>");

            // --- HEADER NHÀ HÀNG ---
            html.append("<div class='header'>");
            html.append("<h2>NHÀ HÀNG TUẤN TRƯỜNG</h2>");
            html.append("<p>12 Nguyễn Văn Bảo, Phường 4, Gò Vấp, TP.HCM</p>");
            html.append("<p>Điện thoại: 0123 456 789</p>");
            html.append("</div>");
            html.append("<div class='divider'></div>");

            // --- TIÊU ĐỀ ---
            html.append("<div class='title'>HÓA ĐƠN THANH TOÁN</div>");

            // --- THÔNG TIN HÓA ĐƠN ---
            html.append("<div class='info'>");
            html.append("<div class='info-row'>");
            
            html.append("<div class='info-col'>");
            html.append("<span><strong>Mã Hóa Đơn: </strong> ").append(maHD).append("</span><br>");
            html.append("<span><strong>Ngày Lập: </strong> ").append(ngayLap).append("</span><br>");
            // GHI TÊN NHÂN VIÊN VÀO HTML
            html.append("<span><strong>Nhân Viên: </strong> ").append(tenNhanVien).append("</span>");
            html.append("</div>");

            html.append("<div class='info-col'>");
            html.append("<span><strong>Bàn: </strong> ").append(maBan).append("</span><br>");
            html.append("<span><strong>Phương Thức TT: </strong> ").append(phuongThuc).append("</span>");
            html.append("</div>");

            html.append("</div>"); 
            html.append("</div>"); 

            html.append("<table><thead><tr>");
            html.append("<th style='width: 8%; text-align: center;'>STT</th>");
            html.append("<th style='width: 40%; text-align: left;'>Tên Món</th>");
            html.append("<th style='width: 12%; text-align: center;'>SL</th>");
            html.append("<th style='width: 20%; text-align: right;'>Đơn Giá</th>");
            html.append("<th style='width: 20%; text-align: right;'>Thành Tiền</th>");
            html.append("</tr></thead><tbody>");

            for (int i = 0; i < modelChiTiet.getRowCount(); i++) {
                String tenMon = modelChiTiet.getValueAt(i, 1).toString();
                String soLuong = modelChiTiet.getValueAt(i, 2).toString();
                String donGia = modelChiTiet.getValueAt(i, 3).toString();
                String thanhTien = modelChiTiet.getValueAt(i, 4).toString();
                
                html.append("<tr>");
                html.append("<td style='text-align: center;'>").append(i + 1).append("</td>");
                html.append("<td style='text-align: left;'>").append(tenMon).append("</td>");
                html.append("<td style='text-align: center;'>").append(soLuong).append("</td>");
                html.append("<td style='text-align: right;'>").append(donGia).append("</td>");
                html.append("<td style='text-align: right;'>").append(thanhTien).append("</td>");
                html.append("</tr>");
            }
            html.append("</tbody></table>");

            // --- PHẦN TỔNG KẾT PHÍA DƯỚI ---
            html.append("<div class='footer'><div class='footer-right'>");
            html.append("<div class='footer-row'><span>Tổng Tiền Hàng:</span> <span>").append(tongTienHang).append("</span></div>");
            html.append("<div class='footer-row'><span>Khuyến Mãi:</span> <span>").append(khuyenMai).append("</span></div>");
            html.append("<div class='footer-row'><span>Thuế (8% VAT):</span> <span>").append(thueVAT).append("</span></div>");
            html.append("<div class='footer-row total'><span>Tổng Cộng:</span> <span>").append(tongThanhToan).append("</span></div>");
            html.append("</div></div>");
            
            html.append("</body></html>");

            // Ghi file
            out.write(html.toString());
            out.close();

            // Mở file nếu người dùng có chọn in
            if (isPrint && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(file.toURI());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}