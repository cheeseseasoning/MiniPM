package kr.or.oti.minipm.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.oti.minipm.dto.NotificationResponseDTO;
import kr.or.oti.minipm.mapper.MemberMapper;
import kr.or.oti.minipm.mapper.NotificationMapper;
import kr.or.oti.minipm.vo.MemberVO;
import kr.or.oti.minipm.vo.NotificationVO;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final int RECENT_NOTIFICATION_LIMIT = 30;

    private final NotificationMapper notificationMapper;
    private final MemberMapper memberMapper;
    private final SimpMessagingTemplate messagingTemplate;

    // 알림을 DB에 저장하고 접속 중인 회원에게 실시간 전송
    @Transactional
    public NotificationResponseDTO createNotification(
            Long memberId,
            String notificationType,
            String message,
            String linkUrl) {

        MemberVO memberVO = memberMapper.selectMemberById(memberId);

        if (memberVO == null) {
            throw new IllegalStateException("알림을 받을 회원을 찾을 수 없습니다.");
        }

        NotificationVO notificationVO = new NotificationVO();
        notificationVO.setMemberId(memberId);
        notificationVO.setNotificationType(notificationType);
        notificationVO.setMessage(message);
        notificationVO.setLinkUrl(linkUrl);

        int insertedRowCount = notificationMapper.insertNotification(notificationVO);

        if (insertedRowCount != 1) {
            throw new IllegalStateException("알림 저장에 실패했습니다.");
        }

        NotificationVO savedNotification = 
                notificationMapper.selectNotification(
                        notificationVO.getNotificationId(),
                        memberId
                );

        if (savedNotification == null) {
            throw new IllegalStateException("저장된 알림을 조회할 수 없습니다.");
        }

        NotificationResponseDTO responseDTO = NotificationResponseDTO.from(savedNotification);

        messagingTemplate.convertAndSendToUser(
                memberVO.getLoginId(),
                "/queue/notifications",
                responseDTO
        );

        return responseDTO;
    }
    
 // 여러 회원에게 같은 알림을 중복 없이 전송
    @Transactional
    public void createNotifications(
            List<Long> recipientMemberIds,
            Long actorMemberId,
            String notificationType,
            String message,
            String linkUrl) {

        if (recipientMemberIds == null || recipientMemberIds.isEmpty()) {
            return;
        }

        Set<Long> uniqueRecipientIds = new LinkedHashSet<>(recipientMemberIds);

        // 잘못 들어온 null 회원번호 제거
        uniqueRecipientIds.remove(null);

        // 작업을 실행한 회원 본인은 알림 대상에서 제외
        if (actorMemberId != null) {
            uniqueRecipientIds.remove(actorMemberId);
        }

        for (Long recipientMemberId : uniqueRecipientIds) {
            createNotification(recipientMemberId, notificationType, message, linkUrl);
        }
    }

    // 로그인 회원의 최신 알림 목록 조회
    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> findRecentNotifications(Long memberId) {

        return notificationMapper
                .selectRecentNotifications(memberId, RECENT_NOTIFICATION_LIMIT)
                .stream()
                .map(NotificationResponseDTO::from)
                .collect(Collectors.toList());
    }

    // 로그인 회원의 읽지 않은 알림 개수 조회
    @Transactional(readOnly = true)
    public int countUnreadNotifications(Long memberId) {

        return notificationMapper.countUnreadNotifications(memberId);
    }

    // 로그인 회원에게 속한 특정 알림 읽음 처리
    @Transactional
    public void readNotification(Long notificationId, Long memberId) {

        NotificationVO notificationVO =
                notificationMapper.selectNotification(notificationId, memberId);

        if (notificationVO == null) {
            throw new IllegalStateException("알림을 찾을 수 없습니다.");
        }

        if ("Y".equals(notificationVO.getIsRead())) {
            return;
        }

        int updatedRowCount = notificationMapper.updateNotificationRead(notificationId, memberId);

        if (updatedRowCount != 1) {
            throw new IllegalStateException("알림 읽음 처리에 실패했습니다.");
        }
    }

    // 로그인 회원의 모든 알림 읽음 처리
    @Transactional
    public int readAllNotifications(Long memberId) {

        return notificationMapper.updateAllNotificationsRead(memberId);
    }
}