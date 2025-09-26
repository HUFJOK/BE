package com.likelion.hufjok.service;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String target, Object id) {
        super(target + " not found: " + id);
    }
}
