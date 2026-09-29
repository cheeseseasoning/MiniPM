package kr.or.oti.minipm.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class IssueUpdateDTO {
	
    @NotBlank(message = "이슈 제목을 입력해주세요.")
    @Size(
        max = 200,
        message = "이슈 제목은 200자 이하여야 합니다."
    )
    private String title;

    @NotBlank(message = "이슈 내용을 입력해주세요.")
    private String content;

    @NotBlank(message = "이슈 유형을 선택해주세요.")
    @Pattern(
        regexp = "FEATURE|BUG|TASK|IMPROVEMENT",
        message = "올바른 이슈 유형을 선택해주세요."
    )
    private String issueType;

    @NotBlank(message = "우선순위를 선택해주세요.")
    @Pattern(
        regexp = "HIGH|MEDIUM|LOW",
        message = "올바른 우선순위를 선택해주세요."
    )
    private String priority;
    
    // 담당자를 지정하지 않을 수 있으므로 null 허용
    private Long assigneeId;
}
