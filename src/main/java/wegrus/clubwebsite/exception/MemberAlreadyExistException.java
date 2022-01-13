package wegrus.clubwebsite.exception;

import wegrus.clubwebsite.dto.error.ErrorCode;
import wegrus.clubwebsite.dto.error.ErrorResponse;

import java.util.List;

public class MemberAlreadyExistException extends BusinessException {

    public MemberAlreadyExistException(ErrorCode errorCode, List<ErrorResponse.FieldError> errors) {
        super(errorCode, errors);
    }
}
