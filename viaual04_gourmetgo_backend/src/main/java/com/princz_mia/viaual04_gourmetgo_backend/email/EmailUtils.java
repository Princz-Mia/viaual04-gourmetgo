package com.princz_mia.viaual04_gourmetgo_backend.email;

import com.princz_mia.viaual04_gourmetgo_backend.data.entity.Coupon;

public class EmailUtils {

    public static String getEmailMessage(String name, String host, String key) {
        return "Hello " + name + ",\n\nYour new account has been created. Please click on the link below to verify your account.\n\n" +
                getCustomerVerificationUrl(host, key) + "\n\nThe Support Team";
    }

    public static String getRestaurantRegistrationEmailMessage(String name, String host) {
        return "Dear " + name + ",\n\nYour restaurant registration has been saved. Soon the support team will check and judge your request. Until then please wait with patience.\n\nThe Support Team";
    }

    public static String getResetPasswordMessage(String name, String host, String key) {
        return "Hello " + name + ",\n\nYou have requested password restoration. Please click on the link below to reset your password.\n\n" +
                getResetPasswordUrl(host, key) + "\n\nThe Support Team";
    }

    public static String getCouponPublishedMessage(String name, Coupon coupon) {
        return "Hello " + name + ",\n\nWe have delightful news for you. A new coupon code is available for you to use.\n\n" +
               "Coupon code: "+ coupon.getCode() + "\n\nUse it next time you order from on of out restaurants. Be quick the expiration date of this coupon is " + coupon.getExpirationDate().toString() + "\n\nThe Support Team";
    }

    public static String getRestaurantApprovedMessage(String name, String host, String key) {
        return "Congratulations " + name + "!\n\nWe have delightful news for you. Your registration request has been approved. Please click on the link below to set your password and verify your account.\n\n" +
        getRestaurantVerificationUrl(host, key) + "\n\nThe Support Team";
    }

    private static String getCustomerVerificationUrl(String host, String key) {
        return host + "/verify/customer?key=" + key;
    }
    private static String getRestaurantVerificationUrl(String host, String key) {
        return host + "/verify/restaurant?key=" + key;
    }

    private static String getResetPasswordUrl(String host, String key) {
        return host + "/reset-password/password?key=" + key;
    }
}
