package kr.or.oti.minipm.service;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.oti.minipm.dto.ManagedDashboardSummaryDTO;
import kr.or.oti.minipm.dto.ManagedProjectSummaryDTO;
import kr.or.oti.minipm.dto.MyDashboardSummaryDTO;
import kr.or.oti.minipm.mapper.MyPageMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MyPageService {

    private final MyPageMapper myPageMapper;

    // 로그인 회원의 마이페이지 대시보드 요약 통계 조회
    @Transactional(readOnly = true)
    public MyDashboardSummaryDTO findDashboardSummary(Long memberId) {

        if (memberId == null) {
            throw new IllegalStateException("로그인한 회원 정보를 찾을 수 없습니다.");
        }

        MyDashboardSummaryDTO summary = myPageMapper.selectDashboardSummary(memberId);

        if (summary == null) {
            summary = new MyDashboardSummaryDTO();
        }

        int assignedIssueCount = summary.getAssignedIssueCount();

        int doneIssueCount = summary.getDoneIssueCount();

        double completionRate = 0.0;

        if (assignedIssueCount > 0) {
            completionRate = Math.round(doneIssueCount * 1000.0 / assignedIssueCount) / 10.0;
        }

        summary.setCompletionRate(completionRate);

        return summary;
    }
    
 // 로그인 회원이 관리하는 프로젝트의 전체 통계 조회
    @Transactional(readOnly = true)
    public ManagedDashboardSummaryDTO findManagedDashboardSummary(Long memberId) {

        if (memberId == null) {
            throw new IllegalStateException("로그인한 회원 정보를 찾을 수 없습니다.");
        }

        ManagedDashboardSummaryDTO managedSummary =
                myPageMapper.selectManagedDashboardSummary(memberId);

        if (managedSummary == null) {
            managedSummary = new ManagedDashboardSummaryDTO();
        }

        int totalIssueCount =
                managedSummary.getTotalIssueCount();

        int doneIssueCount = managedSummary.getDoneIssueCount();

        double completionRate = 0.0;

        if (totalIssueCount > 0) {
            completionRate =
                    Math.round(doneIssueCount * 1000.0 / totalIssueCount) / 10.0;
        }

        managedSummary.setCompletionRate(completionRate);

        return managedSummary;
    }
    
 // 로그인 회원이 ADMIN인 프로젝트별 통계 목록 조회
    @Transactional(readOnly = true)
    public List<ManagedProjectSummaryDTO> findManagedProjectSummaries(Long memberId) {

        if (memberId == null) {
            throw new IllegalStateException("로그인한 회원 정보를 찾을 수 없습니다.");
        }

        List<ManagedProjectSummaryDTO> managedProjects =
                myPageMapper.selectManagedProjectSummaries(memberId);

        if (managedProjects == null) {
            return Collections.emptyList();
        }

        for (ManagedProjectSummaryDTO project : managedProjects) {

            int totalIssueCount = project.getTotalIssueCount();

            int doneIssueCount = project.getDoneIssueCount();

            double completionRate = 0.0;

            if (totalIssueCount > 0) {
                completionRate =
                        Math.round(doneIssueCount * 1000.0 / totalIssueCount) / 10.0;
            }

            project.setCompletionRate(completionRate);
        }

        return managedProjects;
    }
    
}