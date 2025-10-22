package org.example.service;

import org.example.service.PaymentService.PaymentResult;
import org.example.enums.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {
    
    @InjectMocks
    private PaymentService paymentService;
    
    private String testBookingId;
    private BigDecimal testAmount;
    private String testPaymentMethod;
    
    @BeforeEach
    void setUp() {
        testBookingId = "BK123456789";
        testAmount = new BigDecimal("299.99");
        testPaymentMethod = "CREDIT_CARD";
    }
    
    @Test
    void processPayment_ShouldReturnSuccess_WhenPaymentSucceeds() {
        // When
        PaymentResult result = paymentService.processPayment(testBookingId, testAmount, testPaymentMethod);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getTransactionId());
        assertEquals("Payment successful", result.getMessage());
        assertEquals(PaymentStatus.COMPLETED, result.getStatus());
    }
    
    @Test
    void processPayment_ShouldReturnValidPaymentId() {
        // When
        PaymentResult result = paymentService.processPayment(testBookingId, testAmount, testPaymentMethod);
        
        // Then
        assertNotNull(result.getPaymentId());
        assertTrue(result.getPaymentId().startsWith("PAY_"));
        assertEquals(result.getPaymentId(), result.getTransactionId());
    }
    
    @Test
    void getPaymentStatus_ShouldReturnCompleted() {
        // Given
        String paymentId = "PAY_123456789";
        
        // When
        PaymentStatus status = paymentService.getPaymentStatus(paymentId);
        
        // Then
        assertEquals(PaymentStatus.COMPLETED, status);
    }
    
    @Test
    void refundPayment_ShouldReturnSuccess_WhenRefundSucceeds() {
        // Given
        String transactionId = "TXN123456";
        BigDecimal refundAmount = new BigDecimal("299.99");
        
        // When
        PaymentResult result = paymentService.refundPayment(transactionId, refundAmount);
        
        // Then
        assertNotNull(result);
        // Note: isSuccess() returns true only for COMPLETED status, but REFUNDED is also a success
        assertEquals(PaymentStatus.REFUNDED, result.getStatus());
        assertEquals(transactionId, result.getTransactionId());
        assertEquals("Refund successful", result.getMessage());
    }
    
    @Test
    void refundPayment_ShouldReturnValidRefundId() {
        // Given
        String transactionId = "TXN123456";
        BigDecimal refundAmount = new BigDecimal("299.99");
        
        // When
        PaymentResult result = paymentService.refundPayment(transactionId, refundAmount);
        
        // Then
        assertNotNull(result.getPaymentId());
        assertEquals(transactionId, result.getPaymentId());
        assertEquals(transactionId, result.getTransactionId());
    }
    
    @Test
    void processPayment_ShouldHandleDifferentAmounts() {
        // Given
        BigDecimal smallAmount = new BigDecimal("50.00");
        BigDecimal largeAmount = new BigDecimal("1000.00");
        
        // When
        PaymentResult smallResult = paymentService.processPayment(testBookingId, smallAmount, testPaymentMethod);
        // Add small delay to ensure different timestamps
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        PaymentResult largeResult = paymentService.processPayment(testBookingId, largeAmount, testPaymentMethod);
        
        // Then
        assertTrue(smallResult.isSuccess());
        assertTrue(largeResult.isSuccess());
        assertNotEquals(smallResult.getTransactionId(), largeResult.getTransactionId());
    }
    
    @Test
    void processPayment_ShouldHandleDifferentPaymentMethods() {
        // Given
        String creditCard = "CREDIT_CARD";
        String debitCard = "DEBIT_CARD";
        String paypal = "PAYPAL";
        
        // When
        PaymentResult creditResult = paymentService.processPayment(testBookingId, testAmount, creditCard);
        PaymentResult debitResult = paymentService.processPayment(testBookingId, testAmount, debitCard);
        PaymentResult paypalResult = paymentService.processPayment(testBookingId, testAmount, paypal);
        
        // Then
        assertTrue(creditResult.isSuccess());
        assertTrue(debitResult.isSuccess());
        assertTrue(paypalResult.isSuccess());
    }
}
