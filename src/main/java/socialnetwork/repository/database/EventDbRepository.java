package socialnetwork.repository.database;

import socialnetwork.domain.Entity;
import socialnetwork.domain.Event;
import socialnetwork.domain.User;
import socialnetwork.domain.validators.Validator;
import socialnetwork.repository.RepoException;
import socialnetwork.repository.Repository;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class EventDbRepository implements Repository<Long,Event> {
    private String url;
    private String username;
    private String password;

    public EventDbRepository(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    @Override
    public Iterable<Event> findSome(User u, String s) {
        Set<Event> events=new HashSet<>();
        String[] s2=s.split(" ");
        String query="";
        if(s2[0].equals("Going")&&s2[1].equals("Upcoming"))
            query="SELECT * FROM events e INNER JOIN users_events ue ON e.id=ue.event_id where ue.user_id="+u.getId()+"and e.end_date>'"+LocalDateTime.now()+"' order by e.end_date desc offset "+s2[2]+" limit 3";
        if(s2[0].equals("Going")&&s2[1].equals("Past"))
            query="SELECT * FROM events e INNER JOIN users_events ue ON e.id=ue.event_id where ue.user_id="+u.getId()+"and e.end_date<'"+LocalDateTime.now()+"' order by e.end_date desc offset "+s2[2]+" limit 3";
        if(s2[0].equals("Going")&&s2[1].equals("Anytime"))
            query="SELECT * FROM events e INNER JOIN users_events ue ON e.id=ue.event_id where ue.user_id="+u.getId()+" order by e.end_date desc offset "+s2[2]+" limit 3";
        if(s2[0].equals("Yours")&&s2[1].equals("Upcoming"))
            query="SELECT * FROM events where owner="+u.getId()+" and events.end_date>'"+LocalDateTime.now()+"' order by events.end_date desc offset "+s2[2]+" limit 3";
        if(s2[0].equals("Yours")&&s2[1].equals("Past"))
            query="SELECT * FROM events where owner="+u.getId()+" and events.end_date<'"+LocalDateTime.now()+"' order by events.end_date desc offset "+s2[2]+" limit 3";
        if(s2[0].equals("Yours")&&s2[1].equals("Anytime"))
            query="SELECT * FROM events where owner="+u.getId()+" order by events.end_date desc offset "+s2[2]+" limit 3";
        if(s2[0].equals("All")&&s2[1].equals("Upcoming"))
            query="SELECT * FROM events where end_date>'"+LocalDateTime.now()+"' order by events.end_date desc offset "+s2[2]+" limit 3";
        if(s2[0].equals("All")&&s2[1].equals("Past"))
            query="SELECT * FROM events where end_date<'"+LocalDateTime.now()+"' order by events.end_date desc offset "+s2[2]+" limit 3";
        if(s2[0].equals("All")&&s2[1].equals("Anytime"))
            query="SELECT * FROM events order by events.end_date desc offset "+s2[2]+" limit 3";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(query)){
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()) {
                Long idEvent = resultSet.getLong("id");
                String name = resultSet.getString("name");
                String description = resultSet.getString("description");
                Long owner = resultSet.getLong("owner");
                LocalDateTime start_date = resultSet.getTimestamp("start_date").toLocalDateTime();
                LocalDateTime end_date = resultSet.getTimestamp("end_date").toLocalDateTime();
                Event event=new Event(name,description,owner,start_date,end_date);
                event.setId(idEvent);
                events.add(event);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return events;
    }

    @Override
    public Event findOne(Long id) {
        Event event=null;
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * from events WHERE id=?")){
            statement.setBigDecimal(1, BigDecimal.valueOf(id));
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()) {
                Long idEvent = resultSet.getLong("id");
                String name = resultSet.getString("name");
                String description = resultSet.getString("description");
                Long owner = resultSet.getLong("owner");
                LocalDateTime start_date = resultSet.getTimestamp("start_date").toLocalDateTime();
                LocalDateTime end_date = resultSet.getTimestamp("end_date").toLocalDateTime();
                event=new Event(name,description,owner,start_date,end_date);
                event.setId(idEvent);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return event;
    }

    @Override
    public Iterable<Event> findAll() {
        Set<Event> events=new HashSet<>();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * from events")){
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()) {
                Long idEvent = resultSet.getLong("id");
                String name = resultSet.getString("name");
                String description = resultSet.getString("description");
                Long owner = resultSet.getLong("owner");
                LocalDateTime start_date = resultSet.getTimestamp("start_date").toLocalDateTime();
                LocalDateTime end_date = resultSet.getTimestamp("end_date").toLocalDateTime();
                Event event=new Event(name,description,owner,start_date,end_date);
                event.setId(idEvent);
                events.add(event);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return events;
    }

    @Override
    public Event save(Event entity) {
        Event event=null;
        /*Event e=findOne(entity.getId());
        if(e!=null)
            return e;*/
        try (Connection connection = DriverManager.getConnection(url, username, password);
             Statement statement = connection.createStatement()){
            statement.executeUpdate("INSERT INTO events(name,description,owner,start_date,end_date) VALUES ('"+entity.getName()+"','"+entity.getDescription()+"',"+entity.getOwnerId()+",'"+entity.getStartAt()+"','"+entity.getEndAt()+"')");
            PreparedStatement pStatement = connection.prepareStatement("SELECT max(id) as maximum from events ");
                ResultSet resultSet = pStatement.executeQuery();
                Event e=new Event();
                while(resultSet.next()){
                    e.setId(resultSet.getLong("maximum"));
                }
            statement.executeUpdate("INSERT INTO users_events(user_id,event_id,notify,last_seen) values ("+entity.getOwnerId()+","+e.getId()+",true,10)");
        }
        catch (Exception exception){
            exception.printStackTrace();
        }
        return null;
    }

        @Override
    public Event delete(Long id) {
        Event event=findOne(id);
        if(event!=null&&event.getId()!=null){
            try (Connection connection = DriverManager.getConnection(url, username, password);
                 PreparedStatement statement = connection.prepareStatement("DELETE FROM users_events WHERE event_id=?; DELETE FROM events WHERE id=?")){
                statement.setBigDecimal(1, BigDecimal.valueOf(event.getId()));
                statement.setBigDecimal(2, BigDecimal.valueOf(event.getId()));
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return event;
        }
        else
            throw new RepoException("Event doesn't exist!");
    }

    @Override
    public Event update(Event entity) {
        Event event=findOne(entity.getId());
        if(event!=null&&event.getId()!=null){
            try (Connection connection = DriverManager.getConnection(url, username, password);
                 Statement statement = connection.createStatement()){
                statement.executeUpdate("UPDATE events SET name='"+event.getName()+"',description='"+event.getDescription()+"',start_date='"+event.getStartAt()+"',end_date='"+entity.getEndAt()+"' WHERE id="+event.getId());

            } catch (SQLException e) {
                e.printStackTrace();
            }
            return event;
        }
        else
            throw new RepoException("Event doesn't exist!");
    }

    @Override
    public int getNr() {
        int nr=0;
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT count(id) as nr from events")){
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

    @Override
    public void findFriends(Event entity) {

    }

    @Override
    public Event findByEmail(String mail) {
        return null;
    }

    @Override
    public String getStatus(Long id1, Long id2) {
        return null;
    }
}
