package com.server.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JoinRequest {
    @NotBlank(message = "아이디를 입력해주세요.")
    private String id;
    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String password;
    @NotBlank(message = "이름을 입력해주세요.")
    private String name;
    private boolean admin;
    private String adminToken = "";
}