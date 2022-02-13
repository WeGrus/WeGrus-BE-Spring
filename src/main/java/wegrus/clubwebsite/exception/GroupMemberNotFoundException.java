package wegrus.clubwebsite.exception;

import wegrus.clubwebsite.dto.error.ErrorCode;

public class GroupMemberNotFoundException extends BusinessException {

    public GroupMemberNotFoundException() {
        super(ErrorCode.GROUP_MEMBER_NOT_FOUND);
    }
}
