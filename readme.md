# PortOne 결제 시스템 구현

- 결제 시스템을 구현하기 위해 포트원을 사용한 연습 프로젝트입니다.

- 포트원은 웹/앱 서비스에서 PG(Payment Gateway)사와의 결제 연동을 쉽게 구현할 수 있도록 도와주는 **결제 연동 솔루션**입니다.

- 기간: 20260211 ~ 20260303(21일)
- 인원: 1인

## 구성

- backend: springboot
- frontend: html, css, js

## 시연 

![alt text](/images/홈화면.png)

홈페이지에서 구매하기 버튼을 누르면 결제화면으로 넘어갑니다.

![alt text](/images/결제화면.png)

고객이 결제를 진행하면 포트원 API를 통해 결제가 이루어지고, 이후 서버에서 결제 위변조 검증 과정을 거쳐 최종적으로 주문이 완료됩니다. 결제된 금액은 정산 주기에 맞춰 연동된 PG사를 통해 계좌로 입금됩니다.

## 상세 과정

1. 주문 생성 (/api/orders): "구매하기" 버튼을 누르면 먼저 서버에 주문 정보를 알리고 merchant_uid(주문번호)를 받아옵니다.


![alt text](/images/order_controller.png)

2. 결제 요청 (IMP.request_pay): 포트원 창을 띄워 고객이 카드 결제를 진행합니다. 이때 포트원이 PG사(이니시스 등)와 통신합니다.


![alt text](/images/request_pay.png)


3. 결제 검증 (/api/payments/complete): 결제가 성공하면, 프론트엔드가 다시 서버로 요청을 보냅니다. 서버는 포트원 서버와 통신하여 실제 결제 금액이 맞는지 확인하고 최종 주문 처리를 합니다.

![alt text](/images/결제완료.png)

