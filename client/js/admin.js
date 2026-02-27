document.addEventListener('DOMContentLoaded', () => {
    const adminApiBtn = document.getElementById('adminApiBtn');
    const apiResult = document.getElementById('apiResult');

    adminApiBtn.addEventListener('click', async () => {
        try {
            // api.js의 인터셉터가 자동으로 헤더에 토큰을 넣어줍니다.
            const response = await api.get('/admin/dashboard');
            
            apiResult.textContent = JSON.stringify(response.data, null, 2);
            apiResult.style.color = 'black';
        } catch (error) {
            // 403 Forbidden 등 에러 처리
            apiResult.textContent = '접근 거부: ' + (error.response?.statusText || error.message);
            apiResult.style.color = 'red';
        }
    });
});