package ru.stk.server;

import ru.stk.data.User;

import java.sql.*;
import java.util.ArrayList;

public class DBService {
    private static Connection connection;
    private static PreparedStatement psGetUsers;
    private  static ResultSet rs;
    
    public static ArrayList<User> getUsers() {
        ArrayList<User> alUsers = new ArrayList<>();
        try {
            connect();
            initPSGetUsers();

            rs = psGetUsers.executeQuery();

            while (rs.next()) {
                // System.out.println(rs.getString(2) + " " + rs.getString(3) + " " + rs.getString(4) );
                alUsers.add(new User(rs.getString(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4)));
            }
            return alUsers;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
        return null;
    }

    private static void connect() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:CDriveDB.db");
    }

    private static void initPSGetUsers() throws SQLException {
        psGetUsers = connection.prepareStatement("SELECT * FROM users");
    }

    private static void disconnect() {
        try {
            connection.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
