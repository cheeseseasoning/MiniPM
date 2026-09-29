package kr.or.oti.minipm.vo;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NotificationVO {

    private Long notificationId;
    private Long memberId;

    private String notificationType;
    private String message;
    private String linkUrl;

    private String isRead;

    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}