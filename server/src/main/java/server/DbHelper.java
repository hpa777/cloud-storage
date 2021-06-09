package server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DbHelper {

    private final static Logger logger = Logger.getLogger(DbHelper.class.getName());

    private Connection connection;

    private String userName;

    public String getUserName() {
        return userName;
    }

    public DbHelper(Connection connection) {
        this.connection = connection;
    }

    /**
     * Регистрируем нового пользователя
     * @param login
     * @param pass
     * @param name
     * @return
     */
    public Long registration(String login, String pass, String name) {
        try (PreparedStatement addUserQuery = connection.prepareStatement("INSERT INTO `users` (`login`, `password`, `name`) VALUES (?, MD5(?), ?)")){
            addUserQuery.setString(1, login);
            addUserQuery.setString(2, pass);
            addUserQuery.setString(3,name);
            addUserQuery.executeUpdate();
            return getUserId(login, pass);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        return null;
    }

    /**
     * Возвращает id пользователя
     * @param login
     * @param pass
     * @return
     */
    public Long getUserId(String login, String pass) {
        Long id = null;
        try (PreparedStatement getUserQuery = connection.prepareStatement("SELECT `id`, `name` FROM `users` WHERE `login` = ? AND  `password` = MD5(?)")) {
            getUserQuery.setString(1, login);
            getUserQuery.setString(2, pass);
            ResultSet resultSet = getUserQuery.executeQuery();
            if (resultSet.next()) {
                id = resultSet.getLong("id");
                userName = resultSet.getString("name");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        return id;
    }
}
