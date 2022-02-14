package wegrus.clubwebsite.exception;

import wegrus.clubwebsite.dto.error.ErrorCode;

public class MemberAlreadyApplyGroupException extends BusinessException {

    public MemberAlreadyApplyGroupException() {
        super(ErrorCode.MEMBER_ALREADY_APPLY_GROUP);
    }
}
