package com.hotifi.user.services.implementations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.util.Value;
import com.hotifi.authentication.entities.Authentication;
import com.hotifi.authentication.errors.codes.AuthenticationErrorCodes;
import com.hotifi.authentication.repositories.AuthenticationRepository;
import com.hotifi.authentication.utils.OtpUtils;
import com.hotifi.common.constants.ApplicationConstants;
import com.hotifi.common.constants.codes.SocialCodes;
import com.hotifi.common.constants.social.FacebookProcessor;
import com.hotifi.common.dto.UserEventDTO;
import com.hotifi.common.exception.ApplicationException;
import com.hotifi.common.services.interfaces.IVerificationService;
import com.hotifi.user.entitiies.User;
import com.hotifi.user.errors.codes.UserErrorCodes;
import com.hotifi.user.events.UserEvent;
import com.hotifi.user.repositories.UserRepository;
import com.hotifi.user.services.interfaces.INotificationService;
import com.hotifi.user.services.interfaces.IUserService;
import com.hotifi.user.validators.UserValidatorUtils;
import com.hotifi.user.web.request.UserRequest;
import com.hotifi.user.web.response.CredentialsResponse;
import com.hotifi.user.web.response.FacebookDeletionResponse;
import com.hotifi.user.web.response.FacebookDeletionStatusResponse;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.UUID;

import static com.hotifi.common.constants.codes.CloudClientCodes.GOOGLE_CLOUD_PLATFORM;

@Slf4j
public class UserServiceImpl implements IUserService {

    private final UserRepository userRepository;
    private final IVerificationService verificationService;
    private final AuthenticationRepository authenticationRepository;
    private final INotificationService notificationService;
    private final KafkaTemplate<String, String> userEventKafkaTemplate;

    @Value("${email.host}")
    private String emailHost;

    @Value("${email.no-reply-address}")
    private String noReplyEmailAddress;

    @Value("${email.no-reply-password}")
    private String noReplyEmailPassword;

    @Value("${facebook.app.secret}")
    private String facebookAppSecret;

    @Value("${facebook.app.deletion-status-url}")
    private String facebookDeletionStatusUrl;

    public UserServiceImpl(UserRepository userRepository, AuthenticationRepository authenticationRepository, IVerificationService verificationService, INotificationService notificationService, KafkaTemplate<String, String> userEventKafkaTemplate) {
        this.userRepository = userRepository;
        this.authenticationRepository = authenticationRepository;
        this.verificationService = verificationService;
        this.notificationService = notificationService;
        this.userEventKafkaTemplate = userEventKafkaTemplate;
    }

    @Override
    public UserEventDTO addUser(UserRequest userRequest) {
        UserEventDTO userEventDTO;
        Authentication authentication = authenticationRepository.findById(userRequest.getAuthenticationId()).orElse(null);
        if (authentication == null)
            throw new ApplicationException(AuthenticationErrorCodes.EMAIL_NOT_FOUND);
        if (!authentication.isEmailVerified() || !authentication.isPhoneVerified())
            throw new ApplicationException(AuthenticationErrorCodes.AUTHENTICATION_NOT_VERIFIED);
        if (userRepository.existsByFacebookId(userRequest.getFacebookId()) && userRequest.getFacebookId() != null)
            throw new ApplicationException(UserErrorCodes.FACEBOOK_USER_EXISTS);
        if (userRepository.existsByGoogleId(userRequest.getGoogleId()) && userRequest.getGoogleId() != null)
            throw new ApplicationException(UserErrorCodes.GOOGLE_USER_EXISTS);
        if (userRepository.existsByUsername(userRequest.getUsername()))
            throw new ApplicationException(UserErrorCodes.USERNAME_EXISTS);
        try {
            User user = new User();
            authentication.setActivated(true);
            authentication.setEmailOtp(null);
            setUser(userRequest, user, authentication.getId());
            userRepository.save(user);
            authenticationRepository.save(authentication);

            //Kafka Event
            UserEvent userEvent = UserEvent.
                    builder()
                    .userId(user.getId())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .email(authentication.getEmail())
                    .registrationEventTime(new Date(System.currentTimeMillis()))
                    .build();

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonMessage = objectMapper.writeValueAsString(userEvent);
            userEventKafkaTemplate.send(ApplicationConstants.KAFKA_EMAIL_TOPIC, jsonMessage);
            //notificationService.sendNotificationToSingleUser(user.getId(), "A New Beginning !", "Your hotifi account has been created.", GOOGLE_CLOUD_PLATFORM);

            userEventDTO = UserEventDTO.builder()
                    .userId(user.getId())
                    .email(authentication.getEmail())
                    .firstName(user.getFirstName())
                    .build();

        } catch (DataIntegrityViolationException e) {
            throw new ApplicationException(UserErrorCodes.USER_EXISTS);
        } catch (Exception e) {
            log.error("An error occurred : {}", e.getMessage(), e);
            throw new ApplicationException(UserErrorCodes.UNEXPECTED_USER_ERROR);
        }

        return userEventDTO;
    }

