package wegrus.clubwebsite.exception;

import wegrus.clubwebsite.dto.error.ErrorCode;

public class PostNotFoundException extends BusinessException{
    public PostNotFoundException() {
        super(ErrorCode.POST_NOT_FOUND);
    }
}
