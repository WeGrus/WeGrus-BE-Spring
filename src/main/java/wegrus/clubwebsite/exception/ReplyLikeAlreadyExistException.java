package wegrus.clubwebsite.exception;

import wegrus.clubwebsite.dto.error.ErrorCode;

public class ReplyLikeAlreadyExistException extends BusinessException {
    public ReplyLikeAlreadyExistException() {
        super(ErrorCode.REPLY_LIKE_ALREADY_EXIST);
    }
}
