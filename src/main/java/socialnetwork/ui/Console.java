package socialnetwork.ui;

import socialnetwork.domain.Friendship;
import socialnetwork.domain.Tuple;
import socialnetwork.domain.User;
import socialnetwork.service.FriendshipService;
import socialnetwork.service.MessageService;
import socialnetwork.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Console {
    private UserService usrv;
    private ConsoleCommands cmd;
    private FriendshipService fsrv;
    private MessageService msrv;
    //private Scanner scan = new Scanner(System.in);
    public Console(UserService u, FriendshipService f, MessageService m){
        usrv=u;
        fsrv=f;
        msrv=m;
        cmd=new ConsoleCommands(u,f,m);
    }

    public boolean adminMenu(){
        //loadFriendships();
        boolean ok=false;
            System.out.println("0-show users; 1-add user; 2-remove user; 3-add friendship; 4-remove friendship;\n5-number of communities; 6-friends of user; 7-friends of user from a certain month\n8-send message; 9-send reply; 10-show conversation\n11-logout; 12-exit");
            int nr = cmd.scan.nextInt();
            switch (nr){
                case 0:
                    cmd.printAll();
                    break;
                case 1:
                    try {
                        cmd.addUser();
                    }
                    catch(Exception e){
                        System.out.println(e.getMessage());
                    }
                    break;
                case 2:
                    try {
                        System.out.println("Give id:");
                        Long id=cmd.scan.nextLong();
                        cmd.removeUser(id);
                    }
                    catch(Exception e){
                        System.out.println(e.getMessage());
                    }
                    break;
                case 3:
                    try {
                        System.out.println("Give the id of the first user:");
                        Long f1= cmd.scan.nextLong();
                        cmd.addFriendship(f1);
                    }
                    catch(Exception e){
                        System.out.println(e.getMessage());
                    }
                    break;
                case 4:
                    try {
                        System.out.println("Give the id of the first user:");
                        Long f1= cmd.scan.nextLong();
                        cmd.removeFriendship(f1);
                    }
                    catch(Exception e){
                        System.out.println(e.getMessage());
                    }
                    break;
                case 5:
                    System.out.println(usrv.nrOfCommunities());
                    break;
                case 6:
                    System.out.println("Give user id:");
                    Long id=cmd.scan.nextLong();
                    cmd.friends(id);
                    break;
                case 7:
                    System.out.println("Give user id:");
                    Long id2=cmd.scan.nextLong();
                    cmd.friendsFromMonth(id2);
                    break;
                case 8:
                    System.out.println("Give first user's id:");
                    Long from=cmd.scan.nextLong();
                    try {
                        cmd.sendMessage(from);
                    }
                    catch(Exception e){
                        System.out.println(e.getMessage());
                    }
                    break;
                case 9:
                    System.out.println("Give first user's id:");
                    Long from2=cmd.scan.nextLong();
                    try {
                        cmd.replyToMessage(from2);
                    }
                    catch(Exception e){
                        System.out.println(e.getMessage());
                    }
                    break;
                case 10:
                    System.out.println("Give first user's id:");
                    Long id1=cmd.scan.nextLong();
                    cmd.showConversation(id1);
                    break;
                case 11:
                    cmd.logged.setId(null);
                    break;
                case 12:
                    ok=true;
                    break;
                default:
                    System.out.println("Invalid command!");
            }
            return ok;
    }

    public boolean mainMenu(){
        boolean ok=false;
        System.out.println("0-exit; 1-login; 2-register");
        int nr=cmd.scan.nextInt();
        switch(nr) {
            case 0:
                ok = true;
                break;
            case 1:
                try {
                    cmd.login();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
                break;
            case 2:
                try {
                    cmd.logged=cmd.addUser();
                    System.out.println("Your id is "+cmd.logged.getId());
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
                break;
            default:
                System.out.println("Invalid command!");
        }
        return ok;
    }

    public boolean userMenu(){
        boolean ok=false;
        System.out.println("0-exit;1-logout; 2-delete account;\n3-show users; 4-show friend requests; 5-add friend; 6-remove friend;\n7-number of comunities; 8-friends list; 9-friends from a certain month;\n10-send message; 11-send reply; 12-show conversation");
        int nr=cmd.scan.nextInt();
        switch (nr){
            case 0:
                ok=true;
                break;
            case 1:
                cmd.logged.setId(null);
                break;
            case 2:
                try {
                    cmd.removeUser(cmd.logged.getId());
                    cmd.logged.setId(null);
                }
                catch(Exception e){
                    System.out.println(e.getMessage());
                }
                break;
            case 3:
                cmd.printAll();
                break;
            case 4:
                try {
                    cmd.requests(cmd.logged);
                }
                catch(Exception e){
                    System.out.println(e.getMessage());
                }
                break;
            case 5:
                try {
                    cmd.addFriendship(cmd.logged.getId());
                }
                catch(Exception e){
                    System.out.println(e.getMessage());
                }
                break;
            case 6:
                try{
                    cmd.removeFriendship(cmd.logged.getId());
                }
                catch(Exception e){
                    System.out.println(e.getMessage());
                }
                break;
            case 7:
                System.out.println(usrv.nrOfCommunities());
                break;
            case 8:
                cmd.friends(cmd.logged.getId());
                break;
            case 9:
                cmd.friendsFromMonth(cmd.logged.getId());
                break;
            case 10:
                try{
                    cmd.sendMessage(cmd.logged.getId());
                }
                catch(Exception e){
                    System.out.println(e.getMessage());
                }
                break;
            case 11:
                try{
                    cmd.replyToMessage(cmd.logged.getId());
                }
                catch(Exception e){
                    System.out.println(e.getMessage());
                }
                break;
            case 12:
                cmd.showConversation(cmd.logged.getId());
                break;
            default:
                System.out.println("Invalid command!");
        }
        return ok;
    }

    public void run(){
        while(true){
            if(cmd.logged.getId()==null){
                boolean ok=mainMenu();
                if(ok)
                    return;
            }
            else {
                if(cmd.logged.getId()==0){
                    boolean ok=adminMenu();
                    if(ok)
                        return;
                }
                else{
                    boolean ok=userMenu();
                    if(ok)
                        return;
                }
            }
        }
    }
}
