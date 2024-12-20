package com.hotifi.user.services.implementations;

import com.google.api.client.util.Value;
import com.hotifi.authentication.entities.Authentication;
import com.hotifi.authentication.repositories.AuthenticationRepository;
import com.hotifi.common.dto.UserEventDTO;
import com.hotifi.common.exception.ApplicationException;
import com.hotifi.user.entitiies.User;
import com.hotifi.user.entitiies.UserStatus;
import com.hotifi.user.errors.codes.UserStatusErrorCodes;
import com.hotifi.user.events.UserEvent;
import com.hotifi.user.repositories.UserRepository;
import com.hotifi.user.repositories.UserStatusRepository;
import com.hotifi.user.services.interfaces.IDeviceService;
import com.hotifi.user.services.interfaces.INotificationService;
import com.hotifi.user.services.interfaces.IUserStatusService;
import com.hotifi.user.web.request.UserStatusRequest;
import lombok.extern.slf4j.Slf4j;
import com.hotifi.user.errors.codes.UserErrorCodes;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.hotifi.common.constants.ApplicationConstants.KAFKA_EMAIL_TOPIC;
import static com.hotifi.common.constants.codes.CloudClientCodes.GOOGLE_CLOUD_PLATFORM;

@Slf4j
public class UserStatusServiceImpl implements IUserStatusService {

    private final AuthenticationRepository authenticationRepository;
    private final UserStatusRepository userStatusRepository;
    private final UserRepository userRepository;
    private final IDeviceService deviceService;
    private final INotificationService notificationService;
    private final KafkaTemplate<String, UserEvent> userEventKafkaTemplate;

    @Value("${email.no-reply-address}")
    private String noReplyEmailAddress;

    @Value("${email.no-reply-password}")
    private String noReplyEmailPassword;

    public UserStatusServiceImpl(AuthenticationRepository authenticationRepository, UserStatusRepository userStatusRepository, UserRepository userRepository, IDeviceService deviceService, INotificationService notificationService, KafkaTemplate<String, UserEvent> userEventKafkaTemplate){
        this.authenticationRepository = authenticationRepository;
        this.userStatusRepository = userStatusRepository;
        this.userRepository = userRepository;
        this.deviceService = deviceService;
        this.notificationService = notificationService;
        this.userEventKafkaTemplate = userEventKafkaTemplate;
    }

    @Transactional
    @Override
    public List<UserStatus> addUserStatus(UserStatusRequest userStatusRequest) {
        //Request must be of user being warned or deleted
        boolean isUserBeingWarnedXorDeleted = userStatusRequest.getWarningReason() != null ^ userStatusRequest.getDeleteReason() != null;

        if (!isUserBeingWarnedXorDeleted)
            throw new ApplicationException(UserStatusErrorCodes.NEITHER_WARNING_NOR_DELETE_REASON);
        //Get user from id
        User user = userRepository.findById(userStatusRequest.getUserId()).orElse(null);
        Long authenticationId = user != null ? user.getAuthenticationId() : null;
        Authentication authentication = authenticationId != null ? authenticationRepository.findById(authenticationId).orElse(null) : null;

        if (authentication == null)
            throw new ApplicationException(UserErrorCodes.USER_NOT_FOUND);
        //Logic to see user if he/she has been freezed or banned
        if (authentication.isDeleted())
            throw new ApplicationException(UserErrorCodes.USER_ALREADY_DELETED);
        if (authentication.isBanned())
            throw new ApplicationException(UserErrorCodes.USER_ALREADY_BANNED);
        if (authentication.isFrozen())
            throw new ApplicationException(UserErrorCodes.USER_ALREADY_FREEZED);

        //If user is not banned or deleted or freezed
        Long userId = userStatusRequest.getUserId();
        UserStatus userStatus = new UserStatus();
        userStatus.setUser(user);
        userStatus.setRole(userStatusRequest.getRole());

        Date now = new Date(System.currentTimeMillis());

        if (userStatusRequest.getDeleteReason() != null) {
            userStatus.setDeleteReason(userStatusRequest.getDeleteReason());
            userStatus.setDeletedAt(now);
            deleteUser(userId);
        } else {
            //else the warning reason is not null
            userStatus.setWarningReason(userStatusRequest.getWarningReason());
            userStatus.setWarningCreatedAt(now);

            //TODO - remove them after observation and not automate the freeze and ban process

            /*if (userStatuses != null) {
                if (userStatuses.size() == BusinessConfigurations.MINIMUM_WARNINGS_TO_FREEZE - 1) {
                    //freeze
                    if (userStatusRequest.getFreezeReason() == null)
                        throw new ApplicationException(UserStatusErrorCodes.USER_FREEZE_REASON_ABSENT);
                    userStatus.setFreezeReason(userStatusRequest.getFreezeReason());
                    userStatus.setFreezeCreatedAt(now);
                    userStatus.setFreezePeriod(BusinessConfigurations.MINIMUM_FREEZE_PERIOD_HOURS); //24 hours
                    freezeUser(userId, true);
                } else if (userStatuses.size() == BusinessConfigurations.MINIMUM_WARNINGS_TO_BAN - 1) {
                    //ban
                    if (userStatusRequest.getBanReason() == null)
                        throw new ApplicationException(UserStatusErrorCodes.USER_BAN_REASON_ABSENT);
                    userStatus.setBanReason(userStatusRequest.getBanReason());
                    userStatus.setBanCreatedAt(now);
                    banUser(userId, true);
                }
            }*/
        }

        userStatusRepository.save(userStatus);
        return getUserStatusByUserId(userId);
    }

