package wegrus.clubwebsite.exception;

import wegrus.clubwebsite.dto.error.ErrorCode;
import wegrus.clubwebsite.dto.error.ErrorResponse;

import java.util.List;

public class CannotBanMember extends BusinessException {

    public CannotBanMember(List<ErrorResponse.FieldError> errors) {
        super(ErrorCode.CANNOT_BAN_MEMBER, errors);
    }
}
