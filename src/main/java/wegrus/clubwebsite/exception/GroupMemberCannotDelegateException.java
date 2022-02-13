package wegrus.clubwebsite.exception;

import wegrus.clubwebsite.dto.error.ErrorCode;

public class GroupMemberCannotDelegateException extends BusinessException {

    public GroupMemberCannotDelegateException() {
        super(ErrorCode.CANNOT_DELEGATE_GROUP_MEMBER);
    }
}
