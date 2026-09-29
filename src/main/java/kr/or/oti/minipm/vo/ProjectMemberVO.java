package kr.or.oti.minipm.vo;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProjectMemberVO {

    private Long projectId;
    private Long memberId;
    private String role;
    private LocalDateTime joinedAt;
}