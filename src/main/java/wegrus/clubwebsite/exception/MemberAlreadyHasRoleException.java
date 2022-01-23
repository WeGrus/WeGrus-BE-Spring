package wegrus.clubwebsite.exception;

import wegrus.clubwebsite.dto.error.ErrorCode;

public class MemberAlreadyHasRoleException extends BusinessException {

    public MemberAlreadyHasRoleException() {
        super(ErrorCode.MEMBER_ALREADY_HAS_ROLE);
    }
}
