package com.hotifi.common.services.implementations;


import com.google.api.client.util.Value;
import com.hotifi.common.constants.ApplicationConstants;
import com.hotifi.common.constants.BusinessConstants;
import com.hotifi.common.dto.UserRegistrationEventDTO;
import com.hotifi.common.models.EmailModel;
import com.hotifi.common.services.interfaces.IEmailService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.util.FileCopyUtils;

import java.nio.charset.StandardCharsets;

@Slf4j
public class EmailServiceImpl implements IEmailService {

    //private final INotificationService notificationService;

    @Value("${email.host}")
    private String emailHost;

    @Value("${email.port}")
    private Integer emailPort;

    @Value("${email.no-reply-address}")
    private static String noReplyEmailAddress;

    @Value("${email.no-reply-password}")
    private static String noReplyEmailPassword;

    @Override
    @KafkaListener(topics = "email-notifications", groupId = "email-group")
    //TODO - Add UserRegistrationEvent
    public void sendWelcomeEmail(UserRegistrationEventDTO userRegistrationEventDTO) {
        try {
            String subject = "Welcome To Hotifi";
            ClassPathResource classPathResource = new ClassPathResource(ApplicationConstants.EMAIL_WELCOME_HTML_PATH);
            byte[] byteData = FileCopyUtils.copyToByteArray(classPathResource.getInputStream());
            String htmlContent = new String(byteData, StandardCharsets.UTF_8);
            Document document = Jsoup.parse(htmlContent, "UTF-8");
            document.getElementById("first-name").appendText("Hi " + userRegistrationEventDTO.getFirstName() + ",");
            sendEmail(userRegistrationEventDTO.getEmail(), subject, document);

            //notificationService.sendNotificationToSingleUser(userId, "A New Beginning !", "Your hotifi account has been created.", GOOGLE_CLOUD_PLATFORM);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendAccountDeletedEmail(Long userId, String firstName) {
        try {
            String subject = "Your account has been deleted";
            ClassPathResource classPathResource = new ClassPathResource(ApplicationConstants.EMAIL_ACCOUNT_DELETED_HTML_PATH);
            byte[] byteData = FileCopyUtils.copyToByteArray(classPathResource.getInputStream());
            String htmlContent = new String(byteData, StandardCharsets.UTF_8);
            Document document = Jsoup.parse(htmlContent, "UTF-8");
            document.getElementById("first-name").appendText("Hi " + firstName + ",");
            //TODO update toEmailAddress here
            sendEmail(firstName, subject, document);
            //notificationService.sendNotificationToSingleUser(userId, "Sorry To See You Go !", "Your hotifi account has been deleted.", GOOGLE_CLOUD_PLATFORM);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendAccountFrozenEmail(Long userId, String firstName) {
        try {
            String subject = "Your account has been suspended.";
            ClassPathResource classPathResource = new ClassPathResource(ApplicationConstants.EMAIL_ACCOUNT_FROZEN_HTML_PATH);
            byte[] byteData = FileCopyUtils.copyToByteArray(classPathResource.getInputStream());
            String htmlContent = new String(byteData, StandardCharsets.UTF_8);
            Document document = Jsoup.parse(htmlContent, "UTF-8");
            document.getElementById("first-name").appendText("Hi " + firstName + ",");
            document.getElementById("suspend-msg").appendText(BusinessConstants.MINIMUM_FREEZE_PERIOD_HOURS + "hours");
            //TODO update toEmailAddress here
            sendEmail(firstName, subject, document);
            //notificationService.sendNotificationToSingleUser(userId, "Account Suspended", "Your hotifi account for buying data has been suspended.", GOOGLE_CLOUD_PLATFORM);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendBuyerBannedEmail(Long userId, String firstName) {
        try {
            String subject = "Your buying account has been banned";
            ClassPathResource classPathResource = new ClassPathResource(ApplicationConstants.EMAIL_BUYER_BANNED_HTML_PATH);
            byte[] byteData = FileCopyUtils.copyToByteArray(classPathResource.getInputStream());
            String htmlContent = new String(byteData, StandardCharsets.UTF_8);
            Document document = Jsoup.parse(htmlContent, "UTF-8");
            document.getElementById("first-name").appendText("Hi " + firstName + ",");
            //TODO update toEmailAddress here
            sendEmail(firstName, subject, document);
            //notificationService.sendNotificationToSingleUser(userId, "Banned !", "Your hotifi account for buying data has been deleted.", GOOGLE_CLOUD_PLATFORM);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendEmailOtpEmail(String toEmailAddress, String emailOtp) {
        try {
            String subject = "Email Otp Verification";
            ClassPathResource classPathResource = new ClassPathResource(ApplicationConstants.EMAIL_OTP_HTML_PATH);
            byte[] byteData = FileCopyUtils.copyToByteArray(classPathResource.getInputStream());
            String htmlContent = new String(byteData, StandardCharsets.UTF_8);
            Document document = Jsoup.parse(htmlContent, "UTF-8");
            document.getElementById("first-name").appendText("Hi,");
            document.getElementById("email-otp").appendText(emailOtp);
            document.getElementById("expires-in").appendText(BusinessConstants.MAXIMUM_EMAIL_OTP_MINUTES + " minutes");
            //TODO update toEmailAddress here
            sendEmail(toEmailAddress, subject, document);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendLinkedAccountFailed(Long userId, String firstName, String errorDescription) {
        try {
            String subject = "Your linked account verification for payment failed";
            ClassPathResource classPathResource = new ClassPathResource(ApplicationConstants.EMAIL_LINKED_ACCOUNT_FAILED_PATH);
            byte[] byteData = FileCopyUtils.copyToByteArray(classPathResource.getInputStream());
            String htmlContent = new String(byteData, StandardCharsets.UTF_8);
            Document document = Jsoup.parse(htmlContent, "UTF-8");
            document.getElementById("first-name").appendText("Hi " + firstName + ",");
            document.getElementById("error-msg").appendText(errorDescription);
            //TODO update toEmailAddress here
            sendEmail(firstName, subject, document);
            //notificationService.sendNotificationToSingleUser(userId, "Oh No! Oh No! Oh No No No No !", "Your linked account verification for payment has failed.", GOOGLE_CLOUD_PLATFORM);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendLinkedAccountSuccessEmail(Long userId, String firstName) {
        try {
            String subject = "Your linked account verification for payment is successful";
            ClassPathResource classPathResource = new ClassPathResource(ApplicationConstants.EMAIL_LINKED_ACCOUNT_SUCCESS_PATH);
            byte[] byteData = FileCopyUtils.copyToByteArray(classPathResource.getInputStream());
            String htmlContent = new String(byteData, StandardCharsets.UTF_8);
            Document document = Jsoup.parse(htmlContent, "UTF-8");
            document.getElementById("first-name").appendText("Hi " + firstName + ",");
            //TODO update toEmailAddress here
            sendEmail(firstName, subject, document);
            //notificationService.sendNotificationToSingleUser(userId, "Here We Go !", "Your linked account verification for payment is successful.", GOOGLE_CLOUD_PLATFORM);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendEmail(String toEmailAddress, String subject, Document document) {
        String htmlContent = document.html();

        Email email = EmailBuilder.startingBlank()
                .from(noReplyEmailAddress)
                .to(toEmailAddress)
                .withSubject(subject)
                .withHTMLText(htmlContent)
                .buildEmail();

        Mailer mailer = MailerBuilder
                .withSMTPServer(emailHost, emailPort, noReplyEmailAddress, noReplyEmailPassword)
                .withTransportStrategy(TransportStrategy.SMTP_TLS)
                .withSessionTimeout(10 * 1000)
                .async()
                .buildMailer();

        mailer.sendMail(email);

        log.info("Email Sent");
    }

}
