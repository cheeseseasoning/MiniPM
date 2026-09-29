package kr.or.oti.minipm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MyDashboardSummaryDTO {

    private int participatingProjectCount;
    private int managedProjectCount;

    private int assignedIssueCount;
    private int todoIssueCount;
    private int inProgressIssueCount;
    private int doneIssueCount;

    private int highPriorityOpenIssueCount;
    private int mediumPriorityOpenIssueCount;
    private int lowPriorityOpenIssueCount;

    private double completionRate;
}