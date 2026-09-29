package kr.or.oti.minipm.vo;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class IssueStatusHistoryVO {

    private Long historyId;
    private Long issueId;

    private String previousStatus;
    private String newStatus;

    // 상태 변경자 정보
    private Long memberId;
    private String memberName;
    private String memberLoginId;

    private LocalDateTime changedAt;
}