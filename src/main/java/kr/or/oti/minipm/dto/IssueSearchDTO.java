package kr.or.oti.minipm.dto;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class IssueSearchDTO {

    @Size(
        max = 100,
        message = "검색어는 100자 이하여야 합니다."
    )
    private String keyword;

    @Pattern(
        regexp = "^(|FEATURE|BUG|TASK|IMPROVEMENT)$",
        message = "올바른 이슈 유형을 선택해주세요."
    )
    private String issueType;

    @Pattern(
        regexp = "^(|TODO|IN_PROGRESS|DONE)$",
        message = "올바른 상태를 선택해주세요."
    )
    private String status;

    @Pattern(
        regexp = "^(|HIGH|MEDIUM|LOW)$",
        message = "올바른 우선순위를 선택해주세요."
    )
    private String priority;

    @Positive(message = "올바른 담당자를 선택해주세요.")
    private Long assigneeId;
}