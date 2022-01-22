package wegrus.clubwebsite.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wegrus.clubwebsite.dto.board.ReplyCreateRequest;
import wegrus.clubwebsite.entity.board.Board;
import wegrus.clubwebsite.entity.board.CommentLike;
import wegrus.clubwebsite.entity.board.Reply;
import wegrus.clubwebsite.entity.board.ReplyState;
import wegrus.clubwebsite.entity.member.Member;
import wegrus.clubwebsite.exception.*;
import wegrus.clubwebsite.repository.BoardRepository;
import wegrus.clubwebsite.repository.CommentLikeRepository;
import wegrus.clubwebsite.repository.MemberRepository;
import wegrus.clubwebsite.repository.ReplyRepository;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ReplyService {
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;
    private final ReplyRepository replyRepository;
    private final CommentLikeRepository commentLikeRepository;

    @Transactional
    public Long create(ReplyCreateRequest request){
        String memberId = SecurityContextHolder.getContext().getAuthentication().getName();
        final Member member = memberRepository.findById(Long.valueOf(memberId)).orElseThrow(MemberNotFoundException::new);

        final Board board = boardRepository.findById(request.getBoardId()).orElseThrow(BoardNotFoundException::new);

        ReplyState replyState = ReplyState.ACTIVATE;

        Reply reply = Reply.builder()
                .member(member)
                .board(board)
                .content(request.getContent())
                .state(replyState)
                .build();

        return replyRepository.save(reply).getId();
    }

    @Transactional
    public void delete(Long commentId){
        String memberId = SecurityContextHolder.getContext().getAuthentication().getName();
        final Member member = memberRepository.findById(Long.valueOf(memberId)).orElseThrow(MemberNotFoundException::new);

        final Reply reply = replyRepository.findById(commentId).orElseThrow(ReplyNotFoundException::new);

        if(!member.getId().equals(reply.getMember().getId())) {
            throw new ReplyMemberNotMatchException();
        }

        // 댓글 추천수 제거
        commentLikeRepository.deleteCommentLikesByReply(reply);

        replyRepository.deleteById(commentId);
    }

    @Transactional
    public Long like(Long commentId){
        String memberId = SecurityContextHolder.getContext().getAuthentication().getName();
        final Member member = memberRepository.findById(Long.valueOf(memberId)).orElseThrow(MemberNotFoundException::new);

        final Reply reply = replyRepository.findById(commentId).orElseThrow(ReplyNotFoundException::new);

        Optional<CommentLike> commentLikes = commentLikeRepository.findByMemberAndReply(member, reply);

        // 이미 추천이 있다면
        if(commentLikes.isPresent()){
            throw new CommentLikeAlreadyExistException();
        }

        CommentLike commentLike = CommentLike.builder()
                .member(member)
                .reply(reply)
                .build();

        commentLikeRepository.save(commentLike);
        return commentLike.getId();
    }

    @Transactional
    public void dislike(Long commentId){
        String memberId = SecurityContextHolder.getContext().getAuthentication().getName();
        final Member member = memberRepository.findById(Long.valueOf(memberId)).orElseThrow(MemberNotFoundException::new);

        final Reply reply = replyRepository.findById(commentId).orElseThrow(ReplyNotFoundException::new);

        final CommentLike commentLike = commentLikeRepository.findByMemberAndReply(member, reply).orElseThrow(CommentLikeNotFoundException::new);

        commentLikeRepository.deleteById(commentLike.getId());
    }
}
