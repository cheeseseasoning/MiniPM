package kr.or.oti.minipm.service;

import java.util.List;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.oti.minipm.dto.PageRequestDTO;
import kr.or.oti.minipm.dto.PageResponseDTO;
import kr.or.oti.minipm.dto.ProjectCreateDTO;
import kr.or.oti.minipm.dto.ProjectMemberAddDTO;
import kr.or.oti.minipm.dto.ProjectMemberDTO;
import kr.or.oti.minipm.dto.ProjectMembersResponseDTO;
import kr.or.oti.minipm.dto.ProjectSearchDTO;
import kr.or.oti.minipm.dto.ProjectUpdateDTO;
import kr.or.oti.minipm.mapper.MemberMapper;
import kr.or.oti.minipm.mapper.ProjectMapper;
import kr.or.oti.minipm.vo.MemberVO;
import kr.or.oti.minipm.vo.ProjectMemberVO;
import kr.or.oti.minipm.vo.ProjectVO;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjectService {
	
	private final ProjectMapper projectMapper;
    private final MemberMapper memberMapper;
    private final NotificationService notificationService;
	
    @Transactional
    public Long createProject(ProjectCreateDTO projectCreateDTO, Long memberId) {

        if (memberId == null) {
            throw new IllegalStateException("로그인한 회원 정보를 찾을 수 없습니다.");
        }

        // PROJECT 테이블에 저장할 객체 생성
        ProjectVO projectVO = new ProjectVO();
        projectVO.setName(projectCreateDTO.getName());
        projectVO.setDescription(projectCreateDTO.getDescription());
        projectVO.setMemberId(memberId);

        int insertedProjectRowCount = projectMapper.insertProject(projectVO);

        if (insertedProjectRowCount != 1) {
            throw new IllegalStateException("프로젝트 등록에 실패했습니다.");
        }

        // 프로젝트 생성자를 ADMIN으로 등록
        ProjectMemberVO projectMemberVO = new ProjectMemberVO();

        projectMemberVO.setProjectId(projectVO.getProjectId());
        projectMemberVO.setMemberId(memberId);
        projectMemberVO.setRole("ADMIN");

        int insertedMemberRowCount = projectMapper.insertProjectMember(projectMemberVO);

        if (insertedMemberRowCount != 1) {
            throw new IllegalStateException("프로젝트 관리자 등록에 실패했습니다.");
        }
        
        return projectVO.getProjectId();
    }
    
    // 프로젝트 번호로 프로젝트 조회
    @Transactional(readOnly = true)
    public ProjectVO findProjectById(Long projectId) {

        return projectMapper.selectProjectById(projectId);
    }
    
    // 특정 회원의 프로젝트 참여 정보 조회
    @Transactional(readOnly = true)
    public ProjectMemberVO findProjectMember(Long projectId, Long memberId) {

        return projectMapper.selectProjectMember(projectId, memberId);
    }
    
    // 회원이 참여 중인 프로젝트 목록 조회
    @Transactional(readOnly = true)
    public List<ProjectVO> findProjectsByMemberId(Long memberId) {

        return projectMapper.selectProjectsByMemberId(memberId);
    }
    
    // 참여 권한을 검사한 뒤 프로젝트 상세 조회
    @Transactional(readOnly = true)
    public ProjectVO findProjectDetail(Long projectId, Long memberId) {

        ProjectVO projectVO = projectMapper.selectProjectById(projectId);

        if (projectVO == null) {
            throw new IllegalStateException("존재하지 않는 프로젝트입니다.");
        }

        ProjectMemberVO projectMemberVO = projectMapper.selectProjectMember(projectId, memberId);

        if (projectMemberVO == null) {
            throw new IllegalStateException("해당 프로젝트에 접근할 권한이 없습니다.");
        }

        return projectVO;
    }
    
    // 검색 조건에 맞는 참여 프로젝트를 페이지 단위로 조회
    @Transactional(readOnly = true)
    public PageResponseDTO<ProjectVO> findProjectsByMemberId(
            Long memberId,
            ProjectSearchDTO projectSearchDTO,
            PageRequestDTO pageRequestDTO) {

        if (memberId == null) {
            throw new IllegalStateException("로그인한 회원 정보를 찾을 수 없습니다.");
        }

        if (projectSearchDTO == null) {
            projectSearchDTO = new ProjectSearchDTO();
        }

        String keyword = projectSearchDTO.getKeyword();

        if (keyword != null) {
            keyword = keyword.trim();

            if (keyword.isEmpty()) {
                keyword = null;
            }

            projectSearchDTO.setKeyword(keyword);
        }

        List<ProjectVO> projects =
                projectMapper.selectProjectsByMemberIdWithPaging(
                        memberId,
                        projectSearchDTO,
                        pageRequestDTO
                );

        int total = projectMapper.countProjectsByMemberId(memberId, projectSearchDTO);

        return new PageResponseDTO<>(pageRequestDTO, projects, total);
    }
    
    // 멤버 목록 화면에 필요한 프로젝트 정보와 멤버 목록 반환
    @Transactional(readOnly = true)
    public ProjectMembersResponseDTO findProjectMembersPage(Long projectId, Long loginMemberId) {

        // 프로젝트를 조회하면서 접근 권한도 검사
        ProjectVO projectVO = findProjectDetail(projectId, loginMemberId);

        // 검사를 통과한 경우 멤버 목록 조회
        List<ProjectMemberDTO> members = projectMapper.selectProjectMembers(projectId);

        // 조회한 두 데이터를 하나의 응답 DTO로 묶어서 반환
        return new ProjectMembersResponseDTO(projectVO, members);
    }
    
    // ADMIN만 수정 화면의 기존 데이터를 조회할 수 있음
    @Transactional(readOnly = true)
    public ProjectUpdateDTO findProjectForUpdate(Long projectId, Long loginMemberId) {

        ProjectVO projectVO = findProjectAsAdmin(projectId, loginMemberId);

        // DB 조회 결과를 수정 폼에 사용할 DTO로 변환
        ProjectUpdateDTO projectUpdateDTO = new ProjectUpdateDTO();
        projectUpdateDTO.setName(projectVO.getName());
        projectUpdateDTO.setDescription(projectVO.getDescription());

        return projectUpdateDTO;
    }

    // ADMIN만 프로젝트 이름과 설명을 수정할 수 있음
    @Transactional
    public void updateProject(
            Long projectId,
            ProjectUpdateDTO projectUpdateDTO,
            Long loginMemberId) {

        // 저장 요청에서도 관리자 권한을 다시 확인
        ProjectVO projectVO = findProjectAsAdmin(projectId,loginMemberId);

        // 수정할 값만 변경
        projectVO.setName(projectUpdateDTO.getName());
        projectVO.setDescription(projectUpdateDTO.getDescription());

        int updatedRowCount = projectMapper.updateProject(projectVO);

        if (updatedRowCount != 1) {
            throw new IllegalStateException("프로젝트 수정에 실패했습니다.");
        }
    }
    
    // ADMIN만 미삭제 이슈가 없는 프로젝트를 논리 삭제
    @Transactional
    public void deleteProject(Long projectId, Long loginMemberId) {

        // 프로젝트 존재 여부와 ADMIN 권한 검사
        findProjectAsAdmin(projectId, loginMemberId);

        // 미삭제 이슈가 있는지 확인
        int issueCount = projectMapper.countIssuesByProjectId(projectId);

        if (issueCount > 0) {
            throw new IllegalStateException("삭제되지 않은 이슈가 있는 프로젝트는 삭제할 수 없습니다.");
        }

        // 참여 정보는 유지하고 프로젝트에 삭제 정보만 기록
        int updatedRowCount = projectMapper.softDeleteProject(projectId, loginMemberId);

        if (updatedRowCount != 1) {
            throw new IllegalStateException(
                    "프로젝트가 이미 삭제되었거나 이슈가 등록되어 삭제할 수 없습니다."
            );
        }
    }
    
 // 프로젝트 ADMIN이 가입된 회원을 MEMBER로 추가
    @Transactional
    public void addProjectMember(
            Long projectId,
            ProjectMemberAddDTO projectMemberAddDTO,
            Long loginMemberId) {

        // 프로젝트 정보와 요청자의 관리자 권한 확인
        ProjectVO projectVO = findProjectAsAdmin(projectId, loginMemberId);

        String email = projectMemberAddDTO.getEmail();

        if (email == null || email.trim().isEmpty()) {
            throw new IllegalStateException("추가할 회원의 이메일을 입력해주세요.");
        }

        if (email.length() > 255) {
            throw new IllegalStateException("이메일은 255자 이하여야 합니다.");
        }

        MemberVO memberVO = memberMapper.selectMemberByEmail(email);

        if (memberVO == null) {
            throw new IllegalStateException("해당 이메일로 가입된 회원이 없습니다.");
        }

        ProjectMemberVO existingMember =
                projectMapper.selectProjectMember(projectId, memberVO.getMemberId());

        if (existingMember != null) {
            throw new IllegalStateException("이미 프로젝트에 참여 중인 회원입니다.");
        }

        ProjectMemberVO projectMemberVO = new ProjectMemberVO();
        projectMemberVO.setProjectId(projectId);
        projectMemberVO.setMemberId(memberVO.getMemberId());
        projectMemberVO.setRole("MEMBER");

        try {
            int insertedRowCount = projectMapper.insertProjectMember(projectMemberVO);

            if (insertedRowCount != 1) {
                throw new IllegalStateException("프로젝트 멤버 추가에 실패했습니다.");
            }

        } catch (DuplicateKeyException e) {
            throw new IllegalStateException("이미 프로젝트에 참여 중인 회원입니다.", e);
        }

        notificationService.createNotification(
                memberVO.getMemberId(),
                "PROJECT_MEMBER_ADDED",
                "'" + projectVO.getName() + "' 프로젝트에 멤버로 추가되었습니다.",
                "/project/" + projectId
        );
    }
    
 // 프로젝트 ADMIN이 MEMBER 역할의 회원을 내보냄
    @Transactional
    public void removeProjectMember(Long projectId, Long targetMemberId, Long loginMemberId) {

        // 프로젝트 정보와 요청자의 관리자 권한 확인
        ProjectVO projectVO = findProjectAsAdmin(projectId, loginMemberId);

        // 삭제 대상 회원의 프로젝트 참여 정보 조회
        ProjectMemberVO targetProjectMember =
                projectMapper.selectProjectMember(projectId, targetMemberId);

        if (targetProjectMember == null) {
            throw new IllegalStateException("프로젝트에 참여 중이지 않은 회원입니다.");
        }

        if (!"MEMBER".equals(targetProjectMember.getRole())) {
            throw new IllegalStateException("프로젝트 관리자는 내보낼 수 없습니다.");
        }

        // 담당 중인 미완료 이슈가 있는지 확인
        int openIssueCount = projectMapper.countOpenIssuesByAssignee(projectId, targetMemberId);

        if (openIssueCount > 0) {
            throw new IllegalStateException(
                    "담당 중인 미완료 이슈가 있어 내보낼 수 없습니다. 먼저 담당자를 변경해주세요."
            );
        }

        int deletedRowCount =
                projectMapper.deleteProjectMember(projectId, targetMemberId, loginMemberId);

        if (deletedRowCount != 1) {
            throw new IllegalStateException(
                    "멤버 내보내기에 실패했습니다. 참여 여부나 권한을 확인해주세요."
            );
        }

        notificationService.createNotification(
                targetMemberId,
                "PROJECT_MEMBER_REMOVED",
                "'" + projectVO.getName() + "' 프로젝트에서 제외되었습니다.",
                "/project"
        );
    }

    // 프로젝트 존재 여부와 ADMIN 권한을 확인하고 프로젝트 반환
    private ProjectVO findProjectAsAdmin(Long projectId, Long loginMemberId) {

        ProjectVO projectVO = projectMapper.selectProjectById(projectId);

        if (projectVO == null) {
            throw new IllegalStateException("존재하지 않는 프로젝트입니다.");
        }

        ProjectMemberVO projectMemberVO =
                projectMapper.selectProjectMember(projectId, loginMemberId);

        if (projectMemberVO == null) {
            throw new IllegalStateException("해당 프로젝트에 접근할 권한이 없습니다.");
        }

        if (!"ADMIN".equals(projectMemberVO.getRole())) {
            throw new IllegalStateException("프로젝트 관리자만 수행할 수 있는 작업입니다.");
        }

        return projectVO;
    }
    
}
