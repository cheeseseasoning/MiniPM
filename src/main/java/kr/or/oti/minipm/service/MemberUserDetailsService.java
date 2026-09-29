package kr.or.oti.minipm.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import kr.or.oti.minipm.mapper.MemberMapper;
import kr.or.oti.minipm.security.LoginMemberDetails;
import kr.or.oti.minipm.vo.MemberVO;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberUserDetailsService implements UserDetailsService {

    private final MemberMapper memberMapper;

    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {

        MemberVO memberVO = memberMapper.selectMemberByLoginId(loginId);

        if (memberVO == null) {
            throw new UsernameNotFoundException("존재하지 않는 회원입니다.");
        }

        return new LoginMemberDetails(memberVO);
    }
}