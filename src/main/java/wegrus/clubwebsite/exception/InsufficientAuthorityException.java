package wegrus.clubwebsite.exception;

import wegrus.clubwebsite.dto.error.ErrorCode;
import wegrus.clubwebsite.dto.error.ErrorResponse;

import java.util.List;

public class InsufficientAuthorityException extends BusinessException {

    public InsufficientAuthorityException(List<ErrorResponse.FieldError> errors) {
        super(ErrorCode.INSUFFICIENT_AUTHORITY, errors);
    }
}
