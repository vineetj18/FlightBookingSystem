package org.example.service;

import org.example.enums.PaymentStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PaymentService {
    
    public static class PaymentResult {
        private String paymentId;
        private PaymentStatus status;
        private String message;
        
        public PaymentResult(String paymentId, PaymentStatus status, String message) {
            this.paymentId = paymentId;
            this.status = status;
            this.message = message;
        }
        
        public String getPaymentId() { return paymentId; }
        public PaymentStatus getStatus() { return status; }
        public String getMessage() { return message; }
        public String getTransactionId() { return paymentId; }
        public boolean isSuccess() { return status == PaymentStatus.COMPLETED; }
    }
    
    public PaymentResult processPayment(String bookingId, BigDecimal amount, String paymentDetails) {
        // Mock payment processing - always return success for demo
        // In real implementation, this would call a payment gateway
        String paymentId = "PAY_" + System.currentTimeMillis();
        return new PaymentResult(paymentId, PaymentStatus.COMPLETED, "Payment successful");
    }
    
    public PaymentStatus getPaymentStatus(String paymentId) {
        // Mock payment status - always return completed for demo
        return PaymentStatus.COMPLETED;
    }
    
    public PaymentResult refundPayment(String paymentId, BigDecimal amount) {
        // Mock refund processing - always return success for demo
        return new PaymentResult(paymentId, PaymentStatus.REFUNDED, "Refund successful");
    }
}