# CSRF (Cross-Site Request Forgery)

- 사이트 간 요청 위조를 의미하는 보안용어.

- CSRF는 신뢰할 수 있는 사용자의 권한을 도용하여, 사용자의 의지와 무관하게 공격자가 의도한 행위(정보 수정, 결제, 계정 삭제 등)를 웹사이트에 요청하게 만드는 공격 방식입니다.

공격 원리:
1. 사용자가 웹사이트(예: 은행)에 로그인하여 브라우저에 세션 쿠키가 저장됩니다.
2. 사용자가 해커가 만든 악성 사이트나 이메일 링크를 클릭합니다.
3. 악성 사이트의 스크립트가 사용자의 브라우저를 통해 은행 사이트로 요청(예: 송금)을 보냅니다.
4. 브라우저는 은행 사이트의 쿠키를 자동으로 포함하여 요청을 보내므로, 서버는 이를 정상적인 사용자의 요청으로 착각하고 처리하게 됩니다.


- JWT(JSON Web Token) 를 사용하는 REST API 구조는 CSRF 보호가 필요하지 않습니다. 

```java
 @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // JWT를 사용하는 Stateless 환경이므로 CSRF 보호 비활성화
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 사용 안함
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/index.html", "/login", "/join", "/reissue").permitAll() // 로그인, 회원가입, 재발급 경로는 누구나 접근 가능
                .requestMatchers("/admin/**").hasRole("ADMIN") // 관리자만 접근 가능
                .anyRequest().authenticated() // 그 외 모든 요청은 인증 필요
            )
            .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtExceptionFilter, JwtAuthenticationFilter.class);
        
        return http.build();
    }
```

- 이유
1. Stateless (무상태) 세션 정책: SessionCreationPolicy.STATELESS를 사용하여 서버에 세션을 저장하지 않습니다.
2. 헤더 기반 인증: JwtTokenProvider.resolveToken 메서드를 보면 쿠키가 아닌 HTTP 헤더(Authorization) 에 담긴 토큰을 사용하여 인증합니다.
3. 자동 전송 방지: 쿠키와 달리, 헤더에 담긴 토큰은 브라우저가 자동으로 요청에 포함시키지 않습니다. 따라서 해커가 악성 사이트에서 요청을 위조하려 해도 인증 토큰을 실어 보낼 수 없으므로 공격이 성립하지 않습니다.