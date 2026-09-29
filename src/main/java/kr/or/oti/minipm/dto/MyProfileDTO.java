package kr.or.oti.minipm.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyProfileDTO {

    private Long memberId;
    private String loginId;
    private String name;
    private String email;
    private LocalDateTime createdAt;
    private String oauthProvider;
}