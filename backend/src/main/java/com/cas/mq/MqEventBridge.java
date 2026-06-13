package com.cas.mq;

import com.cas.config.RabbitMQConfig;
import com.cas.mq.event.EnrollEvent;
import com.cas.mq.event.ReviewEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class MqEventBridge {

    private final RabbitTemplate rabbitTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEnrollEvent(EnrollEvent event) {
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ENROLLMENT_LOG_KEY, event);
            log.info("Sent enrollment log to MQ: enrollmentId={}", event.getEnrollmentId());
        } catch (Exception e) {
            log.error("Failed to send enrollment log to MQ", e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReviewEvent(ReviewEvent event) {
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.NOTIFICATION_KEY, event);
            log.info("Sent review notification to MQ: offeringId={}, type={}", event.getOfferingId(), event.getType());
        } catch (Exception e) {
            log.error("Failed to send review notification to MQ", e);
        }
    }
}