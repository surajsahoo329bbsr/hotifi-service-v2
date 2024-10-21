package com.hotifi.session.services.interfaces;

import com.hotifi.session.entities.SpeedTest;
import com.hotifi.session.web.request.SpeedTestRequest;

import java.util.List;

public interface ISpeedTestService {

    void addSpeedTest(SpeedTestRequest speedTestRequest);

    SpeedTest getLatestSpeedTest(Long userId, String pinCode, boolean isWifi);

    List<SpeedTest> getSortedSpeedTestByUploadSpeed(Long userId, int page, int size, boolean isDescending);

    List<SpeedTest> getSortedTestByDownloadSpeed(Long userId, int page, int size, boolean isDescending);

    List<SpeedTest> getSortedSpeedTestByDateTime(Long userId, int page, int size, boolean isDescending);

}
