package kr.or.oti.minipm.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import kr.or.oti.minipm.dto.IssueCommentCreateDTO;
import kr.or.oti.minipm.dto.IssueCommentUpdateDTO;
import kr.or.oti.minipm.dto.IssueCreateDTO;
import kr.or.oti.minipm.dto.IssueDetailResponseDTO;
import kr.or.oti.minipm.dto.IssueSearchDTO;
import kr.or.oti.minipm.dto.IssueStatusUpdateDTO;
import kr.or.oti.minipm.dto.IssueUpdateDTO;
import kr.or.oti.minipm.dto.PageRequestDTO;
import kr.or.oti.minipm.dto.PageResponseDTO;
import kr.or.oti.minipm.dto.ProjectMembersResponseDTO;
import kr.or.oti.minipm.security.LoginMemberDetails;
import kr.or.oti.minipm.service.IssueCommentService;
import kr.or.oti.minipm.service.IssueService;
import kr.or.oti.minipm.service.ProjectService;
import kr.or.oti.minipm.vo.IssueVO;
import kr.or.oti.minipm.vo.ProjectMemberVO;
import kr.or.oti.minipm.vo.ProjectVO;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/project/{projectId}/issues")
@RequiredArgsConstructor
public class IssueController {
	
	private final IssueService issueService;
    private final ProjectService projectService;
    private final IssueCommentService issueCommentService;

    // 프로젝트 이슈 검색 및 페이징 목록
    @GetMapping
    public String issueList(
            @PathVariable("projectId") Long projectId,
            @Valid
            @ModelAttribute("issueSearchDTO")
            IssueSearchDTO issueSearchDTO,
            BindingResult searchBindingResult,
            @Valid
            @ModelAttribute("pageRequestDTO")
            PageRequestDTO pageRequestDTO,
            BindingResult pageBindingResult,
            @AuthenticationPrincipal LoginMemberDetails loginMember,
            Model model) {

        IssueSearchDTO searchCondition = issueSearchDTO;
        PageRequestDTO pageCondition = pageRequestDTO;

        // 잘못된 검색 조건은 SQL에 전달하지 않고 전체 조건으로 조회
        if (searchBindingResult.hasErrors()) {
            searchCondition = new IssueSearchDTO();
        }

        // 잘못된 페이지 값은 기본값인 1페이지, 10개로 조회
        if (pageBindingResult.hasErrors()) {
            pageCondition = new PageRequestDTO();
        }

        PageResponseDTO<IssueVO> pageResponse =
                issueService.findIssuesByProjectId(
                        projectId,
                        loginMember.getMemberId(),
                        searchCondition,
                        pageCondition
                );

        ProjectMembersResponseDTO projectResponse =
                projectService.findProjectMembersPage(projectId, loginMember.getMemberId());

        model.addAttribute("project", projectResponse.getProject());

        model.addAttribute("members", projectResponse.getMembers());

        model.addAttribute("issues", pageResponse.getItems());

        model.addAttribute("pageResponse", pageResponse);

        return "issue/list";
    }

    // 이슈 등록 화면
    @GetMapping("/create")
    public String issueCreateForm(
            @PathVariable("projectId") Long projectId,
            @AuthenticationPrincipal LoginMemberDetails loginMember,
            Model model) {

        ProjectMembersResponseDTO response =
                projectService.findProjectMembersPage(projectId, loginMember.getMemberId());

        model.addAttribute("project", response.getProject());
        model.addAttribute("members", response.getMembers());
        model.addAttribute("issueCreateDTO", new IssueCreateDTO());

        return "issue/create";
    }

    // 이슈 등록 처리
    @PostMapping("/create")
    public String createIssue(
            @PathVariable("projectId") Long projectId,
            @Valid
            @ModelAttribute("issueCreateDTO")
            IssueCreateDTO issueCreateDTO,
            BindingResult bindingResult,
            @AuthenticationPrincipal LoginMemberDetails loginMember,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            ProjectMembersResponseDTO response =
                    projectService.findProjectMembersPage(projectId, loginMember.getMemberId());

            model.addAttribute("project", response.getProject());
            model.addAttribute("members", response.getMembers());

            return "issue/create";
        }

