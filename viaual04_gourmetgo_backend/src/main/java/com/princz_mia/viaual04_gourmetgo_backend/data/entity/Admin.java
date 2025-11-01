package com.princz_mia.viaual04_gourmetgo_backend.data.entity;

import com.princz_mia.viaual04_gourmetgo_backend.data.entity.User;
import jakarta.persistence.Entity;
import lombok.*;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Admin extends User {

    private String fullName;
}
