package socialnetwork.repository.database;

import socialnetwork.domain.Event;
import socialnetwork.repository.RepoException;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;

public class UserEventDbRepository {
    private String url;
    private String username;
    private String password;

    public UserEventDbRepository(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public boolean findNotification(Long idUser,Long idEvent){
        boolean notif=false;
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * from users_events WHERE user_id=? and event_id=?")){
            statement.setBigDecimal(1, BigDecimal.valueOf(idUser));
            statement.setBigDecimal(2, BigDecimal.valueOf(idEvent));
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()) {
                notif=resultSet.getBoolean("notify");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notif;
    }

    public boolean findOne(Long idUser,Long idEvent) {
        boolean exists=false;
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * from users_events WHERE user_id=? and event_id=?")){
            statement.setBigDecimal(1, BigDecimal.valueOf(idUser));
            statement.setBigDecimal(2, BigDecimal.valueOf(idEvent));
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()) {
                exists=true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return exists;
    }
    public boolean save(Long idUser,Long idEvent) {
        if(findOne(idUser,idEvent))
            return false;
        try (Connection connection = DriverManager.getConnection(url, username, password);
             Statement statement = connection.createStatement()){
            statement.executeUpdate("INSERT INTO users_events(user_id,event_id,notify) VALUES ("+idUser+","+idEvent+",TRUE)");
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return true;
    }
    public boolean delete(Long idUser,Long idEvent) {
        if(findOne(idUser,idEvent)){
            try (Connection connection = DriverManager.getConnection(url, username, password);
                 PreparedStatement statement = connection.prepareStatement("DELETE FROM users_events WHERE user_id=? and event_id=?")){
                statement.setBigDecimal(1, BigDecimal.valueOf(idUser));
                statement.setBigDecimal(2, BigDecimal.valueOf(idEvent));
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return true;
        }
        else
            throw new RepoException("Relationship doesn't exist!");
    }
    public boolean update(Long idUser, Long idEvent, boolean notify) {
        if(findOne(idUser,idEvent)){
            try (Connection connection = DriverManager.getConnection(url, username, password);
                 Statement statement = connection.createStatement()){
                statement.executeUpdate("UPDATE users_events SET notify="+notify+" WHERE user_id="+idUser+"and event_id="+idEvent);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return true;
        }
        else
            throw new RepoException("Relationship doesn't exist!");
    }
}
