package kr.or.oti.minipm.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.oti.minipm.dto.PageRequestDTO;
import kr.or.oti.minipm.dto.ProjectMemberDTO;
import kr.or.oti.minipm.dto.ProjectSearchDTO;
import kr.or.oti.minipm.vo.ProjectMemberVO;
import kr.or.oti.minipm.vo.ProjectVO;

@Mapper
public interface ProjectMapper {

    int insertProject(ProjectVO projectVO);

    int insertProjectMember(ProjectMemberVO projectMemberVO);

    ProjectVO selectProjectById(
    	@Param("projectId") Long projectId
    );
    
    // 삭제되지 않은 프로젝트 이름 조회
    String selectProjectNameById(
            @Param("projectId") Long projectId
    );

    ProjectMemberVO selectProjectMember(
    	@Param("projectId") Long projectId,
        @Param("memberId") Long memberId
    );

    List<ProjectVO> selectProjectsByMemberId(
    	@Param("memberId") Long memberId
    );

    // 특정 프로젝트에 참여한 전체 멤버 목록
    List<ProjectMemberDTO> selectProjectMembers(
    	@Param("projectId") Long projectId
    );
    
    // 특정 프로젝트의 관리자 회원번호 목록
    List<Long> selectProjectAdminIds(
    	@Param("projectId") Long projectId
    );

    // 프로젝트 이름과 설명 수정
    int updateProject(ProjectVO projectVO);
    
    // 프로젝트의 미삭제 이슈 개수
    int countIssuesByProjectId(
            @Param("projectId") Long projectId
    );

    // 프로젝트 논리 삭제
    int softDeleteProject(
            @Param("projectId") Long projectId,
            @Param("loginMemberId") Long loginMemberId
    );
    
    // ADMIN이 MEMBER 역할의 프로젝트 참여 정보 삭제
    int deleteProjectMember(
            @Param("projectId") Long projectId,
            @Param("targetMemberId") Long targetMemberId,
            @Param("loginMemberId") Long loginMemberId
    );
    
    // 회원이 담당 중인 미완료 이슈 개수 조회
    int countOpenIssuesByAssignee(
            @Param("projectId") Long projectId,
            @Param("memberId") Long memberId
    );
    
    // 검색 조건에 맞는 참여 프로젝트를 페이지 단위로 조회
    List<ProjectVO> selectProjectsByMemberIdWithPaging(
            @Param("memberId") Long memberId,
            @Param("search") ProjectSearchDTO projectSearchDTO,
            @Param("pageRequest") PageRequestDTO pageRequestDTO);

    // 검색 조건에 맞는 참여 프로젝트 전체 개수
    int countProjectsByMemberId(
            @Param("memberId") Long memberId,
            @Param("search") ProjectSearchDTO projectSearchDTO);
}
