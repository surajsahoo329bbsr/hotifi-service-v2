package com.hotifi.common.web.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class NotificationAllRequest {

    public List<Long> userIds;

    public NotificationCommonRequest notificationCommonRequest;

}
