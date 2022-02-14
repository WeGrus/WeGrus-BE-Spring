package wegrus.clubwebsite.exception;

import wegrus.clubwebsite.dto.error.ErrorCode;

public class CannotUpdateNonGroupPostException extends BusinessException {
    public CannotUpdateNonGroupPostException() {
        super(ErrorCode.CANNOT_UPDATE_NON_GROUP_POST);
    }
}
