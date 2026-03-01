package com.server.config;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.server.domain.Role;
import com.server.security.JwtAuthenticationFilter;
import com.server.security.JwtExceptionFilter;
import com.server.security.JwtTokenProvider;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtExceptionFilter jwtExceptionFilter;

   
    @Bean
    public PasswordEncoder passwordEncoder() {
        // 비밀번호를 안전하게 암호화하기 위해 BCrypt 사용
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS 설정 적용
            .csrf(csrf -> csrf.disable()) // JWT를 사용하는 Stateless 환경이므로 CSRF 보호 비활성화
            .formLogin(form -> form.disable()) // 기본 로그인 페이지 비활성화
            .httpBasic(basic -> basic.disable()) // HTTP Basic 인증 비활성화
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 사용 안함
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/index.html", "/login.html", "/join.html", "/login", "/join", "/reissue", "/error", "/api/items").permitAll()
                .requestMatchers("/favicon.ico", "/css/**", "/js/**", "/images/**").permitAll() // 정적 리소스 허용
                .requestMatchers("/api/payments/webhook").permitAll() // 결제 웹훅은 PG사에서 호출하므로 인증 제외
                .requestMatchers("/admin/**").hasAuthority(Role.ADMIN.getValue()) // 관리자만 접근 가능
                .anyRequest().authenticated() // 그 외 모든 요청은 인증 필요
            )
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)) // 인증 실패 시 로그인 페이지 이동 방지 (401 에러 반환)
            )
            .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtExceptionFilter, JwtAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 실무 설정: 프론트엔드 도메인(로컬, 운영 서버)만 명시적으로 허용
        configuration.setAllowedOriginPatterns(List.of("http://localhost:3000", "https://www.mydomain.com"));
        // 허용할 HTTP 메서드 지정 (불필요한 메서드 차단)
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        // 모든 헤더 허용
        configuration.setAllowedHeaders(List.of("*"));
        // 클라이언트가 응답 헤더 중 Authorization을 읽을 수 있도록 허용
        configuration.setExposedHeaders(List.of("Authorization"));
        // 자격 증명(쿠키 등) 허용
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}