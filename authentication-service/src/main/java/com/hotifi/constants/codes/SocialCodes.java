package com.hotifi.constants.codes;

import com.hotifi.constants.BusinessConfigurations;

import java.util.Map;
import java.util.TreeMap;
import java.util.stream.IntStream;

public enum SocialCodes {

    GOOGLE, //100
    FACEBOOK, //101
    TWITTER, //102...and so on
    GITHUB,
    MICROSOFT,
    APPLE;

    private static final Map<Integer, SocialCodes> socialCodes = new TreeMap<>();

    private static final int START_VALUE = BusinessConfigurations.SOCIAL_START_VALUE_CODE;

    static {
        IntStream.range(0, values().length).forEach(i -> {
            values()[i].value = START_VALUE + i;
            socialCodes.put(values()[i].value, values()[i]);
        });
    }

    private int value;

    public static SocialCodes fromInt(int i) {
        return socialCodes.get(i);
    }

    public int value() {
        return value;
    }
}
