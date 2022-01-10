package wegrus.clubwebsite.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
public class MailServiceGmail implements MailService {

    @Value("${image.igrus-logo}")
    private String IGRUS_LOGO_IMAGE_URL;
    @Value("${redirect-url.verify-email}")
    private String REDIRECT_URL;
    private final JavaMailSender mailSender;

    @Override
    public void sendSchoolMailVerification(String receiver, String key) throws MessagingException {
        final String body =
                "<!DOCTYPE html>\n" +
                        "<html>\n" +
                        "<head>\n" +
                        "</head>\n" +
                        "<body>\n" +
                        "    <div style=\"font-family: 'Apple SD Gothic Neo', 'sans-serif' !important; width: 480px; height: 600px; border-top: 4px solid #6CD2D7; margin: 100px auto; padding: 30px 0; box-sizing: border-box;\">\n" +
                        "       <h1 style=\"margin: 0; padding: 0 5px; font-size: 28px; font-weight: 400;\">\n" +
                        "           <img style=\"font-size: 20px; margin: 0 0 10px 3px;\"><img src=\"" + IGRUS_LOGO_IMAGE_URL + "\"></span><br />\n" +
                        "           <span style=\"color: #6CD2D7\">메일인증</span> 안내입니다.\n" +
                        "       </h1>\n" +
                        "       <p style=\"font-size: 16px; line-height: 26px; margin-top: 50px; padding: 0 5px;\"><br />\n" +
                        "           인하대학교 SW 프로그래밍 동아리 IGRUS에 오신걸 환영합니다.<br />\n" +
                        "           아래 <b style=\"color: #6CD2D7\">'메일 인증'</b> 버튼을 클릭하여 회원가입을 완료해 주세요.<br />" +
                        "           인증 유효시간은 메일이 발송된 이후 30분까지 유효합니다.<br />\n" +
                        "           감사합니다.\n" +
                        "       </p>\n" +
                        "       <a style=\"color: #FFF; text-decoration: none; text-align: center; \"href=\"" + REDIRECT_URL + key + "\" target=\"_blank\">\n" +
                        "           <p style=\"display: inline-block; width: 210px; height: 45px; margin: 30px 5px 40px; background: #6CD2D7; line-height: 45px; vertical-align: middle; font-size: 16px;\">메일 인증</p>\n" +
                        "       </a>\n" +
                        "       <div style=\"border-top: 1px solid #DDD; padding: 5px;\"></div>\n" +
                        "    </div>\n" +
                        "</body>\n" +
                        "</html>";

        final MimeMessage mimeMessage = mailSender.createMimeMessage();
        final MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

        helper.setFrom(SENDER);
        helper.setTo(receiver);
        helper.setSubject(VERIFY_SCHOOL_EMAIL_SUBJECT);
        helper.setText(body, true);

        mailSender.send(mimeMessage);
    }
}
