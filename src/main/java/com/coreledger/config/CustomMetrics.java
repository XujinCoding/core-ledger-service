package com.coreledger.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Custom Business Metrics
 * Tracks business-specific metrics for monitoring
 *
 * @author Core Ledger Team
 * @since 1.0.0
 */
@Component
@Getter
public class CustomMetrics {

    private final MeterRegistry meterRegistry;

    // Ledger Metrics
    private final Counter ledgerCreatedCounter;
    private final Counter ledgerPaidCounter;
    private final Timer ledgerProcessingTimer;

    // Payment Metrics
    private final Counter paymentSuccessCounter;
    private final Counter paymentFailureCounter;

    // Customer Metrics
    private final Counter customerCreatedCounter;

    // Product Metrics
    private final Counter productCreatedCounter;

    public CustomMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // Initialize Ledger Metrics
        this.ledgerCreatedCounter = Counter.builder("business.ledger.created")
                .description("Total number of ledgers created")
                .tag("type", "ledger")
                .register(meterRegistry);

        this.ledgerPaidCounter = Counter.builder("business.ledger.paid")
                .description("Total number of ledgers marked as paid")
                .tag("type", "ledger")
                .register(meterRegistry);

        this.ledgerProcessingTimer = Timer.builder("business.ledger.processing.time")
                .description("Time taken to process ledger operations")
                .tag("type", "ledger")
                .register(meterRegistry);

        // Initialize Payment Metrics
        this.paymentSuccessCounter = Counter.builder("business.payment.success")
                .description("Total number of successful payments")
                .tag("type", "payment")
                .register(meterRegistry);

        this.paymentFailureCounter = Counter.builder("business.payment.failure")
                .description("Total number of failed payments")
                .tag("type", "payment")
                .register(meterRegistry);

        // Initialize Customer Metrics
        this.customerCreatedCounter = Counter.builder("business.customer.created")
                .description("Total number of customers created")
                .tag("type", "customer")
                .register(meterRegistry);

        // Initialize Product Metrics
        this.productCreatedCounter = Counter.builder("business.product.created")
                .description("Total number of products created")
                .tag("type", "product")
                .register(meterRegistry);
    }

    // Ledger Methods
    public void incrementLedgerCreated() {
        ledgerCreatedCounter.increment();
    }

    public void incrementLedgerPaid() {
        ledgerPaidCounter.increment();
    }

    public void recordLedgerProcessingTime(long milliseconds) {
        ledgerProcessingTimer.record(milliseconds, TimeUnit.MILLISECONDS);
    }

    // Payment Methods
    public void incrementPaymentSuccess() {
        paymentSuccessCounter.increment();
    }

    public void incrementPaymentFailure() {
        paymentFailureCounter.increment();
    }

    // Customer Methods
    public void incrementCustomerCreated() {
        customerCreatedCounter.increment();
    }

    // Product Methods
    public void incrementProductCreated() {
        productCreatedCounter.increment();
    }
}
