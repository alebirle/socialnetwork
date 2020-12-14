package socialnetwork.domain.validators;

import socialnetwork.domain.Message;
import socialnetwork.domain.User;

public class MessageValidator implements Validator<Message>{
    @Override
    public void validate(Message entity) throws ValidationException {
        UserValidator uv=new UserValidator();
        uv.validate(entity.getFrom());
        for(User u:entity.getTo())
            uv.validate(u);
        if(entity.getMessage().length()>1000)
            throw new ValidationException("Message too long!");
    }
}
