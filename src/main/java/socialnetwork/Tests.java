package socialnetwork;

import socialnetwork.domain.Friendship;
import socialnetwork.domain.Tuple;
import socialnetwork.domain.User;
import socialnetwork.domain.validators.FriendshipValidator;
import socialnetwork.domain.validators.UserValidator;
import socialnetwork.repository.Repository;
import socialnetwork.repository.memory.InMemoryRepository;
import socialnetwork.service.FriendshipService;
import socialnetwork.service.UserService;

public class Tests {
    public void testAll(){
        Repository<Long, User> userMemRepository = new InMemoryRepository<>(new UserValidator());
        Repository<Tuple<Long,Long>, Friendship> friendshipMemRepository=new InMemoryRepository<>(new FriendshipValidator());
        UserService us=new UserService(userMemRepository);
        FriendshipService fs=new FriendshipService(friendshipMemRepository);
        User user=new User("a","a");
        User u2=new User("b","b");
        User u3=new User("c","c");
        User u4=new User("d","d");
        User u5=new User("e","e");
        User u6=new User("f","f");
        assert(us.addUser(user)==null);
        assert(us.addUser(u2)==null);
        assert(us.addUser(u3)==null);
        assert(us.addUser(u4)==null);
        assert(us.addUser(u5)==null);
        assert(us.addUser(u6)==null);
        assert(us.getOne((long) 1)==u1);
        assert(us.getOne((long) 2)==u2);
        Tuple<Long,Long> t1=new Tuple(u1.getId(),u2.getId());
        Tuple<Long,Long> t2=new Tuple(u1.getId(),u3.getId());
        Tuple<Long,Long> t3=new Tuple(u4.getId(),u5.getId());
        Friendship f=new Friendship();
        f.setId(t1);
        try {
            assert (fs.addFriendship(f) == null);
            assert (u1.getFriends().size() == 1);
            assert (u2.getFriends().size() == 1);
            assert (fs.addFriendship(f) == f);
            f.setId(t2);
            assert (fs.addFriendship(f) == null);
            f.setId(t3);
            assert (fs.addFriendship(f) == null);
        }
        catch(Exception e){

        }
        assert(us.nrOfCommunities()==3);
        //assert(us.mostSociable()=="1 2 3 ");
        assert(fs.deleteFriendship(t1)==f);
        assert(u1.getFriends().size()==0);
        assert(u2.getFriends().size()==0);
        assert(us.deleteUser((long) 2)==u2);
    }
}
