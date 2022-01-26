package wegrus.clubwebsite.exception;

import wegrus.clubwebsite.dto.error.ErrorCode;

public class MemberAlreadyBanException extends BusinessException {

    public MemberAlreadyBanException() {
        super(ErrorCode.MEMBER_ALREADY_BAN);
    }
}
