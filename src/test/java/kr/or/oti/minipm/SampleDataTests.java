package kr.or.oti.minipm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import kr.or.oti.minipm.dto.IssueCommentCreateDTO;
import kr.or.oti.minipm.dto.IssueCreateDTO;
import kr.or.oti.minipm.dto.IssueStatusUpdateDTO;
import kr.or.oti.minipm.dto.MemberJoinDTO;
import kr.or.oti.minipm.dto.ProjectCreateDTO;
import kr.or.oti.minipm.dto.ProjectMemberAddDTO;
import kr.or.oti.minipm.service.IssueCommentService;
import kr.or.oti.minipm.service.IssueService;
import kr.or.oti.minipm.service.MemberService;
import kr.or.oti.minipm.service.ProjectService;
import kr.or.oti.minipm.vo.IssueVO;
import kr.or.oti.minipm.vo.MemberVO;
import kr.or.oti.minipm.vo.ProjectVO;

@SpringBootTest
class SampleDataTests {

    private static final int MEMBER_COUNT = 100;
    private static final int PROJECT_COUNT = 100;
    private static final int ISSUE_COUNT = 100;

    @Autowired
    private MemberService memberService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private IssueService issueService;

    @Autowired
    private IssueCommentService issueCommentService;

    @Test
    @Commit
    @Transactional
    void createSampleData() {

        List<MemberVO> members = createMembers();

        MemberVO projectAdmin = members.get(0);

        List<Long> projectIds =
                createProjects(projectAdmin);

        createIssues(
                members,
                projectIds,
                projectAdmin
        );

        System.out.println();
        System.out.println("========== 샘플 데이터 생성 완료 ==========");
        System.out.println("회원 수    : " + MEMBER_COUNT);
        System.out.println("프로젝트 수: " + PROJECT_COUNT);
        System.out.println("이슈 수    : " + ISSUE_COUNT);
        System.out.println("댓글 수    : " + ISSUE_COUNT);
        System.out.println();
        System.out.println("관리자 계정");
        System.out.println("아이디: test_member001");
        System.out.println("비밀번호: 1234");
        System.out.println();
        System.out.println("일반 회원 계정 예시");
        System.out.println("아이디: test_member002");
        System.out.println("비밀번호: 1234");
        System.out.println("=========================================");
    }

    private List<MemberVO> createMembers() {

        List<MemberVO> members = new ArrayList<>();

        for (int number = 1; number <= MEMBER_COUNT; number++) {

            String loginId =
                    String.format("test_member%03d", number);

            String name =
                    String.format("테스트 회원 %03d", number);

            String email =
                    String.format(
                            "test-member%03d@example.com",
                            number
                    );

            MemberVO memberVO =
                    findOrCreateMember(
                            loginId,
                            "1234",
                            name,
                            email
                    );

            members.add(memberVO);
        }

        return members;
    }

    private List<Long> createProjects(MemberVO projectAdmin) {

        List<Long> projectIds = new ArrayList<>();

        List<ProjectVO> existingProjects =
                projectService.findProjectsByMemberId(
                        projectAdmin.getMemberId()
                );

        Map<String, Long> existingProjectIds =
                new HashMap<>();

        for (ProjectVO projectVO : existingProjects) {
            existingProjectIds.put(
                    projectVO.getName(),
                    projectVO.getProjectId()
            );
        }

        for (int number = 1; number <= PROJECT_COUNT; number++) {

            String projectName =
                    String.format("프로젝트 %03d", number);

            Long projectId =
                    existingProjectIds.get(projectName);

            if (projectId == null) {

                ProjectCreateDTO projectCreateDTO =
                        new ProjectCreateDTO();

                projectCreateDTO.setName(projectName);
                projectCreateDTO.setDescription(
                        String.format(
                                "화면 테스트를 위한 %03d번째 샘플 프로젝트입니다.",
                                number
                        )
                );

                projectId =
                        projectService.createProject(
                                projectCreateDTO,
                                projectAdmin.getMemberId()
                        );
            }

            projectIds.add(projectId);
        }

        return projectIds;
    }

