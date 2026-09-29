package kr.or.oti.minipm.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class ProjectCreateDTO {
	
	@NotBlank(message = "프로젝트 이름을 입력해주세요.")
	@Size(
		max = 100,
		message = "프로젝트 이름은 100자 이하여야 합니다.")
	private String name;
	
	private String description;
}
