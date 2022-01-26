package wegrus.clubwebsite.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wegrus.clubwebsite.dto.board.PostCreateRequest;
import wegrus.clubwebsite.dto.board.PostUpdateRequest;
import wegrus.clubwebsite.entity.board.Post;
import wegrus.clubwebsite.entity.board.PostState;
import wegrus.clubwebsite.entity.board.PostLike;
import wegrus.clubwebsite.entity.member.Member;
import wegrus.clubwebsite.exception.*;
import wegrus.clubwebsite.repository.*;

import java.util.Optional;


@RequiredArgsConstructor
@Service
public class BoardService {
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final ReplyRepository replyRepository;
    private final PostLikeRepository postLikeRepository;
    private final CommentLikeRepository commentLikeRepository;

    @Transactional
    public Long create(PostCreateRequest request){
        String postId = SecurityContextHolder.getContext().getAuthentication().getName();
        final Member member = memberRepository.findById(Long.valueOf(postId)).orElseThrow(MemberNotFoundException::new);
        PostState state = PostState.ACTIVATE; // 생성되는 게시물은 모두 활성화 되어있는 상태

        Post post = Post.builder()
                .member(member)
                .category(request.getBoardCategory())
                .type(request.getBoardType())
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
        commentLikeRepository.deleteCommentLikesByPost(postId);

        // 댓글 삭제
        replyRepository.deleteRepliesByPost(post);

        // 게시물 추천 기록 삭제
        postLikeRepository.deletePostLikesByPost(post);

        postRepository.delete(post);
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
