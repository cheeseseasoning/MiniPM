package kr.or.oti.minipm.security;

import java.util.Map;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.oti.minipm.mapper.MemberMapper;
import kr.or.oti.minipm.vo.MemberVO;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final String KAKAO_PROVIDER = "KAKAO";

    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;

    // 카카오 사용자 정보를 조회하고 IssueFlow 회원으로 변환
    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest)
            throws OAuth2AuthenticationException {

        // 액세스 토큰으로 카카오 사용자 정보 조회
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // application.properties에 등록한 kakao 값
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        if (!"kakao".equals(registrationId)) {
            throw createOAuthException("지원하지 않는 소셜 로그인입니다.");
        }

        // 카카오가 반환한 전체 사용자 정보
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String oauthId = extractOAuthId(attributes);

        Map<String, Object> kakaoAccount = extractMap(attributes, "kakao_account");

        String email = extractEmail(kakaoAccount);
        String nickname = extractNickname(kakaoAccount);

        // 카카오 고유번호로 기존 회원 조회
        MemberVO memberVO = memberMapper.selectMemberByOAuth(KAKAO_PROVIDER, oauthId);

        // 조회되지 않았다면 최초 카카오 로그인
        if (memberVO == null) {

            // 같은 이메일의 기존 일반 회원 조회
            MemberVO memberByEmail = memberMapper.selectMemberByEmail(email);

            if (memberByEmail != null) {

                // 기존 일반 회원에게 카카오 계정 연결
                memberVO = connectOAuthAccount(memberByEmail, oauthId);

            } else {

                // 신규 카카오 회원 등록
                memberVO = createOAuthMember(oauthId, email, nickname);
            }
        }

        // Spring Security가 세션에 저장할 로그인 사용자 반환
        return new LoginMemberDetails(memberVO, attributes);
    }

    // 카카오 사용자 고유번호 추출
    private String extractOAuthId(Map<String, Object> attributes) {

        Object idValue = attributes.get("id");

        if (idValue == null) {
            throw createOAuthException("카카오 회원 고유번호를 확인할 수 없습니다.");
        }

        return String.valueOf(idValue);
    }

    // 카카오 계정 이메일 추출
    private String extractEmail(Map<String, Object> kakaoAccount) {

        Object emailValue = kakaoAccount.get("email");

        boolean emailVerified = Boolean.TRUE.equals(kakaoAccount.get("is_email_verified"));

        if (emailValue == null) {
            throw createOAuthException("카카오 계정의 이메일을 확인할 수 없습니다.");
        }

        String email = String.valueOf(emailValue);

        if (email.isBlank()) {
            throw createOAuthException("카카오 계정의 이메일이 비어 있습니다.");
        }

        if (!emailVerified) {
            throw createOAuthException("인증된 카카오 이메일이 필요합니다.");
        }

        return email;
    }

    // 카카오 프로필 닉네임 추출
    private String extractNickname(Map<String, Object> kakaoAccount) {

        Map<String, Object> profile = extractMap(kakaoAccount, "profile");

        Object nicknameValue = profile.get("nickname");

        if (nicknameValue == null) {
            return "카카오 회원";
        }

        String nickname = String.valueOf(nicknameValue);

        if (nickname.isBlank()) {
            return "카카오 회원";
        }

        return nickname;
    }

    // 기존 일반 회원에게 카카오 계정 연결
    private MemberVO connectOAuthAccount(MemberVO memberVO, String oauthId) {

        // 이미 다른 OAuth 계정이 연결되어 있으면 변경하지 않음
        if (memberVO.getOauthProvider() != null
                || memberVO.getOauthId() != null) {

            throw createOAuthException("해당 이메일에는 이미 소셜 계정이 연결되어 있습니다.");
        }

        int updatedRowCount = memberMapper.updateMemberOAuth(
                        memberVO.getMemberId(),
                        KAKAO_PROVIDER,
                        oauthId
                );

        if (updatedRowCount != 1) {
            throw createOAuthException("기존 회원과 카카오 계정을 연결하지 못했습니다.");
        }

        // DB 수정 결과를 현재 객체에도 반영
        memberVO.setOauthProvider(KAKAO_PROVIDER);
        memberVO.setOauthId(oauthId);

        return memberVO;
    }

    // 새로운 카카오 회원 등록
    private MemberVO createOAuthMember(String oauthId, String email, String nickname) {

        MemberVO memberVO = new MemberVO();

        // 카카오 고유번호로 중복되지 않는 로그인 아이디 생성
        memberVO.setLoginId("kakao_" + oauthId);

        // PASSWORD가 NOT NULL이므로 알 수 없는 임의 비밀번호 저장
        memberVO.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));

        memberVO.setName(nickname);
        memberVO.setEmail(email);
        memberVO.setOauthProvider(KAKAO_PROVIDER);
        memberVO.setOauthId(oauthId);

        int insertedRowCount = memberMapper.insertMember(memberVO);

        if (insertedRowCount != 1) {
            throw createOAuthException("카카오 회원 등록에 실패했습니다.");
        }

        return memberVO;
    }

    // 중첩된 카카오 JSON 객체를 Map으로 변환
    @SuppressWarnings("unchecked")
    private Map<String, Object> extractMap(Map<String, Object> parentMap, String key) {

        Object value = parentMap.get(key);

        if (!(value instanceof Map)) {
            throw createOAuthException("카카오 회원정보 형식이 올바르지 않습니다.");
        }

        return (Map<String, Object>) value;
    }

    // OAuth2 로그인 실패 예외 생성
    private OAuth2AuthenticationException createOAuthException(String message) {

        OAuth2Error error = new OAuth2Error("kakao_login_failed");

        return new OAuth2AuthenticationException(error, message);
    }
}