    @Override
    public CredentialsResponse resetPassword(String email, String emailOtp, String identifier, String token, SocialCodes socialCode) {
        boolean isSocialLogin = identifier != null && token != null && socialCode != null;
        boolean isCustomLogin = emailOtp != null;
        boolean isBothSocialCustomLogin = isCustomLogin == isSocialLogin;
        if (isBothSocialCustomLogin)
            throw new ApplicationException(UserErrorCodes.BAD_RESET_PASSWORD_REQUEST);
        Authentication authentication = authenticationRepository.findByEmail(email);
        User user = authentication != null ? userRepository.findByAuthenticationId(authentication.getId()) : null;
        if (user == null)
            throw new ApplicationException(UserErrorCodes.USER_NOT_FOUND);
        if (UserValidatorUtils.isAuthenticationStatusInvalid(authentication))
            throw new ApplicationException(AuthenticationErrorCodes.AUTHENTICATION_NOT_LEGIT);

        boolean isSocialUserVerified = isSocialLogin && verificationService.isSocialUserVerified(email, identifier, token, socialCode);

        if (!isSocialUserVerified && OtpUtils.isEmailOtpExpired(authentication)) {
            log.error("Social User is Not Verified & Email Otp Expired");
            throw new ApplicationException(AuthenticationErrorCodes.EMAIL_OTP_EXPIRED);
        }
        if (isSocialUserVerified || BCrypt.checkpw(emailOtp, authentication.getEmailOtp())) {
            log.info("Social User Email Verified");
            String newPassword = UUID.randomUUID().toString();
            String encryptedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
            //logger.info("new : " + newPassword);
            //logger.info("enc : " + encryptedPassword);
            authentication.setPassword(encryptedPassword);
            authenticationRepository.save(authentication);
            return new CredentialsResponse(email, newPassword);
        }

        throw new ApplicationException(UserErrorCodes.UNEXPECTED_USER_ERROR);
    }

    //To check if username is available in database
    @Override
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public void sendEmailOtpLogin(String email) {
        Authentication authentication = authenticationRepository.findByEmail(email);
        //If user doesn't exist no need to check legit authentication
        if (authentication.getEmailOtp() != null && !OtpUtils.isEmailOtpExpired(authentication))
            throw new ApplicationException(UserErrorCodes.EMAIL_OTP_ALREADY_GENERATED);
        if (UserValidatorUtils.isAuthenticationStatusInvalid(authentication))
            throw new ApplicationException(AuthenticationErrorCodes.AUTHENTICATION_NOT_LEGIT);
        OtpUtils.generateAuthenticationEmailOtp(authentication, authenticationRepository);
    }

