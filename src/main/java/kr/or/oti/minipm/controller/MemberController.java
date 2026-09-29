package kr.or.oti.minipm.controller;

import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import kr.or.oti.minipm.dto.MemberJoinDTO;
import kr.or.oti.minipm.service.MemberService;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {
	
	private final MemberService memberService;
	
	//회원가입 화면 출력
	@GetMapping("/join")
	public String memberJoinForm(Model model) {
		model.addAttribute("memberJoinDTO", new MemberJoinDTO());
		
		return "member/join";
	}
	
    // 회원가입 요청 처리
    @PostMapping("/join")
    public String memberJoin(
            @Valid
            @ModelAttribute("memberJoinDTO")
            MemberJoinDTO memberJoinDTO,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        // DTO 입력값 검증 실패
        if (bindingResult.hasErrors()) {
            return "member/join";
        }

        try {
            memberService.memberJoin(memberJoinDTO);

            redirectAttributes.addFlashAttribute(
                    "message",
                    "회원가입이 완료되었습니다. 로그인해주세요."
            );

            return "redirect:/member/login";

        } catch (IllegalStateException e) {
            bindingResult.reject("memberJoinFailed",e.getMessage());

            return "member/join";
        }
    }
    
    // 로그인 화면 출력
    @GetMapping("/login")
    public String memberLoginForm() {
        return "member/login";
    }
}
