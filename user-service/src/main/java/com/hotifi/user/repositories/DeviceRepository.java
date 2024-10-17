package com.hotifi.user.repositories;

import com.hotifi.user.entitiies.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {

    Device findByAndroidId(String androidId);
}
