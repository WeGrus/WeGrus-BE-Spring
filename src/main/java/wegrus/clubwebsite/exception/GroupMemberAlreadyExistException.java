package wegrus.clubwebsite.exception;

import wegrus.clubwebsite.dto.error.ErrorCode;

public class GroupMemberAlreadyExistException extends BusinessException {
    public GroupMemberAlreadyExistException() {
        super(ErrorCode.GROUP_MEMBER_ALREADY_EXIST);
    }
}
