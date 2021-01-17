package socialnetwork.repository.database;

import socialnetwork.domain.Message;
import socialnetwork.domain.User;
import socialnetwork.domain.validators.Validator;
import socialnetwork.repository.Repository;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class MessageDbRepository implements Repository<Long, Message> {
    private String url;
    private String username;
    private String password;
    private Validator<Message> validator;

    public MessageDbRepository(String url, String username, String password, Validator<Message> validator) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.validator = validator;
    }
    @Override
    public Iterable<Message> findSome(User u, String s){
        Set<Message> msgs=new HashSet<>();
        String query="SELECT * from messages";
        if(s.equals("from"))
            query+=" where m_from=?";
        else {
            if (s.equals("to"))
                query += " where m_to=?";
            else
                query += " where m_from=? and m_to="+u.getEmail()+" or m_to="+u.getId()+" and m_from="+u.getEmail()+" order by id desc fetch first "+s+" rows only";
        }
        try (Connection connection = DriverManager.getConnection(url, username, password);
                PreparedStatement statement = connection.prepareStatement(query)){
            statement.setBigDecimal(1, BigDecimal.valueOf(u.getId()));
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()) {
                Long id = resultSet.getLong("id");
                Long idFrom=resultSet.getLong("m_from");
                Long idTo=resultSet.getLong("m_to");
                String message = resultSet.getString("message");
                LocalDateTime date=resultSet.getTimestamp("m_date").toLocalDateTime();
                Long idReply=resultSet.getLong("reply");
                User from=new User();
                from.setId(idFrom);
                User to=new User();
                to.setId(idTo);
                Message msg=new Message();
                msg.setId(id);
                msg.setDate(date);
                msg.setFrom(from);
                msg.setMessage(message);
                if(idReply!=null)
                    msg.setReply(findOne(idReply));
                msg.setTo(Collections.singletonList(to));
                msgs.add(msg);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return msgs;
    }
    @Override
    public Message findOne(Long aLong) {
        Message msg=null;
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * from messages WHERE id=?")){
            statement.setBigDecimal(1, BigDecimal.valueOf(aLong));
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()) {
                Long id = resultSet.getLong("id");
                Long idFrom=resultSet.getLong("m_from");
                Long idTo=resultSet.getLong("m_to");
                String message = resultSet.getString("message");
                LocalDateTime date=resultSet.getTimestamp("m_date").toLocalDateTime();
                Long idReply=resultSet.getLong("reply");
                User from=new User();
                from.setId(idFrom);
                User to=new User();
                to.setId(idTo);
                msg=new Message();
                msg.setId(aLong);
                msg.setDate(date);
                msg.setFrom(from);
                msg.setMessage(message);
                if(idReply!=null)
                    msg.setReply(findOne(idReply));
                msg.setTo(Collections.singletonList(to));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return msg;
    }

    @Override
    public Iterable<Message> findAll() {
        Set<Message> msgs=new HashSet<>();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * from messages")){
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()) {
                Long id = resultSet.getLong("id");
                Long idFrom=resultSet.getLong("m_from");
                Long idTo=resultSet.getLong("m_to");
                String message = resultSet.getString("message");
                LocalDateTime date=resultSet.getTimestamp("m_date").toLocalDateTime();
                Long idReply=resultSet.getLong("reply");
                User from=new User();
                from.setId(idFrom);
                User to=new User();
                to.setId(idTo);
                Message msg=new Message();
                msg.setId(id);
                msg.setDate(date);
                msg.setFrom(from);
                msg.setMessage(message);
                if(idReply!=null)
                    msg.setReply(findOne(idReply));
                msg.setTo(Collections.singletonList(to));
                msgs.add(msg);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return msgs;
    }

    @Override
    public Message save(Message entity) {
        validator.validate(entity);
        for(User u:entity.getTo()) {
            try (Connection connection = DriverManager.getConnection(url, username, password);
                 Statement statement = connection.createStatement()) {
                if(entity.getReply()!=null)
                    statement.executeUpdate("INSERT INTO messages(m_from,m_to,message,m_date,reply) VALUES ('" + entity.getFrom().getId() + "','" + u.getId() + "','"+entity.getMessage()+"','"+LocalDateTime.now()+"','"+entity.getReply().getId()+"')");
                else
                    statement.executeUpdate("INSERT INTO messages(m_from,m_to,message,m_date) VALUES ('" + entity.getFrom().getId() + "','" + u.getId() + "','"+entity.getMessage()+"','"+LocalDateTime.now()+"')");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public Message delete(Long aLong) {
        return null;
    }

    @Override
    public Message update(Message entity) {
        return null;
    }

    @Override
    public int getNr() {
        int nr=0;
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT count(id) as nr from messages")){
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()) {
                nr= resultSet.getInt("nr");
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return nr;
    }

    @Override
    public void findFriends(Message entity) {

    }

    @Override
    public Message findByEmail(String mail) {
        return null;
    }

    @Override
    public String getStatus(Long id1, Long id2) {
        return null;
    }
}
