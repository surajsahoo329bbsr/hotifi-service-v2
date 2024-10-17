package com.hotifi.common.services.implementations;


import com.google.api.client.util.Value;
import com.hotifi.common.constants.ApplicationConstants;
import com.hotifi.common.constants.BusinessConstants;
import com.hotifi.common.models.EmailModel;
import com.hotifi.common.services.interfaces.IEmailService;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import java.nio.charset.StandardCharsets;

import static com.hotifi.common.constants.codes.CloudClientCodes.GOOGLE_CLOUD_PLATFORM;

@Slf4j
public class EmailServiceImpl implements IEmailService {

    //private final INotificationService notificationService;

    @Value("${email.host}")
    private String emailHost;

    @Value("${email.port}")
    private Integer emailPort;

    private EmailModel emailModel;

    public EmailServiceImpl(EmailModel emailModel) {
        this.emailModel = emailModel;
    }

    @Override
    public void sendWelcomeEmail(Long userId, String firstName) {
        try {
            String subject = "Welcome To Hotifi";
            ClassPathResource classPathResource = new ClassPathResource(ApplicationConstants.EMAIL_WELCOME_HTML_PATH);
            byte[] byteData = FileCopyUtils.copyToByteArray(classPathResource.getInputStream());
            String htmlContent = new String(byteData, StandardCharsets.UTF_8);
            Document document = Jsoup.parse(htmlContent, "UTF-8");
            document.getElementById("first-name").appendText("Hi " + firstName + ",");
            sendEmail(document, emailModel, subject);

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
            sendEmail(document, emailModel, subject);
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
            sendEmail(document, emailModel, subject);
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
            sendEmail(document, emailModel, subject);
            //notificationService.sendNotificationToSingleUser(userId, "Banned !", "Your hotifi account for buying data has been deleted.", GOOGLE_CLOUD_PLATFORM);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendEmailOtpEmail(EmailModel emailModel) {
        try {
            String subject = "Email Otp Verification";
            ClassPathResource classPathResource = new ClassPathResource(ApplicationConstants.EMAIL_OTP_HTML_PATH);
            byte[] byteData = FileCopyUtils.copyToByteArray(classPathResource.getInputStream());
            String htmlContent = new String(byteData, StandardCharsets.UTF_8);
            Document document = Jsoup.parse(htmlContent, "UTF-8");
            document.getElementById("first-name").appendText("Hi,");
            document.getElementById("email-otp").appendText(emailModel.getEmailOtp());
            document.getElementById("expires-in").appendText(BusinessConstants.MAXIMUM_EMAIL_OTP_MINUTES + " minutes");
            sendEmail(document, emailModel, subject);
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
            sendEmail(document, emailModel, subject);
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
            sendEmail(document, emailModel, subject);
            //notificationService.sendNotificationToSingleUser(userId, "Here We Go !", "Your linked account verification for payment is successful.", GOOGLE_CLOUD_PLATFORM);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendEmail(Document document, EmailModel emailModel, String subject) {
        String htmlContent = document.html();
        Email email = EmailBuilder.startingBlank()
                .from(emailModel.getFromEmail())
                .to(emailModel.getToEmail())
                .withSubject(subject)
                .withHTMLText(htmlContent)
                .buildEmail();
        Mailer mailer = MailerBuilder
                .withSMTPServer(emailHost, emailPort, emailModel.getFromEmail(), emailModel.getFromEmailPassword())
                .withTransportStrategy(TransportStrategy.SMTP_TLS)
                .withSessionTimeout(10 * 1000)
                .async()
                .buildMailer();
        mailer.sendMail(email);
        log.info("Email Sent");
    }

}