    private void createIssues(
            List<MemberVO> members,
            List<Long> projectIds,
            MemberVO projectAdmin) {

        String[] issueTypes = {
                "FEATURE",
                "BUG",
                "TASK",
                "IMPROVEMENT"
        };

        String[] priorities = {
                "HIGH",
                "MEDIUM",
                "LOW"
        };

        String[] issueSubjects = {
                "로그인 기능 구현",
                "화면 표시 오류 수정",
                "검색 기능 추가",
                "프로젝트 목록 개선",
                "이슈 상세 화면 구현",
                "댓글 기능 점검",
                "입력값 검증 처리",
                "담당자 선택 기능 개선",
                "상태 변경 기능 확인",
                "사용자 화면 개선"
        };

        for (int number = 1; number <= ISSUE_COUNT; number++) {

            int projectIndex =
                    (number - 1) % projectIds.size();

            int reporterIndex =
                    number % members.size();

            int assigneeIndex =
                    (number + 17) % members.size();

            int commenterIndex =
                    (number + 37) % members.size();

            Long projectId =
                    projectIds.get(projectIndex);

            MemberVO reporter =
                    members.get(reporterIndex);

            MemberVO assignee =
                    members.get(assigneeIndex);

            MemberVO commenter =
                    members.get(commenterIndex);

            addProjectMember(
                    projectId,
                    projectAdmin,
                    reporter
            );

            addProjectMember(
                    projectId,
                    projectAdmin,
                    assignee
            );

            addProjectMember(
                    projectId,
                    projectAdmin,
                    commenter
            );

            String issueTitle =
                    String.format(
                            "샘플 이슈 %03d - %s",
                            number,
                            issueSubjects[
                                    (number - 1)
                                    % issueSubjects.length
                            ]
                    );

            Long existingIssueId =
                    findIssueIdByTitle(
                            projectId,
                            projectAdmin.getMemberId(),
                            issueTitle
                    );

            if (existingIssueId != null) {
                continue;
            }

            String issueType =
                    issueTypes[
                            (number - 1)
                            % issueTypes.length
                    ];

            String priority =
                    priorities[
                            (number - 1)
                            % priorities.length
                    ];

            Long issueId =
                    createIssue(
                            projectId,
                            reporter,
                            assignee,
                            issueTitle,
                            String.format(
                                    "%03d번째 화면 테스트용 이슈 내용입니다.",
                                    number
                            ),
                            issueType,
                            priority
                    );

            changeIssueStatus(
                    number,
                    projectId,
                    issueId,
                    assignee
            );

            createComment(
                    projectId,
                    issueId,
                    commenter,
                    String.format(
                            "%03d번째 화면 테스트용 댓글입니다.",
                            number
                    )
            );
        }
    }

    private MemberVO findOrCreateMember(
            String loginId,
            String password,
            String name,
            String email) {

        MemberVO memberVO =
                memberService.findByLoginId(loginId);

        if (memberVO != null) {
            return memberVO;
        }

        MemberJoinDTO memberJoinDTO =
                new MemberJoinDTO();

        memberJoinDTO.setLoginId(loginId);
        memberJoinDTO.setPassword(password);
        memberJoinDTO.setName(name);
        memberJoinDTO.setEmail(email);

        memberService.memberJoin(memberJoinDTO);

        return memberService.findByLoginId(loginId);
    }

    private void addProjectMember(
            Long projectId,
            MemberVO projectAdmin,
            MemberVO targetMember) {

        if (projectService.findProjectMember(
                projectId,
                targetMember.getMemberId()) != null) {
            return;
        }

        ProjectMemberAddDTO projectMemberAddDTO =
                new ProjectMemberAddDTO();

        projectMemberAddDTO.setEmail(
                targetMember.getEmail()
        );

        projectService.addProjectMember(
                projectId,
                projectMemberAddDTO,
                projectAdmin.getMemberId()
        );
    }

    private Long findIssueIdByTitle(
            Long projectId,
            Long memberId,
            String issueTitle) {

        List<IssueVO> issues =
                issueService.findIssuesByProjectId(
                        projectId,
                        memberId
                );

        for (IssueVO issueVO : issues) {
            if (issueTitle.equals(issueVO.getTitle())) {
                return issueVO.getIssueId();
            }
        }

        return null;
    }

    private Long createIssue(
            Long projectId,
            MemberVO reporter,
            MemberVO assignee,
            String title,
            String content,
            String issueType,
            String priority) {

        IssueCreateDTO issueCreateDTO =
                new IssueCreateDTO();

        issueCreateDTO.setTitle(title);
        issueCreateDTO.setContent(content);
        issueCreateDTO.setIssueType(issueType);
        issueCreateDTO.setPriority(priority);
        issueCreateDTO.setAssigneeId(
                assignee.getMemberId()
        );

        return issueService.createIssue(
                projectId,
                issueCreateDTO,
                reporter.getMemberId()
        );
    }

    private void changeIssueStatus(
            int number,
            Long projectId,
            Long issueId,
            MemberVO assignee) {

        int statusNumber = number % 3;

        if (statusNumber == 0) {
            return;
        }

        IssueStatusUpdateDTO progressDTO =
                new IssueStatusUpdateDTO();

        progressDTO.setStatus("IN_PROGRESS");

        issueService.updateIssueStatus(
                projectId,
                issueId,
                progressDTO,
                assignee.getMemberId()
        );

        if (statusNumber == 1) {
            return;
        }

        IssueStatusUpdateDTO doneDTO =
                new IssueStatusUpdateDTO();

        doneDTO.setStatus("DONE");
        doneDTO.setResolution(
                String.format(
                        "%03d번째 샘플 이슈를 정상적으로 처리했습니다.",
                        number
                )
        );

        issueService.updateIssueStatus(
                projectId,
                issueId,
                doneDTO,
                assignee.getMemberId()
        );
    }

    private void createComment(
            Long projectId,
            Long issueId,
            MemberVO commenter,
            String content) {

        IssueCommentCreateDTO issueCommentCreateDTO =
                new IssueCommentCreateDTO();

        issueCommentCreateDTO.setContent(content);

        issueCommentService.createComment(
                projectId,
                issueId,
                issueCommentCreateDTO,
                commenter.getMemberId()
        );
    }
}