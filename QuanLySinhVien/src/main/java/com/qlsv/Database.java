
package com.qlsv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    public static Connection getConnection() {

        Connection c = null;

        try {

            Class.forName("com.mysql.cj.jdbc.Driver");

            String url = "jdbc:mysql://localhost:3306/qlsv?useSSL=false&serverTimezone=Asia/Ho_Chi_Minh";
            String username = "root";
            String password = "";

            c = DriverManager.getConnection(
                    url, username, password
            );

            System.out.println("Kết nối MySQL thành công!");

        } catch (ClassNotFoundException e) {

            System.out.println("Không tìm thấy MySQL Driver!");
            e.printStackTrace();

        } catch (SQLException e) {

            System.out.println("Kết nối MySQL thất bại!");
            e.printStackTrace();
        }

        return c;
    }

    public static void closeConnection(Connection c) {

        try {

            if (c != null) {
                c.close();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void printInfo(Connection c) {

        try {

            if (c != null) {

                System.out.println(
                        c.getMetaData().getDatabaseProductName()
                );

                System.out.println(
                        c.getMetaData().getDatabaseProductVersion()
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
