# 토큰 (Token)

- 토큰은 **"위조 불가능한 디지털 출입증(또는 신분증)"**입니다. 그렇기에 토큰은 단순한 데이터 형식과는 다릅니다.

1. 데이터 형식 vs 토큰

- 데이터 형식 (Data Format): JSON, XML 등이 여기에 해당합니다. 단순히 정보를 담는 그릇입니다. 누구나 쓰고 읽고 수정할 수 있습니다.

- 토큰 (Token): 데이터 형식(주로 JSON)을 사용하지만, 그 안에 **"서명(Signature)"**이 포함되어 있어 서버가 발급했다는 것을 증명하는 수단입니다.

2. 왜 "출입증"인가요? (JWT 구조로 보는 차이점)

- 토큰은 크게 세 부분으로 나뉩니다

- Header (헤더): "나는 JWT이고, HS256 알고리즘을 썼어"라는 명찰.

- Payload (내용 - Claims): 여기에 데이터가 들어갑니다. 예: sub: user123 (사용자 ID), roles: ADMIN (권한)
여기까지만 보면 일반적인 데이터를 주고받는 형식과 같습니다.

- Signature (서명) - 핵심: 위의 내용(Header + Payload)과 서버만 아는 **비밀키(Secret Key)**를 조합해서 만든 암호화된 값입니다. 이 서명 때문에 토큰은 단순 데이터가 아닙니다.

## 토큰의 타입 (Token Types)

- **JWT (JSON Web Token)**
  - **형식**: JSON
  - **특징**: 정보를 자체적으로 포함(Self-contained)하며 서명되어 있음. Stateless(무상태) 인증 가능.
  - **용도**: 일반적인 최신 웹/앱 인증.

- **Opaque Token (불투명 토큰 / Reference Token)**
  - **형식**: 무작위 문자열 (예: `a1b2-c3d4...`)
  - **특징**: 토큰 자체에 정보가 없음. 서버가 DB나 메모리(Redis 등)를 조회하여 검증해야 함(Stateful).
  - **장점**: 서버가 토큰을 즉시 무효화(로그아웃, 차단)하기 쉬움.
  - **단점**: 매 요청마다 DB 조회 부하 발생. (예: 세션 ID)

- **SAML (Security Assertion Markup Language)**
  - **형식**: XML
  - **특징**: 역사가 깊고 기능이 강력하지만 무겁고 복잡함.
  - **용도**: 기업형 레거시 시스템, 엔터프라이즈 SSO.

- **PASETO (Platform-Agnostic SEcurity TOkens)**
  - **형식**: JSON
  - **특징**: JWT의 보안 취약점(alg 헤더 조작 등)을 개선하기 위해 암호화 알고리즘을 강제한 최신 표준. "더 안전한 JWT".

## JWT

- 일반적인 최신 웹/앱 인증으로 쓰이는 JWT에 대해 자세히 알아보겠습니다.
- JWT(JSON Web Token)는 .(점)으로 구분된 세 부분으로 구성되어 있습니다.

1. **Header (헤더)**

- 토큰의 타입(JWT)과 해싱 알고리즘(예: HS256) 정보를 담습니다.

  **해싱 알고리즘 (Hashing Algorithms)**

  - JWT의 Signature(서명)를 생성할 때 사용하는 알고리즘입니다.

  1. **HMAC 계열 (대칭키 방식)**
    - **종류**: **HS256**, HS384, HS512
      - **차이점 (숫자의 의미)**: 해시값의 **비트 수(Bit Length)**를 나타냅니다.
        - **256**: 현재 업계 표준으로, 보안과 성능(속도, 토큰 길이)의 균형이 가장 좋습니다.
        - **512**: 256보다 보안 강도가 높지만, 연산 비용이 더 들고 토큰 길이가 길어집니다.
        - *참고: 사용하는 알고리즘의 비트 수에 맞춰 Secret Key의 길이도 충분히 길어야 합니다.*
    - **특징**: 하나의 **비밀키(Secret Key)**로 토큰을 생성(서명)하고 검증함.
    - **장점**: 빠르고 구현이 간단함.
    - **용도**: 발급 서버와 검증 서버가 동일한 경우.

  2. **RSA 계열 (비대칭키 방식)**
    - **종류**: **RS256**, RS384, RS512
    - **특징**: **개인키(Private Key)**로 서명하고, **공개키(Public Key)**로 검증함.
    - **장점**: 비밀키를 공유할 필요가 없어 보안 관리가 용이함.
    - **용도**: 인증 서버(Auth Server)와 리소스 서버(API Server)가 분리된 대규모 환경.

  3. **ECDSA 계열 (타원 곡선 방식)**
    - **종류**: ES256, ES384
    - **특징**: RSA와 같은 비대칭키 방식이나, 더 짧은 키 길이로 동일한 보안 강도를 제공하며 성능이 우수함.

