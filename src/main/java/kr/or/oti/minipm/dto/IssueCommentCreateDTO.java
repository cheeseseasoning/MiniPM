package kr.or.oti.minipm.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class IssueCommentCreateDTO {

    @NotBlank(message = "댓글 내용을 입력해주세요.")
    @Size(max = 2000, message = "댓글은 2000자 이하여야 합니다.")
    private String content;
}