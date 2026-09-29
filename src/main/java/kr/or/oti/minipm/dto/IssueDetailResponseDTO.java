package kr.or.oti.minipm.dto;

import java.util.List;

import kr.or.oti.minipm.vo.IssueCommentVO;
import kr.or.oti.minipm.vo.IssueStatusHistoryVO;
import kr.or.oti.minipm.vo.IssueVO;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class IssueDetailResponseDTO {

    private final IssueVO issue;
    private final List<IssueCommentVO> comments;
    private final List<IssueStatusHistoryVO> statusHistories;
}