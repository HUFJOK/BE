package com.likelion.hufjok.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String originalFileName;
    private String storedFilePath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id")
    private Material material;

    @Builder
    public Attachment(String originalFileName, String storedFilePath) {
        this.originalFileName = originalFileName;
        this.storedFilePath = storedFilePath;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }
}