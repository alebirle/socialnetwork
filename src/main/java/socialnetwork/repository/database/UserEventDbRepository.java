package socialnetwork.repository.database;

import socialnetwork.domain.Event;
import socialnetwork.domain.User;
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
    public int findLastSeen(Long idUser,Long idEvent){
        int ls=-1;
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * from users_events WHERE user_id=? and event_id=?")){
            statement.setBigDecimal(1, BigDecimal.valueOf(idUser));
            statement.setBigDecimal(2, BigDecimal.valueOf(idEvent));
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()) {
                    ls=resultSet.getInt("last_seen");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ls;
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
            statement.executeUpdate("INSERT INTO users_events(user_id,event_id,notify,last_seen) VALUES ("+idUser+","+idEvent+",TRUE,10)");
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
    public boolean update(Long idUser, Long idEvent, boolean notify, int lastSeen) {
        if(findOne(idUser,idEvent)){
            try (Connection connection = DriverManager.getConnection(url, username, password);
                 Statement statement = connection.createStatement()){
                statement.executeUpdate("UPDATE users_events SET notify="+notify+", last_seen="+lastSeen+" WHERE user_id="+idUser+"and event_id="+idEvent);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return true;
        }
        else
            throw new RepoException("Relationship doesn't exist!");
    }
    public int getNr(User u, String which, String when)
    {
        int nr=0;
        String query="";
        if(which.equals("All")&&when.equals("Anytime"))
            query="SELECT count(id) as nr FROM events";
        if(which.equals("All")&&when.equals("Past"))
            query="SELECT count(id) as nr FROM events where end_date<'"+LocalDateTime.now()+"'";
        if(which.equals("All")&&when.equals("Upcoming"))
            query="SELECT count(id) as nr FROM events where end_date>'"+LocalDateTime.now()+"'";
        if(which.equals("Yours")&&when.equals("Anytime"))
            query="SELECT count(id) as nr FROM events where owner="+u.getId();
        if(which.equals("Yours")&&when.equals("Past"))
            query="SELECT count(id) as nr FROM events where end_date<'"+LocalDateTime.now()+"' and owner="+u.getId();
        if(which.equals("Yours")&&when.equals("Upcoming"))
            query="SELECT count(id) as nr FROM events where end_date>'"+LocalDateTime.now()+"' and owner="+u.getId();
        if(which.equals("Going")&&when.equals("Anytime"))
            query="SELECT count(id) as nr FROM events e inner join users_events ue on e.id=ue.event_id where ue.user_id="+u.getId();
        if(which.equals("Going")&&when.equals("Past"))
            query="SELECT count(id) as nr FROM events e inner join users_events ue on e.id=ue.event_id where ue.user_id="+u.getId()+" and e.end_date<'"+LocalDateTime.now()+"'";
        if(which.equals("Going")&&when.equals("Upcoming"))
            query="SELECT count(id) as nr FROM events e inner join users_events ue on e.id=ue.event_id where ue.user_id="+u.getId()+" and e.end_date>'"+LocalDateTime.now()+"'";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(query)){
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()) {
                nr=resultSet.getInt("nr");
            }
        }
        catch (SQLException e){
            e.printStackTrace();
        }
        return nr;
    }
}
