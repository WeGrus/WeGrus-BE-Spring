package wegrus.clubwebsite.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wegrus.clubwebsite.dto.board.ReplyCreateRequest;
import wegrus.clubwebsite.entity.board.Board;
import wegrus.clubwebsite.entity.board.Reply;
import wegrus.clubwebsite.entity.board.ReplyState;
import wegrus.clubwebsite.entity.member.Member;
import wegrus.clubwebsite.exception.BoardNotFoundException;
import wegrus.clubwebsite.exception.MemberNotFoundException;
import wegrus.clubwebsite.exception.ReplyMemberNotMatchException;
import wegrus.clubwebsite.exception.ReplyNotFoundException;
import wegrus.clubwebsite.repository.BoardRepository;
import wegrus.clubwebsite.repository.MemberRepository;
import wegrus.clubwebsite.repository.ReplyRepository;

@RequiredArgsConstructor
@Service
public class ReplyService {
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;
    private final ReplyRepository replyRepository;

    @Transactional
    public Long create(Long postId, ReplyCreateRequest request){
        String memberId = SecurityContextHolder.getContext().getAuthentication().getName();
        final Member member = memberRepository.findById(Long.valueOf(memberId)).orElseThrow(MemberNotFoundException::new);

        final Board board = boardRepository.findById(postId).orElseThrow(BoardNotFoundException::new);

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

        replyRepository.deleteById(commentId);
    }
}
