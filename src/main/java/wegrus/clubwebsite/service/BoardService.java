package wegrus.clubwebsite.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wegrus.clubwebsite.dto.board.BoardCreateRequest;
import wegrus.clubwebsite.entity.board.Board;
import wegrus.clubwebsite.entity.board.BoardState;
import wegrus.clubwebsite.entity.member.Member;
import wegrus.clubwebsite.exception.MemberNotFoundException;
import wegrus.clubwebsite.repository.BoardRepository;
import wegrus.clubwebsite.repository.MemberRepository;

@RequiredArgsConstructor
@Service
public class BoardService {
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Long create(BoardCreateRequest request){
        String memberId = SecurityContextHolder.getContext().getAuthentication().getName();
        final Member member = memberRepository.findById(Long.valueOf(memberId)).orElseThrow(MemberNotFoundException::new);
        BoardState state = BoardState.ACTIVATE; // 생성되는 게시물은 모두 활성화 되어있는 상태

        Board board = Board.builder()
                .member(member)
                .category(request.getBoardCategory())
                .type(request.getBoardType())
                .title(request.getTitle())
                .content(request.getContent())
                .secretFlag(request.isSecretFlag())
                .state(state)
                .build();

        return boardRepository.save(board).getId();
    }
}