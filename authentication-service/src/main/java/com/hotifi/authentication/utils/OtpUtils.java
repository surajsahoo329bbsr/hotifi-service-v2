package com.hotifi.authentication.utils;

import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator;
import com.google.api.client.util.Value;
import com.hotifi.common.constants.BusinessConstants;
import com.hotifi.authentication.entities.Authentication;
import com.hotifi.authentication.errors.codes.AuthenticationErrorCodes;
import com.hotifi.common.exception.ApplicationException;
import com.hotifi.common.models.EmailModel;
import com.hotifi.authentication.repositories.AuthenticationRepository;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;

import javax.crypto.KeyGenerator;
import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Slf4j
public class OtpUtils {

    //@Autowired
    //private IEmailService emailService;

    @Value("${email.no-reply-address}")
    private static String noReplyEmailAddress;

    @Value("${email.no-reply-password}")
    private static String noReplyEmailPassword;

    public static String generateEmailOtp() {
        //do stuff
        try {
            TimeBasedOneTimePasswordGenerator timeOtp = new TimeBasedOneTimePasswordGenerator(Duration.ofMinutes(10));

            Key key;
            {
                final KeyGenerator keyGenerator = KeyGenerator.getInstance(timeOtp.getAlgorithm());
                // Key length should match the length of the HMAC output (160 bits for SHA-1, 256 bits
                // for SHA-256, and 512 bits for SHA-512).
                keyGenerator.init(160);
                key = keyGenerator.generateKey();
            }
            //Instant now = Instant.now();
            //final Instant later = now.plus(timeOtp.getTimeStep());

            //System.out.format("Current password: %06d\n", timeOtp.generateOneTimePassword(key, now));
            //System.out.format("Future password:  %06d\n", timeOtp.generateOneTimePassword(key, later));
            return String.valueOf(timeOtp.generateOneTimePassword(key, Instant.now()));
        } catch (Exception e) {
            log.error("Error ", e);
            throw new ApplicationException(AuthenticationErrorCodes.UNEXPECTED_EMAIL_OTP_ERROR);
        }
    }

    public static boolean isEmailOtpExpired(Authentication authentication){
        Date currentTime = new Date(System.currentTimeMillis());
        long timeDifference =  currentTime.getTime() - authentication.getModifiedAt().getTime();
        long minutesDifference = timeDifference / (60L * 1000L);
        return minutesDifference >= BusinessConstants.MAXIMUM_EMAIL_OTP_MINUTES; // If otp generated is more than 10 minutes
    }

    //needs to be called from generateEmailOtpSignUp or generateEmailOtpLogin
    public static void saveAuthenticationEmailOtp(Authentication authentication, AuthenticationRepository authenticationRepository){
        try {
            String emailOtp = generateEmailOtp();
            log.info("Otp " + emailOtp);
            String encryptedEmailOtp = BCrypt.hashpw(emailOtp, BCrypt.gensalt());
            Date now = new Date(System.currentTimeMillis()); //set updated token created time
            authentication.setModifiedAt(now);
            authentication.setEmailOtp(encryptedEmailOtp);
            authenticationRepository.save(authentication); //updating otp in password

            //Populating email model with values
            EmailModel emailModel = new EmailModel();
            emailModel.setToEmail(authentication.getEmail());
            emailModel.setFromEmail(noReplyEmailAddress);
            emailModel.setFromEmailPassword(noReplyEmailPassword);
            emailModel.setEmailOtp(emailOtp);

            //emailService.sendEmailOtpEmail(emailModel);
            //emailService.sendEmail(null, emailModel, 0);
        } catch (Exception e){
            throw new ApplicationException(AuthenticationErrorCodes.UNEXPECTED_EMAIL_OTP_ERROR);
        }
    }

}
