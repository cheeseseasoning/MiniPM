package kr.or.oti.minipm.dto;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProjectMemberDTO {
	
    private Long projectId;
    private Long memberId;

    private String loginId;
    private String name;
    private String role;

    private LocalDateTime joinedAt;
}
