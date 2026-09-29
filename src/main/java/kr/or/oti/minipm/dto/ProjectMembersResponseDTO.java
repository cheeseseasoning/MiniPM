package kr.or.oti.minipm.dto;

import java.util.List;

import kr.or.oti.minipm.vo.ProjectVO;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProjectMembersResponseDTO {
	
	private final ProjectVO project;
	private final List<ProjectMemberDTO> members;
}
