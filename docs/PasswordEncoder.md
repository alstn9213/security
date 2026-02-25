# PasswordEncoder

Spring Security에서는 비밀번호를 안전하게 저장하기 위해 `PasswordEncoder` 인터페이스를 제공합니다.
단순히 비밀번호를 평문으로 저장하거나 단순 해시(MD5, SHA-256 등)로 저장하는 것은 보안상 매우 취약하므로, **Salt(솔트)** 를 추가하고 **Key Stretching(키 스트레칭)** 을 적용하는 적절한 암호화 방식을 사용해야 합니다.

### 용어 설명

1. **Salt (솔트)**
   - **개념**: 비밀번호를 해싱하기 전에 추가하는 **무작위 문자열**입니다.
   - **목적**: **레인보우 테이블(Rainbow Table) 공격 방지**. 같은 비밀번호라도 솔트가 다르면 최종 해시값이 달라지게 만듭니다.

2. **Key Stretching (키 스트레칭)**
   - **개념**: 해싱 과정을 수천~수만 번 **반복**하는 것입니다.
   - **목적**: **브루트 포스(Brute Force) 공격 방지**. 해시 생성 시간을 의도적으로 늦춰(예: 0.2초), 해커가 빠르게 대입 공격을 하지 못하도록 막습니다.

## 1. 주요 암호화 방식 비교

| 방식 | 특징 및 장단점 | 보안 강도 | 추천 대상 |
| :--- | :--- | :--- | :--- |
| **BCrypt** | - 현재 가장 널리 쓰이는 표준.<br>- CPU 연산 부하를 주어 무차별 대입 공격을 방어.<br>- **단점**: 메모리를 적게 사용하여 GPU를 이용한 고속 크랙 공격에 상대적으로 취약할 수 있음. | ★★★★☆ | 일반적인 웹 서비스 |
| **Argon2** | - **2015년 암호 해싱 대회(PHC) 우승 알고리즘.**<br>- **메모리**와 CPU를 동시에 많이 사용하도록 설계됨.<br>- GPU/ASIC 장비를 이용한 공격을 효과적으로 방어.<br>- 파라미터(메모리 양, 반복 횟수 등) 튜닝 가능. | ★★★★★ | **최신 보안 권장**, 금융/개인정보 민감 서비스 |
| **SCrypt** | - Argon2 이전에 나온 메모리 기반 해싱 알고리즘.<br>- 하드웨어(ASIC)를 통한 공격을 어렵게 만듦.<br>- 많은 메모리를 소모하므로 서버 리소스 고려 필요. | ★★★★☆ | Argon2를 사용할 수 없는 환경 |
| **PBKDF2** | - 미국 표준 기술 연구소(NIST) 승인 알고리즘.<br>- CPU 연산만 사용하므로 GPU 공격에 매우 취약.<br>- FIPS 인증이 필요한 경우에만 사용. | ★★★☆☆ | 레거시 시스템, 규제 준수 필요 시 |

## 2. DelegatingPasswordEncoder (권장)

실무에서는 알고리즘을 하나로 고정하기보다, `DelegatingPasswordEncoder`를 사용하는 것이 좋습니다.
이 방식은 비밀번호 앞에 `{id}`(예: `{bcrypt}`, `{argon2}`)를 붙여 어떤 알고리즘으로 암호화되었는지 식별합니다.

### 장점
- **유연성**: 나중에 더 강력한 알고리즘(예: BCrypt → Argon2)으로 변경하더라도, 기존 유저(BCrypt)와 신규 유저(Argon2)가 모두 로그인할 수 있습니다.
- **마이그레이션**: 보안 표준이 바뀌었을 때 비밀번호를 초기화하지 않고 자연스럽게 업그레이드할 수 있습니다.

### 사용 예시

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
}
```

저장된 비밀번호 형태:
- `{bcrypt}$2a$10$dXJ3SW6G7f50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG`
- `{argon2}$argon2id$v=19$m=4096,t=3,p=1$....`

## 3. SecurityConfig 설정 예시 (BCrypt)

가장 대중적인 `BCryptPasswordEncoder`의 예시입니다.

```java
@Bean
public PasswordEncoder passwordEncoder() {
    // 비밀번호를 안전하게 암호화하기 위해 BCrypt 사용
    return new BCryptPasswordEncoder();
}
```

## 4. 사용 방법 (MemberController)

`PasswordEncoder`는 단방향 암호화이므로 복호화가 불가능합니다. 따라서 로그인 시 사용자가 입력한 비밀번호를 암호화하여 DB에 저장된 값과 `matches()` 메서드로 비교해야 합니다.

```java
// 회원가입: 암호화하여 저장
member.setPassword(passwordEncoder.encode(joinRequest.getPassword()));

// 로그인: 입력된 비밀번호와 저장된 암호문 비교
if (!passwordEncoder.matches(loginRequest.getPassword(), member.getPassword())) {
    throw new CustomException(ErrorCode.INVALID_PASSWORD);
}
```