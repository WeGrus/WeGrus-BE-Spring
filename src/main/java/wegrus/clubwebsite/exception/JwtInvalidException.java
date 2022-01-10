package wegrus.clubwebsite.exception;

import wegrus.clubwebsite.dto.error.ErrorCode;

public class JwtInvalidException extends BusinessException {
    public JwtInvalidException(){
        super(ErrorCode.INVALID_JWT);
    }
}
