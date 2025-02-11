package com.hotifi.common.constants;

public class ApplicationConstants {
        //Hotifi Configuration
        public static final String APP_VERSION = "v1";
        public static final String APP_NAME = "Hotifi";

        public static final boolean DIRECT_TRANSFER_API_ENABLED =  false;

        public static final String FIREBASE_SERVICE_ACCOUNT_PATH = "C:\\Users\\Suraj.000\\IdeaProjects\\hotifi-service-v2\\user-service\\src\\main\\resources\\firebase_service_account.yaml";
        public static final String APPLICATION_ENVIRONMENT_FILE_DIRECTORY = "C:\\Users\\Suraj.000\\Desktop\\hotifi-app";
        public static final String APPLICATION_ENVIRONMENT_FILENAME = "hotifi-secrets-pwds.env";

        //Email Paths
        public static final String EMAIL_OTP_HTML_PATH = "static/email_otp.html";
        public static final String EMAIL_WELCOME_HTML_PATH = "static/welcome.html";
        public static final String EMAIL_ACCOUNT_DELETED_HTML_PATH = "static/account_deleted.html";
        public static final String EMAIL_ACCOUNT_FROZEN_HTML_PATH = "static/account_freezed.html";
        public static final String EMAIL_BUYER_BANNED_HTML_PATH = "static/buyer_banned.html";
        public static final String EMAIL_LINKED_ACCOUNT_FAILED_PATH = "static/linked_account_failed.html";
        public static final String EMAIL_LINKED_ACCOUNT_SUCCESS_PATH = "static/linked_account_success.html";

        //Swagger Configuration
        public static final String USER_TAG = "user";
        public static final String SPEED_TEST_TAG = "speedtest";
        public static final String NOTIFICATION_TAG = "notification";
        public static final String USER_STATUS_TAG = "user-status";
        public static final String EMAIL_TAG = "email";
        public static final String DEVICE_TAG = "device";
        public static final String SESSION_TAG = "session";
        public static final String PURCHASE_TAG = "purchase";
        public static final String BANK_ACCOUNT_TAG = "bank-account";
        public static final String OFFER_TAG = "offer";
        public static final String REFERRER_TAG = "referrer";
        public static final String REFERENT_TAG = "referent";
        public static final String PAYMENT_TAG = "payment";
        public static final String STATS_TAG = "stats";
        public static final String FEEDBACK_TAG = "feedback";
        public static final String AUTHENTICATION_TAG = "authenticate";

        //Policy Urls
        public static final String PRIVACY_POLICY_URL = "https://hotifi.com/privacy-policy.html";
        public static final String FACEBOOK_DELETION_STATUS_REASON = "We use your first name, last name and email address for legal reasons as these are involved in financial transactions. To read more please read our privacy policy in below url";

        //Kafka Topics
        public static final String KAFKA_EMAIL_TOPIC = "email-notifications";
        public static final String KAFKA_EMAIL_GROUP_ID = "email-group";

}
