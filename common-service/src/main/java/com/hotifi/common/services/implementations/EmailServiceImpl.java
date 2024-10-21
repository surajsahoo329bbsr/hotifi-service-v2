package com.hotifi.common.services.implementations;

import com.google.api.client.util.Value;
import com.hotifi.common.constants.ApplicationConstants;
import com.hotifi.common.constants.BusinessConstants;
import com.hotifi.common.dto.UserEventDTO;
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
    @KafkaListener(topics = ApplicationConstants.KAFKA_EMAIL_TOPIC, groupId = ApplicationConstants.KAFKA_EMAIL_GROUP_ID)
    public void sendWelcomeEmail(UserEventDTO userEventDTO) {
        try {
            String subject = "Welcome To Hotifi";
            ClassPathResource classPathResource = new ClassPathResource(ApplicationConstants.EMAIL_WELCOME_HTML_PATH);
            byte[] byteData = FileCopyUtils.copyToByteArray(classPathResource.getInputStream());
            String htmlContent = new String(byteData, StandardCharsets.UTF_8);
            Document document = Jsoup.parse(htmlContent, "UTF-8");
            document.getElementById("first-name").appendText("Hi " + userEventDTO.getFirstName() + ",");
            sendEmail(userEventDTO.getEmail(), subject, document);
        } catch (Exception e) {
            log.error("An error occurred : {}", e.getMessage(), e);
        }
    }

    @Override
    @KafkaListener(topics = ApplicationConstants.KAFKA_EMAIL_TOPIC, groupId = ApplicationConstants.KAFKA_EMAIL_GROUP_ID)
    public void sendAccountDeletedEmail(UserEventDTO userEventDTO) {
        try {
            String subject = "Your account has been deleted";
            ClassPathResource classPathResource = new ClassPathResource(ApplicationConstants.EMAIL_ACCOUNT_DELETED_HTML_PATH);
            byte[] byteData = FileCopyUtils.copyToByteArray(classPathResource.getInputStream());
            String htmlContent = new String(byteData, StandardCharsets.UTF_8);
            Document document = Jsoup.parse(htmlContent, "UTF-8");
            document.getElementById("first-name").appendText("Hi " + userEventDTO.getFirstName() + ",");
            sendEmail(userEventDTO.getEmail(), subject, document);
        } catch (Exception e) {
            log.error("An error occurred : {}", e.getMessage(), e);
        }
    }

    @Override
    @KafkaListener(topics = ApplicationConstants.KAFKA_EMAIL_TOPIC, groupId = ApplicationConstants.KAFKA_EMAIL_GROUP_ID)
    public void sendAccountFrozenEmail(UserEventDTO userEventDTO) {
        try {
            String subject = "Your account has been suspended.";
            ClassPathResource classPathResource = new ClassPathResource(ApplicationConstants.EMAIL_ACCOUNT_FROZEN_HTML_PATH);
            byte[] byteData = FileCopyUtils.copyToByteArray(classPathResource.getInputStream());
            String htmlContent = new String(byteData, StandardCharsets.UTF_8);
            Document document = Jsoup.parse(htmlContent, "UTF-8");
            document.getElementById("first-name").appendText("Hi " + userEventDTO.getFirstName() + ",");
            document.getElementById("suspend-msg").appendText(BusinessConstants.MINIMUM_FREEZE_PERIOD_HOURS + "hours");
            sendEmail(userEventDTO.getEmail(), subject, document);
        } catch (Exception e) {
            log.error("An error occurred : {}", e.getMessage(), e);
        }
    }

    @Override
    @KafkaListener(topics = ApplicationConstants.KAFKA_EMAIL_TOPIC, groupId = ApplicationConstants.KAFKA_EMAIL_GROUP_ID)
    public void sendBuyerBannedEmail(UserEventDTO userEventDTO) {
        try {
            String subject = "Your buying account has been banned";
            ClassPathResource classPathResource = new ClassPathResource(ApplicationConstants.EMAIL_BUYER_BANNED_HTML_PATH);
            byte[] byteData = FileCopyUtils.copyToByteArray(classPathResource.getInputStream());
            String htmlContent = new String(byteData, StandardCharsets.UTF_8);
            Document document = Jsoup.parse(htmlContent, "UTF-8");
            document.getElementById("first-name").appendText("Hi " + userEventDTO.getFirstName() + ",");
            sendEmail(userEventDTO.getEmail(), subject, document);
        } catch (Exception e) {
            log.error("An error occurred : {}", e.getMessage(), e);
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
            sendEmail(toEmailAddress, subject, document);
        } catch (Exception e) {
            log.error("An error occurred : {}", e.getMessage(), e);
        }
    }

    @Override
    @KafkaListener(topics = ApplicationConstants.KAFKA_EMAIL_TOPIC, groupId = ApplicationConstants.KAFKA_EMAIL_GROUP_ID)
    public void sendLinkedAccountFailed(UserEventDTO userEventDTO) {
        try {
            String subject = "Your linked account verification for payment failed";
            ClassPathResource classPathResource = new ClassPathResource(ApplicationConstants.EMAIL_LINKED_ACCOUNT_FAILED_PATH);
            byte[] byteData = FileCopyUtils.copyToByteArray(classPathResource.getInputStream());
            String htmlContent = new String(byteData, StandardCharsets.UTF_8);
            Document document = Jsoup.parse(htmlContent, "UTF-8");
            document.getElementById("first-name").appendText("Hi " + userEventDTO.getFirstName() + ",");
            document.getElementById("error-msg").appendText(userEventDTO.getErrorDescription());
            sendEmail(userEventDTO.getEmail(), subject, document);
            //notificationService.sendNotificationToSingleUser(userId, "Oh No! Oh No! Oh No No No No !", "Your linked account verification for payment has failed.", GOOGLE_CLOUD_PLATFORM);
        } catch (Exception e) {
            log.error("An error occurred : {}", e.getMessage(), e);
        }
    }

    @Override
    @KafkaListener(topics = ApplicationConstants.KAFKA_EMAIL_TOPIC, groupId = ApplicationConstants.KAFKA_EMAIL_GROUP_ID)
    public void sendLinkedAccountSuccessEmail(UserEventDTO userEventDTO) {
        try {
            String subject = "Your linked account verification for payment is successful";
            ClassPathResource classPathResource = new ClassPathResource(ApplicationConstants.EMAIL_LINKED_ACCOUNT_SUCCESS_PATH);
            byte[] byteData = FileCopyUtils.copyToByteArray(classPathResource.getInputStream());
            String htmlContent = new String(byteData, StandardCharsets.UTF_8);
            Document document = Jsoup.parse(htmlContent, "UTF-8");
            document.getElementById("first-name").appendText("Hi " + userEventDTO.getFirstName() + ",");
            sendEmail(userEventDTO.getEmail(), subject, document);
            //notificationService.sendNotificationToSingleUser(userId, "Here We Go !", "Your linked account verification for payment is successful.", GOOGLE_CLOUD_PLATFORM);
        } catch (Exception e) {
            log.error("An error occurred : {}", e.getMessage(), e);
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
