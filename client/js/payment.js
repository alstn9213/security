document.addEventListener('DOMContentLoaded', () => {
    const IMP = window.IMP;
    IMP.init("imp68206770"); 

    const paymentBtn = document.getElementById('paymentBtn');
    const paymentResult = document.getElementById('paymentResult');

    paymentBtn.addEventListener('click', async () => {
        // 로그인 여부 체크
        if (!localStorage.getItem('accessToken')) {
            alert('로그인이 필요합니다.');
            return;
        }

        const itemId = document.getElementById('itemId').value;
        
        let merchantUid = '';
        try {
            // 1. 서버에 주문 생성 요청 (OrderController 호출)
            const response = await api.post('/api/orders', { itemId: itemId });
            merchantUid = response.data.orderUid; // 서버 DB에 저장된 실제 주문 번호
        } catch (error) {
            alert('주문 생성 실패: ' + (error.response?.data?.message || error.message));
            return;
        }

        // 2. 결제 요청 (PG사 창 띄우기)
        IMP.request_pay({
            pg: "html5_inicis",       // PG사 (이니시스 등)
            pay_method: "card",       // 결제 수단
            merchant_uid: merchantUid,// 주문 번호
            name: "테스트 상품 " + itemId, // 상품명
            amount: 100,              // 결제 금액 (테스트용 100원)
            buyer_email: "test@test.com",
            buyer_name: "테스터",
            buyer_tel: "010-1234-5678",
        }, async function (rsp) { // callback
            if (rsp.success) {
                // 3. 결제 성공 시 서버 검증 요청
                paymentResult.textContent = '결제 성공! 서버 검증 중...';
                
                try {
                    // PaymentService.java의 paymentByCallback 로직에 대응하는 API 호출
                    await api.post('/api/payments/complete', {
                        paymentUid: rsp.imp_uid,      // 포트원 결제 고유 번호
                        orderUid: rsp.merchant_uid    // 주문 번호
                    });

                    paymentResult.textContent = '결제 및 검증 완료!';
                    paymentResult.style.color = 'blue';
                    alert('결제가 정상적으로 완료되었습니다.');
                } catch (error) {
                    paymentResult.textContent = '결제 검증 실패 (위변조 위험)';
                    paymentResult.style.color = 'red';
                    console.error(error);
                }
            } else {
                // 결제 실패
                paymentResult.textContent = '결제 실패: ' + rsp.error_msg;
                paymentResult.style.color = 'red';
            }
        });
    });
});