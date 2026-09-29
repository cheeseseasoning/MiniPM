package kr.or.oti.minipm.controller;

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

import kr.or.oti.minipm.dto.PageRequestDTO;
import kr.or.oti.minipm.dto.PageResponseDTO;
import kr.or.oti.minipm.dto.ProjectCreateDTO;
import kr.or.oti.minipm.dto.ProjectMemberAddDTO;
import kr.or.oti.minipm.dto.ProjectMembersResponseDTO;
import kr.or.oti.minipm.dto.ProjectSearchDTO;
import kr.or.oti.minipm.dto.ProjectUpdateDTO;
import kr.or.oti.minipm.security.LoginMemberDetails;
import kr.or.oti.minipm.service.ProjectService;
import kr.or.oti.minipm.vo.ProjectMemberVO;
import kr.or.oti.minipm.vo.ProjectVO;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/project")
@RequiredArgsConstructor
public class ProjectController {
	
	private final ProjectService projectService;
	
    // 프로젝트 검색 및 페이징 목록
    @GetMapping
    public String projectMain(
            @Valid
            @ModelAttribute("projectSearchDTO")
            ProjectSearchDTO projectSearchDTO,
            BindingResult searchBindingResult,
            @Valid
            @ModelAttribute("pageRequestDTO")
            PageRequestDTO pageRequestDTO,
            BindingResult pageBindingResult,
            @AuthenticationPrincipal LoginMemberDetails loginMember,
            Model model) {

        ProjectSearchDTO searchCondition = projectSearchDTO;

        PageRequestDTO pageCondition = pageRequestDTO;

        // 잘못된 검색 조건은 SQL에 전달하지 않고 전체 조건으로 조회
        if (searchBindingResult.hasErrors()) {
            searchCondition = new ProjectSearchDTO();
        }

        // 잘못된 페이지 값은 기본값인 1페이지, 10개로 조회
        if (pageBindingResult.hasErrors()) {
            pageCondition = new PageRequestDTO();
        }

        PageResponseDTO<ProjectVO> pageResponse =
                projectService.findProjectsByMemberId(
                        loginMember.getMemberId(),
                        searchCondition,
                        pageCondition
                );

        model.addAttribute("projects", pageResponse.getItems());

        model.addAttribute("pageResponse", pageResponse);

        return "project/main";
    }

    // 프로젝트 생성 화면
    @GetMapping("/create")
    public String projectCreateForm(Model model) {

        model.addAttribute("projectCreateDTO", new ProjectCreateDTO());

        return "project/create";
    }

    // 프로젝트 생성 요청 처리
    @PostMapping("/create")
    public String createProject(
            @Valid
            @ModelAttribute
            ProjectCreateDTO projectCreateDTO,
            BindingResult bindingResult,
            @AuthenticationPrincipal
            LoginMemberDetails loginMember,
            RedirectAttributes redirectAttributes) {

        // 프로젝트 이름 등의 입력값 검증 실패
        if (bindingResult.hasErrors()) {
            return "project/create";
        }

        Long projectId = projectService.createProject(projectCreateDTO, loginMember.getMemberId());

        redirectAttributes.addFlashAttribute(
        		"message",
                "프로젝트가 생성되었습니다. 프로젝트 번호: " + projectId);

        return "redirect:/project";
    }
    
