package com.princz_mia.viaual04_gourmetgo_backend.email;

import com.princz_mia.viaual04_gourmetgo_backend.data.entity.Coupon;
import com.princz_mia.viaual04_gourmetgo_backend.exception.AppException;
import com.princz_mia.viaual04_gourmetgo_backend.exception.ErrorType;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import static com.princz_mia.viaual04_gourmetgo_backend.email.EmailUtils.*;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final String NEW_USER_ACCOUNT_VERIFICATION = "New User Account Verification";
    private static final String NEW_RESTAURANT_ACCOUNT_REGISTRATION = "Your Restaurant Registration Is Being Reviewed";
    private static final String PASSWORD_RESET_REQUEST = "Reset Password Request";
    private static final String NEW_COUPON_HAS_BEEN_PUBLISHED = "A new Coupon has been published";
    private static final String RESTAURANT_HAS_BEEN_APPROVED = "Congratulations! Your Restaurant has been approved";

    private final JavaMailSender sender;

    @Value("http://localhost:5173")
    private String host;
    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    public void sendNewAccountEmail(String name, String toEmail, String key) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setSubject(NEW_USER_ACCOUNT_VERIFICATION);
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setText(getEmailMessage(name, host, key));
            sender.send(message);
        } catch (Exception e) {
            throw new AppException("Unable to send email, cause: " + e.getMessage(), ErrorType.EXTERNAL_SERVICE_ERROR);
        }
    }

    @Async
    public void sendPasswordResetEmail(String name, String toEmail, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setSubject(PASSWORD_RESET_REQUEST);
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setText(getResetPasswordMessage(name, host, token));
            sender.send(message);
        } catch (Exception e) {
            throw new AppException("Unable to send email, cause: " + e.getMessage(), ErrorType.EXTERNAL_SERVICE_ERROR);
        }
    }

    @Async
    public void sendRestaurantRegistrationEmail(String ownerName, @Email String toEmail) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setSubject(NEW_RESTAURANT_ACCOUNT_REGISTRATION);
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setText(getRestaurantRegistrationEmailMessage(ownerName, host));
            sender.send(message);
        } catch (Exception e) {
            throw new AppException("Unable to send email, cause: " + e.getMessage(), ErrorType.EXTERNAL_SERVICE_ERROR);
        }
    }

    @Async
    public void sendNewCouponPublishedEmail(String name, String toEmail, Coupon coupon) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setSubject(NEW_COUPON_HAS_BEEN_PUBLISHED);
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setText(getCouponPublishedMessage(name, coupon));
            sender.send(message);
        } catch (Exception e) {
            throw new AppException("Unable to send email, cause: " + e.getMessage(), ErrorType.EXTERNAL_SERVICE_ERROR);
        }
    }

    @Async
    public void sendRestaurantApprovedEmail(String ownerName, @Email String toEmail, String key) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setSubject(RESTAURANT_HAS_BEEN_APPROVED);
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setText(getRestaurantApprovedMessage(ownerName, host, key));
            sender.send(message);
        } catch (Exception e) {
            throw new AppException("Unable to send email, cause: " + e.getMessage(), ErrorType.EXTERNAL_SERVICE_ERROR);
        }
    }
}