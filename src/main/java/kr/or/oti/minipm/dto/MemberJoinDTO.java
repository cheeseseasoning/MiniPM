package kr.or.oti.minipm.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.Data;
import lombok.ToString;

@Data
public class MemberJoinDTO {
	
	@NotBlank(message = "로그인 아이디를 입력해주세요.")
	@Size(
		min = 2,
		max = 30,
		message = "로그인 아이디는 4자 이상 50자 이하여야 합니다.")
    private String loginId;

    @ToString.Exclude
    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Size(
        min = 4,
        max = 50,
        message = "비밀번호는 4자 이상 50자 이하여야 합니다.")
    private String password;
    
    @NotBlank(message = "이름을 입력해주세요.")
    @Size(
        max = 50,
        message = "이름은 50자 이하여야 합니다.")
    private String name;
    
    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @Size(
        max = 255,
        message = "이메일은 255자 이하여야 합니다.")
    private String email;
}