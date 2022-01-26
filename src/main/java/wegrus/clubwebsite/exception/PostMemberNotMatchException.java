package wegrus.clubwebsite.exception;

import wegrus.clubwebsite.dto.error.ErrorCode;

public class PostMemberNotMatchException extends BusinessException {
    public PostMemberNotMatchException() {
        super(ErrorCode.POST_MEMBER_NOT_MATCH);
    }
}
