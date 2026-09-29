package kr.or.oti.minipm.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.oti.minipm.vo.MemberVO;

@Mapper
public interface MemberMapper {

    int countMembers();
    int insertMember(MemberVO member);
    MemberVO selectMemberByLoginId(
            @Param("loginId") String loginId
    );
    
    // 정확한 이메일로 회원 조회
    MemberVO selectMemberByEmail(
            @Param("email") String email
    );
    
    // OAuth 제공자와 고유번호로 회원 조회
    MemberVO selectMemberByOAuth(
            @Param("oauthProvider") String oauthProvider,
            @Param("oauthId") String oauthId
    );

    // 기존 일반 회원에게 OAuth 계정 연결
    int updateMemberOAuth(
            @Param("memberId") Long memberId,
            @Param("oauthProvider") String oauthProvider,
            @Param("oauthId") String oauthId
    );
    
    // 회원번호로 회원 조회
    MemberVO selectMemberById(
            @Param("memberId") Long memberId
    );
}
