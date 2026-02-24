# JpaConfig

JpaConfig.java 파일은 JPA Auditing(감사) 기능을 활성화하기 위해 존재하는 설정 파일입니다.

```java
@Configuration
@EnableJpaAuditing
public class JpaConfig {
    // 이 메서드는 JPA가 데이터를 저장하거나 수정할 때 호출됩니다.
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getName);
    }
}
```
- 핵심 역할: 자동 시간 기록 활성화
이 파일에 있는 @EnableJpaAuditing 어노테이션이 핵심입니다. 이 설정이 있어야 BaseTimeEntity 클래스에 정의된 @CreatedDate와 @LastModifiedDate가 실제로 동작하게 됩니다.

```java

@EntityListeners(AuditingEntityListener.class)
public abstract class BaseTimeEntity {
    @CreatedDate
    private LocalDateTime createdAt; // 생성 시간 자동 저장

    @LastModifiedDate
    private LocalDateTime updatedAt; // 수정 시간 자동 저장
}

```
이 설정 파일이 없으면, 데이터를 저장(save)하거나 수정할 때 위 필드(createdAt, updatedAt)에 값이 자동으로 들어가지 않고 null로 남게 됩니다.

## 왜 별도의 파일로 분리했나요?
보통 메인 애플리케이션 클래스(@SpringBootApplication이 붙은 곳)에 @EnableJpaAuditing을 붙여도 동작은 합니다. 하지만 실무에서는 이렇게 별도의 설정 파일(JpaConfig)로 분리하는 것을 권장합니다.

테스트 격리 때문: @WebMvcTest와 같은 슬라이스 테스트(Controller만 테스트하는 경우)를 진행할 때, 메인 클래스에 이 어노테이션이 붙어 있으면 JPA 관련 빈(Bean)들을 찾으려고 시도하다가 에러가 발생할 수 있습니다.
이렇게 설정을 분리해두면, JPA가 필요 없는 테스트 환경에서는 이 설정을 로드하지 않을 수 있어 테스트 관리가 용이해집니다.


```