package socialnetwork.ui;

import socialnetwork.domain.Friendship;
import socialnetwork.domain.Message;
import socialnetwork.domain.Tuple;
import socialnetwork.domain.User;
import socialnetwork.service.FriendshipService;
import socialnetwork.service.MessageService;
import socialnetwork.service.UserService;

import java.time.LocalDateTime;
import java.util.*;

public class ConsoleCommands {
    private UserService usrv;
    public User logged=new User();
    private FriendshipService fsrv;
    private MessageService msrv;
    public Scanner scan = new Scanner(System.in);
    public ConsoleCommands(UserService u, FriendshipService f, MessageService m){
        usrv=u;
        fsrv=f;
        msrv=m;
    }
    public User addUser(){
        System.out.println("Give first name:");
        //scan.nextLine();
        String fname=scan.nextLine();
        System.out.println("Give last name:");
        String lname=scan.nextLine();
        User u=new User(fname,lname);
        User rez=usrv.addUser(u);
        if(rez==null)
            System.out.println("User added!");
        else
            System.out.println("User already exists!");
        return usrv.getOne(u.getId());
    }
    public void removeUser(Long id){
        for(Friendship f: fsrv.getAll()){
            if((f.getId().getLeft()==id)||(f.getId().getRight()==id)) {
                fsrv.deleteFriendship(f.getId());
                /*User u1 = usrv.getOne(f.getId().getLeft());
                User u2 = usrv.getOne(f.getId().getRight());
                u1.remove(u2);
                u2.remove(u1);*/
            }
        }
        User d=usrv.deleteUser(id);
        if(d==null)
            System.out.println("User doesn't exist!");
        else {
            System.out.println(d.toString() + " was removed.");
        }
    }
    public void addFriendship(Long f1) throws Exception{
        System.out.println("Give the id of the second user:");
        Long f2= scan.nextLong();
        if(usrv.getOne(f1)==null||usrv.getOne(f2)==null){
            System.out.println("Non-existent user!");
            return;
        }
        Friendship f=new Friendship();
        Tuple<Long,Long> t=new Tuple(f1,f2);
        f.setId(t);
        Friendship rez=fsrv.addFriendship(f);
        if(rez==null) {
            System.out.println("Request sent!");
            User u1=usrv.getOne(f1);
            User u2=usrv.getOne(f2);
            u1.addFriend(u2);
            u2.addFriend(u1);
        }
        else
            System.out.println("Friendship already exists!");
    }
    public void removeFriendship(Long f1) {
        System.out.println("Give the id of the second user:");
        Long f2= scan.nextLong();
        Tuple<Long,Long> f=new Tuple(f1,f2);
        Friendship rez=fsrv.deleteFriendship(f);
        if(rez==null)
            System.out.println("Friendship doesn't exist!");
        else {
            System.out.println(rez.toString() + " was removed.");
            User u1=usrv.getOne(f1);
            User u2=usrv.getOne(f2);
            u1.remove(u2);
            u2.remove(u1);
        }
    }

    /**
     * logs into a certain user's account
     */
    public void login(){
        System.out.println("Give first name:");
        scan.nextLine();
        String fname=scan.nextLine();
        if(fname.equals("admin")) {
            logged.setId(Long.valueOf(0));
        }
        else {
            System.out.println("Give last name:");
            String lname = scan.nextLine();
            List<User> us = usrv.getUsers(fname, lname);
            if (us.size() == 0) {
                System.out.println("User doesn't exist!");
            } else {
                for (User u : us)
                    System.out.println(u.toString());
                System.out.println("Give user id:");
                Long id = scan.nextLong();
                User k = new User(fname, lname);
                k.setId(id);
                if (us.contains(k)) {
                    logged = usrv.getOne(id);
                    System.out.println(logged.toString() + " succesfully logged!");
                }
                else{
                    System.out.println("User doesn't exist!");
                }
            }
        }
    }

    /**
     *
     * @param u - user
     * @throws Exception
     * shows all of the user's friend requests
     */
    public void requests(User u) throws Exception{
        List<User> req=new ArrayList<>();
        for(Friendship f: fsrv.getAll()){
            if(f.getStatus()!=null&&f.getStatus().equals("Pending"))
                if(u.getId().equals(f.getId().getRight()))
                    req.add(usrv.getOne(f.getId().getLeft()));
        }
        if(req.size()==0){
            System.out.println("You have no friend requests!");
        }
        else {
            for (User user : req) {
                System.out.println(user.toString());
            }
            System.out.println("Give id:");
            Long id=scan.nextLong();
            boolean ok=false;
            for(User user:req)
                if(user.getId().equals(id))
                    ok=true;
            if(ok) {
                System.out.println("Answer request(0-accept;1-reject):");
                int ans = scan.nextInt();
                Tuple<Long, Long> t = new Tuple(u.getId(), id);
                Friendship f = fsrv.getOne(t);
                if (ans == 0) {
                    f.setStatus("Accepted");
                    f.setDate(LocalDateTime.now());
                    fsrv.update(f);
                }
                else
                if(ans==1){
                    f.setStatus("Rejected");
                    fsrv.update(f);
                }
                else
                    System.out.println("Invalid command!");
            }
            else
                System.out.println("Request doesn't exist!");
        }
    }

    /**
     * loads all friendships
     */
    public void loadFriendships(){
        for(Friendship f: fsrv.getAll()){
            Long f1=f.getId().getLeft();
            Long f2=f.getId().getRight();
            User u1= usrv.getOne(f1);
            User u2= usrv.getOne(f2);
            u1.addFriend(u2);
            u2.addFriend(u1);
        }
    }

