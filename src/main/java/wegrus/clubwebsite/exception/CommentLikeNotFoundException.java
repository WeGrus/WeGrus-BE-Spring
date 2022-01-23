package wegrus.clubwebsite.exception;

import wegrus.clubwebsite.dto.error.ErrorCode;

public class CommentLikeNotFoundException extends BusinessException{

    public CommentLikeNotFoundException() {
        super(ErrorCode.COMMENT_LIKE_NOT_FOUND);
    }
}
