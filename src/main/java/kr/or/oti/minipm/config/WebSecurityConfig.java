package kr.or.oti.minipm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import kr.or.oti.minipm.security.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            // 일반 요청에는 CSRF 보호를 적용하고 SockJS 연결 주소만 제외
            .csrf()
                .ignoringAntMatchers("/ws/**")

            .and()

            .authorizeRequests()
                .antMatchers(
                        "/member/join",
                        "/member/login",
                        "/oauth2/**",
                        "/login/oauth2/**",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/error"
                )
                .permitAll()
                .anyRequest()
                .authenticated()

            // 기존 아이디와 비밀번호 로그인
            .and()
            .formLogin()
                .loginPage("/member/login")
                .loginProcessingUrl("/member/login")
                .usernameParameter("loginId")
                .passwordParameter("password")
                .defaultSuccessUrl("/project", true)
                .failureUrl("/member/login?error")
                .permitAll()

            // 카카오 OAuth2 로그인
            .and()
            .oauth2Login()
                .loginPage("/member/login")
                .userInfoEndpoint()
                    .userService(customOAuth2UserService)
                .and()
                .defaultSuccessUrl("/project", true)
                .failureUrl("/member/login?oauthError")

            // IssueFlow 로그아웃
            .and()
            .logout()
                .logoutUrl("/member/logout")
                .logoutSuccessUrl("/member/login?logout")
                .permitAll();

        return http.build();
    }
}