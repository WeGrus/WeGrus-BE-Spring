package wegrus.clubwebsite.Board;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import wegrus.clubwebsite.entity.board.*;
import wegrus.clubwebsite.entity.member.Member;
import wegrus.clubwebsite.entity.member.MemberAcademicStatus;
import wegrus.clubwebsite.entity.member.MemberGrade;
import wegrus.clubwebsite.repository.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class PostRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    ReplyRepository replyRepository;

    @Autowired
    PostLikeRepository postLikeRepository;

    @Autowired
    ViewRepository viewRepository;

    @Autowired
    CommentLikeRepository commentLikeRepository;

    @Test
    @DisplayName("게시물 관련 엔티티들 등록 및 확인")
    public void boardSaveLoad(){
        final Member member = Member.builder()
                .name("김철수")
                .department("컴퓨터공학과")
                .grade(MemberGrade.JUNIOR)
                .userId("21358921357")
                .phone("010-1234-5678")
                .email("test1@naver.com")
                .academicStatus(MemberAcademicStatus.ATTENDING)
                .build();

        memberRepository.save(member);

        // Boards
        String title = "테스트 제목";
        String content = "테스트 내용";
        BoardCategory boardCategory = BoardCategory.BOARD;
        BoardType boardType = BoardType.FREE;
        PostState postState = PostState.ACTIVATE;

        final Post post = Post.builder()
                .member(member)
                .category(boardCategory)
                .type(boardType)
                .title(title)
                .content(content)
                .secretFlag(false)
                .state(postState)
                .build();

        postRepository.save(post);

        List<Post> postList = postRepository.findAll();

        Post post1 = postList.get(0);
        assertThat(post1.getTitle()).isEqualTo(title);
        assertThat(post1.getContent()).isEqualTo(content);
        assertThat(post1.getCategory()).isEqualTo(boardCategory);
        assertThat(post1.getType()).isEqualTo(boardType);
        assertThat(post1.getState()).isEqualTo(postState);


        // Replies
        String replyContent = "테스트 댓글 내용";
        ReplyState replyState = ReplyState.ACTIVATE;
        final Reply reply = Reply.builder()
                .member(member)
                .post(post)
                .content(replyContent)
                .state(replyState)
                .build();

        replyRepository.save(reply);

        List<Reply> replyList = replyRepository.findAll();

        Reply reply1 = replyList.get(0);
        assertThat(reply1.getContent()).isEqualTo(replyContent);
        assertThat(reply1.getState()).isEqualTo(replyState);

        // Post_Likes
        final PostLike postLike = PostLike.builder()
                .member(member)
                .post(post)
                .build();

        postLikeRepository.save(postLike);

        List<PostLike> postLikeList = postLikeRepository.findAll();

        PostLike postLike1 = postLikeList.get(0);
        assertThat(postLike1.getMember()).isEqualTo(member);
        assertThat(postLike1.getPost()).isEqualTo(post);

        // Views
        final View view = View.builder()
                .member(member)
                .post(post)
                .build();

        viewRepository.save(view);

        List<View> viewList = viewRepository.findAll();

        View view1 = viewList.get(0);
        assertThat(view1.getMember()).isEqualTo(member);
        assertThat(view1.getPost()).isEqualTo(post);

        // Comment_Likes
        final CommentLike commentLike = CommentLike.builder()
                .member(member)
                .reply(reply)
                .build();

        commentLikeRepository.save(commentLike);

        List<CommentLike> commentLikeList = commentLikeRepository.findAll();

        CommentLike commentLike1 = commentLikeList.get(0);
        assertThat(commentLike1.getMember()).isEqualTo(member);
        assertThat(commentLike1.getReply()).isEqualTo(reply);
    }
}
