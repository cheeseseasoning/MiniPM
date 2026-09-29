package kr.or.oti.minipm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ManagedDashboardSummaryDTO {

    private int managedProjectCount;

    private int totalIssueCount;
    private int openIssueCount;
    private int doneIssueCount;
    private int unassignedIssueCount;

    private double completionRate;
}