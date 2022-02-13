package wegrus.clubwebsite.exception;

import wegrus.clubwebsite.dto.error.ErrorCode;

public class GroupMemberCannotDegradeException extends BusinessException {

    public GroupMemberCannotDegradeException() {
        super(ErrorCode.CANNOT_DEGRADE_GROUP_MEMBER);
    }
}
