package wegrus.clubwebsite.exception;

import wegrus.clubwebsite.dto.error.ErrorCode;

public class RequestNotFoundException extends BusinessException{

    public RequestNotFoundException() {
        super(ErrorCode.REQUEST_NOT_FOUND);
    }
}
