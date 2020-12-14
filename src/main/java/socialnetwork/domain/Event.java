package socialnetwork.domain;

import java.time.LocalDateTime;

public class Event extends Entity<Long>{
    private String name;
    private String description;
    private Long ownerId;
    private LocalDateTime startAt;
    private LocalDateTime endAt;

    public Event() {}

    public Event(String name, String description, Long ownerId, LocalDateTime startAt, LocalDateTime endAt) {
        this.name = name;
        this.description = description;
        this.ownerId = ownerId;
        this.startAt = startAt;
        this.endAt = endAt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public LocalDateTime getStartAt() {
        return startAt;
    }

    public void setStartAt(LocalDateTime startAt) {
        this.startAt = startAt;
    }

    public LocalDateTime getEndAt() {
        return endAt;
    }

    public void setEndAt(LocalDateTime endAt) {
        this.endAt = endAt;
    }

    @Override
    public String toString() {
        return name + "\n" + description + "\nPeriod: " + startAt + " - " + endAt +"\n";
    }
}
