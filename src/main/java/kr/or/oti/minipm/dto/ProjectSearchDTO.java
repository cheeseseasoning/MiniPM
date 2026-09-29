package kr.or.oti.minipm.dto;

import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class ProjectSearchDTO {

    @Size(
        max = 100,
        message = "검색어는 100자 이하여야 합니다."
    )
    private String keyword;
}