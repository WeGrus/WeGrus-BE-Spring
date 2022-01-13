package wegrus.clubwebsite.exception;

import wegrus.clubwebsite.dto.error.ErrorCode;

public class AuthorizationHeaderInvalidException extends BusinessException {
    public AuthorizationHeaderInvalidException() {
        super(ErrorCode.INVALID_AUTHORIZATION_HEADER);
    }
}

