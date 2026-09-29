package kr.or.oti.minipm.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.oti.minipm.dto.IssueCreateDTO;
import kr.or.oti.minipm.dto.IssueSearchDTO;
import kr.or.oti.minipm.dto.IssueStatusUpdateDTO;
import kr.or.oti.minipm.dto.IssueUpdateDTO;
import kr.or.oti.minipm.dto.PageRequestDTO;
import kr.or.oti.minipm.dto.PageResponseDTO;
import kr.or.oti.minipm.mapper.IssueMapper;
import kr.or.oti.minipm.mapper.ProjectMapper;
import kr.or.oti.minipm.vo.IssueStatusHistoryVO;
import kr.or.oti.minipm.vo.IssueVO;
import kr.or.oti.minipm.vo.ProjectMemberVO;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IssueService {
	
	private final IssueMapper issueMapper;
	private final ProjectMapper projectMapper;
	private final NotificationService notificationService;
	
	// 이슈 등록
	@Transactional
	public Long createIssue(Long projectId, IssueCreateDTO issueCreateDTO, Long reporterId) {

	    // 작성자가 프로젝트 참여자인지 확인
	    validateProjectMember(projectId, reporterId);

	    Long assigneeId = issueCreateDTO.getAssigneeId();

	    // 담당자를 지정했다면 담당자도 프로젝트 참여자인지 확인
	    if (assigneeId != null) {
	        validateProjectMember(projectId, assigneeId);
	    }

	    IssueVO issueVO = new IssueVO();
	    issueVO.setProjectId(projectId);
	    issueVO.setTitle(issueCreateDTO.getTitle());
	    issueVO.setContent(issueCreateDTO.getContent());
	    issueVO.setIssueType(issueCreateDTO.getIssueType());
	    issueVO.setPriority(issueCreateDTO.getPriority());
	    issueVO.setReporterId(reporterId);
	    issueVO.setAssigneeId(assigneeId);

	    int insertedRowCount = issueMapper.insertIssue(issueVO);

	    if (insertedRowCount != 1) {
	        throw new IllegalStateException("이슈 등록에 실패했습니다.");
	    }

	    String projectName = projectMapper.selectProjectNameById(projectId);

	    String issueDetailUrl = "/project/" + projectId + "/issues/" + issueVO.getIssueId();

	    String issueMessagePrefix = "[" + projectName + "] '" + issueVO.getTitle() + "' 이슈";

	    // 프로젝트 관리자 회원번호만 조회
	    List<Long> adminMemberIds = projectMapper.selectProjectAdminIds(projectId);

	    // 이슈 등록자를 제외한 프로젝트 관리자에게 알림 전송
	    notificationService.createNotifications(
	            adminMemberIds,
	            reporterId,
	            "ISSUE_CREATED",
	            issueMessagePrefix + "가 등록되었습니다.",
	            issueDetailUrl
	    );

	    // 담당자가 등록자와 다른 회원이면 담당자 지정 알림 전송
	    if (assigneeId != null && !assigneeId.equals(reporterId)) {
	        notificationService.createNotification(
	                assigneeId,
	                "ISSUE_ASSIGNEE_CHANGED",
	                issueMessagePrefix + "의 담당자로 지정되었습니다.",
	                issueDetailUrl
	        );
	    }

	    return issueVO.getIssueId();
	}
	
	// 프로젝트별 이슈 목록 조회
    // 참여 권한을 확인하고 검색 조건에 맞는 이슈 목록 조회
    @Transactional(readOnly = true)
    public List<IssueVO> findIssuesByProjectId(
            Long projectId,
            Long memberId,
            IssueSearchDTO issueSearchDTO) {

        validateProjectMember(projectId, memberId);

        if (issueSearchDTO == null) {
            issueSearchDTO = new IssueSearchDTO();
        }

        String keyword = issueSearchDTO.getKeyword();

        if (keyword != null) {
            keyword = keyword.trim();

            if (keyword.isEmpty()) {
                keyword = null;
            }

            issueSearchDTO.setKeyword(keyword);
        }

        return issueMapper.selectIssuesByProjectId(projectId, issueSearchDTO);
    }
    
    // 검색 조건에 맞는 프로젝트 이슈 페이징 조회
    @Transactional(readOnly = true)
    public PageResponseDTO<IssueVO> findIssuesByProjectId(
            Long projectId,
            Long memberId,
            IssueSearchDTO issueSearchDTO,
            PageRequestDTO pageRequestDTO) {

        validateProjectMember(projectId, memberId);

        if (issueSearchDTO == null) {
            issueSearchDTO = new IssueSearchDTO();
        }

        String keyword = issueSearchDTO.getKeyword();

        if (keyword != null) {
            keyword = keyword.trim();

            if (keyword.isEmpty()) {
                keyword = null;
            }

            issueSearchDTO.setKeyword(keyword);
        }

        List<IssueVO> issues =
                issueMapper.selectIssuesByProjectIdWithPaging(
                        projectId,
                        issueSearchDTO,
                        pageRequestDTO
                );

        int total = issueMapper.countIssuesByProjectId(projectId, issueSearchDTO);

        return new PageResponseDTO<>(pageRequestDTO, issues, total);
    }
    
    // 검색 조건 없이 프로젝트 전체 이슈 목록 조회
    @Transactional(readOnly = true)
    public List<IssueVO> findIssuesByProjectId(Long projectId, Long memberId) {

        return findIssuesByProjectId(projectId, memberId, new IssueSearchDTO());
    }
    
    // 참여 권한을 검사한 뒤 이슈 상세 조회
    @Transactional(readOnly = true)
    public IssueVO findIssueDetail(Long projectId, Long issueId, Long memberId) {

        validateProjectMember(projectId, memberId);

        IssueVO issueVO = issueMapper.selectIssueByIdAndProjectId(issueId, projectId);

        if (issueVO == null) {
            throw new IllegalStateException("존재하지 않는 이슈입니다.");
        }

        return issueVO;
    }
    
    // 수정 화면에 표시할 기존 이슈 정보 조회
    @Transactional(readOnly = true)
    public IssueUpdateDTO findIssueForUpdate(Long projectId, Long issueId, Long loginMemberId) {

        // 참여 권한과 이슈 존재 여부를 검사하면서 조회
        IssueVO issueVO = findIssueDetail(projectId, issueId, loginMemberId);

        // DB 조회 결과를 수정 폼용 DTO로 변환
        IssueUpdateDTO issueUpdateDTO = new IssueUpdateDTO();
        issueUpdateDTO.setTitle(issueVO.getTitle());
        issueUpdateDTO.setContent(issueVO.getContent());
        issueUpdateDTO.setIssueType(issueVO.getIssueType());
        issueUpdateDTO.setPriority(issueVO.getPriority());
        issueUpdateDTO.setAssigneeId(issueVO.getAssigneeId());

        return issueUpdateDTO;
    }

    // 이슈 제목·내용·유형·우선순위·담당자 수정
    @Transactional
    public void updateIssue(
            Long projectId,
            Long issueId,
            IssueUpdateDTO issueUpdateDTO,
            Long loginMemberId) {

        // 참여 권한과 이슈 존재 여부 확인
        IssueVO issueVO = findIssueDetail(projectId, issueId, loginMemberId);

        Long previousAssigneeId = issueVO.getAssigneeId();
        Long newAssigneeId = issueUpdateDTO.getAssigneeId();

        // 새 담당자가 지정된 경우 프로젝트 참여자인지 확인
        if (newAssigneeId != null) {
            validateProjectMember(projectId, newAssigneeId);
        }

        // 기존 담당자와 새 담당자가 다른지 확인
        boolean assigneeChanged = !Objects.equals(previousAssigneeId, newAssigneeId);

        // 수정 가능한 값 변경
        issueVO.setTitle(issueUpdateDTO.getTitle());
        issueVO.setContent(issueUpdateDTO.getContent());
        issueVO.setIssueType(issueUpdateDTO.getIssueType());
        issueVO.setPriority(issueUpdateDTO.getPriority());
        issueVO.setAssigneeId(newAssigneeId);

        int updatedRowCount = issueMapper.updateIssue(issueVO);

        if (updatedRowCount != 1) {
            throw new IllegalStateException("이슈 수정에 실패했습니다.");
        }

        if (!assigneeChanged) {
            return;
        }

        // 담당자가 실제로 바뀐 경우에만 프로젝트 이름 조회
        String projectName = projectMapper.selectProjectNameById(projectId);

        String issueDetailUrl = "/project/" + projectId + "/issues/" + issueId;

        String issueMessagePrefix = "[" + projectName + "] '" + issueVO.getTitle() + "' 이슈";

        // 기존 담당자에게 담당 해제 알림 전송
        if (previousAssigneeId != null && !previousAssigneeId.equals(loginMemberId)) {

            notificationService.createNotification(
                    previousAssigneeId,
                    "ISSUE_ASSIGNEE_CHANGED",
                    issueMessagePrefix + " 담당자에서 해제되었습니다.",
                    issueDetailUrl
            );
        }

        // 새 담당자에게 담당 지정 알림 전송
        if (newAssigneeId != null && !newAssigneeId.equals(loginMemberId)) {

            notificationService.createNotification(
                    newAssigneeId,
                    "ISSUE_ASSIGNEE_CHANGED",
                    issueMessagePrefix + "의 담당자로 지정되었습니다.",
                    issueDetailUrl
            );
        }
    }
    
 // 참여 중인 작성자 또는 ADMIN만 이슈 논리 삭제
    @Transactional
    public void deleteIssue(Long projectId, Long issueId, Long loginMemberId) {

        // 권한 확인과 알림에 필요한 이슈 정보 조회
        IssueVO issueVO = findIssueDetail(projectId, issueId, loginMemberId);

        ProjectMemberVO projectMemberVO = projectMapper.selectProjectMember(projectId, loginMemberId);

        if (projectMemberVO == null) {
            throw new IllegalStateException("해당 프로젝트에 접근할 권한이 없습니다.");
        }

        boolean isReporter = loginMemberId != null && loginMemberId.equals(issueVO.getReporterId());

        boolean isAdmin = "ADMIN".equals(projectMemberVO.getRole());

        if (!isReporter && !isAdmin) {
            throw new IllegalStateException("이슈 작성자 또는 프로젝트 관리자만 삭제할 수 있습니다.");
        }

        int updatedRowCount = issueMapper.softDeleteIssue(projectId, issueId, loginMemberId);

        if (updatedRowCount != 1) {
            throw new IllegalStateException("이슈가 이미 삭제되었거나 삭제 권한이 변경되었습니다.");
        }

        // 프로젝트 관리자를 기준으로 알림 대상 목록 생성
        List<Long> recipientMemberIds = new ArrayList<>(
                projectMapper.selectProjectAdminIds(projectId)
        );

        // 이슈 작성자와 담당자 추가
        recipientMemberIds.add(issueVO.getReporterId());

        if (issueVO.getAssigneeId() != null) {
            recipientMemberIds.add(issueVO.getAssigneeId());
        }

        String projectName = projectMapper.selectProjectNameById(projectId);

        String message =
                "[" + projectName + "] '"
                + issueVO.getTitle()
                + "' 이슈가 삭제되었습니다.";

        notificationService.createNotifications(
                recipientMemberIds,
                loginMemberId,
                "ISSUE_DELETED",
                message,
                "/project/" + projectId + "/issues"
        );
    }
    
    // 프로젝트 참여 여부 검사
    private void validateProjectMember(Long projectId, Long memberId) {

        ProjectMemberVO projectMemberVO = projectMapper.selectProjectMember(projectId, memberId);

        if (projectMemberVO == null) {
            throw new IllegalStateException("해당 프로젝트에 접근할 권한이 없습니다.");
        }
    }
    
    // 이슈 상태 변경 및 변경 이력 저장
    @Transactional
    public void updateIssueStatus(
            Long projectId,
            Long issueId,
            IssueStatusUpdateDTO issueStatusUpdateDTO,
            Long loginMemberId) {

        // 프로젝트 참여 여부 확인
        validateProjectMember(projectId, loginMemberId);

        String newStatus = issueStatusUpdateDTO.getStatus();
        String resolution = issueStatusUpdateDTO.getResolution();

        // 허용한 상태인지 확인
        if (!"TODO".equals(newStatus) && !"IN_PROGRESS".equals(newStatus)
                && !"DONE".equals(newStatus)) {

            throw new IllegalStateException("올바른 상태를 선택해주세요.");
        }

        // 해결 내용 길이 확인
        if (resolution != null && resolution.length() > 1000) {
            throw new IllegalStateException("해결 내용은 1000자 이하여야 합니다.");
        }

        // 완료 처리에는 해결 내용이 필수
        if ("DONE".equals(newStatus)) {
            if (resolution == null || resolution.trim().isEmpty()) {
                throw new IllegalStateException("완료 처리하려면 해결 내용을 입력해주세요.");
            }

            resolution = resolution.trim();
        }

        // 대상 이슈를 잠그고 현재 상태와 알림 정보를 조회
        IssueVO issueVO = issueMapper.selectIssueForStatusUpdate(issueId, projectId);

        if (issueVO == null) {
            throw new IllegalStateException("존재하지 않거나 삭제된 이슈입니다.");
        }

        String previousStatus = issueVO.getStatus();

        // 같은 상태로 변경하는 요청은 거부
        if (previousStatus.equals(newStatus)) {
            throw new IllegalStateException("현재 상태와 다른 상태를 선택해주세요.");
        }

        // 새로운 상태와 해결 정보 준비
        issueVO.setStatus(newStatus);

        if ("DONE".equals(newStatus)) {
            issueVO.setResolution(resolution);
            issueVO.setResolvedBy(loginMemberId);
        } else {
            issueVO.setResolution(null);
            issueVO.setResolvedBy(null);
        }

        // 상태 변경
        int updatedRowCount = issueMapper.updateIssueStatus(issueVO);

        if (updatedRowCount != 1) {
            throw new IllegalStateException("이슈 상태 변경에 실패했습니다.");
        }

        // 이전 상태와 새 상태를 이력에 저장
        IssueStatusHistoryVO historyVO = new IssueStatusHistoryVO();

        historyVO.setIssueId(issueId);
        historyVO.setPreviousStatus(previousStatus);
        historyVO.setNewStatus(newStatus);
        historyVO.setMemberId(loginMemberId);

        int insertedRowCount = issueMapper.insertIssueStatusHistory(historyVO);

        if (insertedRowCount != 1) {
            throw new IllegalStateException("상태 변경 이력 저장에 실패했습니다.");
        }

        // 프로젝트 관리자 목록을 기준으로 알림 대상 목록 생성
        List<Long> recipientMemberIds = new ArrayList<>(
        	projectMapper.selectProjectAdminIds(projectId)
        );

        // 이슈 작성자와 담당자 추가
        recipientMemberIds.add(issueVO.getReporterId());

        if (issueVO.getAssigneeId() != null) {
            recipientMemberIds.add(issueVO.getAssigneeId());
        }

        String projectName = projectMapper.selectProjectNameById(projectId);

        String message =
                "[" + projectName + "] '"
                + issueVO.getTitle()
                + "' 이슈 상태가 '"
                + getStatusName(previousStatus)
                + "'에서 '"
                + getStatusName(newStatus)
                + "'으로 변경되었습니다.";

        notificationService.createNotifications(
                recipientMemberIds,
                loginMemberId,
                "ISSUE_STATUS_CHANGED",
                message,
                "/project/" + projectId + "/issues/" + issueId
        );
    }
    
    // 상태 코드의 화면 표시 이름 반환
    private String getStatusName(String status) {

        switch (status) {
            case "TODO":
                return "할 일";

            case "IN_PROGRESS":
                return "진행 중";

            case "DONE":
                return "완료";

            default:
                return status;
        }
    }
}
