package wegrus.clubwebsite.service;

import javax.mail.MessagingException;

public interface MailService {

    String VERIFY_SCHOOL_EMAIL_SUBJECT = "[IGRUS] 본인 이메일 인증 안내";
    String VERIFY_CODE = "[IGRUS] 인증 코드 안내";
    String SENDER = "GraduProject11@gmail.com";
    void sendSchoolMailVerification(String receiver, String key) throws MessagingException;
    String sendCertificationCode(String email);
}
