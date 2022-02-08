package wegrus.clubwebsite.exception;

import wegrus.clubwebsite.dto.error.ErrorCode;
import wegrus.clubwebsite.dto.error.ErrorResponse;

import java.util.List;

public class AuthorizationCodeInvalidException extends BusinessException {
    public AuthorizationCodeInvalidException(List<ErrorResponse.FieldError> errors) {
        super(ErrorCode.INVALID_INPUT_VALUE, errors);
    }
}
