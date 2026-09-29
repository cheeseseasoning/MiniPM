package kr.or.oti.minipm.service;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.oti.minipm.dto.MemberJoinDTO;
import kr.or.oti.minipm.dto.MyProfileDTO;
import kr.or.oti.minipm.mapper.MemberMapper;
import kr.or.oti.minipm.vo.MemberVO;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberService {
	
	private final MemberMapper memberMapper;
	private final PasswordEncoder passwordEncoder;
	
	 //회원 등록: DB 데이터가 변경되므로 일반 트랜잭션
    @Transactional
    public Long memberJoin(MemberJoinDTO memberJoinDTO) {

        // 로그인 아이디 중복 확인
        MemberVO memberByLoginId =
                memberMapper.selectMemberByLoginId(memberJoinDTO.getLoginId());

        if (memberByLoginId != null) {
            throw new IllegalStateException("이미 사용 중인 로그인 아이디입니다.");
        }

        // 이메일 중복 확인
        MemberVO memberByEmail =
                memberMapper.selectMemberByEmail(memberJoinDTO.getEmail());

        if (memberByEmail != null) {
            throw new IllegalStateException("이미 사용 중인 이메일입니다.");
        }

        // 회원가입 DTO를 DB 저장용 VO로 변환
        MemberVO memberVO = new MemberVO();
        memberVO.setLoginId(memberJoinDTO.getLoginId());

        // 입력받은 비밀번호를 BCrypt로 암호화
        memberVO.setPassword(
                passwordEncoder.encode(memberJoinDTO.getPassword())
        );

        memberVO.setName(memberJoinDTO.getName());
        memberVO.setEmail(memberJoinDTO.getEmail());

        try {
            int insertedRowCount = memberMapper.insertMember(memberVO);

            if (insertedRowCount != 1) {
                throw new IllegalStateException("회원 등록에 실패했습니다.");
            }

        } catch (DuplicateKeyException e) {
            throw new IllegalStateException("로그인 아이디 또는 이메일이 이미 사용 중입니다.", e);
        }

        return memberVO.getMemberId();
    }
	
	//회원 조회: SELECT만 실행하므로 조회 전용 트랜잭션
	@Transactional(readOnly = true)
	public MemberVO findByLoginId(String loginId) {
		return memberMapper.selectMemberByLoginId(loginId);
	}
	
	// 로그인 회원의 내 정보 조회
	@Transactional(readOnly = true)
	public MyProfileDTO findMyProfile(String loginId) {

	    MemberVO memberVO = memberMapper.selectMemberByLoginId(loginId);

	    if (memberVO == null) {
	        throw new IllegalStateException("회원 정보를 찾을 수 없습니다.");
	    }

	    return MyProfileDTO.builder()
	            .memberId(memberVO.getMemberId())
	            .loginId(memberVO.getLoginId())
	            .name(memberVO.getName())
	            .email(memberVO.getEmail())
	            .createdAt(memberVO.getCreatedAt())
	            .oauthProvider(memberVO.getOauthProvider())
	            .build();
	}
	
}
