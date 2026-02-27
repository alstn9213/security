# Validation (@Valid)

Spring Boot에서는 `spring-boot-starter-validation` 라이브러리와 `@Valid` 어노테이션을 사용하여 입력값 검증(Validation)을 매우 효율적으로 처리할 수 있습니다.
기존에 `if` 문으로 하나하나 검사하던 로직을 어노테이션 하나로 대체하여 코드의 가독성과 유지보수성을 높여줍니다.

## 1. 의존성 추가 (build.gradle)

Spring Boot 2.3 버전 이상부터는 `spring-boot-starter-web`에 validation이 기본 포함되지 않으므로 별도로 추가해야 합니다.

```groovy
implementation 'org.springframework.boot:spring-boot-starter-validation'
```

## 2. 주요 어노테이션 (DTO)

DTO 클래스의 필드에 제약 조건을 설정합니다.

| 어노테이션 | 설명 | 비고 |
| :--- | :--- | :--- |
| **@NotNull** | `null`만 허용하지 않음. | `""`이나 `" "`은 허용 |
| **@NotEmpty** | `null`과 `""`(빈 문자열)을 허용하지 않음. | `" "` (공백)은 허용 |
| **@NotBlank** | `null`, `""`, `" "` (공백) 모두 허용하지 않음. | **가장 엄격함 (String에 추천)** |
| **@Email** | 이메일 형식이 맞는지 검사. | |
| **@Pattern** | 정규표현식(Regex)으로 검사. | 예: 비밀번호 복잡도 검사 |
| **@Size** | 문자열이나 컬렉션의 길이 제한. | `@Size(min=2, max=10)` |
| **@Min / @Max** | 숫자의 최소/최대값 제한. | |

### 사용 예시 (JoinRequest.java)

```java
public class JoinRequest {
    @NotBlank(message = "아이디는 필수 입력 값입니다.")
    private String id;

    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$", message = "비밀번호는 영문, 숫자 포함 8자 이상이어야 합니다.")
    private String password;

    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;
}
```

## 3. 컨트롤러 적용 (@Valid)

컨트롤러 메서드의 파라미터 앞에 `@Valid`를 붙여야 DTO의 제약 조건이 동작합니다.

### 3.1. BindingResult 사용 (직접 처리)

검증 오류가 발생하면 `BindingResult` 객체에 에러 정보가 담깁니다. 이를 통해 유연하게 에러 처리를 할 수 있습니다.

```java
@PostMapping("/join")
public ResponseEntity<String> join(@RequestBody @Valid JoinRequest request, BindingResult bindingResult) {
    // 유효성 검사 실패 시
    if (bindingResult.hasErrors()) {
        // 첫 번째 에러 메시지 반환
        String errorMessage = bindingResult.getFieldError().getDefaultMessage();
        return ResponseEntity.badRequest().body(errorMessage);
    }
    
    memberService.join(request);
    return ResponseEntity.ok("가입 성공");
}
```
*주의: `BindingResult`는 반드시 `@Valid`가 붙은 파라미터 **바로 뒤**에 위치해야 합니다.*

## 4. @Valid vs @Validated

- **@Valid**: 자바 표준 스펙(JSR-303)입니다. 기본적으로 이것을 사용하면 됩니다.
- **@Validated**: 스프링 전용 어노테이션입니다. **Validation Group** 기능을 사용하여 상황(등록, 수정)에 따라 다른 검증 로직을 적용할 때 사용합니다.