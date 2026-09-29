package kr.or.oti.minipm.controller;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kr.or.oti.minipm.dto.NotificationResponseDTO;
import kr.or.oti.minipm.security.LoginMemberDetails;
import kr.or.oti.minipm.service.NotificationService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // 로그인 회원의 최근 알림 목록 조회
    @GetMapping
    public List<NotificationResponseDTO> notifications(
            @AuthenticationPrincipal
            LoginMemberDetails loginMember) {

        Long memberId = loginMember.getMemberId();

        return notificationService.findRecentNotifications(memberId);
    }

    // 로그인 회원의 읽지 않은 알림 개수 조회
    @GetMapping("/unread-count")
    public Map<String, Integer> unreadCount(
            @AuthenticationPrincipal
            LoginMemberDetails loginMember) {

        Long memberId = loginMember.getMemberId();

        int unreadCount = notificationService.countUnreadNotifications(memberId);

        return Map.of("unreadCount", unreadCount);
    }

    // 로그인 회원에게 속한 특정 알림 읽음 처리
    @PatchMapping("/{notificationId}/read")
    public Map<String, String> readNotification(
            @PathVariable("notificationId")
            Long notificationId,
            @AuthenticationPrincipal
            LoginMemberDetails loginMember) {

        Long memberId = loginMember.getMemberId();

        notificationService.readNotification(notificationId, memberId);

        return Map.of("message", "알림을 읽음 처리했습니다.");
    }

    // 로그인 회원의 모든 알림 읽음 처리
    @PatchMapping("/read-all")
    public Map<String, Integer> readAllNotifications(
            @AuthenticationPrincipal
            LoginMemberDetails loginMember) {

        Long memberId = loginMember.getMemberId();

        int updatedCount = notificationService.readAllNotifications(memberId);

        return Map.of("updatedCount", updatedCount);
    }
}