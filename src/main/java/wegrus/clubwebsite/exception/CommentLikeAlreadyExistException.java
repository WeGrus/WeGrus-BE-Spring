package wegrus.clubwebsite.exception;

import wegrus.clubwebsite.dto.error.ErrorCode;

public class CommentLikeAlreadyExistException extends BusinessException {
    public CommentLikeAlreadyExistException() {
        super(ErrorCode.COMMENT_LIKE_ALREADY_EXIST);
    }
}