    @Transactional
    @Override
    public List<UserStatus> getUserStatusByUserId(Long userId) {
        return userStatusRepository.findUserStatusByUserId(userId);
    }

    //for ban, deactivate, freeze and delete check user_status table for reasons to do so
    //UserDefined functions
    @Override
    @Transactional
    public UserEventDTO freezeUser(Long userId, boolean freezeUser) {

        User user = userRepository.findById(userId).orElse(null);
        Long authenticationId = user != null ? user.getAuthenticationId() : null;
        Authentication authentication = authenticationId != null ? authenticationRepository.findById(authenticationId).orElse(null) : null;
        UserEventDTO userEventDTO = null;

        if (authentication == null)
            throw new ApplicationException(UserErrorCodes.USER_NOT_FOUND);
        //If we are unfreezing a freezed user
        if (!freezeUser) {
            //Logic to activate user if he/she has been freezed or banned
            if (authentication.isDeleted())
                throw new ApplicationException(UserErrorCodes.USER_ALREADY_DELETED);
            if (authentication.isBanned())
                throw new ApplicationException(UserErrorCodes.USER_ALREADY_BANNED);
            //Check if freezed user is activating
            if (authentication.isFrozen()) {

                List<UserStatus> userStatuses = getUserStatusByUserId(user.getId())
                        .stream()
                        .filter(userStatus -> userStatus.getFreezeReason() != null)
                        .collect(Collectors.toList());

                userStatuses.forEach(userStatus -> {
                    if (!isFreezePeriodExpired(userStatus)) {
                        log.error("Freeze period not over yet.");
                        throw new ApplicationException(UserStatusErrorCodes.USER_FREEZE_PERIOD_ACTIVE);
                    }
                });
            }
        }

        if (authentication.isFrozen() && freezeUser) {
            throw new ApplicationException(UserErrorCodes.USER_ALREADY_FREEZED);
        } else if (!authentication.isFrozen() && !freezeUser) {
            throw new ApplicationException(UserErrorCodes.USER_ALREADY_NOT_FREEZED);
        } else {
            authentication.setFrozen(freezeUser);
            authenticationRepository.save(authentication);
            if (freezeUser) {
                UserEvent userEvent = UserEvent.
                        builder()
                        .userId(user.getId())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .email(authentication.getEmail())
                        .registrationEventTime(new Date(System.currentTimeMillis()))
                        .build();

                userEventKafkaTemplate.send(KAFKA_EMAIL_TOPIC, userEvent);
                notificationService.sendNotificationToSingleUser(userId, "Account Suspended", "Your hotifi account for buying data has been suspended.", GOOGLE_CLOUD_PLATFORM);

                userEventDTO = UserEventDTO.builder()
                        .userId(user.getId())
                        .email(authentication.getEmail())
                        .firstName(user.getFirstName())
                        .build();
            }
        }

        return userEventDTO;
    }

