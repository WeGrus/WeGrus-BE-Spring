package wegrus.clubwebsite.exception;

import wegrus.clubwebsite.dto.error.ErrorCode;
import wegrus.clubwebsite.dto.error.ErrorResponse;

import java.util.List;

public class CannotRequestAuthorityException extends BusinessException {

    public CannotRequestAuthorityException(List<ErrorResponse.FieldError> errors) {
        super(ErrorCode.CANNOT_REQUEST_AUTHORITY, errors);
    }
}
