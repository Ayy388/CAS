package com.cas.mq.consumer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cas.modules.course.entity.Course;
import com.cas.modules.course.mapper.CourseMapper;
import com.cas.modules.enrollment.entity.Enrollment;
import com.cas.modules.enrollment.mapper.EnrollmentMapper;
import com.cas.modules.notification.entity.Notification;
import com.cas.modules.notification.mapper.NotificationMapper;
import com.cas.modules.offering.entity.Offering;
import com.cas.modules.offering.mapper.OfferingMapper;
import com.cas.mq.event.ReviewEvent;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final OfferingMapper offeringMapper;
    private final CourseMapper courseMapper;
    private final EnrollmentMapper enrollmentMapper;
    private final NotificationMapper notificationMapper;

    @RabbitListener(queues = "cas.queue.notification", ackMode = "MANUAL")
    public void handleReviewNotification(ReviewEvent event, Channel channel,
                                         @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        try {
            Offering offering = offeringMapper.selectById(event.getOfferingId());
            if (offering == null) {
                channel.basicAck(tag, false);
                return;
            }

            Course course = courseMapper.selectById(offering.getCourseId());
            String courseName = course != null ? course.getName() : "";

            // Get all enrollments for this offering (don't filter by status)
            LambdaQueryWrapper<Enrollment> eq = Wrappers.lambdaQuery();
            eq.eq(Enrollment::getOfferingId, event.getOfferingId());
            List<Enrollment> enrollments = enrollmentMapper.selectList(eq);

            String type = event.getType();
            String title = "APPROVED".equals(type)
                    ? "《" + courseName + "》审核通过"
                    : "《" + courseName + "》审核未通过";
            String content = "APPROVED".equals(type)
                    ? "您报名的《" + courseName + "》已审核通过，将正常开课。"
                    : "您报名的《" + courseName + "》未通过审核，课程将取消。";

            List<Notification> notifications = enrollments.stream().map(e -> {
                Notification n = new Notification();
                n.setUserId(e.getStudentId());
                n.setType(type);
                n.setTitle(title);
                n.setContent(content);
                n.setCourseName(courseName);
                n.setIsRead(0);
                n.setCreatedAt(LocalDateTime.now());
                return n;
            }).collect(Collectors.toList());

            for (Notification n : notifications) {
                notificationMapper.insert(n);
            }

            channel.basicAck(tag, false);
            log.info("Generated {} notifications for offering {}", notifications.size(), event.getOfferingId());
        } catch (Exception e) {
            log.error("Failed to process review notification", e);
            channel.basicNack(tag, false, true);
        }
    }
}