package wegrus.clubwebsite.exception;

import wegrus.clubwebsite.dto.error.ErrorCode;

public class GroupMemberCannotPromoteException extends BusinessException {

    public GroupMemberCannotPromoteException() {
        super(ErrorCode.CANNOT_PROMOTE_GROUP_MEMBER);
    }
}
