document.addEventListener('DOMContentLoaded', () => {
    window.IMP.init("imp18800351"); 

    const productList = document.getElementById('productList');

    // 1. 상품 목록 불러오기 함수
    async function loadProducts() {
        try {
            const response = await api.get('/api/items');
            const items = response.data;
            
            productList.innerHTML = ''; // 로딩 메시지 제거

            items.forEach(item => {
                const card = document.createElement('div');
                card.className = 'card';
                card.style.cssText = 'border: 1px solid #ddd; padding: 15px; border-radius: 8px; width: 200px;';
                card.innerHTML = `
                    <h4>${item.name}</h4>
                    <p>가격: ${item.price.toLocaleString()}원</p>
                    <button class="order-btn" data-id="${item.id}" data-name="${item.name}" style="width: 100%; background-color: #28a745; color: white; border: none; padding: 8px; cursor: pointer;">구매하기</button>
                `;
                productList.appendChild(card);
            });
        } catch (error) {
            productList.innerHTML = '<p style="color:red">상품 목록을 불러오지 못했습니다.</p>';
            console.error(error);
        }
    }

    // 2. 초기 로드 실행
    loadProducts();

    // 3. 이벤트 위임 (동적으로 생성된 버튼 클릭 처리)
    productList.addEventListener('click', async (e) => {
        if (!e.target.classList.contains('order-btn')) return;

        // 로그인 여부 체크
        if (!localStorage.getItem('accessToken')) {
            alert('로그인이 필요합니다.');
            return;
        }

        const itemId = e.target.getAttribute('data-id');
        const itemName = e.target.getAttribute('data-name');
        
        let merchantUid = '';
        let orderAmount = 0;
        let buyerName = '';
        let buyerEmail = '';
        let buyerTel = '010-1234-5678'; // 기본값 (서버에서 받지 못할 경우 대비)

        try {
            // 1. 서버에 주문 생성 요청 (Token이 헤더에 포함됨 -> 로그인 된 유저 정보 조회 가능)
            const response = await api.post('/api/orders', { itemId: itemId });
            merchantUid = response.data.orderUid;
            orderAmount = response.data.price;
            buyerName = response.data.buyerName;
            buyerEmail = response.data.buyerEmail;
            if (response.data.buyerTel) buyerTel = response.data.buyerTel;
        } catch (error) {
            alert('주문 생성 실패: ' + (error.response?.data?.message || error.message));
            return;
        }

        // 2. 결제 요청
        IMP.request_pay({
            pg: "html5_inicis",
            pay_method: "card",
            merchant_uid: merchantUid,
            name: itemName,
            amount: orderAmount,
            buyer_email: buyerEmail,
            buyer_name: buyerName,
            buyer_tel: buyerTel,
        }, async (rsp) => {
            if (rsp.success) {
                try {
                    await api.post('/api/payments/complete', {
                        paymentUid: rsp.imp_uid,
                        orderUid: rsp.merchant_uid
                    });
                    alert('결제가 정상적으로 완료되었습니다.');
                } catch (error) {
                    console.error(error);
                    alert('결제 검증 실패: ' + (error.response?.data?.message || error.message));
                }
            } else {
                alert(`결제에 실패하였습니다. 에러 내용: ${rsp.error_msg}`);
            }
        });
    });
});