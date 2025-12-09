package com.princz_mia.viaual04_gourmetgo_backend.events;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HappyHourEvent {
    private String action;
    private Long happyHourId;
}