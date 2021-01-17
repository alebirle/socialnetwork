package socialnetwork.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class UserFriendshipDTO extends Entity<Long>{
    private String fname;
    private String lname;
    private String email;
    private LocalDate date;
    private String status;

    public UserFriendshipDTO(String fname,String lname,String email,LocalDate date,String status){
        this.fname=fname;
        this.lname=lname;
        this.email=email;
        this.date=date;
        this.status=status;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getLname() {
        return lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
