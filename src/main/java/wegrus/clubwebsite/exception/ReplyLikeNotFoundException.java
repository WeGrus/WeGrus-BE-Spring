package wegrus.clubwebsite.exception;

import wegrus.clubwebsite.dto.error.ErrorCode;

public class ReplyLikeNotFoundException extends BusinessException{

    public ReplyLikeNotFoundException() {
        super(ErrorCode.REPLY_LIKE_NOT_FOUND);
    }
}
