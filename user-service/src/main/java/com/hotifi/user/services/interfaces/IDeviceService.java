package com.hotifi.user.services.interfaces;


import com.hotifi.user.entitiies.Device;
import com.hotifi.user.web.request.DeviceRequest;

import java.util.Set;

public interface IDeviceService {

    Device getDeviceByAndroidId(String androidId);

    void addDevice(DeviceRequest deviceRequest);

    void updateDevice(DeviceRequest deviceRequest);

    void deleteUserDevices(Long userId);

    Set<Device> getUserDevices(Long userId);

}
