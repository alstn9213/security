package com.server.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JoinRequest {
    private String id;
    private String password;
    private String name;
    private boolean admin;
    private String adminToken = "";
}