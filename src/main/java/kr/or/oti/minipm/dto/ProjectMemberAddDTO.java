package kr.or.oti.minipm.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class ProjectMemberAddDTO {

    @NotBlank(message = "추가할 회원의 이메일을 입력해주세요.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @Size(
        max = 255,
        message = "이메일은 255자 이하여야 합니다.")
    private String email;
}