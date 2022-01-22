package wegrus.clubwebsite.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wegrus.clubwebsite.dto.board.BoardCreateRequest;
import wegrus.clubwebsite.dto.board.BoardUpdateRequest;
import wegrus.clubwebsite.entity.board.Board;
import wegrus.clubwebsite.entity.board.BoardState;
import wegrus.clubwebsite.entity.board.PostLike;
import wegrus.clubwebsite.entity.member.Member;
import wegrus.clubwebsite.exception.*;
import wegrus.clubwebsite.repository.*;

import java.util.Optional;


@RequiredArgsConstructor
@Service
public class BoardService {
    private final BoardRepository boardRepository;
    private final MemberRepository memberRepository;
    private final ReplyRepository replyRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommentLikeRepository commentLikeRepository;

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

    @Transactional
    public Long update(BoardUpdateRequest request){
        String memberId = SecurityContextHolder.getContext().getAuthentication().getName();
        final Member member = memberRepository.findById(Long.valueOf(memberId)).orElseThrow(MemberNotFoundException::new);

        final Board board = boardRepository.findById(request.getBoardId()).orElseThrow(BoardNotFoundException::new);

        if(!member.getId().equals(board.getMember().getId())){
            throw new BoardMemberNotMatchException();
        }

        board.update(request);
        return board.getId();
    }

    @Transactional
    public void delete(Long postId){
        String memberId = SecurityContextHolder.getContext().getAuthentication().getName();
        final Member member = memberRepository.findById(Long.valueOf(memberId)).orElseThrow(MemberNotFoundException::new);

        final Board board = boardRepository.findById(postId).orElseThrow(BoardNotFoundException::new);

        if(!member.getId().equals(board.getMember().getId())){
            throw new BoardMemberNotMatchException();
        }

        // 게시물 댓글 추천 삭제
        commentLikeRepository.deleteCommentLikesByBoard(postId);

        // 댓글 삭제
        replyRepository.deleteRepliesByBoard(board);

        // 게시물 추천 기록 삭제
        postLikeRepository.deletePostLikesByBoard(board);

        boardRepository.delete(board);
    }

    @Transactional
    public Long like(Long postId){
        String memberId = SecurityContextHolder.getContext().getAuthentication().getName();
        final Member member = memberRepository.findById(Long.valueOf(memberId)).orElseThrow(MemberNotFoundException::new);

        final Board board = boardRepository.findById(postId).orElseThrow(BoardNotFoundException::new);

        Optional<PostLike> postLikes = postLikeRepository.findByMemberAndBoard(member, board);

        // 추천 기록이 있다면
        if(postLikes.isPresent()){
            throw new PostLikeAlreadyExistException();
        }

        PostLike postLike = PostLike.builder()
                .board(board)
                .member(member)
                .build();

        postLikeRepository.save(postLike);

        return postLike.getId();
    }

    @Transactional
    public void dislike(Long postId){
        String memberId = SecurityContextHolder.getContext().getAuthentication().getName();
        final Member member = memberRepository.findById(Long.valueOf(memberId)).orElseThrow(MemberNotFoundException::new);

        final Board board = boardRepository.findById(postId).orElseThrow(BoardNotFoundException::new);

        final PostLike postLike = postLikeRepository.findByMemberAndBoard(member, board).orElseThrow(PostLikeNotFoundException::new);

        postLikeRepository.delete(postLike);
    }
}