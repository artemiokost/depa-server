package io.depa.common.util;

import java.time.format.DateTimeFormatter;

public interface Constants {

    DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    String EMPTY = "empty";

    Integer JWT_EXPIRES_AFTER_DAY = 1440;
    Integer JWT_EXPIRES_AFTER_WEEK = 10080;
    Integer JWT_EXPIRES_AFTER_MONTH = 43800;

    String  JWT_ALGORITHM = "RS256";
    String  JWT_ISSUER = "depa.io";
    String  JWT_TOKEN_TYPE = "Bearer";

    String PRIVATE_KEY = "-----BEGIN PRIVATE KEY-----\n" +
            "YOUR PRIVATE KEY" +
            "-----END PRIVATE KEY-----";

    String PUBLIC_KEY = "-----BEGIN PUBLIC KEY-----\n" +
            "YOUR PUBLIC KEY" +
            "-----END PUBLIC KEY-----";

    String ROBOT_EMAIL = "robot@depa.io";
    String ROBOT_USERNAME = "root";
    String ROBOT_PASSWORD = "root123?";
}
