package kr.or.oti.minipm.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import kr.or.oti.minipm.dto.ManagedDashboardSummaryDTO;
import kr.or.oti.minipm.dto.ManagedProjectSummaryDTO;
import kr.or.oti.minipm.dto.MyDashboardSummaryDTO;
import kr.or.oti.minipm.dto.MyProfileDTO;
import kr.or.oti.minipm.security.LoginMemberDetails;
import kr.or.oti.minipm.service.MemberService;
import kr.or.oti.minipm.service.MyPageService;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;  
    private final MemberService memberService;

    // 마이페이지 기본 주소를 대시보드로 이동
    @GetMapping
    public String myPage() {

        return "redirect:/mypage/dashboard";
    }
    
    // 로그인 회원의 내 정보 화면
    @GetMapping("/profile")
    public String profile(
            @AuthenticationPrincipal
            LoginMemberDetails loginMember,
            Model model) {

        MyProfileDTO profile = memberService.findMyProfile(loginMember.getUsername());

        model.addAttribute("profile", profile);

        return "mypage/profile";
    }

    // 로그인 회원의 마이페이지 대시보드
    @GetMapping("/dashboard")
    public String dashboard(
            @AuthenticationPrincipal
            LoginMemberDetails loginMember,
            Model model) {

        Long memberId = loginMember.getMemberId();

        MyDashboardSummaryDTO summary = myPageService.findDashboardSummary(memberId);

        ManagedDashboardSummaryDTO managedSummary =
                myPageService.findManagedDashboardSummary(memberId);

        model.addAttribute("summary", summary);

        model.addAttribute("managedSummary", managedSummary);

        return "mypage/dashboard";
    }
    
    // 로그인 회원이 ADMIN인 프로젝트별 통계 화면
    @GetMapping("/managed-projects")
    public String managedProjects(
            @AuthenticationPrincipal
            LoginMemberDetails loginMember,
            Model model) {

        Long memberId = loginMember.getMemberId();

        List<ManagedProjectSummaryDTO> managedProjects =
                myPageService.findManagedProjectSummaries(memberId);

        model.addAttribute("managedProjects", managedProjects);

        return "mypage/managed-projects";
    }
}