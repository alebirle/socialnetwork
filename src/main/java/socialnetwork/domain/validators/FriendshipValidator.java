package socialnetwork.domain.validators;

import socialnetwork.domain.Friendship;

public class FriendshipValidator implements Validator<Friendship>{
    @Override
    public void validate(Friendship entity) throws ValidationException {
        if(entity==null)
            throw new ValidationException("Friendship can't be null!");
        if(entity.getId().getLeft()==null||entity.getId().getRight()==null)
            throw new ValidationException("Ids can't be empty!");
        if(entity.getId().getLeft()==entity.getId().getRight())
            throw new ValidationException("Ids can't be the same!");
    }
}
