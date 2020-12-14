package socialnetwork.domain;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class Friendship extends Entity<Tuple<Long,Long>> {

    private LocalDateTime date;
    private String status;

    public Friendship() {
        status="Pending";
        date= LocalDateTime.now();
    }
    public Friendship(Long f1,Long f2){
        Tuple<Long,Long> t = new Tuple(f1,f2);
        setId(t);
        date= LocalDateTime.now();
    }
    /**
     *
     * @return the date when the friendship was created
     */
    public LocalDateTime getDate() {
        return date;
    }
    public void setDate(LocalDateTime d){date=d;}
    public String getStatus(){return status;}
    public void setStatus(String s){status=s;}

    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (!(o instanceof Friendship)) return false;
        Friendship that = (Friendship) o;
        return getId().getLeft().equals(that.getId().getLeft())&&getId().getRight().equals(that.getId().getRight())||
                getId().getRight().equals(that.getId().getLeft())&&getId().getLeft().equals(that.getId().getRight());
    }
}
