package socialnetwork.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class User extends Entity<Long>{
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private List<User> friends;

    public User(){friends=new ArrayList<>();}
    public User(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
        friends=new ArrayList<>();
    }
    public User(String firstName, String lastName, String email,String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email=email;
        this.password=password;
        friends=new ArrayList<>();
    }
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public List<User> getFriends() {
        return friends;
    }

    public void emptyFriends(){friends.clear();}

    public void addFriend(User friend){
        friends.add(friend);
    }

    @Override
    public String toString() {
        return "User{id=" +getId()+'\''+
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                //", friends=" + friends +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User that = (User) o;
        /*return getFirstName().equals(that.getFirstName()) &&
                getLastName().equals(that.getLastName()) &&
                getFriends().equals(that.getFriends());*/
        return getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFirstName(), getLastName(), getFriends());
    }

    public void remove(User f) {
        friends.remove(f);
    }
}