    @Transactional
    public UserEventDTO banUser(Long userId, boolean banUser) {
        User user = userRepository.findById(userId).orElse(null);
        Long authenticationId = user != null ? user.getAuthenticationId() : null;
        Authentication authentication = authenticationId != null ? authenticationRepository.findById(authenticationId).orElse(null) : null;
        UserEventDTO userEventDTO = null;

        if ((authentication != null && authentication.isBanned()) && banUser)
            throw new ApplicationException(UserErrorCodes.USER_ALREADY_BANNED);

        if (!(authentication != null && authentication.isBanned()) && !banUser)
            throw new ApplicationException(UserErrorCodes.USER_ALREADY_NOT_BANNED);

        if (user != null && authentication != null) {
            if (banUser) {
                user.setLoggedIn(false);
                userRepository.save(user);
                UserEvent userEvent = UserEvent.
                        builder()
                        .userId(user.getId())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .email(authentication.getEmail())
                        .registrationEventTime(new Date(System.currentTimeMillis()))
                        .build();

                userEventKafkaTemplate.send(KAFKA_EMAIL_TOPIC, userEvent);
                notificationService.sendNotificationToSingleUser(userId, "Banned !", "Your hotifi account for buying data has been deleted.", GOOGLE_CLOUD_PLATFORM);
                userEventDTO = UserEventDTO.builder()
                        .userId(user.getId())
                        .email(authentication.getEmail())
                        .firstName(user.getFirstName())
                        .build();
            }
            authentication.setBanned(banUser);
            authenticationRepository.save(authentication);

            return userEventDTO;
        }
        return userEventDTO;
    }

    @Transactional
    public UserEventDTO deleteUser(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        Long authenticationId = user != null ? user.getAuthenticationId() : null;
        Authentication authentication = authenticationId != null ? authenticationRepository.findById(authenticationId).orElse(null) : null;
        if (authentication == null)
            throw new ApplicationException(UserErrorCodes.USER_NOT_FOUND);

        if (authentication.isDeleted()) {
            log.error("Account already deleted");
            throw new ApplicationException(UserErrorCodes.USER_ALREADY_DELETED);
        }
        //set authentication values to null
        Date deletedAt = new Date(System.currentTimeMillis());
        authentication.setDeleted(true);
        authentication.setEmail("(deleted)" + deletedAt); //email cannot be null
        authentication.setPhone(null);
        authenticationRepository.save(authentication);

        //delete user devices
        deviceService.deleteUserDevices(userId);

        //delete linked bank account
        //TODO - move it to other service
        /*if (user.getBankAccount() != null)
            bankAccountRepository.delete(user.getBankAccount());*/

        //set user values to null
        user.setFacebookId(null);
        user.setGoogleId(null);
        user.setLoggedIn(false);
        userRepository.save(user);

        UserEvent userEvent = UserEvent.
                builder()
                .userId(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(authentication.getEmail())
                .registrationEventTime(new Date(System.currentTimeMillis()))
                .build();

        userEventKafkaTemplate.send(KAFKA_EMAIL_TOPIC, userEvent);
        notificationService.sendNotificationToSingleUser(userId, "Sorry To See You Go !", "Your hotifi account has been deleted.", GOOGLE_CLOUD_PLATFORM);

        return UserEventDTO.builder()
                .userId(user.getId())
                .email(authentication.getEmail())
                .firstName(user.getFirstName())
                .build();
    }

    public boolean isFreezePeriodExpired(UserStatus userStatus) {
        Date currentTime = new Date(System.currentTimeMillis());
        long timeDifference = currentTime.getTime() - userStatus.getFreezeCreatedAt().getTime();
        long hoursDifference = timeDifference / (60L * 60L * 1000L);
        return hoursDifference >= userStatus.getFreezePeriod(); // If time period has exceeded freeze period
    }

}
