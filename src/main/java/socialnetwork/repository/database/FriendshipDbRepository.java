package socialnetwork.repository.database;

import socialnetwork.domain.Friendship;
import socialnetwork.domain.Message;
import socialnetwork.domain.Tuple;
import socialnetwork.domain.User;
import socialnetwork.domain.validators.Validator;
import socialnetwork.repository.Repository;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class FriendshipDbRepository implements Repository<Tuple<Long,Long>, Friendship> {
    private String url;
    private String username;
    private String password;
    private Validator<Friendship> validator;

    public FriendshipDbRepository(String url, String username, String password, Validator<Friendship> validator) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.validator = validator;
    }

    @Override
    public Iterable<Friendship> findSome(User u, String s) {
        String query="";
        Long index;
        if(s.equals("All")) {
            query = "SELECT id1,id2,friendships.date,status from friendships where (id1=? or id2=?) and status='Accepted' order by id1+id2 offset ?";
            index= Long.valueOf(0);
        }
        else {
            index = Long.parseLong(s, 10);//+getMin();
            query = "SELECT id1,id2,friendships.date,status from friendships where (id1=? or id2=?) and status='Accepted' order by id1+id2 offset ? limit 7";
        }
        Set<Friendship> f = new HashSet<>();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(query)){
            statement.setBigDecimal(1, BigDecimal.valueOf(u.getId()));
            statement.setBigDecimal(2, BigDecimal.valueOf(u.getId()));
            statement.setBigDecimal(3, BigDecimal.valueOf(index));
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()) {
                Long id1 = resultSet.getLong("id1");
                Long id2 = resultSet.getLong("id2");
                LocalDateTime d=resultSet.getTimestamp("date").toLocalDateTime();
                String status=resultSet.getString("status");
                Friendship fr = new Friendship(id1,id2);
                fr.setDate(d);
                fr.setStatus(status);
                f.add(fr);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return f;
    }

    private Long getMin() {
        Long min= Long.valueOf(0);
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT min(id1) as min_id1,min(id2) as min_id2 from friendships")){
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()) {
                Long id1 = resultSet.getLong("min_id1");
                Long id2 = resultSet.getLong("min_id2");
                min=id1+id2;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return min;
    }

    @Override
    public Friendship findOne(Tuple<Long, Long> t) {
        Friendship f=new Friendship();
        f.setId(null);
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * from friendships WHERE id1=? AND id2=? OR id1=? AND id2=?")){
            statement.setBigDecimal(1, BigDecimal.valueOf(t.getLeft()));
            statement.setBigDecimal(2, BigDecimal.valueOf(t.getRight()));
            statement.setBigDecimal(4, BigDecimal.valueOf(t.getLeft()));
            statement.setBigDecimal(3, BigDecimal.valueOf(t.getRight()));
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()) {
                Long id1 = resultSet.getLong("id1");
                Long id2 = resultSet.getLong("id2");
                LocalDateTime d=resultSet.getTimestamp("date").toLocalDateTime();
                String s=resultSet.getString("status");
                f.setId(t);
                f.setStatus(s);
                f.setDate(d);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return f;
    }

    @Override
    public Iterable<Friendship> findAll() {
        Set<Friendship> f = new HashSet<>();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * from friendships")){
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()) {
                Long id1 = resultSet.getLong("id1");
                Long id2 = resultSet.getLong("id2");
                LocalDateTime d=resultSet.getTimestamp("date").toLocalDateTime();
                String s=resultSet.getString("status");
                Friendship fr = new Friendship(id1,id2);
                fr.setDate(d);
                fr.setStatus(s);
                f.add(fr);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return f;
    }

    @Override
    public Friendship save(Friendship entity) {
        Friendship f=findOne(entity.getId());
        if(f.getId()==null){
            try (Connection connection = DriverManager.getConnection(url, username, password);
                 Statement statement = connection.createStatement()){
                statement.executeUpdate("INSERT INTO friendships(id1,id2,date,status) VALUES ('"+entity.getId().getLeft()+"','"+entity.getId().getRight()+"','"+LocalDateTime.now()+"','Pending')");

            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }
        else
            if(f.getStatus()!=null&&f.getStatus().equals("Rejected")){
                f.setStatus("Pending");
                f.setDate(LocalDateTime.now());
                update(f);
                return null;
            }
            else
                return f;
    }

    @Override
    public Friendship delete(Tuple<Long, Long> t) {
        Friendship f=findOne(t);
        if(f.getId()!=null){
            try (Connection connection = DriverManager.getConnection(url, username, password);
                 PreparedStatement statement = connection.prepareStatement("DELETE FROM friendships WHERE id1=? AND id2=? OR id1=? AND id2=?")){
                statement.setBigDecimal(1, BigDecimal.valueOf(f.getId().getLeft()));
                statement.setBigDecimal(2, BigDecimal.valueOf(f.getId().getRight()));
                statement.setBigDecimal(4, BigDecimal.valueOf(f.getId().getLeft()));
                statement.setBigDecimal(3, BigDecimal.valueOf(f.getId().getRight()));
                statement.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
            }
            return f;
        }
        else
            return null;
    }

    @Override
    public Friendship update(Friendship entity) {
        Friendship f=findOne(entity.getId());
        if(f.getId()!=null){
            try (Connection connection = DriverManager.getConnection(url, username, password);
                 Statement statement = connection.createStatement()){
                statement.executeUpdate("UPDATE friendships SET status='"+entity.getStatus()+"', date='"+entity.getDate()+"' WHERE id1="+f.getId().getLeft()+" AND id2="+f.getId().getRight());

            } catch (SQLException e) {
                e.printStackTrace();
            }
            return f;
        }
        else
            return null;
    }

    @Override
    public int getNr() {
        return 0;
    }
    @Override
    public void findFriends(Friendship f){}

    @Override
    public Friendship findByEmail(String mail) {
        return null;
    }

    @Override
    public String getStatus(Long id1,Long id2){
        String status="None";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT status from friendships where id1=? and id2=? or id1=? and id2=? ")) {
            statement.setBigDecimal(1, BigDecimal.valueOf(id1));
            statement.setBigDecimal(2, BigDecimal.valueOf(id2));
            statement.setBigDecimal(3, BigDecimal.valueOf(id2));
            statement.setBigDecimal(4, BigDecimal.valueOf(id1));
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                status= resultSet.getString("status");
            }
        }
        catch(SQLException e) {
            e.printStackTrace();
        }
        return status;
    }
}
