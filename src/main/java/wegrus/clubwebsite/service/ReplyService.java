package wegrus.clubwebsite.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wegrus.clubwebsite.dto.post.ReplyCreateRequest;
import wegrus.clubwebsite.entity.post.Post;
import wegrus.clubwebsite.entity.post.ReplyLike;
import wegrus.clubwebsite.entity.post.Reply;
import wegrus.clubwebsite.entity.post.ReplyState;
import wegrus.clubwebsite.entity.member.Member;
import wegrus.clubwebsite.exception.*;
import wegrus.clubwebsite.repository.PostRepository;
import wegrus.clubwebsite.repository.ReplyLikeRepository;
import wegrus.clubwebsite.repository.MemberRepository;
import wegrus.clubwebsite.repository.ReplyRepository;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ReplyService {
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final ReplyRepository replyRepository;
    private final ReplyLikeRepository replyLikeRepository;

    @Transactional
    public Long create(ReplyCreateRequest request){
        String memberId = SecurityContextHolder.getContext().getAuthentication().getName();
        final Member member = memberRepository.findById(Long.valueOf(memberId)).orElseThrow(MemberNotFoundException::new);

        final Post post = postRepository.findById(request.getPostId()).orElseThrow(PostNotFoundException::new);

        ReplyState replyState = ReplyState.ACTIVATE;

        Reply reply = Reply.builder()
                .member(member)
                .post(post)
                .content(request.getContent())
                .state(replyState)
                .build();

        post.postReplyNum(post.getPostReplyNum()+1);

        return replyRepository.save(reply).getId();
    }

    @Transactional
    public void delete(Long commentId){
        String memberId = SecurityContextHolder.getContext().getAuthentication().getName();
        final Member member = memberRepository.findById(Long.valueOf(memberId)).orElseThrow(MemberNotFoundException::new);

        final Reply reply = replyRepository.findById(commentId).orElseThrow(ReplyNotFoundException::new);

        final Post post = reply.getPost();

        if(!member.getId().equals(reply.getMember().getId())) {
            throw new ReplyMemberNotMatchException();
        }

        post.postReplyNum(post.getPostReplyNum()-1);

        // 댓글 추천수 제거
        replyLikeRepository.deleteReplyLikesByReply(reply);

        replyRepository.delete(reply);
    }

    @Transactional
    public Long like(Long commentId){
        String memberId = SecurityContextHolder.getContext().getAuthentication().getName();
        final Member member = memberRepository.findById(Long.valueOf(memberId)).orElseThrow(MemberNotFoundException::new);

        final Reply reply = replyRepository.findById(commentId).orElseThrow(ReplyNotFoundException::new);

        Optional<ReplyLike> replyLikes = replyLikeRepository.findByMemberAndReply(member, reply);

        // 이미 추천이 있다면
        if(replyLikes.isPresent()){
            throw new ReplyLikeAlreadyExistException();
        }

        ReplyLike replyLike = ReplyLike.builder()
                .member(member)
                .reply(reply)
                .build();

        replyLikeRepository.save(replyLike);
        return replyLike.getId();
    }

    @Transactional
    public void dislike(Long commentId){
        String memberId = SecurityContextHolder.getContext().getAuthentication().getName();
        final Member member = memberRepository.findById(Long.valueOf(memberId)).orElseThrow(MemberNotFoundException::new);

        final Reply reply = replyRepository.findById(commentId).orElseThrow(ReplyNotFoundException::new);

        final ReplyLike replyLike = replyLikeRepository.findByMemberAndReply(member, reply).orElseThrow(ReplyLikeNotFoundException::new);

        replyLikeRepository.delete(replyLike);
    }
}
