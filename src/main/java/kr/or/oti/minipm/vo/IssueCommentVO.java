package kr.or.oti.minipm.vo;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class IssueCommentVO {

    private Long commentId;
    private Long issueId;

    // 댓글 작성자 정보
    private Long memberId;
    private String memberName;
    private String memberLoginId;

    private String content;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;
    private Long deletedBy;
}