package socialnetwork.repository.database;

import socialnetwork.domain.Message;
import socialnetwork.domain.User;
import socialnetwork.domain.validators.Validator;
import socialnetwork.repository.RepoException;
import socialnetwork.repository.Repository;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

public class UserDbRepository implements Repository<Long, User> {
    private String url;
    private String username;
    private String password;
    private Validator<User> validator;

    public UserDbRepository(String url, String username, String password, Validator<User> validator) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.validator = validator;
    }

    @Override
    public Iterable<User> findSome(User u, String s) {
        Set<User> users = new HashSet<>();
        Long index= Long.parseLong(s,10)+getMin();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT users.id,first_name,last_name,email,password from users where users.id>=? order by users.id limit 7");) {
            statement.setBigDecimal(1, BigDecimal.valueOf(index));
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Long id = resultSet.getLong("id");
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                String email = resultSet.getString("email");
                String pswd = resultSet.getString("password");
                User user = new User(firstName, lastName,email,pswd);
                user.setId(id);
                users.add(user);
            }
            return users;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    private Long getMin() {
        Long id= Long.valueOf(0);
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT MIN(users.id) as min_id from users")){
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()) {
                id = resultSet.getLong("min_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }

    @Override
    public User findOne(Long aLong) {
        User user=null;
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * from users WHERE id=?")){
            statement.setBigDecimal(1, BigDecimal.valueOf(aLong));
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()) {
                Long id = resultSet.getLong("id");
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                String email = resultSet.getString("email");
                String pswd = resultSet.getString("password");
                user=new User();
                user.setFirstName(firstName);
                user.setEmail(email);
                user.setPassword(pswd);
                user.setLastName(lastName);
                user.setId(aLong);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

    @Override
    public Iterable<User> findAll() {
        Set<User> users = new HashSet<>();
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * from users");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Long id = resultSet.getLong("id");
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                String email = resultSet.getString("email");
                String pswd = resultSet.getString("password");
                User user = new User(firstName, lastName,email,pswd);
                user.setId(id);
                users.add(user);
            }
            return users;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    @Override
    public User save(User entity) {
        validator.validate(entity);
        User usr=findByEmail(entity.getEmail());
        if(usr!=null)
            return usr;
        try (Connection connection = DriverManager.getConnection(url, username, password);
             Statement statement = connection.createStatement()){
            statement.executeUpdate("INSERT INTO users(first_name,last_name,email,password) VALUES ('"+entity.getFirstName()+"','"+entity.getLastName()+"','"+entity.getEmail()+"','"+entity.getPassword()+"')");
            Long id= Long.valueOf(0);
            for(User u:findAll())
                if(u.getId()>id)
                    id=u.getId();
            entity.setId(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public User delete(Long aLong) {
        User u=findOne(aLong);
        if(u!=null&&u.getId()!=null){
            try (Connection connection = DriverManager.getConnection(url, username, password);
                 PreparedStatement statement = connection.prepareStatement("DELETE FROM users WHERE id=?")){
                statement.setBigDecimal(1, BigDecimal.valueOf(u.getId()));
                statement.executeUpdate();

            } catch (SQLException e) {
                e.printStackTrace();
            }
            return u;
        }
        else
            throw new RepoException("User doesn't exist!");
    }

    @Override
    public User update(User entity) {
        User u=findOne(entity.getId());
        if(u!=null&&u.getId()!=null){
            try (Connection connection = DriverManager.getConnection(url, username, password);
                 Statement statement = connection.createStatement()){
                 statement.executeUpdate("UPDATE users SET first_name="+u.getFirstName()+",last_name="+u.getLastName()+" WHERE id="+u.getId());

            } catch (SQLException e) {
                e.printStackTrace();
            }
            return u;
        }
        else
            throw new RepoException("User doesn't exist!");
    }

    @Override
    public int getNr() {
        int n=0;
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) AS nr FROM users");
             ResultSet resultSet = statement.executeQuery()) {
            while(resultSet.next())
                n=resultSet.getInt("nr");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n;
    }

    /**
     *
     * @param user - a user from the database
     * loads user's list of friends
     */
    public void findFriends(User user){
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * from friendships WHERE status='Accepted'");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Long id1 = resultSet.getLong("id1");
                Long id2 = resultSet.getLong("id2");
                if(user.getId()==id1){
                    User u=findOne(id2);
                    user.addFriend(u);
                }
                if(user.getId()==id2){
                    User u=findOne(id1);
                    user.addFriend(u);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public User findByEmail(String mail){
        User user=null;
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement("SELECT * from users WHERE email=?")){
            statement.setString(1,mail);
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()) {
                Long id = resultSet.getLong("id");
                String firstName = resultSet.getString("first_name");
                String lastName = resultSet.getString("last_name");
                String email = resultSet.getString("email");
                String pswd = resultSet.getString("password");
                user=new User();
                user.setFirstName(firstName);
                user.setEmail(email);
                user.setPassword(pswd);
                user.setLastName(lastName);
                user.setId(id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

    @Override
    public String getStatus(Long id1, Long id2) {
        return null;
    }
}
