package socialnetwork.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ConversationDTO {
    private String from;
    private String to;
    private String message;
    private LocalDate date;
    private String reply;

    public ConversationDTO(String from, String to, String message, LocalDate date, String reply) {
        this.from = from;
        this.to=to;
        this.message = message;
        this.date = date;
        this.reply = reply;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String fname) {
        this.from = fname;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    @Override
    public String toString(){
        if(reply==null)
            return from+" "+to+": "+message+" | "+date;
        else
            return from+" "+to+": "+message+" | "+date+"| reply to: "+reply;
    }
}
