package wegrus.clubwebsite.exception;

import wegrus.clubwebsite.dto.error.ErrorCode;

public class ReplyNotFoundException extends BusinessException{
    public ReplyNotFoundException() {
        super(ErrorCode.REPLY_NOT_FOUND);
    }
}
