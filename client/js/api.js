const API_BASE_URL = 'http://localhost:8080';

// Axios 인스턴스 생성
const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
    withCredentials: true // CORS 쿠키 전송 필요 시
});

// [요청 인터셉터] 모든 요청 전에 실행
api.interceptors.request.use(
    (config) => {
        // 로컬 스토리지에서 토큰 가져오기
        const token = localStorage.getItem('accessToken');
        
        // 토큰이 있으면 헤더에 포함 (Bearer 스키마)
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// [응답 인터셉터] 응답 받은 후 실행
api.interceptors.response.use(
    (response) => response,
    (error) => {
        console.error('API Error:', error.response ? error.response.data : error.message);
        // 401 에러(인증 실패) 시 로그인 페이지로 리다이렉트하거나 로그아웃 처리하는 로직을 추가할 수 있습니다.
        return Promise.reject(error);
    }
);