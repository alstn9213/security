package com.server.controller;

import com.server.domain.Member;
import com.server.domain.RefreshToken;
import com.server.domain.Role;
import com.server.dto.JoinRequest;
import com.server.dto.LoginRequest;
import com.server.dto.MemberUpdateRequest;
import com.server.dto.TokenRequest;
import com.server.exception.CustomException;
import com.server.exception.ErrorCode;
import com.server.repository.MemberRepository;
import com.server.repository.RefreshTokenRepository;
import com.server.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    // 관리자 가입을 위한 전용 토큰 (실무에서는 application.yml 등에서 관리 권장)
    private static final String ADMIN_TOKEN = "AAABnvxRVklrnYxKZ0aHgTBcXukeZygoC";

    // 회원가입 메서드
    @PostMapping("/join")
    public String join(@RequestBody JoinRequest joinRequest) {
        Member member = new Member();
        member.setId(joinRequest.getId());
        member.setName(joinRequest.getName());
        // 비밀번호 암호화
        member.setPassword(passwordEncoder.encode(joinRequest.getPassword()));
        
        // 관리자 토큰 확인
        if (joinRequest.isAdmin()) {
            if (!joinRequest.getAdminToken().equals(ADMIN_TOKEN)) {
                throw new CustomException(ErrorCode.INVALID_ADMIN_TOKEN);
            }
            member.getRoles().add(Role.ADMIN);
        } else {
            member.getRoles().add(Role.USER);
        }
        
        memberRepository.save(member);
        return "회원가입 완료";
    }

    // 로그인 메서드
    @PostMapping("/login")
    public Map<String, String> login(@RequestBody LoginRequest loginRequest) {
        Member member = memberRepository.findById(loginRequest.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if (!passwordEncoder.matches(loginRequest.getPassword(), member.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }
        
        String accessToken = jwtTokenProvider.createAccessToken(member.getId(), member.getRoles().stream()
                .map(Role::getValue)
                .collect(Collectors.toList()));
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getId());

        //  Refresh Token은 서버의 데이터베이스에 저장
        refreshTokenRepository.save(RefreshToken.builder()
                .userId(member.getId())
                .refreshToken(refreshToken)
                .build());

        // 메시지 컨버터(Jackson)에 의해 JSON 형식으로 변환되어 요청을 보낸 클라이언트(웹 브라우저, 모바일 앱 등)에게 전송.
        return Map.of("accessToken", accessToken, "refreshToken", refreshToken);
    }

    // 토큰 재발급 메서드
    @PostMapping("/reissue")
    public Map<String, String> reissue(@RequestBody TokenRequest tokenRequest) {
        String refreshToken = tokenRequest.getRefreshToken();
        // Refresh Token 유효성 검사
        jwtTokenProvider.validateToken(refreshToken);

        String userId = jwtTokenProvider.getUserPk(refreshToken);
        RefreshToken storedToken = refreshTokenRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

        // DB에 저장된 토큰과 일치하는지 확인
        if (!storedToken.getRefreshToken().equals(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        String newAccessToken = jwtTokenProvider.createAccessToken(member.getId(), member.getRoles().stream()
                .map(Role::getValue)
                .collect(Collectors.toList()));
        String newRefreshToken = jwtTokenProvider.createRefreshToken(member.getId());
        
        // Refresh Token Rotation
        refreshTokenRepository.save(RefreshToken.builder()
                .userId(member.getId())
                .refreshToken(newRefreshToken)
                .build());

        return Map.of("accessToken", newAccessToken, "refreshToken", newRefreshToken);
    }

    // 회원 정보 수정 메서드
    @PutMapping("/member/{id}")
    public String update(@PathVariable String id, @RequestBody MemberUpdateRequest request) {
        // 1. 인증 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserId = authentication.getName();

        // 2. 본인 확인 (토큰의 ID와 요청 URL의 ID가 일치하는지 검사)
        if (!currentUserId.equals(id)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 3. 정보 수정 (비밀번호 암호화 포함)
        member.update(passwordEncoder.encode(request.getPassword()), request.getName());
        memberRepository.save(member);

        return "회원 정보 수정 완료";
    }

    // 회원 탈퇴 메서드
    @DeleteMapping("/member/{id}")
    public String delete(@PathVariable String id) {
        // 1. 인증 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserId = authentication.getName();

        // 2. 본인 확인
        if (!currentUserId.equals(id)) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // 3. Refresh Token 삭제 (존재할 경우에만
        refreshTokenRepository.findById(id).ifPresent(refreshTokenRepository::delete);

        // 4. 회원 삭제
        memberRepository.delete(member);

        return "회원 탈퇴 완료";
    }
}
