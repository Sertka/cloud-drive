package ru.stk.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.stk.common.Settings;
import ru.stk.data.User;

import java.sql.*;
import java.util.ArrayList;

/**
 * Provides DB connection
 */
public class DBService {
    private static Connection connection;
    private static PreparedStatement psGetUsers;

    private static final Logger logger = LogManager.getLogger(AuthController.class);

    /*
     * Writes all users to collection and returns it
     */
    public static ArrayList<User> getUsers() {
        ArrayList<User> alUsers = new ArrayList<>();
        try {
            connect();
            initPSGetUsers();

            ResultSet rs = psGetUsers.executeQuery();

            while (rs.next()) {
                alUsers.add(new User(rs.getString(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4)));
            }
            return alUsers;
        } catch (Exception e) {
            logger.error("DB connection problem - " + e.getMessage());
        } finally {
            disconnect();
        }
        return null;
    }

    /*
     * Establishes DB connection
     */
    private static void connect() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection(Settings.DB_PATH);
    }

    /*
     * DB request
     */
    private static void initPSGetUsers() throws SQLException {
        psGetUsers = connection.prepareStatement("SELECT * FROM users");
    }

    /*
     * Closes connection
     */
    private static void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            logger.error("DB connection not closed - " + e.getMessage());
        }
    }
}
