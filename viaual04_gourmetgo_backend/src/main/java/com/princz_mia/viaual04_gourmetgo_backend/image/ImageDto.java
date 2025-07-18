package com.princz_mia.viaual04_gourmetgo_backend.image;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageDto {
    private UUID id;
    private String fileName;
    private String downloadUrl;
}