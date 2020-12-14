package socialnetwork.domain.validators;

import socialnetwork.domain.User;

public class UserValidator implements Validator<User> {
    @Override
    public void validate(User entity) throws ValidationException {
        if(entity==null)
            throw new ValidationException("User can't be null!");
        if(entity.getFirstName()==""||entity.getLastName()=="")
            throw new ValidationException("User's name can't be empty!");
        if(entity.getFirstName().length()>20||entity.getLastName().length()>20)
            throw new ValidationException("Name too long!");
        if(!(entity.getFirstName().matches("[a-zA-Z]+"))||!(entity.getLastName().matches("[a-zA-Z]+")))
            throw new ValidationException("Name must only contain letters!");
        if(!(entity.getEmail().matches(".+"+"@"+".+")))
            throw new ValidationException("Invalid email format!");
        if(entity.getPassword().length()<6)
            throw new ValidationException("Password must have at least 6 characters!");
    }
}
