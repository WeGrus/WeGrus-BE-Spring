package wegrus.clubwebsite.exception;

import wegrus.clubwebsite.dto.error.ErrorCode;

public class GroupMemberCannotKickException extends BusinessException {

    public GroupMemberCannotKickException() {
        super(ErrorCode.CANNOT_KICK_GROUP_MEMBER);
    }
}
