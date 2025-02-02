package com.hotifi.authentication.services.implementations;

import com.hotifi.authentication.utils.OtpUtils;
import com.hotifi.common.constants.codes.CloudClientCodes;
import com.hotifi.common.constants.codes.SocialCodes;
import com.hotifi.authentication.entities.Authentication;
import com.hotifi.authentication.entities.Role;
import com.hotifi.common.exception.ApplicationException;
import com.hotifi.authentication.errors.codes.AuthenticationErrorCodes;
import com.hotifi.authentication.errors.messages.AuthenticationErrorMessages;
import com.hotifi.common.services.interfaces.IEmailService;
import com.hotifi.common.services.interfaces.IVerificationService;
import com.hotifi.common.models.RoleName;
import com.hotifi.authentication.repositories.AuthenticationRepository;
import com.hotifi.authentication.repositories.RoleRepository;
import com.hotifi.authentication.services.interfaces.IAuthenticationService;
import com.hotifi.authentication.web.response.CredentialsResponse;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Date;
import java.util.UUID;

@Slf4j
public class AuthenticationServiceImpl implements IAuthenticationService {

    private final AuthenticationRepository authenticationRepository;
    private final RoleRepository roleRepository;
    private final IVerificationService verificationService;
    private final IEmailService emailService;

    public AuthenticationServiceImpl(AuthenticationRepository authenticationRepository, RoleRepository roleRepository, IVerificationService verificationService, IEmailService emailService) {
        this.authenticationRepository = authenticationRepository;
        this.roleRepository = roleRepository;
        this.verificationService = verificationService;
        this.emailService = emailService;
    }

    @Override
    @Transactional(readOnly = true)
    public Authentication getAuthentication(String email) {
        Authentication authentication = authenticationRepository.findByEmail(email);
        /*boolean isAdminEmail = authentication != null && authentication.getRoles()
                .stream().anyMatch(role -> role.getName() == RoleName.ADMINISTRATOR);*/
        if (authentication == null)
            throw new ApplicationException(AuthenticationErrorCodes.EMAIL_NOT_FOUND);
        authentication.setCountryCode(null);
        authentication.setPhone(null);
        authentication.setEmailOtp(null);
        authentication.setPassword(null);
        return authentication;
    }

    @Transactional
    @Override
    //If login client already has email verified no need for further verification
    public CredentialsResponse addEmail(String email, String identifier, String token, String socialClient) {
        boolean socialAddEmail = !(identifier == null && token == null && socialClient == null);
        if (socialAddEmail && !verificationService.isSocialUserVerified(email, identifier, token, SocialCodes.valueOf(socialClient)))
            throw new ApplicationException(AuthenticationErrorCodes.USER_SOCIAL_IDENTIFIER_INVALID);
        try {
            boolean isEmailVerified = socialClient != null; //Do any not null check for social client or token
            Authentication authentication = new Authentication();
            Role role = roleRepository.findByRoleName(RoleName.CUSTOMER.name());
            String password = UUID.randomUUID().toString();
            String encryptedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            //log.info("pass : " + password);
            authentication.setEmail(email);
            authentication.setEmailVerified(isEmailVerified);
            authentication.setPassword(encryptedPassword);
            authentication.setRoles(Collections.singletonList(role));
            if(isEmailVerified){
                Date modifiedAt = new Date(System.currentTimeMillis());
                authentication.setModifiedAt(modifiedAt);
                authenticationRepository.save(authentication);
            }
            else {
                String emailOtp = OtpUtils.generateAuthenticationEmailOtp(authentication, authenticationRepository);
                //Populating email model with values
                emailService.sendEmailOtpEmail(email, emailOtp);
            }
            return new CredentialsResponse(authentication.getEmail(), password);
        } catch (DataIntegrityViolationException e) {
            log.error(AuthenticationErrorMessages.AUTHENTICATED_USER_EXISTS);
            throw new ApplicationException(AuthenticationErrorCodes.EMAIL_ALREADY_EXISTS);
        } catch (Exception e) {
            log.error("Error Occurred ", e);
            throw new ApplicationException(AuthenticationErrorCodes.UNEXPECTED_AUTHENTICATION_ERROR);
        }
    }

