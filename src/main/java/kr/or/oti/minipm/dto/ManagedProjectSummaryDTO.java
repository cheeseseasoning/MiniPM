package kr.or.oti.minipm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ManagedProjectSummaryDTO {

    private Long projectId;
    private String projectName;

    private int memberCount;

    private int totalIssueCount;
    private int todoIssueCount;
    private int inProgressIssueCount;
    private int doneIssueCount;

    private int unassignedIssueCount;

    private double completionRate;
}