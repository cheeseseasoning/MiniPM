package kr.or.oti.minipm.vo;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProjectVO {
    private Long projectId;
    private String name;
    private String description;

    // 프로젝트를 최초로 생성한 회원번호
    private Long memberId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
