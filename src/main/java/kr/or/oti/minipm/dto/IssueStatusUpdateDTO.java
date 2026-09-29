package kr.or.oti.minipm.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class IssueStatusUpdateDTO {

    @NotBlank(message = "변경할 상태를 선택해주세요.")
    @Pattern(
        regexp = "TODO|IN_PROGRESS|DONE",
        message = "올바른 상태를 선택해주세요."
    )
    private String status;

    @Size(max = 1000, message = "해결 내용은 1000자 이하여야 합니다.")
    private String resolution;
}