    @Override
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }

    @Override
    public void resendEmailOtpLogin(String email) {
        Authentication authentication = authenticationRepository.findByEmail(email);
        User user = authentication != null ? userRepository.findByAuthenticationId(authentication.getId()) : null;
        if (user == null)
            throw new ApplicationException(UserErrorCodes.USER_EXISTS);
        if (UserValidatorUtils.isAuthenticationStatusInvalid(authentication))
            throw new ApplicationException(AuthenticationErrorCodes.AUTHENTICATION_NOT_LEGIT);
        OtpUtils.generateAuthenticationEmailOtp(authentication, authenticationRepository);
    }

    //DO NOT ADD @Transactional
    @Override
    public void verifyEmailOtpLogin(String email, String emailOtp) {
        Authentication authentication = authenticationRepository.findByEmail(email);
        if (OtpUtils.isEmailOtpExpired(authentication)) {
            log.error("Email Otp Expired");
            authentication.setEmailOtp(null);
            authenticationRepository.save(authentication);
            throw new ApplicationException(AuthenticationErrorCodes.EMAIL_OTP_EXPIRED);
        }

        String encryptedEmailOtp = authentication.getEmailOtp();
        String password = UUID.randomUUID().toString();
        String encryptedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        if (BCrypt.checkpw(emailOtp, encryptedEmailOtp)) {
            authentication.setEmailOtp(null);
            authentication.setEmailVerified(true);
            authentication.setPassword(encryptedPassword);
            authenticationRepository.save(authentication);
            log.info("User Email Verified");
        } else
            throw new ApplicationException(AuthenticationErrorCodes.EMAIL_OTP_INVALID);
    }

    @Override
    public void updateUser(UserRequest userRequest) {
        Long authenticationId = userRequest.getAuthenticationId();
        Authentication authentication = authenticationRepository.findById(authenticationId).orElse(null);
        User user = userRepository.findByAuthenticationId(authenticationId);
        if (UserValidatorUtils.isUserInvalid(user, authentication) && !user.isLoggedIn())
            throw new ApplicationException(UserErrorCodes.USER_NOT_LEGIT);
        setUser(userRequest, user, authenticationId);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void updateUserLogin(String email, boolean isLoggedIn) {
        Authentication authentication = authenticationRepository.findByEmail(email);
        User user = authentication != null ? userRepository.findByAuthenticationId(authentication.getId()) : null;
        if (UserValidatorUtils.isUserInvalid(user, authentication))
            throw new ApplicationException(UserErrorCodes.USER_NOT_LEGIT);
        Date logTime = new Date(System.currentTimeMillis());
        user.setLoggedIn(isLoggedIn);
        user.setLoggedAt(logTime);
        userRepository.save(user);
    }

    @Transactional
    @Override
    public User getUserByEmail(String email) {
        Authentication authentication = authenticationRepository.findByEmail(email);
        Long authenticationId = (authentication != null) ? authentication.getId() : null;
        User user = userRepository.findByAuthenticationId(authenticationId);
        if (UserValidatorUtils.isUserInvalid(user, authentication))
            throw new ApplicationException(UserErrorCodes.USER_NOT_LEGIT);
        return user;
    }

    @Override
    public FacebookDeletionResponse deleteFacebookUserData(String signedRequest) {
        FacebookProcessor facebookProcessor = new FacebookProcessor();
        try {
            JSONObject jsonObject = facebookProcessor.parseFacebookSignedRequest(signedRequest, facebookAppSecret);
            String facebookUserId = jsonObject.getString("user_id");
            String confirmationCode = "FB" + OtpUtils.generateEmailOtp();
            String url = facebookDeletionStatusUrl + facebookUserId + "/" + confirmationCode;

            //Saving Deletion Request
            User user = userRepository.findByFacebookId(facebookUserId);
            Date currentTime = new Date(System.currentTimeMillis());
            user.setFacebookDeletionCode(confirmationCode);
            user.setFacebookDeleteRequestedAt(currentTime);
            userRepository.save(user);

            return new FacebookDeletionResponse(url, confirmationCode);
        } catch (Exception e) {
            log.error("An error occurred : {}", e.getMessage(), e);
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public FacebookDeletionStatusResponse getFacebookDeletionStatus(String facebookId, String confirmationCode) {
        User user = userRepository.findByFacebookId(facebookId);
        if (user == null) throw new ApplicationException(UserErrorCodes.USER_NOT_FOUND);

        boolean isUserAuthorized = user.getFacebookDeletionCode().equals(confirmationCode);
        if (!isUserAuthorized) throw new ApplicationException(UserErrorCodes.USER_FORBIDDEN);

        Date deletionRequestedAt = user.getFacebookDeleteRequestedAt();
        return new FacebookDeletionStatusResponse(facebookId, deletionRequestedAt, false, ApplicationConstants.FACEBOOK_DELETION_STATUS_REASON, ApplicationConstants.PRIVACY_POLICY_URL);
    }

    //user defined functions
    //setting up user's values
    public void setUser(UserRequest userRequest, User user, Long authenticationId) {
        user.setAuthenticationId(authenticationId);
        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());
        user.setFacebookId(userRequest.getFacebookId());
        user.setGoogleId(userRequest.getGoogleId());
        user.setUsername(userRequest.getUsername());
        user.setPhotoUrl(userRequest.getPhotoUrl());
        user.setDateOfBirth(userRequest.getDateOfBirth());
    }

}