    @Transactional
    @Override
    public void resendEmailOtpSignUp(String email) {
        Authentication authentication = authenticationRepository.findByEmail(email);
        //Since it is signup so no need for verifying legit user
        if (authentication == null)
            throw new ApplicationException(AuthenticationErrorCodes.EMAIL_NOT_FOUND);
        if (authentication.isEmailVerified())
            throw new ApplicationException(AuthenticationErrorCodes.EMAIL_ALREADY_VERIFIED);
        //If token created at is null, it means otp is generated for first time or Otp duration expired and we are setting new Otp
        log.info("Regenerating Otp...");
        String emailOtp = OtpUtils.generateAuthenticationEmailOtp(authentication, authenticationRepository);

        emailService.sendEmailOtpEmail(email, emailOtp);
    }

    @Transactional
    @Override
    public void verifyEmailOtpSignUp(String email, String emailOtp) {
        Authentication authentication = authenticationRepository.findByEmail(email);
        if (authentication == null)
            throw new ApplicationException(AuthenticationErrorCodes.EMAIL_NOT_FOUND);
        if (authentication.isEmailVerified())
            throw new ApplicationException(AuthenticationErrorCodes.EMAIL_ALREADY_VERIFIED);
        if (OtpUtils.isEmailOtpExpired(authentication)) {
            log.error("Otp Expired");
            authentication.setEmailOtp(null);
            authenticationRepository.save(authentication);
            throw new ApplicationException(AuthenticationErrorCodes.EMAIL_OTP_EXPIRED);
        }
        String encryptedEmailOtp = authentication.getEmailOtp();
        if (BCrypt.checkpw(emailOtp, encryptedEmailOtp)) {
            authentication.setEmailOtp(null);
            authentication.setEmailVerified(true);
            authenticationRepository.save(authentication);
            log.info("User Email Verified");
        } else {
            throw new ApplicationException(AuthenticationErrorCodes.EMAIL_OTP_INVALID);
        }
    }

    @Transactional
    @Override
    public void verifyPhone(String email, String countryCode, String phone, String token) {
        Authentication authentication = authenticationRepository.findByEmail(email);
        if (authentication == null)
            throw new ApplicationException(AuthenticationErrorCodes.EMAIL_NOT_FOUND);
        if (!authentication.isEmailVerified())
            throw new ApplicationException(AuthenticationErrorCodes.EMAIL_NOT_VERIFIED);
        if (!verificationService.isPhoneUserVerified(countryCode, phone, token, CloudClientCodes.GOOGLE_CLOUD_PLATFORM))
            throw new ApplicationException(AuthenticationErrorCodes.PHONE_TOKEN_INVALID);
        try {
            authentication.setCountryCode(countryCode);
            authentication.setPhone(phone);
            authentication.setPhoneVerified(true);
            authenticationRepository.save(authentication);
        } catch (DataIntegrityViolationException e) {
            throw new ApplicationException(AuthenticationErrorCodes.PHONE_ALREADY_EXISTS);
        } catch (Exception e) {
            throw new ApplicationException(AuthenticationErrorCodes.UNEXPECTED_AUTHENTICATION_ERROR);
        }
    }

    @Override
    public boolean isPhoneAvailable(String phone) {
        return !authenticationRepository.existsByPhone(phone);
    }

    @Override
    public boolean isEmailAvailable(String email) {
        return !authenticationRepository.existsByEmail(email);
    }

    /*@Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Authentication authentication = authenticationRepository.findByEmail(email);
        if (email == null)
            throw new UsernameNotFoundException("Email not found");
        List<GrantedAuthority> authorities = authentication.getRoles()
                .stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .collect(Collectors.toList());
        return new User(authentication.getEmail(), authentication.getPassword(), authorities);
    }*/
}
