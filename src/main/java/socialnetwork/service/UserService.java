package socialnetwork.service;

import socialnetwork.domain.Tuple;
import socialnetwork.domain.User;
import socialnetwork.repository.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class UserService {
    private Repository<Long, User> repo;

    public UserService(Repository<Long, User> repo) {
        this.repo = repo;
    }

    /**
     *
     * @param messageTask - the user that has to be added
     * @return a user, if it already existed
     *          or null, if the user was added succesfully
     */
    public User addUser(User messageTask) {
        Long m= Long.valueOf(0);
        for(User u:repo.findAll()){
            if(u.getId()>m)
                m=u.getId();
        }
        messageTask.setId(m+1);
        User task = repo.save(messageTask);
        return task;
    }

    /**
     *
     * @param id - the id of the user
     * @return the user with the given id
     */
    public User getOne(Long id){
        return repo.findOne(id);
    }

    /**
     *
     * @param email - the email adress of the user
     * @return the user with the given email adress
     */
    public User getByEmail(String email){
        return repo.findByEmail(email);
    }

    /**
     *
     * @return all of the existing users
     */
    public Iterable<User> getAll(){
        return repo.findAll();
    }

    /**
     *
     * @param id - the id of the user that has to be deleted
     * @return the deleted user
     *          or null, if it didn't exist in the first place
     */
    public User deleteUser(Long id){
        User user=repo.delete(id);
        return user;
    }

    /**
     *
     * @param u - the user that's currently being visited
     * @param visited - array that shows which users have already been visited
     * @return list of user ids that were visited
     */
    public String dfs(User u, boolean[] visited){
        String x=u.getId()+" ";
        visited[Math.toIntExact(u.getId())]=true;
        if(u.getFriends().size()==0)
            repo.findFriends(u);
        for(User f:u.getFriends()){
            if(!visited[Math.toIntExact(f.getId())]) {
                x+=dfs(f, visited);
            }
        }
        return x;
    }

    /**
     *
     * @return number of communities (connected components in the friendship graph)
     */
    public int nrOfCommunities(){
        boolean[] visited=new boolean[max()+1];
        int nr=0;
        for(User u:getAll()){
            if(u.getFriends().size()==0)
                repo.findFriends(u);
            if(!visited[Math.toIntExact(u.getId())]){
                dfs(u,visited);
                nr++;
            }
        }
        return nr;
    }

    public void getFriends(User user){
        repo.findFriends(user);
    }
    public int max(){
        int max=0;
        for(User u:repo.findAll()){
            if(u.getId()>max)
                max= Math.toIntExact(u.getId());
        }
        return max;
    }

    public List<User> getUsers(String f, String l){
        List<User> us=new ArrayList<>();
        for(User u: repo.findAll()){
            if(f.equals(u.getFirstName())&&l.equals(u.getLastName()))
                us.add(u);
        }
        return us;
    }

    public Iterable<User> getSome(User logged, String s) {
        return repo.findSome(logged,s);
    }
}
