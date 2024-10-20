package com.hotifi.user.services.interfaces;


import com.hotifi.common.dto.UserEventDTO;
import com.hotifi.user.entitiies.UserStatus;
import com.hotifi.user.web.request.UserStatusRequest;

import java.util.List;

public interface IUserStatusService {

    List<UserStatus> addUserStatus(UserStatusRequest userStatusRequest);

    List<UserStatus> getUserStatusByUserId(Long userId);

    UserEventDTO freezeUser(Long id, boolean freezeUser);

}
