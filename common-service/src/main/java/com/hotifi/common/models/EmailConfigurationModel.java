package com.hotifi.common.models;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "email")
@Getter
@Setter
public class EmailConfigurationModel {

    private String host;

    private Integer port;

    private String noReplyAddress;

    private String noReplyPassword;

}
