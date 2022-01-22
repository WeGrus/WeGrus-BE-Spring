package wegrus.clubwebsite.exception;

import wegrus.clubwebsite.dto.error.ErrorCode;

public class ReplyMemberNotMatchException extends BusinessException {
    public ReplyMemberNotMatchException() {
        super(ErrorCode.REPLY_MEMBER_NOT_MATCH);
    }
}
