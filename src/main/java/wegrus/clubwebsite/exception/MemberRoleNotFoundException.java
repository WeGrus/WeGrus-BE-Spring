package wegrus.clubwebsite.exception;

import wegrus.clubwebsite.dto.error.ErrorCode;

public class MemberRoleNotFoundException extends BusinessException{

    public MemberRoleNotFoundException() {
        super(ErrorCode.MEMBER_ROLE_NOT_FOUND);
    }
}
