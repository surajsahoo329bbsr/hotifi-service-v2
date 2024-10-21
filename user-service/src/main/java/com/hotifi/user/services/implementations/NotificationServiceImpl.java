package com.hotifi.user.services.implementations;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.hotifi.common.services.interfaces.IFirebaseMessagingService;
import com.hotifi.common.constants.codes.CloudClientCodes;
import com.hotifi.user.entitiies.Device;
import com.hotifi.user.repositories.DeviceRepository;
import com.hotifi.user.services.interfaces.IDeviceService;
import com.hotifi.user.services.interfaces.INotificationService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class NotificationServiceImpl implements INotificationService {

    private final DeviceRepository deviceRepository;
    private final IDeviceService deviceService;
    private final IFirebaseMessagingService firebaseMessagingService;

    public NotificationServiceImpl(DeviceRepository deviceRepository, IDeviceService deviceService, IFirebaseMessagingService firebaseMessagingService) {
        this.deviceRepository = deviceRepository;
        this.deviceService = deviceService;
        this.firebaseMessagingService = firebaseMessagingService;
    }

    @Override
    public void sendNotificationToSingleUser(Long userId, String title, String message, CloudClientCodes notificationClientCode) {
        switch (notificationClientCode) {
            case GOOGLE_CLOUD_PLATFORM:
                Optional<String> optional = deviceService.getUserDevices(userId)
                        .stream().map(Device::getToken)
                        .reduce((first, second) -> second);
                String fcmToken = optional.orElse(null);

                try {
                    firebaseMessagingService.sendNotificationToSingleUser(title, message, fcmToken);
                } catch (FirebaseMessagingException e) {
                    log.error("Error occurred at {}", e.getMessage(), e);
                }
            case AMAZON_WEB_SERVICES:
            case AZURE:
        }
    }

    @Override
    public void sendPhotoNotificationsToMultipleUsers(List<Long> buyerIds, String title, String message, String commonPhotoUrl, CloudClientCodes notificationClientCode) {
        switch (notificationClientCode) {
            case GOOGLE_CLOUD_PLATFORM:
                List<String> fcmTokens = deviceRepository.findAllById(buyerIds).stream()
                        .map(Device::getToken)
                        .collect(Collectors.toList());
                try {
                    firebaseMessagingService.sendPhotoNotificationToMultipleUsers(title, message, commonPhotoUrl, fcmTokens);
                } catch (FirebaseMessagingException e) {
                    log.error("Error occurred at {}", e.getMessage(), e);
                }
            case AMAZON_WEB_SERVICES:
            case AZURE:
        }
    }

    @Override
    public void sendPhotoNotificationsToAllUsers(String title, String message, String photoUrl, CloudClientCodes notificationClientCode) {
        switch (notificationClientCode) {
            case GOOGLE_CLOUD_PLATFORM:
                List<String> fcmTokens = deviceRepository.findAll()
                        .stream().map(Device::getToken)
                        .collect(Collectors.toList());
                try {
                    firebaseMessagingService.sendPhotoNotificationToMultipleUsers(title, message, photoUrl, fcmTokens);
                } catch (FirebaseMessagingException e) {
                    log.error("Error occurred at {}", e.getMessage(), e);
                }

            case AMAZON_WEB_SERVICES:
            case AZURE:
        }
    }

}
