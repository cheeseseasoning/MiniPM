package kr.or.oti.minipm.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.oti.minipm.dto.ManagedDashboardSummaryDTO;
import kr.or.oti.minipm.dto.ManagedProjectSummaryDTO;
import kr.or.oti.minipm.dto.MyDashboardSummaryDTO;

@Mapper
public interface MyPageMapper {
	
    MyDashboardSummaryDTO selectDashboardSummary(
            @Param("memberId") Long memberId
    );
    
    // 로그인 회원이 관리하는 프로젝트의 전체 통계 조회
    ManagedDashboardSummaryDTO selectManagedDashboardSummary(
            @Param("memberId") Long memberId
    );
    
    // 로그인 회원이 ADMIN인 프로젝트별 통계 목록 조회
    List<ManagedProjectSummaryDTO> selectManagedProjectSummaries(
            @Param("memberId") Long memberId
    );
}
