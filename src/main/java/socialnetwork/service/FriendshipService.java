package socialnetwork.service;

import socialnetwork.domain.Friendship;
import socialnetwork.domain.Tuple;
import socialnetwork.domain.User;
import socialnetwork.repository.Repository;

import java.util.ArrayList;
import java.util.List;

public class FriendshipService {
    private Repository<Tuple<Long,Long>, Friendship> repo;

    public FriendshipService(Repository<Tuple<Long,Long>, Friendship> repo) {
        this.repo = repo;
    }

    /**
     *
     * @param f -the friendship that has to be added
     *           f must not be null
     * @return a friendship - if it already existed
     *          or null - if the friendship was added
     */
    public Friendship addFriendship(Friendship f){
        Tuple<Long, Long> t=new Tuple(f.getId().getRight(),f.getId().getLeft());
        Friendship k=getOne(t);
        if(k!=null&&k.getId()!=null&&!k.getStatus().equals("Rejected"))
            return k;
        Friendship fr = repo.save(f);
        return fr;
    }

    /**
     *
     * @param t - the id of the friendship
     * @return the friendship with the specified id
     */
    public Friendship getOne(Tuple<Long,Long> t){
        Friendship f= repo.findOne(t);
        if(f.getId()==null) {
            Tuple<Long, Long> t2 = new Tuple(t.getRight(), t.getLeft());
            return repo.findOne(t2);
        }
        else
            return f;
    }

    /**
     *
     * @return all existing friendships
     */
    public Iterable<Friendship> getAll(){
        return repo.findAll();
    }

    /**
     *
     * @param id - the id of the friendship
     * @return the friendship that was deleted
     *          or null, if it didn't exist in the first place
     */
    public Friendship deleteFriendship(Tuple<Long,Long> id){
        Friendship f=repo.delete(id);
        return f;
    }

    public Friendship update(Friendship f){
        return repo.update(f);
    }

    public String getStatus(Long id1, Long id2){
        return repo.getStatus(id1,id2);
    }

    public Iterable<Friendship> getSome(User u, String s) {
        return repo.findSome(u,s);
    }
}
