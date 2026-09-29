package kr.or.oti.minipm.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.oti.minipm.vo.IssueCommentVO;

@Mapper
public interface IssueCommentMapper {

    // 댓글 등록
    int insertComment(IssueCommentVO commentVO);

    // 특정 이슈의 미삭제 댓글 목록
    List<IssueCommentVO> selectCommentsByIssueId(
            @Param("issueId") Long issueId
    );
    
    // 해당 이슈에 속한 미삭제 댓글 단건 조회
    IssueCommentVO selectCommentByIdAndIssueId(
            @Param("commentId") Long commentId,
            @Param("issueId") Long issueId
    );

    // 작성자 본인의 미삭제 댓글 수정
    int updateComment(IssueCommentVO commentVO);
    
    // 작성자 또는 프로젝트 ADMIN의 댓글 논리 삭제
    int softDeleteComment(
            @Param("projectId") Long projectId,
            @Param("issueId") Long issueId,
            @Param("commentId") Long commentId,
            @Param("loginMemberId") Long loginMemberId
    );
}