 // 프로젝트 상세 화면
    @GetMapping("/{projectId}")
    public String projectDetail(
            @PathVariable("projectId") Long projectId,
            @AuthenticationPrincipal LoginMemberDetails loginMember,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            ProjectVO projectVO = 
            		projectService.findProjectDetail(projectId, loginMember.getMemberId());

            // 로그인 회원의 프로젝트 내 역할 조회
            ProjectMemberVO projectMemberVO = 
                    projectService.findProjectMember(projectId, loginMember.getMemberId());

            // 해당 프로젝트의 ADMIN인지 확인
            boolean isAdmin =
                    projectMemberVO != null
                    && "ADMIN".equals(projectMemberVO.getRole());

            model.addAttribute("project", projectVO);
            model.addAttribute("isAdmin", isAdmin);

            return "project/detail";

        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

            return "redirect:/project";
        }
    }
    
    // 프로젝트 멤버 목록 및 멤버 추가 폼
    @GetMapping("/{projectId}/members")
    public String projectMembers(
            @PathVariable("projectId") Long projectId,
            @AuthenticationPrincipal LoginMemberDetails loginMember,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            ProjectMembersResponseDTO response =
                    projectService.findProjectMembersPage(
                            projectId,
                            loginMember.getMemberId()
                    );

            ProjectMemberVO loginProjectMember =
                    projectService.findProjectMember(
                            projectId,
                            loginMember.getMemberId()
                    );

            boolean isAdmin = loginProjectMember != null
                    && "ADMIN".equals(loginProjectMember.getRole());

            model.addAttribute("project", response.getProject());
            model.addAttribute("members", response.getMembers());
            model.addAttribute("isAdmin", isAdmin);

            // 처음 화면에 들어왔을 때만 빈 입력 DTO 생성
            if (!model.containsAttribute("projectMemberAddDTO")) {
                model.addAttribute("projectMemberAddDTO", new ProjectMemberAddDTO());
            }

            return "project/members";

        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

            return "redirect:/project";
        }
    }
    
    // 프로젝트 멤버 추가 처리
    @PostMapping("/{projectId}/members/add")
    public String addProjectMember(
            @PathVariable("projectId") Long projectId,
            @Valid
            @ModelAttribute("projectMemberAddDTO")
            ProjectMemberAddDTO projectMemberAddDTO,
            BindingResult bindingResult,
            @AuthenticationPrincipal LoginMemberDetails loginMember,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            if (bindingResult.hasErrors()) {
                // 입력한 이메일과 검증 오류를 유지하면서 목록 화면 다시 표시
                return projectMembers(projectId, loginMember, model, redirectAttributes);
            }

            projectService.addProjectMember(
                    projectId,
                    projectMemberAddDTO,
                    loginMember.getMemberId()
            );

            redirectAttributes.addFlashAttribute("message", "프로젝트 멤버가 추가되었습니다.");

            return "redirect:/project/" + projectId + "/members";

        } catch (IllegalStateException e) {
            bindingResult.rejectValue("email", "memberAddFailed", e.getMessage());

            return projectMembers(projectId, loginMember, model, redirectAttributes);
        }
    }
    
    // 프로젝트 멤버 내보내기
    @PostMapping("/{projectId}/members/{targetMemberId}/remove")
    public String removeProjectMember(
            @PathVariable("projectId") Long projectId,
            @PathVariable("targetMemberId") Long targetMemberId,
            @AuthenticationPrincipal LoginMemberDetails loginMember,
            RedirectAttributes redirectAttributes) {

        try {
            projectService.removeProjectMember(
                    projectId,
                    targetMemberId,
                    loginMember.getMemberId()
            );

            redirectAttributes.addFlashAttribute("message", "프로젝트 멤버를 내보냈습니다.");

        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/project/" + projectId + "/members";
    }
    
    // 프로젝트 수정 화면
    @GetMapping("/{projectId}/edit")
    public String projectEditForm(
            @PathVariable("projectId") Long projectId,
            @AuthenticationPrincipal LoginMemberDetails loginMember,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            // ADMIN 권한을 검사하고 기존 이름과 설명을 가져옴
            ProjectUpdateDTO projectUpdateDTO =
                    projectService.findProjectForUpdate(projectId, loginMember.getMemberId());

            model.addAttribute("projectId", projectId);
            model.addAttribute("projectUpdateDTO", projectUpdateDTO);

            return "project/edit";

        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

            return "redirect:/project";
        }
    }

    // 프로젝트 수정 저장
    @PostMapping("/{projectId}/edit")
    public String updateProject(
            @PathVariable("projectId") Long projectId,
            @Valid
            @ModelAttribute("projectUpdateDTO")
            ProjectUpdateDTO projectUpdateDTO,
            BindingResult bindingResult,
            @AuthenticationPrincipal LoginMemberDetails loginMember,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            // 입력값 검증에 실패하면 수정 화면을 다시 표시
            if (bindingResult.hasErrors()) {

                // 수정 화면을 다시 보여주기 전에도 ADMIN 권한 확인
                projectService.findProjectForUpdate(projectId, loginMember.getMemberId());

                model.addAttribute("projectId", projectId);

                // 사용자가 입력한 DTO와 오류 정보는 그대로 유지
                return "project/edit";
            }

            // ADMIN 권한을 검사한 뒤 DB 수정
            projectService.updateProject(projectId, projectUpdateDTO, loginMember.getMemberId());

            redirectAttributes.addFlashAttribute("message", "프로젝트가 수정되었습니다.");

            return "redirect:/project/" + projectId;

        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

            return "redirect:/project";
        }
    }
    
    // 프로젝트 삭제
    @PostMapping("/{projectId}/delete")
    public String deleteProject(
            @PathVariable("projectId") Long projectId,
            @AuthenticationPrincipal LoginMemberDetails loginMember,
            RedirectAttributes redirectAttributes) {

        try {
            projectService.deleteProject(projectId, loginMember.getMemberId());

            redirectAttributes.addFlashAttribute(
                    "message",
                    "프로젝트가 삭제되었습니다."
            );

        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/project";
    }
}
