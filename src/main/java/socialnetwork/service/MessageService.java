package socialnetwork.service;

import socialnetwork.domain.Message;
import socialnetwork.domain.User;
import socialnetwork.repository.Repository;

public class MessageService {
    private Repository<Long, Message> repo;

    public MessageService(Repository<Long,Message> r){
        repo=r;
    }

    public Message save(Message message){
        return repo.save(message);
    }
    public Message getOne(Long id){
        return repo.findOne(id);
    }

    public Iterable<Message> getAll(){
        return repo.findAll();
    }

    public Iterable<Message> getSome(User u, String s) {
        return repo.findSome(u,s);
    }
}
