package com.cas.modules.notification;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cas.modules.notification.entity.Notification;
import com.cas.modules.notification.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationMapper notificationMapper;

    public List<Map<String, Object>> listNotifications(Long userId, String type) {
        LambdaQueryWrapper<Notification> q = Wrappers.lambdaQuery();
        q.eq(Notification::getUserId, userId);
        if (type != null) q.eq(Notification::getType, type);
        q.orderByDesc(Notification::getCreatedAt);
        return notificationMapper.selectList(q).stream().map(this::toMap).collect(Collectors.toList());
    }

    public void markRead(Long userId, Long notificationId) {
        Notification notif = notificationMapper.selectById(notificationId);
        if (notif == null) throw new com.cas.common.exception.BusinessException(404, "通知不存在");
        if (!notif.getUserId().equals(userId)) throw new com.cas.common.exception.BusinessException(403, "无权操作此通知");
        notif.setIsRead(1);
        notificationMapper.updateById(notif);
    }

    private Map<String, Object> toMap(Notification n) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", n.getId());
        m.put("type", n.getType());
        m.put("title", n.getTitle());
        m.put("content", n.getContent());
        m.put("courseName", n.getCourseName());
        m.put("read", n.getIsRead() == 1);
        m.put("createdAt", n.getCreatedAt());
        return m;
    }
}