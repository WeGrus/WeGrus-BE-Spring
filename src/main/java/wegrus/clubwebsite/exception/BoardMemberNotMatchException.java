package wegrus.clubwebsite.exception;

import wegrus.clubwebsite.dto.error.ErrorCode;

public class BoardMemberNotMatchException extends BusinessException {
    public BoardMemberNotMatchException() {
        super(ErrorCode.BOARD_MEMBER_NOT_MATCH);
    }
}
