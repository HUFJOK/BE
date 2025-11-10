package com.likelion.hufjok.controller;

import com.likelion.hufjok.service.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.LinkedHashMap;
import java.util.Map;


@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException e) {
        var first = e.getBindingResult().getFieldErrors().stream().findFirst();
        String msg = first.map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .orElse("Validation error");
        return ResponseEntity.badRequest().body(Map.of("error", msg));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<?> handleNotFound(NotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<String> handleMax(MaxUploadSizeExceededException ex) {
        System.err.println("[MAX] ex.getMaxUploadSize() = " + ex.getMaxUploadSize());
        return ResponseEntity.status(413).body("업로드 가능한 최대 파일 크기를 초과했습니다.");
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<?> handleMissingPart(MissingServletRequestPartException e, HttpServletRequest request) {

        System.err.println("[ERROR] Missing Request Part: " + e.getRequestPartName());
        e.printStackTrace();

        // 401 에러 대신 정확한 400 Bad Request를 반환합니다.
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", "필수 요청 필드가 누락되었습니다.",
                "part_name", e.getRequestPartName()
        ));
    }

}
