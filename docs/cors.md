# CORS(Cross-Origin Resource Sharing, 교차 출처 리소스 공유)

- CORS는 웹 브라우저에서 실행되는 스크립트가 다른 출처(Origin)의 리소스에 접근할 수 있도록 허용하는 보안 메커니즘입니다.
- 여기서 **출처(Origin)**란 프로토콜(Protocol), 호스트(Host), 포트(Port)의 조합을 말합니다. 예를 들어, http://localhost:3000(프론트엔드)에서 http://localhost:8080(백엔드)으로 API 요청을 보내면, 포트 번호가 다르기 때문에 **다른 출처(Cross-Origin)**로 간주됩니다.
- 보안상의 이유로 브라우저는 기본적으로 다른 출처로의 요청을 차단(Same-Origin Policy)하지만, 서버에서 CORS 설정을 통해 "이 출처는 안전하니 허용해줘"라고 브라우저에 알려주면 통신이 가능해집니다.

- 개발단계에서는 편의성을 위해 모든 출처를 허용합니다.
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    
    // 모든 출처(Origin) 허용 (* 패턴)
    configuration.setAllowedOriginPatterns(List.of("*"));
    // 모든 HTTP 메서드 허용 (GET, POST, PUT, DELETE 등)
    configuration.setAllowedMethods(List.of("*"));
    // 모든 헤더 허용
    configuration.setAllowedHeaders(List.of("*"));
    // 자격 증명(쿠키, Authorization 헤더 등) 허용
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}

```

- 실무에서는 허용하는 출처를 명시합니다.
```java

public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    
    // 실무 설정: 프론트엔드 도메인(로컬, 운영 서버)만 명시적으로 허용
    configuration.setAllowedOriginPatterns(List.of("http://localhost:3000", "https://www.mydomain.com"));
    // 허용할 HTTP 메서드 지정 (불필요한 메서드 차단)
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
    // 모든 헤더 허용
    configuration.setAllowedHeaders(List.of("*"));
    // 자격 증명(쿠키 등) 허용
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```