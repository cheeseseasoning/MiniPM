package kr.or.oti.minipm.dto;

import java.time.LocalDateTime;

import kr.or.oti.minipm.vo.NotificationVO;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationResponseDTO {

    private final Long notificationId;

    private final String notificationType;
    private final String message;
    private final String linkUrl;

    private final boolean read;

    private final LocalDateTime createdAt;
    private final LocalDateTime readAt;

    // NotificationVO를 화면과 WebSocket 응답용 DTO로 변환
    public static NotificationResponseDTO from(NotificationVO notificationVO) {

        return NotificationResponseDTO.builder()
                .notificationId(notificationVO.getNotificationId())
                .notificationType(notificationVO.getNotificationType())
                .message(notificationVO.getMessage())
                .linkUrl(notificationVO.getLinkUrl())
                .read("Y".equals(notificationVO.getIsRead()))
                .createdAt(notificationVO.getCreatedAt())
                .readAt(notificationVO.getReadAt())
                .build();
    }
}