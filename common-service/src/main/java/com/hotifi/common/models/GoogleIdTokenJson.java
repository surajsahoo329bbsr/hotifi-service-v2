package com.hotifi.common.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@Getter
@AllArgsConstructor
public class GoogleIdTokenJson implements Serializable {

    private final String idToken;

}
