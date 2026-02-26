package com.server.service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import com.server.domain.Order;
import com.server.domain.OrderStatus;
import com.server.domain.Payment;
import com.server.dto.PaymentCallbackRequest;
import com.server.dto.IamportPaymentInfo;
import com.server.exception.CustomException;
import com.server.exception.ErrorCode;
import com.server.repository.OrderRepository;
import com.server.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${iamport.api.key}")
    private String apiKey;

    @Value("${iamport.api.secret}")
    private String apiSecret;

    private String iamportApiUrl = "https://api.iamport.kr";

    public void paymentByCallback(PaymentCallbackRequest request) {
        // 1. 주문 조회
        Order order = orderRepository.findByOrderUid(request.getOrderUid())
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        // 2. 아임포트(PortOne) 결제 내역 조회
        IamportPaymentInfo paymentInfo = getPaymentInfo(request.getPaymentUid());

        // 3. 결제 상태 검증 (결제 완료 상태가 아니면 예외 발생)
        if (!"paid".equals(paymentInfo.getStatus())) {
            throw new CustomException(ErrorCode.PAYMENT_NOT_PAID);
        }

        // 4. 결제 금액 검증 (DB 주문 금액 vs PG사 결제 금액)
        if (!order.getPrice().equals(paymentInfo.getAmount())) {
            // 금액이 다르면 결제 취소
            cancelPayment(request.getPaymentUid());

            // 결제 실패 이력 저장
            Payment payment = Payment.builder()
                    .amount(paymentInfo.getAmount())
                    .order(order)
                    .build();
            payment.paymentFail(request.getPaymentUid());
            paymentRepository.save(payment);

            throw new CustomException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        // 5. 결제 정보 저장 및 주문 상태 변경
        Payment payment = Payment.builder()
                .amount(paymentInfo.getAmount())
                .order(order)
                .build();
        
        payment.paymentSuccess(request.getPaymentUid());
        paymentRepository.save(payment);

        order.changeStatus(OrderStatus.PAID);
    }

    public void refund(PaymentCallbackRequest request) {
        Order order = orderRepository.findByOrderUid(request.getOrderUid())
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        // 아임포트 결제 취소
        cancelPayment(request.getPaymentUid());

        order.changeStatus(OrderStatus.CANCELLED);
    }

    // 아임포트 API 호출: 결제 정보 조회
    private IamportPaymentInfo getPaymentInfo(String impUid) {
        String accessToken = getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                iamportApiUrl + "/payments/" + impUid,
                HttpMethod.GET,
                entity,
                String.class
        );

        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode responseNode = root.path("response");
            return new IamportPaymentInfo(
                    responseNode.path("amount").asLong(),
                    responseNode.path("status").asString()
            );
        } catch (Exception e) {
            log.error("아임포트 결제 조회 실패: {}", e.getMessage());
            throw new CustomException(ErrorCode.IAMPORT_ERROR);
        }
    }

    // 아임포트 API 호출: 액세스 토큰 발급
    private String getAccessToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, String> body = Map.of("imp_key", apiKey, "imp_secret", apiSecret);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(iamportApiUrl + "/users/getToken", new HttpEntity<>(body, headers), String.class);
            return objectMapper.readTree(response.getBody()).path("response").path("access_token").asString();
        } catch (Exception e) {
            log.error("아임포트 토큰 발급 실패: {}", e.getMessage());
            throw new CustomException(ErrorCode.IAMPORT_ERROR);
        }
    }

    // 아임포트 API 호출: 결제 취소
    private void cancelPayment(String impUid) {
        try {
            String accessToken = getAccessToken();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", accessToken);

            Map<String, String> body = Map.of("imp_uid", impUid);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

            restTemplate.postForEntity(iamportApiUrl + "/payments/cancel", entity, String.class);
        } catch (Exception e) {
            log.error("아임포트 결제 취소 실패: {}", e.getMessage());
            throw new CustomException(ErrorCode.IAMPORT_ERROR);
        }
    }
}