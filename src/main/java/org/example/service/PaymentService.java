package org.example.service;

import org.example.enums.PaymentStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;

@Service
public class PaymentService {
    
    @Autowired
    private WebClient.Builder webClientBuilder;
    
    @Value("${app.payment.gateway-url:http://localhost:8081/payment}")
    private String paymentGatewayUrl;
    
    @Value("${app.payment.timeout:30000}")
    private long timeout;
    
    @Value("${app.payment.retry-attempts:3}")
    private int retryAttempts;
    
    public PaymentResult processPayment(String bookingId, BigDecimal amount, String paymentMethod) {
        PaymentRequest request = new PaymentRequest(bookingId, amount, paymentMethod);
        
        try {
            PaymentResponse response = webClientBuilder.build()
                    .post()
                    .uri(paymentGatewayUrl + "/process")
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(HttpStatus::is4xxClientError, clientResponse -> 
                        Mono.error(new RuntimeException("Payment failed: " + clientResponse.statusCode())))
                    .onStatus(HttpStatus::is5xxServerError, clientResponse -> 
                        Mono.error(new RuntimeException("Payment gateway error: " + clientResponse.statusCode())))
                    .bodyToMono(PaymentResponse.class)
                    .timeout(Duration.ofMillis(timeout))
                    .retry(retryAttempts)
                    .block();
            
            return new PaymentResult(true, response.getPaymentId(), response.getMessage());
            
        } catch (WebClientResponseException e) {
            return new PaymentResult(false, null, "Payment failed: " + e.getMessage());
        } catch (Exception e) {
            return new PaymentResult(false, null, "Payment processing error: " + e.getMessage());
        }
    }
    
    public PaymentResult refundPayment(String paymentId, BigDecimal amount) {
        RefundRequest request = new RefundRequest(paymentId, amount);
        
        try {
            RefundResponse response = webClientBuilder.build()
                    .post()
                    .uri(paymentGatewayUrl + "/refund")
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(HttpStatus::is4xxClientError, clientResponse -> 
                        Mono.error(new RuntimeException("Refund failed: " + clientResponse.statusCode())))
                    .onStatus(HttpStatus::is5xxServerError, clientResponse -> 
                        Mono.error(new RuntimeException("Payment gateway error: " + clientResponse.statusCode())))
                    .bodyToMono(RefundResponse.class)
                    .timeout(Duration.ofMillis(timeout))
                    .retry(retryAttempts)
                    .block();
            
            return new PaymentResult(true, response.getRefundId(), response.getMessage());
            
        } catch (WebClientResponseException e) {
            return new PaymentResult(false, null, "Refund failed: " + e.getMessage());
        } catch (Exception e) {
            return new PaymentResult(false, null, "Refund processing error: " + e.getMessage());
        }
    }
    
    // Inner classes for payment requests and responses
    public static class PaymentRequest {
        private String bookingId;
        private BigDecimal amount;
        private String paymentMethod;
        
        public PaymentRequest() {}
        
        public PaymentRequest(String bookingId, BigDecimal amount, String paymentMethod) {
            this.bookingId = bookingId;
            this.amount = amount;
            this.paymentMethod = paymentMethod;
        }
        
        public String getBookingId() { return bookingId; }
        public void setBookingId(String bookingId) { this.bookingId = bookingId; }
        
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    }
    
    public static class PaymentResponse {
        private String paymentId;
        private String message;
        private PaymentStatus status;
        
        public PaymentResponse() {}
        
        public String getPaymentId() { return paymentId; }
        public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public PaymentStatus getStatus() { return status; }
        public void setStatus(PaymentStatus status) { this.status = status; }
    }
    
    public static class RefundRequest {
        private String paymentId;
        private BigDecimal amount;
        
        public RefundRequest() {}
        
        public RefundRequest(String paymentId, BigDecimal amount) {
            this.paymentId = paymentId;
            this.amount = amount;
        }
        
        public String getPaymentId() { return paymentId; }
        public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
        
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
    }
    
    public static class RefundResponse {
        private String refundId;
        private String message;
        
        public RefundResponse() {}
        
        public String getRefundId() { return refundId; }
        public void setRefundId(String refundId) { this.refundId = refundId; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
    
    public static class PaymentResult {
        private boolean success;
        private String transactionId;
        private String message;
        
        public PaymentResult() {}
        
        public PaymentResult(boolean success, String transactionId, String message) {
            this.success = success;
            this.transactionId = transactionId;
            this.message = message;
        }
        
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getTransactionId() { return transactionId; }
        public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
