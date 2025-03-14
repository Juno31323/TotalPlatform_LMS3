package com.example.demo.lms.paymentInfo;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.stereotype.Controller;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Controller
public class PaymentInfoService {
	
	private final TossPaymentsApiClient tossPaymentsApiClient;
	private final PaymentInfoRepository paymentInfoRepository;

	
	
	
	public String confirmPayment(String paymentKey, String orderId, Long amount) throws IOException {
	    return tossPaymentsApiClient.confirmPayment(paymentKey, orderId, amount);
	}
	public String getPayment(String paymentKey) throws IOException {
	    return tossPaymentsApiClient.getPayment(paymentKey);
	}
	
    // 1. 결제 요청 처리
    public String requestPayment(PaymentRequest request) throws IOException {
        return tossPaymentsApiClient.requestPayment(request);
    }
    // 2. 결제 승인 처리 및 저장
    public PaymentResponse processPaymentSuccess(String paymentKey, String orderId, Long amount) throws IOException {
        // 1) 토스페이먼츠 API에 승인 요청
        String confirmResponse = tossPaymentsApiClient.confirmPayment(paymentKey, orderId, amount);
        // 2) 응답 데이터 파싱 (Gson 또는 Jackson 사용 가능)
        PaymentInfoDetails details = PaymentInfoDetails.fromJson(confirmResponse);
        // 3) 데이터베이스에 저장할 엔티티 생성 및 저장
        
        if(details.getMethod().equals("간편결제")) {
        	PaymentInfo paymentInfo = PaymentInfo.builder()
                    .paymentKey(details.getPaymentKey())
                    .orderId(details.getOrderId())
                    .amount(details.getTotalAmount())
                    .paymentMethod(details.getMethod())
                    //.cardInfo(details.getCardInfo().getIssuerCode())
                    .build();
                paymentInfoRepository.save(paymentInfo);
                
        }else if(details.getMethod().equals("카드")) {
        	PaymentInfo paymentInfo = PaymentInfo.builder()
                    .paymentKey(details.getPaymentKey())
                    .orderId(details.getOrderId())
                    .amount(details.getTotalAmount())
                    .paymentMethod(details.getMethod())
                    .cardInfo(details.getCard().getIssuerCode())
                    .build();
                paymentInfoRepository.save(paymentInfo);
        }
    
        // 4) 응답 DTO 반환
        return convertToResponse(details);
    }
    // 응답을 변환하는 헬퍼 메서드
    private PaymentResponse convertToResponse(PaymentInfoDetails details) {
        return PaymentResponse.builder()
            .paymentKey(details.getPaymentKey())
            .orderId(details.getOrderId())
            .amount(details.getTotalAmount())
            .paymentMethod(details.getMethod())
            .orderName(details.getOrderName())
            .status("COMPLETED")
            .paymentDate(LocalDateTime.now())
            .build();
    }
	 

}
