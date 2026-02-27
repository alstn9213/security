document.addEventListener('DOMContentLoaded', () => {
    const joinBtn = document.getElementById('joinBtn');
    const loginBtn = document.getElementById('loginBtn');
    const logoutBtn = document.getElementById('logoutBtn');
    const loginResult = document.getElementById('loginResult');
    const isAdminCheckbox = document.getElementById('isAdmin');
    const adminTokenInput = document.getElementById('adminToken');

    // 관리자 체크박스 토글 UI 처리
    isAdminCheckbox.addEventListener('change', (e) => {
        adminTokenInput.style.display = e.target.checked ? 'block' : 'none';
    });

    // 1. 회원가입
    joinBtn.addEventListener('click', async () => {
        const id = document.getElementById('joinId').value;
        const pw = document.getElementById('joinPw').value;
        const name = document.getElementById('joinName').value;
        const isAdmin = isAdminCheckbox.checked;
        const adminToken = document.getElementById('adminToken').value;

        const data = {
            username: id,
            password: pw,
            name: name,
            role: isAdmin ? 'ADMIN' : 'USER',
            adminToken: isAdmin ? adminToken : null
        };

        try {
            // api.js에서 만든 axios 인스턴스 사용
            await api.post('/join', data);
            alert('회원가입 성공! 로그인해주세요.');
        } catch (error) {
            alert('회원가입 실패: ' + (error.response?.data?.message || error.message));
        }
    });

    // 2. 로그인
    loginBtn.addEventListener('click', async () => {
        const id = document.getElementById('loginId').value;
        const pw = document.getElementById('loginPw').value;

        try {
            const response = await api.post('/login', { username: id, password: pw });
            
            // 백엔드에서 헤더나 바디로 토큰을 줍니다. (여기서는 Authorization 헤더 가정)
            // 보통 Bearer {token} 형태이므로 분리해서 저장하거나 그대로 저장
            const authHeader = response.headers['authorization'];
            const accessToken = authHeader && authHeader.startsWith('Bearer ') 
                                ? authHeader.substring(7) 
                                : authHeader;

            if (accessToken) {
                localStorage.setItem('accessToken', accessToken);
                loginResult.textContent = '로그인 상태: 성공 (' + id + ')';
                loginResult.style.color = 'green';
            } else {
                throw new Error('토큰을 받지 못했습니다.');
            }
        } catch (error) {
            console.error('로그인 에러:', error); // 콘솔에 에러 상세 내용 출력
            loginResult.textContent = '로그인 상태: 실패';
            loginResult.style.color = 'red';
            alert('로그인 실패: ' + (error.response?.data?.message || '아이디/비번 확인 필요'));
            alert('로그인 실패: ' + (error.response?.data?.message || error.message || '아이디/비번 확인 필요'));
        }
    });

    // 3. 로그아웃
    logoutBtn.addEventListener('click', () => {
        localStorage.removeItem('accessToken');
        loginResult.textContent = '로그인 상태: 로그아웃됨';
        loginResult.style.color = 'black';
        alert('로그아웃 되었습니다.');
    });
});