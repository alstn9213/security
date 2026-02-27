package com.server.controller;

import com.server.dto.JoinRequest;
import com.server.dto.LoginRequest;
import com.server.dto.MemberUpdateRequest;
import com.server.dto.TokenRequest;
import com.server.service.MemberService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // 회원가입 메서드
    @PostMapping("/join")
    public ResponseEntity<String> join(@RequestBody @Valid JoinRequest joinRequest, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getFieldError().getDefaultMessage());
        }
        memberService.join(joinRequest);
        return ResponseEntity.ok("회원가입 완료");
    }

    // 로그인 메서드
    @PostMapping("/login")
    public Map<String, String> login(@RequestBody LoginRequest loginRequest) {
        return memberService.login(loginRequest);
    }

    // 토큰 재발급 메서드
    @PostMapping("/reissue")
    public Map<String, String> reissue(@RequestBody TokenRequest tokenRequest) {
        return memberService.reissue(tokenRequest);
    }

    // 회원 정보 수정 메서드
    @PutMapping("/member/{id}")
    public String update(@PathVariable String id, @RequestBody MemberUpdateRequest request) {
        memberService.update(id, request);
        return "회원 정보 수정 완료";
    }

    // 회원 탈퇴 메서드
    @DeleteMapping("/member/{id}")
    public String delete(@PathVariable String id) {
        memberService.delete(id);
        return "회원 탈퇴 완료";
    }
}
