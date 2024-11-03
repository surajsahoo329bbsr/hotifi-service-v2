package com.hotifi.speedtest.services.implementations;

import com.hotifi.authentication.entities.Authentication;
import com.hotifi.common.constants.BusinessConstants;
import com.hotifi.common.exception.ApplicationException;
import com.hotifi.speedtest.constants.codes.NetworkProviderCodes;
import com.hotifi.speedtest.entities.SpeedTest;
import com.hotifi.speedtest.errors.codes.SpeedTestErrorCodes;
import com.hotifi.speedtest.repositories.SpeedTestRepository;
import com.hotifi.speedtest.services.interfaces.ISpeedTestService;
import com.hotifi.speedtest.web.request.SpeedTestRequest;
import com.hotifi.user.entitiies.User;
import com.hotifi.user.errors.codes.UserErrorCodes;
import com.hotifi.user.repositories.UserRepository;
import com.hotifi.user.validators.UserValidatorUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
public class SpeedTestServiceImpl implements ISpeedTestService {

    private final UserRepository userRepository;
    private final SpeedTestRepository speedTestRepository;

    public SpeedTestServiceImpl(UserRepository userRepository, SpeedTestRepository speedTestRepository) {
        this.userRepository = userRepository;
        this.speedTestRepository = speedTestRepository;
    }

    @Transactional
    @Override
    public void addSpeedTest(SpeedTestRequest speedTestRequest) {

        boolean isBelowWifiDownloadSpeed = Double.compare(speedTestRequest.getDownloadSpeed(), BusinessConstants.MINIMUM_WIFI_DOWNLOAD_SPEED_MEGABYTES) < 0
                && speedTestRequest.getNetworkProvider().equals(NetworkProviderCodes.WIFI.name());

        boolean isBelowWifiUploadSpeed = Double.compare(speedTestRequest.getUploadSpeed(), BusinessConstants.MINIMUM_WIFI_UPLOAD_SPEED_MEGABYTES) < 0
                && speedTestRequest.getNetworkProvider().equals(NetworkProviderCodes.WIFI.name());

        if(isBelowWifiDownloadSpeed || isBelowWifiUploadSpeed){
            throw new ApplicationException(SpeedTestErrorCodes.SPEED_TEST_INVALID_WIFI_RECORD);
        }

        User user = userRepository.findById(speedTestRequest.getUserId()).orElse(null);

        RestTemplate restTemplate = new RestTemplate();
        Authentication authentication = restTemplate.getForObject("http://localhost:5001/authentication/" + user.getAuthenticationId(), Authentication.class);

        if (UserValidatorUtils.isUserInvalid(user, authentication) && !user.isLoggedIn())
            throw new ApplicationException(UserErrorCodes.USER_NOT_LEGIT);
        try {
            SpeedTest speedTest = new SpeedTest();
            speedTest.setNetworkProvider(speedTestRequest.getNetworkProvider());
            speedTest.setDownloadSpeed(speedTestRequest.getDownloadSpeed());
            speedTest.setUploadSpeed(speedTestRequest.getUploadSpeed());
            speedTest.setPinCode(speedTestRequest.getPinCode());
            speedTest.setUser(user);
            speedTestRepository.save(speedTest);
        } catch (Exception e) {
            log.error("Error occurred at {}", e.getMessage(), e);
            throw new ApplicationException(SpeedTestErrorCodes.UNEXPECTED_SPEED_TEST_ERROR);
        }
    }

    @Transactional
    @Override
    public SpeedTest getLatestSpeedTest(Long userId, String pinCode, boolean isWifi) {
        SpeedTest speedTest = isWifi ?
                speedTestRepository.findLatestWifiSpeedTest(userId, pinCode) :
                speedTestRepository.findLatestNonWifiSpeedTest(userId, pinCode);
        if (speedTest == null)
            throw new ApplicationException(SpeedTestErrorCodes.SPEED_TEST_RECORD_NOT_FOUND);
        return speedTest;
    }

    //For Get Speed Tests call sortByDateTime in Descending format
    @Transactional
    @Override
    public List<SpeedTest> getSortedSpeedTestByDateTime(Long userId, int page, int size, boolean isDescending) {
        try {
            Pageable sortedPageableByDateTime
                    = isDescending ?
                    PageRequest.of(page, size, Sort.by("created_at").descending())
                    : PageRequest.of(page, size, Sort.by("created_at"));
            return speedTestRepository.findSpeedTestsByUserId(userId, sortedPageableByDateTime);
        } catch (Exception e) {
            log.error("Error occurred at {}", e.getMessage(), e);
            throw new ApplicationException(SpeedTestErrorCodes.UNEXPECTED_SPEED_TEST_ERROR);
        }
    }

    @Transactional
    @Override
    public List<SpeedTest> getSortedSpeedTestByUploadSpeed(Long userId, int page, int size, boolean isDescending) {
        try {
            Pageable sortedPageableByUploadSpeed
                    = isDescending ?
                    PageRequest.of(page, size, Sort.by("upload_speed").descending())
                    : PageRequest.of(page, size, Sort.by("upload_speed"));
            return speedTestRepository.findSpeedTestsByUserId(userId, sortedPageableByUploadSpeed);
        } catch (Exception e) {
            log.error("Error occurred at {}", e.getMessage(), e);
            throw new ApplicationException(SpeedTestErrorCodes.UNEXPECTED_SPEED_TEST_ERROR);
        }
    }

    @Transactional
    @Override
    public List<SpeedTest> getSortedTestByDownloadSpeed(Long userId, int page, int size, boolean isDescending) {
        try {
            Pageable sortedPageableByDownloadSpeed
                    = isDescending ?
                    PageRequest.of(page, size, Sort.by("download_speed").descending())
                    : PageRequest.of(page, size, Sort.by("download_speed"));
            return speedTestRepository.findSpeedTestsByUserId(userId, sortedPageableByDownloadSpeed);
        } catch (Exception e) {
            log.error("Error occurred at {}", e.getMessage(), e);
            throw new ApplicationException(SpeedTestErrorCodes.UNEXPECTED_SPEED_TEST_ERROR);
        }
    }

}
