package com.investagg.service;

import com.investagg.dto.response.NotificationResponse;
import com.investagg.entity.Notification;
import com.investagg.entity.User;
import com.investagg.entity.enums.NotificationType;
import com.investagg.exception.EntityNotFoundException;
import com.investagg.exception.ForbiddenException;
import com.investagg.repository.NotificationRepository;
import com.investagg.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public void send(UUID userId, NotificationType type, String title, String body) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setNotificationType(type);
        notification.setTitle(title);
        notification.setBody(body);
        notificationRepository.save(notification);

        log.info("Notification sent to user {}: [{}] {}", userId, type, title);
    }

    public List<NotificationResponse> getForUser(UUID userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public NotificationResponse markRead(UUID userId, UUID notificationId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new ForbiddenException("Notification not found or access denied"));

        notification.setRead(true);
        notificationRepository.save(notification);
        return toResponse(notification);
    }

    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getNotificationType(),
                n.getTitle(),
                n.getBody(),
                n.isRead(),
                n.getCreatedAt()
        );
    }
}