2. **Payload (내용)**

- 실질적인 데이터인 **Claims(클레임)**가 들어가는 곳입니다.
- Claims는 JWT(JSON Web Token)의 구조 중 **Payload(내용)**에 담기는 정보의 단위입니다. 쉽게 말해, **토큰 안에 저장되는 데이터(Key-Value 쌍)**를 관리하는 객체입니다.

```java
private static final String AUTHORITIES_KEY = "roles";
    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private final long tokenValidTime = 30 * 60 * 1000L; // 30분
    private final long refreshTokenValidTime = 14 * 24 * 60 * 60 * 1000L; // 14일
    private final UserDetailsService userDetailsService;

    // Access Token 생성
    public String createAccessToken(String userPk, List<String> roles) {
        Claims claims = Jwts.claims().setSubject(userPk);
        claims.put(AUTHORITIES_KEY, roles);
        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + tokenValidTime))
                .signWith(key)
                .compact();
    }
```

- Registered Claims (등록된 클레임): 표준 스펙에 정의된 정보
- sub (Subject): 사용자 식별자 (userPk)
- iat (Issued At): 발행 시간
- exp (Expiration Time): 만료 시간
- Private Claims (비공개 클레임): 개발자가 임의로 정의한 정보
- roles: 사용자 권한 목록 (AUTHORITIES_KEY)


- **Jwts.claims().setSubject()**: JWT 표준 스펙인 sub (Subject) 클레임에 **사용자 식별자(ID)**를 저장합니다. "이 토큰은 userPk라는 사용자의 것입니다"라고 명찰을 다는 것과 같습니다.

- **claims.put()**: 표준 스펙 외에 개발자가 필요한 정보를 추가로 담을 때 사용합니다 (Custom Claim). 여기서는 AUTHORITIES_KEY("roles")라는 키로 사용자의 권한 목록(roles)을 저장하고 있습니다. 즉, 토큰 안에 사용자의 권한 정보(예: ROLE_USER, ROLE_ADMIN)를 함께 밀봉합니다.

이렇게 Claims에 정보를 담아 토큰을 만들면, 나중에 클라이언트가 토큰을 보내왔을 때 서버는 데이터베이스를 다시 조회하지 않고도 토큰만 해석(Parsing)하여 "아, 이 사용자는 ID가 뭐고, 관리자 권한이 있구나"라는 것을 바로 알 수 있게 됩니다.


3. **Signature (서명)**
토큰이 변조되지 않았음을 증명하는 부분입니다. Header와 Payload를 Base64로 인코딩한 값과, 서버만 알고 있는 Secret Key를 조합하여 해싱합니다. 만약 누군가 Payload의 내용을 조작하더라도(예: 권한을 일반 유저에서 관리자로 변경), 서버가 가진 key로 생성한 서명과 일치하지 않게 되므로 서버는 이 토큰을 거부하게 됩니다.

## 그 외 Jwt와 관련된 개념들

### JWS (JSON Web Signature)

JWT의 구현 방식 중 하나로, "서명(Signature)"이 포함된 토큰을 말합니다. 우리가 흔히 "JWT"라고 부르는 것은 기술적으로 대부분 JWS입니다.

JWT는 크게 두 가지 형태로 나뉩니다.

JWS: 내용이 보이지만, 서명이 있어 위변조가 불가능한 것 (대부분 이거 씀).
JWE (Encryption): 내용을 암호화해서 안 보이게 만든 것.

```java
// Jws<Claims>는 "서명이 확인된(Verified) 클레임 덩어리"를 의미합니다.
Jws<Claims> claims = Jwts.parserBuilder() ... .parseClaimsJws(jwtToken);
```

### Jwts

라이브러리(jjwt)에서 제공하는 **도구(Helper Class)**입니다.
이것은 JWT 표준 용어가 아니라, Java 라이브러리(io.jsonwebtoken)의 클래스 이름입니다. 토큰을 **생성(Build)**하거나 **해석(Parse)**하는 도구를 모아놓은 공장(Factory) 역할을 합니다.