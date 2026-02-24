# ThreadLocal

- 자바의 ThreadLocal은 스레드(Thread) 하나만을 위한 전용 저장소입니다.
- Thread: 웹 서버(Tomcat 등)는 동시에 여러 요청을 처리하기 위해 수십, 수백 개의 스레드를 운영합니다.
- ThreadLocal: 각 스레드마다 별도로 생성되는 변수 공간입니다.
- 예시: A 스레드가 자신의 ThreadLocal에 값을 저장하면, B 스레드는 그 값을 절대 볼 수 없습니다.
A 스레드가 실행되는 동안에는, A 스레드가 어디(Controller, Service, Repository)에 있든 자신의 ThreadLocal에 접근해서 값을 꺼낼 수 있습니다.

## SecurityContextHolder의 동작 흐름

**Step 1: 요청 수신 및 스레드 할당**
사용자가 HTTP 요청을 보내면, 서버(Tomcat)는 스레드 풀(Thread Pool)에서 놀고 있는 스레드-1을 하나 배정하여 작업을 시작합니다.

**Step 2: 인증 필터 실행 (저장 단계)**
스레드-1이 JwtAuthenticationFilter의 doFilter 메서드를 실행합니다.


```java
// JwtAuthenticationFilter.java

if (token != null && jwtTokenProvider.validateToken(token)) {
    Authentication authentication = jwtTokenProvider.getAuthentication(token);
    
    // 1. 빈 Context 생성
    SecurityContext context = SecurityContextHolder.createEmptyContext();
    // 2. 인증 객체 담기
    context.setAuthentication(authentication);
    
    // 3. [핵심] 현재 스레드(스레드-1)의 ThreadLocal에 저장
    SecurityContextHolder.setContext(context); 
}

```

**Step 3: 비즈니스 로직 수행 (사용 단계)**

- 요청이 Controller나 Service로 넘어갑니다. 여전히 스레드-1이 코드를 실행 중입니다. 개발자가 현재 로그인한 유저를 알고 싶어서 다음과 같이 호출합니다.

```java
// 어디선가 실행되는 코드
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
```

- SecurityContextHolder는 현재 코드를 실행 중인 스레드가 스레드-1임을 확인합니다. 스레드-1의 ThreadLocal을 뒤져서 아까 저장해둔 Authentication 객체를 꺼내줍니다. 이 덕분에 메서드 파라미터로 유저 정보를 계속 넘겨주지 않아도, 어디서든 전역 변수처럼 접근이 가능합니다.

**Step 4: 요청 종료 및 정리 (초기화 단계)**

- 응답이 클라이언트에게 전송되면 스레드-1의 할 일이 끝납니다. 스레드-1은 다시 스레드 풀로 돌아가서 다음 요청을 기다리게 됩니다. (재사용됨)

- 이때 ThreadLocal을 비우지 않으면, 다음에 스레드-1을 할당받은 다른 사용자가 이전 사용자의 정보를 보게 되는 심각한 보안 문제가 발생할 수 있습니다. 스프링 시큐리티는 필터 체인의 가장 바깥쪽(주로 SecurityContextHolderFilter 등)에서 finally 블록을 통해 SecurityContextHolder.clearContext()를 호출하여 스레드-1를 깨끗이 비워줍니다.