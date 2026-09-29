package kr.or.oti.minipm.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.oti.minipm.vo.NotificationVO;

@Mapper
public interface NotificationMapper {

    // 새로운 알림 등록
    int insertNotification(NotificationVO notificationVO);

    // 회원에게 속한 특정 알림 조회
    NotificationVO selectNotification(
            @Param("notificationId") Long notificationId,
            @Param("memberId") Long memberId
    );

    // 회원의 최신 알림 목록 조회
    List<NotificationVO> selectRecentNotifications(
            @Param("memberId") Long memberId,
            @Param("limit") int limit
    );

    // 회원의 읽지 않은 알림 개수 조회
    int countUnreadNotifications(
            @Param("memberId") Long memberId
    );

    // 특정 알림 읽음 처리
    int updateNotificationRead(
            @Param("notificationId") Long notificationId,
            @Param("memberId") Long memberId
    );

    // 회원의 모든 알림 읽음 처리
    int updateAllNotificationsRead(
            @Param("memberId") Long memberId
    );
}