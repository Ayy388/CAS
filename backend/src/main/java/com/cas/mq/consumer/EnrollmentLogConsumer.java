package com.cas.mq.consumer;

import com.cas.mq.event.EnrollEvent;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class EnrollmentLogConsumer {

    @RabbitListener(queues = "cas.queue.enrollment.log", ackMode = "MANUAL")
    public void handleEnrollmentLog(EnrollEvent event, Channel channel,
                                    @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        try {
            log.info("Enrollment log: enrollmentId={}, studentId={}, offeringId={}",
                    event.getEnrollmentId(), event.getStudentId(), event.getOfferingId());
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("Failed to process enrollment log", e);
            channel.basicNack(tag, false, true);
        }
    }
}