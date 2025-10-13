package com.princz_mia.viaual04_gourmetgo_backend.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base entity representing a user in the system.
 * <p>
 * This class is designed to be extended by specific user roles (e.g., Customer, Admin)
 * using joined table inheritance strategy.
 * </p>
 *
 * Fields include identification, authentication status, and metadata like login timestamps.
 */
@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name="\"user\"")
@SQLDelete(sql = "UPDATE \"user\" SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
@Inheritance(strategy = InheritanceType.JOINED)
public class User {

    /**
     * Unique identifier for the user.
     * Automatically generated using a random UUID strategy.
     */
    @Id
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    private UUID id;

    /**
     * Unique and natural identifier used for login and communication.
     * Must be a valid email address.
     */
    @NaturalId
    @Email
    @Column(unique = true)
    private String emailAddress;

    /**
     * Timestamp of the user's last successful login.
     */
    private LocalDateTime lastLogin;

    /**
     * Timestamp indicating when the user account was created.
     */
    private LocalDateTime createdAt;

    /**
     * Number of consecutive failed login attempts.
     * Can be used for locking the account after a threshold.
     */
    private Integer loginAttempts;

    /**
     * Flag indicating if the account is currently non-locked.
     */
    private boolean isAccountNonLocked;

    /**
     * Flag indicating if the account is currently enabled.
     */
    private boolean isEnabled;

    @Column(nullable = false)
    private boolean deleted = false;
}

