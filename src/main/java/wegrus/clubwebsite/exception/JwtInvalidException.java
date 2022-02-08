package wegrus.clubwebsite.exception;

import wegrus.clubwebsite.dto.error.ErrorCode;
import wegrus.clubwebsite.dto.error.ErrorResponse;

import java.util.List;

public class JwtInvalidException extends BusinessException {
    public JwtInvalidException(List<ErrorResponse.FieldError> errors){
        super(ErrorCode.INVALID_JWT, errors);
    }
}
