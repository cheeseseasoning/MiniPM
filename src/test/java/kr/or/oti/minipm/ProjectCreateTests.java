package kr.or.oti.minipm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import kr.or.oti.minipm.dto.MemberJoinDTO;
import kr.or.oti.minipm.dto.ProjectCreateDTO;
import kr.or.oti.minipm.service.MemberService;
import kr.or.oti.minipm.service.ProjectService;
import kr.or.oti.minipm.vo.MemberVO;
import kr.or.oti.minipm.vo.ProjectMemberVO;
import kr.or.oti.minipm.vo.ProjectVO;

@SpringBootTest
public class ProjectCreateTests {

    @Autowired
    private MemberService memberService;

    @Autowired
    private ProjectService projectService;

    @Test
    @Transactional
    void createProjectTest() {

        // 프로젝트를 생성할 테스트 회원 준비
        MemberJoinDTO memberJoinDTO = new MemberJoinDTO();
        memberJoinDTO.setLoginId("project_creator_test");
        memberJoinDTO.setPassword("1234");
        memberJoinDTO.setName("프로젝트 생성자");
        memberJoinDTO.setEmail("project_creator@test.com");

        // 회원가입
        Long memberId = memberService.memberJoin(memberJoinDTO);

        // 등록된 회원 조회
        MemberVO loginMember = memberService.findByLoginId("project_creator_test");

        assertNotNull(memberId);
        assertNotNull(loginMember);
        assertEquals(memberId, loginMember.getMemberId());

        // 프로젝트 생성 요청 데이터 준비
        ProjectCreateDTO projectCreateDTO = new ProjectCreateDTO();
        projectCreateDTO.setName("테스트 프로젝트");
        projectCreateDTO.setDescription("프로젝트 생성 테스트입니다.");

        // 프로젝트 생성
        Long projectId = projectService.createProject(projectCreateDTO, loginMember.getMemberId());

        // 생성된 프로젝트 조회
        ProjectVO foundProject = projectService.findProjectById(projectId);

        // 생성자의 프로젝트 참여 정보 조회
        ProjectMemberVO foundProjectMember = projectService.findProjectMember(
                projectId,
                loginMember.getMemberId()
        );

        // 회원이 참여 중인 프로젝트 목록 조회
        List<ProjectVO> projects = projectService.findProjectsByMemberId(
                loginMember.getMemberId()
        );

        // 프로젝트 등록 결과 검증
        assertNotNull(projectId);
        assertNotNull(foundProject);
        assertEquals(projectId, foundProject.getProjectId());
        assertEquals("테스트 프로젝트", foundProject.getName());
        assertEquals("프로젝트 생성 테스트입니다.", foundProject.getDescription());
        assertEquals(loginMember.getMemberId(), foundProject.getMemberId());
        assertNotNull(foundProject.getCreatedAt());
        assertNotNull(foundProject.getUpdatedAt());

        // 프로젝트 참여 정보 검증
        assertNotNull(foundProjectMember);
        assertEquals(projectId, foundProjectMember.getProjectId());
        assertEquals(loginMember.getMemberId(), foundProjectMember.getMemberId());
        assertEquals("ADMIN", foundProjectMember.getRole());
        assertNotNull(foundProjectMember.getJoinedAt());

        // 프로젝트 목록 조회 결과 검증
        assertNotNull(projects);
        assertEquals(1, projects.size());

        boolean containsCreatedProject = projects.stream()
                .anyMatch(project -> projectId.equals(project.getProjectId()));

        assertTrue(containsCreatedProject);

        // 확인용 출력
        System.out.println("생성된 회원번호: " + memberId);
        System.out.println("생성된 프로젝트 번호: " + projectId);
        System.out.println("프로젝트 이름: " + foundProject.getName());
        System.out.println("프로젝트 설명: " + foundProject.getDescription());
        System.out.println("프로젝트 생성자 회원번호: " + foundProject.getMemberId());
        System.out.println("프로젝트 내 역할: " + foundProjectMember.getRole());
        System.out.println("프로젝트 생성일: " + foundProject.getCreatedAt());
        System.out.println("참여 프로젝트 개수: " + projects.size());
    }
}