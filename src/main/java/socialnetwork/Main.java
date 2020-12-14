package socialnetwork;

import socialnetwork.config.ApplicationContext;
import socialnetwork.domain.Friendship;
import socialnetwork.domain.Message;
import socialnetwork.domain.Tuple;
import socialnetwork.domain.User;
import socialnetwork.domain.validators.*;
import socialnetwork.repository.Repository;
import socialnetwork.repository.database.FriendshipDbRepository;
import socialnetwork.repository.database.MessageDbRepository;
import socialnetwork.repository.database.UserDbRepository;
import socialnetwork.repository.file.FriendshipFile;
import socialnetwork.repository.file.UserFile;
import socialnetwork.repository.memory.InMemoryRepository;
import socialnetwork.service.FriendshipService;
import socialnetwork.service.MessageService;
import socialnetwork.service.UserService;
import socialnetwork.ui.Console;

public class Main {
    public static void main(String[] args) {
        //String fileName=ApplicationContext.getPROPERTIES().getProperty("data.socialnetwork.users");
        //String fileName2="data/friendships.csv";
        //String fileName2=ApplicationContext.getPROPERTIES().getProperty("data.socialnetwork.friendships");
        //String fileName="data/users.csv";
        //Repository<Long, User> userFileRepository = new UserFile(fileName, new UserValidator());
        //Repository<Tuple<Long,Long>, Friendship> friendshipFileRepository=new FriendshipFile(fileName2,new FriendshipValidator());

        //Repository<Long,User> userMemRepository = new InMemoryRepository<>(new UserValidator());
        //Repository<Tuple<Long,Long>,Friendship> friendshipMemRepository=new InMemoryRepository<>(new FriendshipValidator());
        //userFileRepository.findAll().forEach(System.out::println);

        /*final String url = ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.url");
        final String username= ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.username");
        final String password= ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.password");
        Repository<Long,User> userDbRepository = new UserDbRepository(url,username, password,  new UserValidator());
        Repository<Tuple<Long,Long>,Friendship> friendshipDbRepository=new FriendshipDbRepository(url,username,password,new FriendshipValidator());
        Repository<Long, Message> messageDbRepository=new MessageDbRepository(url,username,password,new MessageValidator());
        UserService userService=new UserService(userDbRepository);
        FriendshipService friendshipService=new FriendshipService(friendshipDbRepository);
        MessageService messageService=new MessageService(messageDbRepository);
        Console console=new Console(userService,friendshipService,messageService);
        Tests test=new Tests();
        test.testAll();*/
        //console.run();
        MainApp.main(args);
    }
}


