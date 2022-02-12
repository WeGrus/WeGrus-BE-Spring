package wegrus.clubwebsite.exception;

import wegrus.clubwebsite.dto.error.ErrorCode;

public class CannotDelegateMember extends BusinessException {

    public CannotDelegateMember() {
        super(ErrorCode.CANNOT_DELEGATE_MEMBER);
    }
}
