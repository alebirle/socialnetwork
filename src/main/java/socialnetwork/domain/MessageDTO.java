package socialnetwork.domain;

import java.time.LocalDateTime;
import java.util.List;

public class MessageDTO {
    private Long id;
    private String fname;
    private String lname;
    private String email;
    private String message;
    private LocalDateTime date;
    private String reply;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MessageDTO(Long id, String fname, String lname, String email, String message, LocalDateTime date, String reply) {
        this.id=id;
        this.fname = fname;
        this.lname = lname;
        this.email = email;
        this.message = message;
        this.date = date;
        this.reply = reply;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    @Override
    public String toString(){
        if(reply==null)
            return fname+" "+lname+" "+email+": "+message+" | "+date;
        else
            return fname+" "+lname+" "+email+": "+message+" | "+date+"| reply to: "+reply;
    }
}
