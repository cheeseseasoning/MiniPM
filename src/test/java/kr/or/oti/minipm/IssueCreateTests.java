package kr.or.oti.minipm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import kr.or.oti.minipm.dto.IssueCreateDTO;
import kr.or.oti.minipm.dto.MemberJoinDTO;
import kr.or.oti.minipm.dto.ProjectCreateDTO;
import kr.or.oti.minipm.service.IssueService;
import kr.or.oti.minipm.service.MemberService;
import kr.or.oti.minipm.service.ProjectService;
import kr.or.oti.minipm.vo.IssueVO;
import kr.or.oti.minipm.vo.MemberVO;

@SpringBootTest
public class IssueCreateTests {

    @Autowired
    private MemberService memberService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private IssueService issueService;

    @Test
    @Transactional
    void createIssueTest() {

        // 이슈를 작성할 테스트 회원 준비
        MemberJoinDTO memberJoinDTO = new MemberJoinDTO();
        memberJoinDTO.setLoginId("issue_creator_test");
        memberJoinDTO.setPassword("1234");
        memberJoinDTO.setName("이슈 작성자");
        memberJoinDTO.setEmail("issue_creator@test.com");

        Long memberId = memberService.memberJoin(memberJoinDTO);
        MemberVO loginMember = memberService.findByLoginId("issue_creator_test");

        assertNotNull(memberId);
        assertNotNull(loginMember);
        assertEquals(memberId, loginMember.getMemberId());

        // 테스트 프로젝트 생성
        ProjectCreateDTO projectCreateDTO = new ProjectCreateDTO();
        projectCreateDTO.setName("이슈 테스트 프로젝트");
        projectCreateDTO.setDescription("이슈 등록을 테스트하는 프로젝트입니다.");

        Long projectId = projectService.createProject(projectCreateDTO, loginMember.getMemberId());

        assertNotNull(projectId);

        // 이슈 생성 요청 데이터 준비
        IssueCreateDTO issueCreateDTO = new IssueCreateDTO();
        issueCreateDTO.setTitle("로그인 기능 오류");
        issueCreateDTO.setContent("올바른 비밀번호를 입력해도 로그인이 되지 않습니다.");
        issueCreateDTO.setIssueType("BUG");
        issueCreateDTO.setPriority("HIGH");
        issueCreateDTO.setAssigneeId(null);

        // 이슈 등록
        Long issueId = issueService.createIssue(
                projectId,
                issueCreateDTO,
                loginMember.getMemberId()
        );

        // 등록한 이슈 상세 조회
        IssueVO foundIssue = issueService.findIssueDetail(
                projectId,
                issueId,
                loginMember.getMemberId()
        );

        // 프로젝트의 전체 이슈 목록 조회
        List<IssueVO> issues = issueService.findIssuesByProjectId(
                projectId,
                loginMember.getMemberId()
        );

        // 이슈 등록 결과 검증
        assertNotNull(issueId);
        assertNotNull(foundIssue);
        assertEquals(issueId, foundIssue.getIssueId());
        assertEquals(projectId, foundIssue.getProjectId());
        assertEquals("로그인 기능 오류", foundIssue.getTitle());
        assertEquals("올바른 비밀번호를 입력해도 로그인이 되지 않습니다.", foundIssue.getContent());
        assertEquals("BUG", foundIssue.getIssueType());
        assertEquals("HIGH", foundIssue.getPriority());
        assertEquals("TODO", foundIssue.getStatus());
        assertEquals(loginMember.getMemberId(), foundIssue.getReporterId());
        assertNull(foundIssue.getAssigneeId());
        assertNull(foundIssue.getResolution());
        assertNull(foundIssue.getResolvedBy());
        assertNull(foundIssue.getResolvedAt());
        assertNotNull(foundIssue.getCreatedAt());
        assertNotNull(foundIssue.getUpdatedAt());

        // 이슈 목록 조회 결과 검증
        assertNotNull(issues);
        assertEquals(1, issues.size());
        assertEquals(issueId, issues.get(0).getIssueId());

        // 확인용 출력
        System.out.println("생성된 회원번호: " + memberId);
        System.out.println("생성된 프로젝트 번호: " + projectId);
        System.out.println("생성된 이슈 번호: " + issueId);
        System.out.println("이슈 제목: " + foundIssue.getTitle());
        System.out.println("이슈 유형: " + foundIssue.getIssueType());
        System.out.println("이슈 우선순위: " + foundIssue.getPriority());
        System.out.println("이슈 상태: " + foundIssue.getStatus());
        System.out.println("이슈 작성자 번호: " + foundIssue.getReporterId());
        System.out.println("이슈 담당자 번호: " + foundIssue.getAssigneeId());
        System.out.println("프로젝트 이슈 개수: " + issues.size());
        System.out.println("이슈 생성일: " + foundIssue.getCreatedAt());
    }
}