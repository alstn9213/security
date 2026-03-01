document.addEventListener('DOMContentLoaded', () => {
    const joinBtn = document.getElementById('joinBtn');
    const loginBtn = document.getElementById('loginBtn');
    const logoutBtn = document.getElementById('logoutBtn');
    const isAdminCheckbox = document.getElementById('isAdmin');
    const adminTokenInput = document.getElementById('adminToken');

    // 네비게이션 바 UI 처리 (로그인 상태에 따라 버튼 표시)
    const updateNavbar = () => {
        const token = localStorage.getItem('accessToken');
        const userName = localStorage.getItem('userName');
        const navLogin = document.getElementById('nav-login');
        const navJoin = document.getElementById('nav-join');
        const navLogout = document.getElementById('nav-logout');
        let nameSpan = document.getElementById('nav-user-name');

        if (token) {
            if (navLogin) navLogin.style.display = 'none';
            if (navJoin) navJoin.style.display = 'none';
            if (navLogout) {
                navLogout.style.display = 'inline-block';
                if (userName) {
                    if (!nameSpan) {
                        nameSpan = document.createElement('span');
                        nameSpan.id = 'nav-user-name';
                        nameSpan.style.marginRight = '10px';
                        nameSpan.style.fontWeight = 'bold';
                        navLogout.parentNode.insertBefore(nameSpan, navLogout);
                    }
                    nameSpan.textContent = `${userName}님`;
                }
            }
        } else {
            if (navLogin) navLogin.style.display = 'inline-block';
            if (navJoin) navJoin.style.display = 'inline-block';
            if (navLogout) navLogout.style.display = 'none';
            if (nameSpan) nameSpan.remove();
        }
    };
    updateNavbar();

    // 관리자 체크박스 토글 UI 처리
    if (isAdminCheckbox && adminTokenInput) {
        isAdminCheckbox.addEventListener('change', (e) => {
            adminTokenInput.style.display = e.target.checked ? 'block' : 'none';
        });
    }

    // 1. 회원가입
    if (joinBtn) {
        joinBtn.addEventListener('click', async () => {
            const id = document.getElementById('joinId').value;
            const pw = document.getElementById('joinPw').value;
            const name = document.getElementById('joinName').value;
            const isAdmin = isAdminCheckbox ? isAdminCheckbox.checked : false;
            const adminToken = document.getElementById('adminToken') ? document.getElementById('adminToken').value : '';

            const data = {
                id: id,
                password: pw,
                name: name,
                admin: isAdmin,
                adminToken: isAdmin ? adminToken : null
            };

            try {
                // api.js에서 만든 axios 인스턴스 사용
                await api.post('/join', data);
                alert('회원가입 성공! 로그인해주세요.');
                window.location.href = 'login.html'; // 페이지 이동
            } catch (error) {
                alert('회원가입 실패: ' + (error.response?.data?.message || error.message));
            }
        });
    }

    // 2. 로그인
    if (loginBtn) {
        loginBtn.addEventListener('click', async () => {
            const id = document.getElementById('loginId').value;
            const pw = document.getElementById('loginPw').value;

            try {
                const response = await api.post('/login', { id: id, password: pw });
                
                // 백엔드에서 헤더나 바디로 토큰을 줍니다. (여기서는 Authorization 헤더)
                // 1. 헤더 확인
                const authHeader = response.headers['Authorization'];
                let accessToken = authHeader && authHeader.startsWith('Bearer ') 
                                    ? authHeader.substring(7) : authHeader;

                // 2. 바디 확인 (헤더에 없으면 바디의 accessToken 필드 확인)
                if (!accessToken && response.data && response.data.accessToken) {
                    accessToken = response.data.accessToken;
                }

                if (accessToken) {
                    localStorage.setItem('accessToken', accessToken);
                    if (response.data.name) {
                        localStorage.setItem('userName', response.data.name);
                    }
                    alert('로그인 성공');
                    window.location.href = 'index.html'; // 메인 페이지로 이동
                } else {
                    throw new Error('토큰을 받지 못했습니다.');
                }
            } catch (error) {
                console.error('로그인 에러:', error); // 콘솔에 에러 상세 내용 출력
                alert('로그인 실패: ' + (error.message || '아이디/비번 확인 필요'));
            }
        });
    }

    // 3. 로그아웃
    if (logoutBtn) {
        logoutBtn.addEventListener('click', (e) => {
            e.preventDefault(); // a 태그 클릭 시 이동 방지
            localStorage.removeItem('accessToken');
            localStorage.removeItem('userName');
            alert('로그아웃 되었습니다.');
            window.location.href = 'index.html';
            updateNavbar();
        });
    }
});