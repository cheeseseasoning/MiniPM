package kr.or.oti.minipm.vo;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class IssueVO {

    private Long issueId;
    private Long projectId;

    private String title;
    private String content;
    private String issueType;
    private String priority;
    private String status;

    // 작성자 정보
    private Long reporterId;
    private String reporterName;
    private String reporterLoginId;

    // 담당자 정보
    private Long assigneeId;
    private String assigneeName;
    private String assigneeLoginId;

    // 해결 정보
    private String resolution;

    private Long resolvedBy;
    private String resolvedByName;
    private String resolvedByLoginId;

    private LocalDateTime resolvedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}