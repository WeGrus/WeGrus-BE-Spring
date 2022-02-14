package wegrus.clubwebsite.exception;

import wegrus.clubwebsite.dto.error.ErrorCode;

public class CannotUpdateGroupPostException extends BusinessException {
    public CannotUpdateGroupPostException() {
        super(ErrorCode.CANNOT_UPDATE_GROUP_POST);
    }
}
