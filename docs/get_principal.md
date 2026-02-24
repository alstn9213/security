# Principal 

**Principal(프린시펄)**은 보안 용어로 **"접근 주체"**를 의미합니다. 쉽게 말해, **"현재 로그인을 시도하거나, 로그인에 성공한 사용자 그 자체"**를 가리키는 객체입니다.

- Authentication (인증 객체): 검문소를 통과할 때 제출하는 **"신분증 전체"**입니다.
- Principal (주체): 신분증에 적힌 "신분증 주인(사람)" 정보입니다. (예: 이름, ID, 생년월일 등)
- Credentials (자격 증명): 본인임을 증명하는 **"비밀번호"**입니다.
- Authorities (권한): 이 사람이 갈 수 있는 **"출입 가능 구역"**입니다. (예: 관리자실, 일반실)

## 인증 객체 생성(Authentication)

JwtTokenProvider 클래스에서 getAuthentication(String token) 메서드는 토큰에서 추출한 사용자 정보(UserDetails)를 기반으로 Spring Security가 인식하는 인증 객체(Authentication)를 생성하며, 이때 첫 번째 파라미터로 넘겨주는 값이 바로 Principal이 됩니다.

```java
// JWT 토큰에서 인증 정보 조회
public Authentication getAuthentication(String token) {
    // 1. 토큰에서 userId(PK)를 추출하여 DB에서 사용자 정보를 가져옴 (UserDetails) -> 이것이 Principal
    UserDetails userDetails = userDetailsService.loadUserByUsername(this.getUserPk(token));
    
    // 2. 가져온 userDetails를 Principal(주체)로 설정하여 Authentication 객체 생성
    return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
}

```

### 동작과정

1. this.getUserPk(token): 토큰의 Payload(Claims)에 저장된 Subject(여기서는 userId)를 꺼냅니다.
2. userDetailsService.loadUserByUsername(...): 위에서 꺼낸 userId로 DB 등을 조회하여 UserDetails 객체를 만듭니다. 이 객체 안에는 userId(username), password, roles 등이 들어있습니다.
3. new UsernamePasswordAuthenticationToken(userDetails, ...): 이 생성자의 첫 번째 인자가 바로 Principal입니다.
여기서 userDetails 객체 통째로 Principal 자리에 들어갑니다.

Authentication 인터페이스의 getName() 메서드는 내부적으로 Principal이 UserDetails 타입일 경우, 
UserDetails.getUsername()을 호출하여 반환합니다.

## Spring Security 내부 구조

Spring Security에서 Authentication 객체는 다음과 같은 구조를 가집니다.

```java

public interface Authentication extends Principal, Serializable {
    Object getPrincipal(); // 주체 (User 객체 또는 ID)
    Object getCredentials(); // 비밀번호
    Collection<? extends GrantedAuthority> getAuthorities(); // 권한 목록
    // ...
}

```
여기서 getPrincipal()의 반환 타입이 Object인 이유는, 개발자가 원하는 대로 **문자열(ID)**만 넣을 수도 있고, **사용자 객체(User DTO)**를 통째로 넣을 수도 있기 때문입니다.

## userPk
userPk는 User Primary Key의 줄임말로, 데이터베이스에서 사용자를 구분하는 **고유 식별자(ID)**를 의미합니다.
```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private String id;
```
여기서 id 필드의 값이 바로 userPk입니다.

userPk는 **JWT 토큰의 주인(Subject)**을 설정하는 데 사용됩니다.
```java
// JwtTokenProvider.java

public String createAccessToken(String userPk, List<String> roles) {
    // JWT의 Payload(내용)에 'sub' (Subject)라는 이름으로 userPk를 저장합니다.
    Claims claims = Jwts.claims().setSubject(userPk); 
}
