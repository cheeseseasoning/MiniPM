package kr.or.oti.minipm.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.oti.minipm.dto.IssueCommentCreateDTO;
import kr.or.oti.minipm.dto.IssueCommentUpdateDTO;
import kr.or.oti.minipm.dto.IssueDetailResponseDTO;
import kr.or.oti.minipm.mapper.IssueCommentMapper;
import kr.or.oti.minipm.mapper.IssueMapper;
import kr.or.oti.minipm.mapper.ProjectMapper;
import kr.or.oti.minipm.vo.IssueCommentVO;
import kr.or.oti.minipm.vo.IssueStatusHistoryVO;
import kr.or.oti.minipm.vo.IssueVO;
import kr.or.oti.minipm.vo.ProjectMemberVO;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IssueCommentService {

    private final IssueCommentMapper issueCommentMapper;
    private final IssueService issueService;
    private final ProjectMapper projectMapper;
    private final IssueMapper issueMapper;
    private final NotificationService notificationService;

    // 접근 권한을 확인한 뒤 이슈, 댓글, 상태 변경 이력을 함께 조회
    @Transactional(readOnly = true)
    public IssueDetailResponseDTO findIssueDetailWithComments(
            Long projectId,
            Long issueId,
            Long loginMemberId
    ) {

        IssueVO issueVO =
                issueService.findIssueDetail(projectId, issueId, loginMemberId);

        List<IssueCommentVO> comments =
                issueCommentMapper.selectCommentsByIssueId(issueVO.getIssueId());

        List<IssueStatusHistoryVO> statusHistories =
                issueMapper.selectStatusHistoriesByIssueId(issueVO.getIssueId());

        return new IssueDetailResponseDTO(issueVO, comments, statusHistories);
    }

 // 이슈 댓글 등록
    @Transactional
    public Long createComment(
            Long projectId,
            Long issueId,
            IssueCommentCreateDTO issueCommentCreateDTO,
            Long loginMemberId) {

        // 참여 권한과 이슈 존재 여부를 확인하고 알림 정보도 함께 조회
        IssueVO issueVO = issueService.findIssueDetail(projectId, issueId, loginMemberId);

        String content = issueCommentCreateDTO.getContent();

        if (content == null || content.trim().isEmpty()) {
            throw new IllegalStateException("댓글 내용을 입력해주세요.");
        }

        if (content.length() > 2000) {
            throw new IllegalStateException("댓글은 2000자 이하여야 합니다.");
        }

        IssueCommentVO commentVO = new IssueCommentVO();

        commentVO.setIssueId(issueVO.getIssueId());
        commentVO.setMemberId(loginMemberId);
        commentVO.setContent(content);

        int insertedRowCount = issueCommentMapper.insertComment(commentVO);

        if (insertedRowCount != 1) {
            throw new IllegalStateException("댓글 등록에 실패했습니다.");
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
                + "' 이슈에 새 댓글이 등록되었습니다.";

        notificationService.createNotifications(
                recipientMemberIds,
                loginMemberId,
                "COMMENT_CREATED",
                message,
                "/project/" + projectId + "/issues/" + issueId
        );

        return commentVO.getCommentId();
    }
    
    // 수정 권한을 확인하고 댓글 수정 폼에 표시할 내용 조회
    @Transactional(readOnly = true)
    public IssueCommentUpdateDTO findCommentForUpdate(
            Long projectId,
            Long issueId,
            Long commentId,
            Long loginMemberId
    ) {

        IssueCommentVO commentVO =
                findEditableComment(projectId, issueId, commentId, loginMemberId);

        IssueCommentUpdateDTO issueCommentUpdateDTO = new IssueCommentUpdateDTO();
        issueCommentUpdateDTO.setContent(commentVO.getContent());

        return issueCommentUpdateDTO;
    }
    
    // 작성자 본인의 댓글 수정
    @Transactional
    public void updateComment(
            Long projectId,
            Long issueId,
            Long commentId,
            IssueCommentUpdateDTO issueCommentUpdateDTO,
            Long loginMemberId
    ) {

        IssueCommentVO commentVO =
                findEditableComment(projectId, issueId, commentId, loginMemberId);

        String content = issueCommentUpdateDTO.getContent();

        if (content == null || content.trim().isEmpty()) {
            throw new IllegalStateException("댓글 내용을 입력해주세요.");
        }

        if (content.length() > 2000) {
            throw new IllegalStateException("댓글은 2000자 이하여야 합니다.");
        }

        commentVO.setContent(content);

        // 수정 SQL의 작성자 조건에는 로그인 사용자 번호 사용
        commentVO.setMemberId(loginMemberId);

        int updatedRowCount = issueCommentMapper.updateComment(commentVO);

        if (updatedRowCount != 1) {
            throw new IllegalStateException("댓글 수정에 실패했습니다. 삭제 여부를 확인해주세요.");
        }
    }
    
    // 작성자 또는 프로젝트 ADMIN의 댓글 논리 삭제
    @Transactional
    public void deleteComment(Long projectId, Long issueId, Long commentId, Long loginMemberId) {

        // 프로젝트 참여 여부와 미삭제 이슈 존재 여부 확인
        issueService.findIssueDetail(projectId, issueId, loginMemberId);

        IssueCommentVO commentVO =
                issueCommentMapper.selectCommentByIdAndIssueId(commentId, issueId);

        if (commentVO == null) {
            throw new IllegalStateException("존재하지 않거나 삭제된 댓글입니다.");
        }

        ProjectMemberVO projectMemberVO =
                projectMapper.selectProjectMember(projectId, loginMemberId);

        if (projectMemberVO == null) {
            throw new IllegalStateException("해당 프로젝트에 접근할 권한이 없습니다.");
        }

        boolean isAuthor = loginMemberId != null && loginMemberId.equals(commentVO.getMemberId());
        boolean isAdmin = "ADMIN".equals(projectMemberVO.getRole());

        if (!isAuthor && !isAdmin) {
            throw new IllegalStateException("댓글 작성자 또는 프로젝트 관리자만 삭제할 수 있습니다.");
        }

        int updatedRowCount =
                issueCommentMapper.softDeleteComment(projectId, issueId, commentId, loginMemberId);

        if (updatedRowCount != 1) {
            throw new IllegalStateException("댓글 삭제에 실패했습니다. 이미 삭제되었거나 권한이 변경되었을 수 있습니다.");
        }
    }
    
    // 이슈 접근 권한과 댓글 작성자 여부를 확인한 뒤 댓글 반환
    private IssueCommentVO findEditableComment(
            Long projectId,
            Long issueId,
            Long commentId,
            Long loginMemberId
    ) {

        // 프로젝트 참여 여부와 미삭제 이슈 존재 여부 확인
        issueService.findIssueDetail(projectId, issueId, loginMemberId);

        IssueCommentVO commentVO =
                issueCommentMapper.selectCommentByIdAndIssueId(commentId, issueId);

        if (commentVO == null) {
            throw new IllegalStateException("존재하지 않거나 삭제된 댓글입니다.");
        }

        if (loginMemberId == null || !loginMemberId.equals(commentVO.getMemberId())) {
            throw new IllegalStateException("댓글 작성자만 수정할 수 있습니다.");
        }

        return commentVO;
    }
}