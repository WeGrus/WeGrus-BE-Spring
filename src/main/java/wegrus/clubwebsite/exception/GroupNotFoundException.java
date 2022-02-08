package wegrus.clubwebsite.exception;

import wegrus.clubwebsite.dto.error.ErrorCode;

public class GroupNotFoundException extends BusinessException {
    public GroupNotFoundException() {
        super(ErrorCode.GROUP_NOT_FOUND);
    }
}
