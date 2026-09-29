package kr.or.oti.minipm.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.oti.minipm.dto.IssueSearchDTO;
import kr.or.oti.minipm.dto.PageRequestDTO;
import kr.or.oti.minipm.vo.IssueStatusHistoryVO;
import kr.or.oti.minipm.vo.IssueVO;

@Mapper
public interface IssueMapper {
	
    int insertIssue(IssueVO issueVO);

    // 프로젝트별 검색 조건에 맞는 이슈 목록 조회
    List<IssueVO> selectIssuesByProjectId(
    	@Param("projectId") Long projectId,
    	@Param("search") IssueSearchDTO issueSearchDTO
    );

    IssueVO selectIssueByIdAndProjectId(
        @Param("issueId") Long issueId,
        @Param("projectId") Long projectId
    );
    
    // 이슈 제목·내용·유형·우선순위 수정
    int updateIssue(IssueVO issueVO);

    // 이슈 논리 삭제
    int softDeleteIssue(
            @Param("projectId") Long projectId,
            @Param("issueId") Long issueId,
            @Param("loginMemberId") Long loginMemberId
    );
    
    // 상태 변경 대상 이슈를 잠그고 조회
    IssueVO selectIssueForStatusUpdate(
            @Param("issueId") Long issueId,
            @Param("projectId") Long projectId
    );

    // 상태와 해결 정보 변경
    int updateIssueStatus(IssueVO issueVO);

    // 상태 변경 이력 저장
    int insertIssueStatusHistory(IssueStatusHistoryVO historyVO);
    
    // 특정 이슈의 상태 변경 이력 조회
    List<IssueStatusHistoryVO> selectStatusHistoriesByIssueId(
            @Param("issueId") Long issueId
    );
    
    // 검색 조건에 맞는 이슈를 페이지 단위로 조회
    List<IssueVO> selectIssuesByProjectIdWithPaging(
    	@Param("projectId") Long projectId,
        @Param("search") IssueSearchDTO issueSearchDTO,
        @Param("pageRequest") PageRequestDTO pageRequestDTO);

    // 검색 조건에 맞는 미삭제 이슈 전체 개수
    int countIssuesByProjectId(
    	@Param("projectId") Long projectId,
        @Param("search") IssueSearchDTO issueSearchDTO);
}
