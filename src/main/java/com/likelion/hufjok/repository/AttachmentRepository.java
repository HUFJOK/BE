package com.likelion.hufjok.repository;

import com.likelion.hufjok.domain.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
}