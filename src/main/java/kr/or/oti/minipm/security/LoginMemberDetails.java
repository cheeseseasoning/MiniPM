package kr.or.oti.minipm.security;

import java.util.Collections;
import java.util.Map;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import kr.or.oti.minipm.vo.MemberVO;

public class LoginMemberDetails extends User implements OAuth2User {

    private static final long serialVersionUID = 1L;

    private final Long memberId;

    private final String memberName;
    private final String email;
    private final String oauthProvider;

    private final Map<String, Object> attributes;

    // 일반 아이디 로그인에서 사용
    public LoginMemberDetails(MemberVO memberVO) {

        this(memberVO, Collections.emptyMap());
    }

    // OAuth2 로그인에서 사용
    public LoginMemberDetails(MemberVO memberVO, Map<String, Object> attributes) {

        super(
                memberVO.getLoginId(),
                memberVO.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_MEMBER"))
        );

        this.memberId = memberVO.getMemberId();

        this.memberName = memberVO.getName();
        this.email = memberVO.getEmail();
        this.oauthProvider = memberVO.getOauthProvider();

        if (attributes == null) {
            this.attributes = Collections.emptyMap();

        } else {
            this.attributes = attributes;
        }
    }

    public Long getMemberId() {
        return memberId;
    }

    public String getMemberName() {
        return memberName;
    }

    public String getEmail() {
        return email;
    }

    public String getOauthProvider() {
        return oauthProvider;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return getUsername();
    }
}