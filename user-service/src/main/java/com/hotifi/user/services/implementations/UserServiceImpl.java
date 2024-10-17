package com.hotifi.user.services.implementations;

import com.google.api.client.util.Value;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
public class UserServiceImpl {

    //private final UserRepository userRepository;
    //private final IEmailService emailService;
    //private final IVerificationService verificationService;

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

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);


    /*public UserServiceImpl(UserRepository userRepository, AuthenticationRepository authenticationRepository, IEmailService emailService, IVerificationService verificationService) {
        this.userRepository = userRepository;
        this.authenticationRepository = authenticationRepository;
        this.emailService = emailService;
        this.verificationService = verificationService;
    }

    @Override
    @Transactional
    public void addUser(UserRequest userRequest) {
        Authentication authentication = authenticationRepository.findById(userRequest.getAuthenticationId()).orElse(null);
        if (authentication == null)
            throw new HotifiException(AuthenticationErrorCodes.EMAIL_NOT_FOUND);
        if (!authentication.isEmailVerified() || !authentication.isPhoneVerified())
            throw new HotifiException(AuthenticationErrorCodes.AUTHENTICATION_NOT_VERIFIED);
        if (userRepository.existsByFacebookId(userRequest.getFacebookId()) && userRequest.getFacebookId() != null)
            throw new HotifiException(UserErrorCodes.FACEBOOK_USER_EXISTS);
        if (userRepository.existsByGoogleId(userRequest.getGoogleId()) && userRequest.getGoogleId() != null)
            throw new HotifiException(UserErrorCodes.GOOGLE_USER_EXISTS);
        if (userRepository.existsByUsername(userRequest.getUsername()))
            throw new HotifiException(UserErrorCodes.USERNAME_EXISTS);
        try {
            User user = new User();
            authentication.setActivated(true);
            authentication.setEmailOtp(null);
            setUser(userRequest, user, authentication);
            userRepository.save(user);
            authenticationRepository.save(authentication);

            EmailModel emailModel = new EmailModel();
            emailModel.setToEmail(authentication.getEmail());
            emailModel.setFromEmail(noReplyEmailAddress);
            emailModel.setFromEmailPassword(noReplyEmailPassword);
            emailService.sendWelcomeEmail(user, emailModel);
        } catch (DataIntegrityViolationException e) {
            throw new HotifiException(UserErrorCodes.USER_EXISTS);
        } catch (Exception e) {
            log.error("Error ", e);
            throw new HotifiException(UserErrorCodes.UNEXPECTED_USER_ERROR);
        }
    }

    @Transactional
    @Override
    public CredentialsResponse resetPassword(String email, String emailOtp, String identifier, String token, SocialCodes socialCode) {
        boolean isSocialLogin = identifier != null && token != null && socialCode != null;
        boolean isCustomLogin = emailOtp != null;
        boolean isBothSocialCustomLogin = isCustomLogin == isSocialLogin;
        if (isBothSocialCustomLogin)
            throw new HotifiException(UserErrorCodes.BAD_RESET_PASSWORD_REQUEST);
        Authentication authentication = authenticationRepository.findByEmail(email);
        User user = authentication != null ? userRepository.findByAuthenticationId(authentication.getId()) : null;
        if (user == null)
            throw new HotifiException(UserErrorCodes.USER_NOT_FOUND);
        if (!LegitUtils.isAuthenticationLegit(authentication))
            throw new HotifiException(AuthenticationErrorCodes.AUTHENTICATION_NOT_LEGIT);

        boolean isSocialUserVerified = isSocialLogin && verificationService.isSocialUserVerified(email, identifier, token, socialCode);

        if (!isSocialUserVerified && OtpUtils.isEmailOtpExpired(authentication)) {
            log.error("Otp Expired");
            throw new HotifiException(AuthenticationErrorCodes.EMAIL_OTP_EXPIRED);
        }
        if (isSocialUserVerified || BCrypt.checkpw(emailOtp, authentication.getEmailOtp())) {
            log.info("User Email Verified");
            String newPassword = UUID.randomUUID().toString();
            String encryptedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
            //log.info("new : " + newPassword);
            //log.info("enc : " + encryptedPassword);
            authentication.setPassword(encryptedPassword);
            authenticationRepository.save(authentication);
            return new CredentialsResponse(email, newPassword);
        }

        throw new HotifiException(UserErrorCodes.UNEXPECTED_USER_ERROR);
    }

    //To check if username is available in database
    @Override
    @Transactional
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional
    @Override
    public void sendEmailOtpLogin(String email) {
        Authentication authentication = authenticationRepository.findByEmail(email);
        //If user doesn't exist no need to check legit authentication
        if (authentication.getEmailOtp() != null && !OtpUtils.isEmailOtpExpired(authentication))
            throw new HotifiException(UserErrorCodes.EMAIL_OTP_ALREADY_GENERATED);
        if (!LegitUtils.isAuthenticationLegit(authentication))
            throw new HotifiException(AuthenticationErrorCodes.AUTHENTICATION_NOT_LEGIT);

        OtpUtils.saveAuthenticationEmailOtp(authentication, authenticationRepository, emailService);
    }

    @Override
    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }

    @Transactional
    @Override
    public void resendEmailOtpLogin(String email) {
        Authentication authentication = authenticationRepository.findByEmail(email);
        User user = authentication != null ? userRepository.findByAuthenticationId(authentication.getId()) : null;
        if (user == null)
            throw new HotifiException(UserErrorCodes.USER_EXISTS);
        if (!LegitUtils.isAuthenticationLegit(authentication))
            throw new HotifiException(AuthenticationErrorCodes.AUTHENTICATION_NOT_LEGIT);

        OtpUtils.saveAuthenticationEmailOtp(authentication, authenticationRepository, emailService);
    }

    //DO NOT ADD @Transactional
    @Override
    public void verifyEmailOtpLogin(String email, String emailOtp) {
        Authentication authentication = authenticationRepository.findByEmail(email);
        if (OtpUtils.isEmailOtpExpired(authentication)) {
            log.error("Otp Expired");
            authentication.setEmailOtp(null);
            authenticationRepository.save(authentication);
            throw new HotifiException(AuthenticationErrorCodes.EMAIL_OTP_EXPIRED);
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
            throw new HotifiException(AuthenticationErrorCodes.EMAIL_OTP_INVALID);
    }

    @Override
    @Transactional
    public void updateUserLogin(String email, boolean isLogin) {
        Authentication authentication = authenticationRepository.findByEmail(email);
        User user = authentication != null ? userRepository.findByAuthenticationId(authentication.getId()) : null;
        if (!LegitUtils.isUserLegit(user))
            throw new HotifiException(UserErrorCodes.USER_NOT_LEGIT);
        Date logTime = new Date(System.currentTimeMillis());
        user.setLoggedIn(isLogin);
        user.setLoggedAt(logTime);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void updateUser(UserRequest userUpdateRequest) {
        Long authenticationId = userUpdateRequest.getAuthenticationId();
        User user = userRepository.findByAuthenticationId(authenticationId);
        if (!LegitUtils.isUserLegit(user) && !user.isLoggedIn())
            throw new HotifiException(UserErrorCodes.USER_NOT_LEGIT);
        Authentication authentication = user.getAuthentication();
        setUser(userUpdateRequest, user, authentication);
        userRepository.save(user);
    }

    @Transactional
    @Override
    public User getUserByEmail(String email) {
        Authentication authentication = authenticationRepository.findByEmail(email);
        Long authenticationId = (authentication != null) ? authentication.getId() : null;
        User user = userRepository.findByAuthenticationId(authenticationId);
        if (!LegitUtils.isUserLegit(user))
            throw new HotifiException(UserErrorCodes.USER_NOT_LEGIT);
        return user;
    }


    @Override
    @Transactional
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
            logger.error("An error occurred : {}", e.getMessage(), e);
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public FacebookDeletionStatusResponse getFacebookDeletionStatus(String facebookId, String confirmationCode) {
        User user = userRepository.findByFacebookId(facebookId);
        if (user == null) throw new HotifiException(UserErrorCodes.USER_NOT_FOUND);

        boolean isUserAuthorized = user.getFacebookDeletionCode().equals(confirmationCode);
        if (!isUserAuthorized) throw new HotifiException(UserErrorCodes.USER_FORBIDDEN);

        Date deletionRequestedAt = user.getFacebookDeleteRequestedAt();
        String reason = "We use your first name, last name and email address for legal reasons as these are involved in financial transactions. To read more please read our privacy policy in below url";
        return new FacebookDeletionStatusResponse(facebookId, deletionRequestedAt, false, reason, AppConfigurations.PRIVACY_POLICY_URL);
    }

    //user defined functions
    //setting up user's values
    public void setUser(UserRequest userRequest, User user, Authentication authentication) {
        user.setAuthentication(authentication);
        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());
        user.setFacebookId(userRequest.getFacebookId());
        user.setGoogleId(userRequest.getGoogleId());
        user.setUsername(userRequest.getUsername());
        user.setPhotoUrl(userRequest.getPhotoUrl());
        user.setDateOfBirth(userRequest.getDateOfBirth());
    }*/

}