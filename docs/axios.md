# axios vs fetch

실무에서는 Axios를 압도적으로 많이 사용합니다.
일반적인 리액트(React), 뷰(Vue) 기반의 기업형 프로젝트(SI, 서비스 기업 등)에서는 여전히 Axios가 표준처럼 쓰입니다.

 1. 인터셉터 (Interceptors) 기능

- fetch 사용 시: 요청 전에 헤더에 토큰을 넣는 코드, 응답 후에 401 에러가 나면 토큰을 재발급받고 재요청하는 코드 등의 로직을 authFetch라는 함수 안에 직접 구현해야합니다. 매번 API를 호출할 때마다 이 함수를 써야 하고, 로직이 복잡해지면 관리가 힘듭니다.

- Axios 사용 시: Axios는 Interceptors라는 기능을 기본 제공합니다. "요청하기 직전"과 "응답받은 직후"에 실행할 코드를 전역적으로 설정할 수 있습니다.

```js
// Axios 설정 예시
const api = axios.create({ baseURL: 'http://localhost:8080' });

// 요청 인터셉터: 모든 요청에 자동으로 토큰 추가
api.interceptors.request.use(config => {
    const token = localStorage.getItem('accessToken');
    if (token) config.headers.Authorization = `Bearer ${token}`;
    return config;
});

// 응답 인터셉터: 401 발생 시 자동 재발급 및 재요청
api.interceptors.response.use(
    response => response,
    async error => {
        if (error.response.status === 401) {
            // 토큰 재발급 로직 및 재요청 로직 (깔끔하게 분리 가능)
        }
        return Promise.reject(error);
    }
);

```
이렇게 한 번만 설정해두면, 개발자는 그냥 api.get('/admin/dashboard')만 호출하면 됩니다. 토큰 신경 쓸 필요가 없어지죠.

2. JSON 자동 변환
Fetch: 응답을 받으면 .json()을 매번 호출해서 변환해야 합니다

```js
const response = await fetch('/url');
const data = await response.json(); // 귀찮음

```
Axios: 자동으로 JSON으로 변환해서 data 속성에 넣어줍니다.

```js
const response = await axios.get('/url');
console.log(response.data); // 바로 사용 가능

```

3. 에러 처리 (Error Handling)

- Fetch: 404(Not Found)나 500(Server Error)가 발생해도 catch로 잡히지 않습니다. (네트워크 장애일 때만 잡힘). 그래서 if (!response.ok)를 매번 써줘야 합니다.

- Axios: 상태 코드가 200번대가 아니면(4xx, 5xx) 자동으로 에러를 발생시켜 catch 블록으로 넘겨줍니다. 코드가 훨씬 직관적입니다.

4. 기타 편의 기능

- 요청 취소: 사용자가 페이지를 이동했을 때 진행 중인 요청을 쉽게 취소할 수 있습니다.
- 타임아웃 설정: timeout: 3000 옵션 하나로 3초 뒤 요청 중단을 설정할 수 있습니다. (fetch는 AbortController를 써야 해서 코드가 깁니다.)
- 브라우저 호환성: 구형 브라우저(IE 등) 호환 처리가 잘 되어 있습니다.


# 결론: 언제 무엇을 쓸까?

## Axios
- 로그인, 토큰 재발급 등 인증 로직이 복잡한 프로젝트.
- 팀 프로젝트나 실무 환경 (생산성이 중요함).
- 에러 처리를 일관성 있게 하고 싶을 때.


## Fetch

- Next.js 프로젝트 (Next.js는 fetch를 확장해서 캐싱 기능을 제공하므로 fetch 사용을 권장함).
- 라이브러리 설치 없이 가볍게 만들고 싶은 토이 프로젝트.
- React Query (TanStack Query) 같은 데이터 페칭 라이브러리를 쓸 때 (이 라이브러리들이 Axios의 역할을 대신해주기도 함).