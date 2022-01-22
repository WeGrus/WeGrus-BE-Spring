package wegrus.clubwebsite.exception;

import wegrus.clubwebsite.dto.error.ErrorCode;

public class PostLikeAlreadyExistException extends BusinessException {
    public PostLikeAlreadyExistException() {
        super(ErrorCode.POST_LIKE_ALREADY_EXIST);
    }
}