    /**
     * prints all users and their friend list
     */
    public void printAll(){
        for(User user: usrv.getAll()) {
            System.out.println(user.toString());
            usrv.getFriends(user);
            System.out.println("Friends: "+user.getFriends());
        }
    }

    public void sendMessage(Long idFrom){
        User from=usrv.getOne(idFrom);
        if(from==null) {
            System.out.println("User doesn't exist!");
            return;
        }
        System.out.println("Give the number of people to send to:");
        int nr=scan.nextInt();
        System.out.println("Give the list of people's ids:");
        List<User> to=new ArrayList<>();
        for(int i=0;i<nr;i++){
            Long id=scan.nextLong();
            User u=usrv.getOne(id);
            if(u==null){
                System.out.println("User doesn't exist!");
                return;
            }
            to.add(u);
        }
        System.out.println("Give the message:");
        scan.nextLine();
        String message=scan.nextLine();
        Message m=new Message(from,to,message);
        Message rez=msrv.save(m);
        if(rez==null)
            System.out.println("Message sent!");
    }

    public void replyToMessage(Long idFrom){
        User from=usrv.getOne(idFrom);
        if(from==null){
            System.out.println("User doesn't exist!");
            return;
        }
        System.out.println("Give the message's id that you want to reply to:");
        Long idMsg=scan.nextLong();
        Message reply=msrv.getOne(idMsg);
        if(reply!=null) {
            User to = usrv.getOne(reply.getFrom().getId());
            System.out.println("Give the message:");
            scan.nextLine();
            String message = scan.nextLine();
            Message m = new Message(from, Collections.singletonList(to),message);
            m.setReply(reply);
            msrv.save(m);
            System.out.println("Message sent!");
        }
        else
            System.out.println("Message doesn't exist!");
    }

    public void showConversation(Long id1){
        User u1=usrv.getOne(id1);
        System.out.println("Give the second user's id:");
        Long id2=scan.nextLong();
        User u2=usrv.getOne(id2);
        if(u1==null||u2==null){
            System.out.println("User doesn't exist!");
            return;
        }
        List<Message> conv=new ArrayList<>();
        for(Message m: msrv.getAll()){
            List<User> to=m.getTo();
            if(m.getFrom().getId().equals(u1.getId())&&to.get(0).getId().equals(u2.getId())||m.getFrom().getId().equals(u2.getId())&&to.get(0).getId().equals(u1.getId()))
                conv.add(m);
        }
        Collections.sort(conv, Comparator.comparing(obj->obj.getDate()));
        for(Message m: conv){
            List<User> to=m.getTo();
            if(m.getFrom().getId().equals(u1.getId())&&to.get(0).getId().equals(u2.getId())){
                System.out.print(u1.getId()+" "+u1.getFirstName()+" "+u1.getLastName()+": "+m.getId()+" | "+m.getMessage()+" | "+m.getDate());
                if(m.getReply()!=null)
                    System.out.println(" | reply to: "+m.getReply().getMessage());
                else
                    System.out.println();
            }
            if(m.getFrom().getId().equals(u2.getId())&&to.get(0).getId().equals(u1.getId())) {
                System.out.print(u2.getId() + " " + u2.getFirstName() + " " + u2.getLastName() + ": " + m.getMessage()+" | "+m.getDate());
                if (m.getReply() != null)
                    System.out.println(" | reply to: " + m.getReply().getMessage());
                else
                    System.out.println();
            }
        }
    }

    /**
     *
     * @param id - user's id
     * prints all the friends of the user with given id
     */
    public void friends(Long id){
        List<Friendship> fr = new ArrayList<>();
        fsrv.getAll().forEach(fr::add);
        fr.stream()
                .filter(x->x.getStatus()!=null&&x.getStatus().equals("Accepted"))
                .filter(x->x.getId().getLeft().equals(id))
                .forEach(x->{
                    User f=usrv.getOne(x.getId().getRight());
                    System.out.println(f.getLastName()+"|"+f.getFirstName()+"|"+x.getDate());
                });
        fr.stream()
                .filter(x->x.getStatus()!=null&&x.getStatus().equals("Accepted"))
                .filter(x->x.getId().getRight().equals(id))
                .forEach(x->{
                    User f=usrv.getOne(x.getId().getLeft());
                    System.out.println(f.getLastName()+"|"+f.getFirstName()+"|"+x.getDate());
                });
    }

    /**
     *
     * @param id - user's id
     * prints all the friends of the user with given id if the friendships was created in a certain month and year
     */
    public void friendsFromMonth(Long id){
        System.out.println("Give the month:");
        int month=scan.nextInt();
        System.out.println("Give the year:");
        int year=scan.nextInt();
        List<Friendship> fr = new ArrayList<>();
        fsrv.getAll().forEach(fr::add);
        fr.stream()
                .filter(x->x.getStatus()!=null&&x.getStatus().equals("Accepted"))
                .filter(x->x.getId().getLeft().equals(id))
                .filter(x->x.getDate().getMonthValue()==month&&x.getDate().getYear()==year)
                .forEach(x->{
                    User f=usrv.getOne(x.getId().getRight());
                    System.out.println(f.getLastName()+"|"+f.getFirstName()+"|"+x.getDate());
                });
        fr.stream()
                .filter(x->x.getStatus()!=null&&x.getStatus().equals("Accepted"))
                .filter(x->x.getId().getRight().equals(id))
                .filter(x->x.getDate().getMonthValue()==month&&x.getDate().getYear()==year)
                .forEach(x->{
                    User f=usrv.getOne(x.getId().getLeft());
                    System.out.println(f.getLastName()+"|"+f.getFirstName()+"|"+x.getDate());
                });
    }
}
