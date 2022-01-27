package wegrus.clubwebsite.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wegrus.clubwebsite.dto.post.*;
import wegrus.clubwebsite.entity.post.*;
import wegrus.clubwebsite.entity.member.Member;
import wegrus.clubwebsite.exception.*;
import wegrus.clubwebsite.repository.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Service
public class PostService {
    private final BoardRepository boardRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final ReplyRepository replyRepository;
    private final PostLikeRepository postLikeRepository;
    private final ReplyLikeRepository replyLikeRepository;
    private final ViewRepository viewRepository;

    @Transactional
    public Long create(PostCreateRequest request){
        String memberId = SecurityContextHolder.getContext().getAuthentication().getName();
        final Member member = memberRepository.findById(Long.valueOf(memberId)).orElseThrow(MemberNotFoundException::new);
        final Board board = boardRepository.findByName(request.getBoardName()).orElseThrow(BoardNotFoundException::new);
        PostState state = PostState.ACTIVATE; // 생성되는 게시물은 모두 활성화 되어있는 상태

        Post post = Post.builder()
                .member(member)
                .board(board)
                .type(request.getType())
                .title(request.getTitle())
                .content(request.getContent())
                .secretFlag(request.isSecretFlag())
                .state(state)
                .build();

        return postRepository.save(post).getId();
    }

    @Transactional
    public Long update(PostUpdateRequest request){
        String memberId = SecurityContextHolder.getContext().getAuthentication().getName();
        final Member member = memberRepository.findById(Long.valueOf(memberId)).orElseThrow(MemberNotFoundException::new);

        final Post post = postRepository.findById(request.getPostId()).orElseThrow(PostNotFoundException::new);

        if(!member.getId().equals(post.getMember().getId())){
            throw new PostMemberNotMatchException();
        }

        post.update(request);
        return post.getId();
    }

    @Transactional
    public void delete(Long postId){
        String memberId = SecurityContextHolder.getContext().getAuthentication().getName();
        final Member member = memberRepository.findById(Long.valueOf(memberId)).orElseThrow(MemberNotFoundException::new);

        final Post post = postRepository.findById(postId).orElseThrow(PostNotFoundException::new);

        if(!member.getId().equals(post.getMember().getId())){
            throw new PostMemberNotMatchException();
        }

        // 게시물 댓글 추천 삭제
        replyLikeRepository.deleteReplyLikesByPost(postId);

        // 댓글 삭제
        replyRepository.deleteRepliesByPost(post);

        // 게시물 추천 기록 삭제
        postLikeRepository.deletePostLikesByPost(post);

        postRepository.delete(post);
    }

    @Transactional
    public PostResponse view(Long postId){
        String memberId = SecurityContextHolder.getContext().getAuthentication().getName();
        final Member member = memberRepository.findById(Long.valueOf(memberId)).orElseThrow(MemberNotFoundException::new);

        final Post post = postRepository.findById(postId).orElseThrow(PostNotFoundException::new);

        // 조회수 추가
        Optional<View> views = viewRepository.findByMemberAndPost(member, post);

        if(views.isEmpty()){
            View view = View.builder()
                    .member(member)
                    .post(post)
                    .build();
            viewRepository.save(view);
        }

        // 정보 반환
        List<ReplyDto> replies = post.getReplies()
                .stream()
                .map(ReplyDto::new)
                .collect(Collectors.toList());

        return new PostResponse(new PostDto(post), replies);
    }

    @Transactional
    public Long like(Long postId){
        String memberId = SecurityContextHolder.getContext().getAuthentication().getName();
        final Member member = memberRepository.findById(Long.valueOf(memberId)).orElseThrow(MemberNotFoundException::new);

        final Post post = postRepository.findById(postId).orElseThrow(PostNotFoundException::new);

        Optional<PostLike> postLikes = postLikeRepository.findByMemberAndPost(member, post);

        // 추천 기록이 있다면
        if(postLikes.isPresent()){
            throw new PostLikeAlreadyExistException();
        }

        PostLike postLike = PostLike.builder()
                .post(post)
                .member(member)
                .build();

        postLikeRepository.save(postLike);

        return postLike.getId();
    }

    @Transactional
    public void dislike(Long postId){
        String memberId = SecurityContextHolder.getContext().getAuthentication().getName();
        final Member member = memberRepository.findById(Long.valueOf(memberId)).orElseThrow(MemberNotFoundException::new);

        final Post post = postRepository.findById(postId).orElseThrow(PostNotFoundException::new);

        final PostLike postLike = postLikeRepository.findByMemberAndPost(member, post).orElseThrow(PostLikeNotFoundException::new);

        postLikeRepository.delete(postLike);
    }
}
