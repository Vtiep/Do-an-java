
package com.qlsv;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class Main extends JFrame {

    private final JTextField txtMa = new JTextField(12);
    private final JTextField txtTen = new JTextField(20);
    private final JTextField txtNgaySinh = new JTextField(12);

    private final JComboBox<String> cboGioiTinh =
            new JComboBox<>(new String[]{"Nam", "Nu", "Khac"});

    private final JTextField txtEmail = new JTextField(20);
    private final JTextField txtSDT = new JTextField(15);
    private final JTextField txtDiaChi = new JTextField(20);
    private final JComboBox<ClassItem> cboLop = new JComboBox<>();
    private final JTextField txtTimKiem = new JTextField(22);

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{
                "Mã SV", "Họ tên", "Ngày sinh", "Giới tính",
                "Email", "Số điện thoại", "Địa chỉ",
                "Mã lớp", "Tên lớp"
            }, 0) {

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable table = new JTable(model);

    // Constructor
    public Main() {

        setTitle("PHẦN MỀM QUẢN LÝ SINH VIÊN");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1120, 680);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel(
                "QUẢN LÝ SINH VIÊN",
                SwingConstants.CENTER
        );

        title.setFont(new Font("Arial", Font.BOLD, 25));
        title.setBorder(
                BorderFactory.createEmptyBorder(15, 5, 5, 5)
        );

        add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.setBorder(
                BorderFactory.createEmptyBorder(5, 12, 12, 12)
        );

        JPanel form = new JPanel(new GridLayout(4, 4, 8, 8));
        form.setBorder(
                BorderFactory.createTitledBorder("Thông tin sinh viên")
        );

        addField(form, "Mã sinh viên:", txtMa);
        addField(form, "Họ và tên:", txtTen);
        addField(form, "Ngày sinh (yyyy-MM-dd):", txtNgaySinh);
        addField(form, "Giới tính:", cboGioiTinh);
        addField(form, "Email:", txtEmail);
        addField(form, "Số điện thoại:", txtSDT);
        addField(form, "Địa chỉ:", txtDiaChi);
        addField(form, "Lớp:", cboLop);

        // Các nút chức năng
        JPanel actions = new JPanel(
                new FlowLayout(FlowLayout.CENTER, 10, 8)
        );

        JButton btnThem = new JButton("Thêm");
        JButton btnSua = new JButton("Sửa");
        JButton btnXoa = new JButton("Xóa");
        JButton btnMoi = new JButton("Làm mới");
        JButton btnTim = new JButton("Tìm kiếm");
        JButton btnTaiLai = new JButton("Tải lại");

        actions.add(btnThem);
        actions.add(btnSua);
        actions.add(btnXoa);
        actions.add(btnMoi);
        actions.add(btnTim);
        actions.add(btnTaiLai);

        // Khu vực tìm kiếm
        JPanel search = new JPanel(
                new FlowLayout(FlowLayout.LEFT)
        );

        search.add(new JLabel("Tìm theo mã hoặc họ tên:"));
        search.add(txtTimKiem);
        search.add(btnTim);

        JPanel top = new JPanel(new BorderLayout());

        top.add(form, BorderLayout.CENTER);
        top.add(actions, BorderLayout.SOUTH);

        center.add(top, BorderLayout.NORTH);
        center.add(new JScrollPane(table), BorderLayout.CENTER);
        center.add(search, BorderLayout.SOUTH);

        add(center, BorderLayout.CENTER);

        // Xử lý sự kiện các nút
        btnThem.addActionListener(e -> themSinhVien());
        btnSua.addActionListener(e -> suaSinhVien());
        btnXoa.addActionListener(e -> xoaSinhVien());
        btnMoi.addActionListener(e -> lamMoi());
        btnTaiLai.addActionListener(e -> taiDuLieu());
        btnTim.addActionListener(e -> timKiem());

        // Chọn một dòng trong bảng
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()
                    && table.getSelectedRow() >= 0) {
                doChonDong();
            }
        });

        napDanhSachLop();
        taiDuLieu();
    }

    // Kết nối với Database.java
    private Connection moKetNoi() throws SQLException {

        Connection c = Database.getConnection();

        if (c == null) {
            throw new SQLException(
                    "Không thể kết nối MySQL. "
                    + "Hãy kiểm tra Database.java và MySQL."
            );
        }

        return c;
    }

    // Thêm nhãn và ô nhập liệu
    private void addField(
            JPanel panel, String label, Component input) {

        panel.add(new JLabel(label));
        panel.add(input);
    }

    // Lấy danh sách lớp từ CSDL
    private void napDanhSachLop() {

        cboLop.removeAllItems();

        String sql = "SELECT MaLop, TenLop FROM LOP ORDER BY MaLop";

        try (Connection c = moKetNoi();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                cboLop.addItem(
                        new ClassItem(
                                rs.getString("MaLop"),
                                rs.getString("TenLop")
                        )
                );
            }

        } catch (SQLException ex) {

            baoLoi(
                    "Không tải được danh sách lớp!\n"
                    + ex.getMessage()
            );
        }
    }

    // Hiển thị danh sách sinh viên
    private void taiDuLieu() {

        model.setRowCount(0);

        String sql =
                "SELECT SV.MaSV, SV.HoTen, SV.NgaySinh, "
                + "SV.GioiTinh, SV.Email, SV.SoDienThoai, "
                + "SV.DiaChi, SV.MaLop, L.TenLop "
                + "FROM SINHVIEN SV "
                + "JOIN LOP L ON SV.MaLop = L.MaLop "
                + "ORDER BY SV.MaSV";

        try (Connection c = moKetNoi();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                model.addRow(new Object[]{
                    rs.getString("MaSV"),
                    rs.getString("HoTen"),
                    rs.getDate("NgaySinh"),
                    rs.getString("GioiTinh"),
                    rs.getString("Email"),
                    rs.getString("SoDienThoai"),
                    rs.getString("DiaChi"),
                    rs.getString("MaLop"),
                    rs.getString("TenLop")
                });
            }

        } catch (SQLException ex) {

            baoLoi("Không tải được sinh viên: " + ex.getMessage());
        }
    }

    // Đọc dữ liệu từ form
    private SinhVien docForm() {

        String ma = txtMa.getText().trim();
        String ten = txtTen.getText().trim();
        String ngay = txtNgaySinh.getText().trim();

        ClassItem lop = (ClassItem) cboLop.getSelectedItem();

        if (ma.isEmpty() || ten.isEmpty()
                || ngay.isEmpty() || lop == null) {

            throw new IllegalArgumentException(
                    "Vui lòng nhập mã, họ tên, ngày sinh và chọn lớp."
            );
        }

        if (ma.length() > 10) {
            throw new IllegalArgumentException(
                    "Mã sinh viên tối đa 10 ký tự."
            );
        }

        if (ten.length() > 100) {
            throw new IllegalArgumentException(
                    "Họ tên tối đa 100 ký tự."
            );
        }

        LocalDate date;

        try {
            date = LocalDate.parse(ngay);

        } catch (DateTimeParseException ex) {

            throw new IllegalArgumentException(
                    "Ngày sinh phải có dạng yyyy-MM-dd. "
                    + "Ví dụ: 2004-05-12."
            );
        }

        String email = txtEmail.getText().trim();
        String sdt = txtSDT.getText().trim();
        String diaChi = txtDiaChi.getText().trim();

        if (email.length() > 100
                || sdt.length() > 15
                || diaChi.length() > 255) {

            throw new IllegalArgumentException(
                    "Email tối đa 100, số điện thoại tối đa 15, "
                    + "địa chỉ tối đa 255 ký tự."
            );
        }

        return new SinhVien(
                ma,
                ten,
                date,
                (String) cboGioiTinh.getSelectedItem(),
                email.isEmpty() ? null : email,
                sdt.isEmpty() ? null : sdt,
                diaChi,
                lop.ma
        );
    }

    // Thêm sinh viên
    private void themSinhVien() {

        try {

            SinhVien sv = docForm();

            String sql =
                    "INSERT INTO SINHVIEN "
                    + "(MaSV, HoTen, NgaySinh, GioiTinh, "
                    + "Email, SoDienThoai, DiaChi, MaLop) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            try (Connection c = moKetNoi();
                 PreparedStatement ps = c.prepareStatement(sql)) {

                ganThamSo(ps, sv);
                ps.executeUpdate();
            }

            JOptionPane.showMessageDialog(
                    this, "Thêm sinh viên thành công!"
            );

            taiDuLieu();
            lamMoi();

        } catch (IllegalArgumentException ex) {

            baoLoi(ex.getMessage());

        } catch (SQLException ex) {

            baoLoi("Không thể thêm sinh viên!\n" + ex.getMessage());
        }
    }

    // Sửa thông tin sinh viên
    private void suaSinhVien() {

        if (table.getSelectedRow() < 0) {

            baoLoi("Hãy chọn sinh viên cần sửa trong bảng.");
            return;
        }

        try {

            SinhVien sv = docForm();

            String sql =
                    "UPDATE SINHVIEN SET "
                    + "HoTen=?, NgaySinh=?, GioiTinh=?, "
                    + "Email=?, SoDienThoai=?, DiaChi=?, "
                    + "MaLop=? WHERE MaSV=?";

            try (Connection c = moKetNoi();
                 PreparedStatement ps = c.prepareStatement(sql)) {

                ps.setString(1, sv.getHoTen());
                ps.setDate(2, Date.valueOf(sv.getNgaySinh()));
                ps.setString(3, sv.getGioiTinh());
                ps.setString(4, sv.getEmail());
                ps.setString(5, sv.getSoDienThoai());
                ps.setString(6, sv.getDiaChi());
                ps.setString(7, sv.getMaLop());
                ps.setString(8, sv.getMaSV());

                if (ps.executeUpdate() == 0) {

                    baoLoi("Không tìm thấy sinh viên để sửa.");
                    return;
                }
            }

            JOptionPane.showMessageDialog(
                    this, "Cập nhật sinh viên thành công!"
            );

            taiDuLieu();
            lamMoi();

        } catch (IllegalArgumentException ex) {

            baoLoi(ex.getMessage());

        } catch (SQLException ex) {

            baoLoi("Không thể sửa sinh viên: " + ex.getMessage());
        }
    }

    // Xóa sinh viên
    private void xoaSinhVien() {

        int row = table.getSelectedRow();

        if (row < 0) {

            baoLoi("Hãy chọn sinh viên cần xóa trong bảng.");
            return;
        }

        String ma = String.valueOf(model.getValueAt(row, 0));

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn xóa sinh viên " + ma + "?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String sql = "DELETE FROM SINHVIEN WHERE MaSV=?";

        try (Connection c = moKetNoi();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, ma);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(
                    this, "Đã xóa sinh viên!"
            );

            taiDuLieu();
            lamMoi();

        } catch (SQLException ex) {

            baoLoi(
                    "Không thể xóa sinh viên. "
                    + "Có thể sinh viên đang có dữ liệu điểm.\n"
                    + ex.getMessage()
            );
        }
    }

    // Tìm kiếm sinh viên
    private void timKiem() {

        model.setRowCount(0);

        String key = "%" + txtTimKiem.getText().trim() + "%";

        String sql =
                "SELECT SV.MaSV, SV.HoTen, SV.NgaySinh, "
                + "SV.GioiTinh, SV.Email, SV.SoDienThoai, "
                + "SV.DiaChi, SV.MaLop, L.TenLop "
                + "FROM SINHVIEN SV "
                + "JOIN LOP L ON SV.MaLop = L.MaLop "
                + "WHERE SV.MaSV LIKE ? OR SV.HoTen LIKE ? "
                + "ORDER BY SV.MaSV";

        try (Connection c = moKetNoi();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, key);
            ps.setString(2, key);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    model.addRow(new Object[]{
                        rs.getString("MaSV"),
                        rs.getString("HoTen"),
                        rs.getDate("NgaySinh"),
                        rs.getString("GioiTinh"),
                        rs.getString("Email"),
                        rs.getString("SoDienThoai"),
                        rs.getString("DiaChi"),
                        rs.getString("MaLop"),
                        rs.getString("TenLop")
                    });
                }
            }

        } catch (SQLException ex) {

            baoLoi("Lỗi tìm kiếm: " + ex.getMessage());
        }
    }

    // Gán tham số cho câu lệnh SQL
    private void ganThamSo(
            PreparedStatement ps, SinhVien sv) throws SQLException {

        ps.setString(1, sv.getMaSV());
        ps.setString(2, sv.getHoTen());
        ps.setDate(3, Date.valueOf(sv.getNgaySinh()));
        ps.setString(4, sv.getGioiTinh());
        ps.setString(5, sv.getEmail());
        ps.setString(6, sv.getSoDienThoai());
        ps.setString(7, sv.getDiaChi());
        ps.setString(8, sv.getMaLop());
    }

    // Chọn sinh viên trong bảng để sửa
    private void doChonDong() {

        int r = table.getSelectedRow();

        if (r < 0) {
            return;
        }

        txtMa.setText(String.valueOf(model.getValueAt(r, 0)));
        txtTen.setText(String.valueOf(model.getValueAt(r, 1)));
        txtNgaySinh.setText(String.valueOf(model.getValueAt(r, 2)));

        cboGioiTinh.setSelectedItem(model.getValueAt(r, 3));

        txtEmail.setText(value(model.getValueAt(r, 4)));
        txtSDT.setText(value(model.getValueAt(r, 5)));
        txtDiaChi.setText(value(model.getValueAt(r, 6)));

        String maLop = String.valueOf(model.getValueAt(r, 7));

        for (int i = 0; i < cboLop.getItemCount(); i++) {

            if (cboLop.getItemAt(i).ma.equals(maLop)) {
                cboLop.setSelectedIndex(i);
            }
        }

        txtMa.setEditable(false);
    }

    // Xử lý giá trị null
    private String value(Object o) {

        return o == null ? "" : o.toString();
    }

    // Làm mới form
    private void lamMoi() {

        table.clearSelection();

        txtMa.setEditable(true);
        txtMa.setText("");
        txtTen.setText("");
        txtNgaySinh.setText("");

        cboGioiTinh.setSelectedIndex(0);

        txtEmail.setText("");
        txtSDT.setText("");
        txtDiaChi.setText("");

        if (cboLop.getItemCount() > 0) {
            cboLop.setSelectedIndex(0);
        }
    }

    // Hiển thị thông báo lỗi
    private void baoLoi(String message) {

        JOptionPane.showMessageDialog(
                this,
                message,
                "Thông báo",
                JOptionPane.WARNING_MESSAGE
        );
    }

    // Lớp lưu thông tin lớp học
    private static class ClassItem {

        String ma;
        String ten;

        ClassItem(String ma, String ten) {

            this.ma = ma;
            this.ten = ten;
        }

        @Override
        public String toString() {

            return ma + " - " + ten;
        }
    }

    // Hàm main chạy chương trình
    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            new Main().setVisible(true);
        });
    }
}
