package kr.or.oti.minipm.vo;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
public class MemberVO {

    private Long memberId;
    private String loginId;

    @ToString.Exclude
    private String password;

    private String name;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private String oauthProvider;
    private String oauthId;
}