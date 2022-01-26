package wegrus.clubwebsite.exception;

import wegrus.clubwebsite.dto.error.ErrorCode;
import wegrus.clubwebsite.dto.error.ErrorResponse;

import java.util.List;

public class MemberResignException extends BusinessException {

    public MemberResignException(List<ErrorResponse.FieldError> errors) {
        super(ErrorCode.MEMBER_CANNOT_RESIGN, errors);
    }
}
