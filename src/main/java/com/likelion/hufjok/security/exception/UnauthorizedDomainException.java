package com.likelion.hufjok.security.exception;

import org.springframework.security.core.AuthenticationException;

public class UnauthorizedDomainException extends AuthenticationException {
    public UnauthorizedDomainException(String s) {
        super(s);
    }
}
