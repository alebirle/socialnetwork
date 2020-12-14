package socialnetwork.service;

import socialnetwork.domain.Event;
import socialnetwork.repository.Repository;
import socialnetwork.repository.database.UserEventDbRepository;

public class EventService {
    private Repository<Long, Event> eRepo;
    private UserEventDbRepository ueRepo;

    public EventService(Repository<Long, Event> eRepo, UserEventDbRepository ueRepo) {
        this.eRepo = eRepo;
        this.ueRepo = ueRepo;
    }

    public Iterable<Event> getAll(){
        return eRepo.findAll();
    }
    public Event save(Event e){
        if(e.getName().contains("'"))
            e.setName(e.getName().replaceAll("'", "$0$0"));
        if(e.getDescription().contains("'"))
            e.setDescription(e.getDescription().replaceAll("'", "$0$0"));
        return eRepo.save(e);
    }
    public Event delete(Event e){
        return eRepo.delete(e.getId());
    }
    public Event update(Event e){
        return eRepo.update(e);
    }
    public Event getOne(Long id){
        return eRepo.findOne(id);
    }
    public boolean setNotify(Long idUser, Long idEvent, boolean notify){
        return ueRepo.update(idUser,idEvent,notify);
    }
    public boolean addParticipant(Long idUser, Long idEvent){
        return ueRepo.save(idUser, idEvent);
    }
    public boolean removeParticipant(Long idUser, Long idEvent){
        return ueRepo.delete(idUser, idEvent);
    }
    public boolean getNotifications(Long idUser,Long idEvent){return ueRepo.findNotification(idUser,idEvent);}
    public boolean getExists(Long idUser, Long idEvent){return ueRepo.findOne(idUser,idEvent);}
}