        Long issueId =
                issueService.createIssue(projectId, issueCreateDTO, loginMember.getMemberId());

        redirectAttributes.addFlashAttribute(
                "message",
                "이슈가 등록되었습니다. 이슈 번호: " + issueId
        );

        return "redirect:/project/" + projectId + "/issues";
    }

    // 이슈 상세, 댓글 목록, 상태 변경 이력 조회
    @GetMapping("/{issueId}")
    public String issueDetail(
            @PathVariable("projectId") Long projectId,
            @PathVariable("issueId") Long issueId,
            @AuthenticationPrincipal LoginMemberDetails loginMember,
            Model model) {

        IssueDetailResponseDTO issueDetailResponseDTO =
                issueCommentService.findIssueDetailWithComments(
                        projectId,
                        issueId,
                        loginMember.getMemberId()
                );

        IssueVO issueVO = issueDetailResponseDTO.getIssue();

        ProjectVO projectVO = projectService.findProjectById(projectId);

        model.addAttribute("project", projectVO);
        model.addAttribute("issue", issueVO);
        model.addAttribute("comments", issueDetailResponseDTO.getComments());
        model.addAttribute("statusHistories", issueDetailResponseDTO.getStatusHistories());
        model.addAttribute("loginMemberId", loginMember.getMemberId());

        // 버튼 표시용 권한이며, 실제 변경 권한은 Service에서 다시 검사
        ProjectMemberVO projectMemberVO = projectService.findProjectMember(projectId, loginMember.getMemberId());

        boolean isProjectAdmin = projectMemberVO != null
                && "ADMIN".equals(projectMemberVO.getRole());

        boolean canDelete = projectMemberVO != null
                && (loginMember.getMemberId().equals(issueVO.getReporterId())
                    || isProjectAdmin);

        model.addAttribute("canDelete", canDelete);
        model.addAttribute("isProjectAdmin", isProjectAdmin);

        // 처음 상세 화면에 들어올 때만 상태 변경 폼 초기화
        if (!model.containsAttribute("issueStatusUpdateDTO")) {
            IssueStatusUpdateDTO issueStatusUpdateDTO = new IssueStatusUpdateDTO();
            issueStatusUpdateDTO.setStatus(issueVO.getStatus());
            issueStatusUpdateDTO.setResolution(issueVO.getResolution());

            model.addAttribute("issueStatusUpdateDTO", issueStatusUpdateDTO);
        }

        // 처음 상세 화면에 들어올 때만 댓글 등록 폼 초기화
        if (!model.containsAttribute("issueCommentCreateDTO")) {
            model.addAttribute("issueCommentCreateDTO", new IssueCommentCreateDTO());
        }

        return "issue/detail";
    }
    
    // 이슈 논리 삭제
    @PostMapping("/{issueId}/delete")
    public String deleteIssue(
            @PathVariable("projectId") Long projectId,
            @PathVariable("issueId") Long issueId,
            @AuthenticationPrincipal LoginMemberDetails loginMember,
            RedirectAttributes redirectAttributes) {

        try {
            issueService.deleteIssue(projectId, issueId, loginMember.getMemberId());

            redirectAttributes.addFlashAttribute("message", "이슈가 삭제되었습니다.");

            return "redirect:/project/" + projectId + "/issues";

        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

            return "redirect:/project";
        }
    }

    // 이슈 수정 화면
    @GetMapping("/{issueId}/edit")
    public String issueEditForm(
            @PathVariable("projectId") Long projectId,
            @PathVariable("issueId") Long issueId,
            @AuthenticationPrincipal LoginMemberDetails loginMember,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            // 참여 권한과 이슈 존재 여부를 검사한 뒤 기존 값 조회
            IssueUpdateDTO issueUpdateDTO =
                    issueService.findIssueForUpdate(projectId, issueId, loginMember.getMemberId());

            // 담당자 선택란에 표시할 프로젝트 멤버 목록 조회
            ProjectMembersResponseDTO response =
                    projectService.findProjectMembersPage(projectId, loginMember.getMemberId());

            model.addAttribute("projectId", projectId);
            model.addAttribute("issueId", issueId);
            model.addAttribute("members", response.getMembers());
            model.addAttribute("issueUpdateDTO", issueUpdateDTO);

            return "issue/edit";

        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

            return "redirect:/project";
        }
    }

    // 이슈 수정 저장
    @PostMapping("/{issueId}/edit")
    public String updateIssue(
            @PathVariable("projectId") Long projectId,
            @PathVariable("issueId") Long issueId,
            @Valid
            @ModelAttribute("issueUpdateDTO")
            IssueUpdateDTO issueUpdateDTO,
            BindingResult bindingResult,
            @AuthenticationPrincipal LoginMemberDetails loginMember,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            if (bindingResult.hasErrors()) {
                // 오류 화면을 보여주기 전에도 접근 권한과 이슈 존재 여부 확인
                issueService.findIssueForUpdate(projectId, issueId, loginMember.getMemberId());

                // 수정 화면에서 사용할 프로젝트 멤버 목록 다시 조회
                ProjectMembersResponseDTO response =
                        projectService.findProjectMembersPage(projectId, loginMember.getMemberId());

                model.addAttribute("projectId", projectId);
                model.addAttribute("issueId", issueId);
                model.addAttribute("members", response.getMembers());

                // 입력한 DTO와 검증 오류는 그대로 유지
                return "issue/edit";
            }

            issueService.updateIssue(projectId, issueId, issueUpdateDTO, loginMember.getMemberId());

            redirectAttributes.addFlashAttribute("message", "이슈가 수정되었습니다.");

            return "redirect:/project/" + projectId + "/issues/" + issueId;

        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

            return "redirect:/project";
        }
    }

    // 이슈 상태 변경
    @PostMapping("/{issueId}/status")
    public String updateIssueStatus(
            @PathVariable("projectId") Long projectId,
            @PathVariable("issueId") Long issueId,
            @Valid
            @ModelAttribute("issueStatusUpdateDTO")
            IssueStatusUpdateDTO issueStatusUpdateDTO,
            BindingResult bindingResult,
            @AuthenticationPrincipal LoginMemberDetails loginMember,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            // 오류 화면을 표시하기 전에도 참여 권한과 이슈 존재 여부 확인
            IssueVO issueVO = issueService.findIssueDetail(projectId, issueId, loginMember.getMemberId());

            String newStatus = issueStatusUpdateDTO.getStatus();
            String resolution = issueStatusUpdateDTO.getResolution();

            // 같은 상태를 선택한 경우
            if (!bindingResult.hasFieldErrors("status") && issueVO.getStatus().equals(newStatus)) {
                bindingResult.rejectValue("status", "sameStatus", "현재 상태와 다른 상태를 선택해주세요.");
            }

            // 완료 처리 시 해결 내용 필수
            if ("DONE".equals(newStatus) && (resolution == null || resolution.trim().isEmpty())) {
                bindingResult.rejectValue("resolution", "required", "완료 처리하려면 해결 내용을 입력해주세요.");
            }

            if (bindingResult.hasErrors()) {
                // 상세 화면 데이터를 다시 준비하되 입력한 DTO와 오류는 유지
                return issueDetail(projectId, issueId, loginMember, model);
            }

            issueService.updateIssueStatus(
                    projectId,
                    issueId,
                    issueStatusUpdateDTO,
                    loginMember.getMemberId()
            );

            redirectAttributes.addFlashAttribute("message", "이슈 상태가 변경되었습니다.");

            return "redirect:/project/" + projectId + "/issues/" + issueId;

        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

            return "redirect:/project";
        }
    }
    
    // 댓글 등록 처리
    @PostMapping("/{issueId}/comments")
    public String createComment(
            @PathVariable("projectId") Long projectId,
            @PathVariable("issueId") Long issueId,
            @Valid
            @ModelAttribute("issueCommentCreateDTO")
            IssueCommentCreateDTO issueCommentCreateDTO,
            BindingResult bindingResult,
            @AuthenticationPrincipal LoginMemberDetails loginMember,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            if (bindingResult.hasErrors()) {
                // 접근 권한을 확인하고 상세 화면 데이터를 다시 준비
                // 입력한 댓글과 검증 오류는 그대로 유지
                return issueDetail(projectId, issueId, loginMember, model);
            }

            issueCommentService.createComment(
                    projectId,
                    issueId,
                    issueCommentCreateDTO,
                    loginMember.getMemberId()
            );

            redirectAttributes.addFlashAttribute("message", "댓글이 등록되었습니다.");

            return "redirect:/project/" + projectId + "/issues/" + issueId;

        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

            return "redirect:/project";
        }
    }
    
    // 댓글 수정 화면
    @GetMapping("/{issueId}/comments/{commentId}/edit")
    public String commentEditForm(
            @PathVariable("projectId") Long projectId,
            @PathVariable("issueId") Long issueId,
            @PathVariable("commentId") Long commentId,
            @AuthenticationPrincipal LoginMemberDetails loginMember,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            IssueCommentUpdateDTO issueCommentUpdateDTO =
                    issueCommentService.findCommentForUpdate(
                            projectId,
                            issueId,
                            commentId,
                            loginMember.getMemberId()
                    );

            model.addAttribute("projectId", projectId);
            model.addAttribute("issueId", issueId);
            model.addAttribute("commentId", commentId);
            model.addAttribute("issueCommentUpdateDTO", issueCommentUpdateDTO);

            return "issue/comment-edit";

        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

            return "redirect:/project";
        }
    }
    
    // 댓글 수정 저장
    @PostMapping("/{issueId}/comments/{commentId}/edit")
    public String updateComment(
            @PathVariable("projectId") Long projectId,
            @PathVariable("issueId") Long issueId,
            @PathVariable("commentId") Long commentId,
            @Valid
            @ModelAttribute("issueCommentUpdateDTO")
            IssueCommentUpdateDTO issueCommentUpdateDTO,
            BindingResult bindingResult,
            @AuthenticationPrincipal LoginMemberDetails loginMember,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            if (bindingResult.hasErrors()) {
                // 오류 화면을 보여주기 전에도 수정 권한 확인
                issueCommentService.findCommentForUpdate(
                        projectId,
                        issueId,
                        commentId,
                        loginMember.getMemberId()
                );

                model.addAttribute("projectId", projectId);
                model.addAttribute("issueId", issueId);
                model.addAttribute("commentId", commentId);

                // 입력한 DTO와 검증 오류는 그대로 유지
                return "issue/comment-edit";
            }

            issueCommentService.updateComment(
                    projectId,
                    issueId,
                    commentId,
                    issueCommentUpdateDTO,
                    loginMember.getMemberId()
            );

            redirectAttributes.addFlashAttribute("message", "댓글이 수정되었습니다.");

            return "redirect:/project/" + projectId + "/issues/" + issueId;

        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

            return "redirect:/project";
        }
    }
    
    // 댓글 논리 삭제
    @PostMapping("/{issueId}/comments/{commentId}/delete")
    public String deleteComment(
            @PathVariable("projectId") Long projectId,
            @PathVariable("issueId") Long issueId,
            @PathVariable("commentId") Long commentId,
            @AuthenticationPrincipal LoginMemberDetails loginMember,
            RedirectAttributes redirectAttributes) {

        try {
            issueCommentService.deleteComment(
                    projectId,
                    issueId,
                    commentId,
                    loginMember.getMemberId()
            );

            redirectAttributes.addFlashAttribute("message", "댓글이 삭제되었습니다.");

            return "redirect:/project/" + projectId + "/issues/" + issueId;

        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

            return "redirect:/project";
        }
    }
}
