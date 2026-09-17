package org.txf.myblogsprinboot.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class Asset {
    private Long id;
    private String publicId;
    private Long uploaderId;
    private String originalName;
    private String mediaType;
    private String assetKind;
    private Long sizeBytes;
    private String storageProvider;
    private String bucket;
    private String objectKey;
    private String sha256;
    private String status;
    private Integer rowVersion;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
