package kr.or.oti.minipm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import kr.or.oti.minipm.dto.MemberJoinDTO;
import kr.or.oti.minipm.service.MemberService;
import kr.or.oti.minipm.vo.MemberVO;

@SpringBootTest
public class MemberJoinTests {
	
	@Autowired
	private MemberService memberService;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Test
	@Transactional
	void memberJoinTest() {
		
		//회원가입에 필요한 요청 데이터 준비
		MemberJoinDTO memberJoinDTO = new MemberJoinDTO();
	    memberJoinDTO.setLoginId("service_join_test");
	    memberJoinDTO.setPassword("1234");
	    memberJoinDTO.setName("서비스 테스트 회원");
	    memberJoinDTO.setEmail("service@test.com");

		//회원가입 실행
	    Long memberId = memberService.memberJoin(memberJoinDTO);
	    
	    //등록된 회원 다시 조회
	    MemberVO foundMember = memberService.findByLoginId("service_join_test");
	    
	    // Then: 실행 결과 검증
	    assertNotNull(memberId);
	    assertNotNull(foundMember);

	    assertEquals(
	            memberId,
	            foundMember.getMemberId()
	    );

	    assertEquals(
	            "service_join_test",
	            foundMember.getLoginId()
	    );

	    assertEquals(
	            "서비스 테스트 회원",
	            foundMember.getName()
	    );

	    assertEquals(
	            "service@test.com",
	            foundMember.getEmail()
	    );

	    // DB에 평문 비밀번호가 저장되지 않았는지 확인
	    assertNotEquals(
	            "1234",
	            foundMember.getPassword()
	    );

	    // 저장된 BCrypt 비밀번호가 원래 비밀번호와 일치하는지 확인
	    assertTrue(
	            passwordEncoder.matches(
	                    "1234",
	                    foundMember.getPassword()
	            )
	    );

	    System.out.println("생성된 회원번호: " + memberId);
	    System.out.println(
	            "저장된 비밀번호: "
	                    + foundMember.getPassword()
	    );
	}
	
	@Test
	@Transactional
	void duplicateLoginIdTest() {

	    // Given
	    MemberJoinDTO memberJoinDTO = new MemberJoinDTO();
	    memberJoinDTO.setLoginId("duplicate_login_test");
	    memberJoinDTO.setPassword("1234");
	    memberJoinDTO.setName("중복 테스트 회원");
	    memberJoinDTO.setEmail("duplicate@test.com");

	    // 첫 번째 회원가입은 성공
	    memberService.memberJoin(memberJoinDTO);

	    // When & Then
	    IllegalStateException exception =
	            assertThrows(
	                    IllegalStateException.class,
	                    () -> memberService.memberJoin(memberJoinDTO)
	            );

	    assertEquals(
	            "이미 사용 중인 로그인 아이디입니다.",
	            exception.getMessage()
	    );

	    System.out.println(
	            "예외 메시지: " + exception.getMessage()
	    );
	}
}
