# ObjectMapper

- 자바 객체(Object)와 JSON 데이터 간의 변환기

- 서버 간 통신은 대부분 JSON 형식(문자열)으로 이루어지지만, 자바 코드는 **객체(Class)** 로 데이터를 다룹니다. 이 둘 사이를 통역해 주는 것이 ObjectMapper(Jackson 라이브러리)입니다.

- 직렬화 (Serialization): 자바 객체 → JSON (요청 보낼 때)
- 역직렬화 (Deserialization): JSON → 자바 객체 (응답 받을 때)

```java
// response.getBody()는 JSON 형태의 긴 문자열입니다.
// 예: {"response": {"amount": 1000, "status": "paid", ...}, ...}

JsonNode root = objectMapper.readTree(response.getBody()); // 문자열을 트리 구조(Node)로 변환
JsonNode responseNode = root.path("response"); // "response" 안쪽 데이터로 진입

// 필요한 데이터만 쏙쏙 뽑아서 자바 객체(IamportPaymentInfo)로 만듦
return new IamportPaymentInfo(
        responseNode.path("amount").asLong(),
        responseNode.path("status").asText()
);

```

- 위 코드에서 ObjectMapper는 알아보기 힘든 JSON 문자열을 자바가 이해할 수 있는 구조로 바꿔주는 "통역사" 역할을 합니다.

- 스프링 부트는 ObjectMapper를 기본적으로 빈으로 제공하기 때문에 `private final ObjectMapper objectMapper = new ObjectMapper();` 처럼 생성자를 사용하지않고 `private final ObjectMapper objectMapper;` 만으로  objectMapper의 객체 생성이 가능합니다.
