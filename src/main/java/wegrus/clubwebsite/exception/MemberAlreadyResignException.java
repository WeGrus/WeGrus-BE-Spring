package wegrus.clubwebsite.exception;

import wegrus.clubwebsite.dto.error.ErrorCode;

public class MemberAlreadyResignException extends BusinessException {

    public MemberAlreadyResignException() {
        super(ErrorCode.MEMBER_ALREADY_RESIGN);
    }
}
