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

    @Column(nullable = false)
    private String originalFileName;

    @Column(nullable = false)
    private String storedFilePath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @Builder
    public Attachment(String originalFileName, String storedFilePath, Material material) {
        this.originalFileName = originalFileName;
        this.storedFilePath = storedFilePath;

        if (material != null) {
            setMaterial(material);
        }
    }

    public void setMaterial(Material material) {
        this.material = material;
    }
}