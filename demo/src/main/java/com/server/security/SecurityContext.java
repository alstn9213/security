package com.server.security;

import java.io.Serializable;

import org.springframework.security.core.Authentication;

public interface SecurityContext extends Serializable {
    Authentication getAuthentication();
    void setAuthentication(Authentication authentication);
}