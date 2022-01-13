package wegrus.clubwebsite.exception;

import wegrus.clubwebsite.dto.error.ErrorCode;

public class MemberNotFoundException extends BusinessException {
    public MemberNotFoundException(){
        super(ErrorCode.MEMBER_NOT_FOUND);
    }
}
