package wegrus.clubwebsite.Board;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import wegrus.clubwebsite.entity.member.Member;
import wegrus.clubwebsite.entity.member.MemberAcademicStatus;
import wegrus.clubwebsite.entity.member.MemberGrade;
import wegrus.clubwebsite.entity.post.*;
import wegrus.clubwebsite.exception.*;
import wegrus.clubwebsite.repository.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class PostRepositoryTest {

    @MockBean
    JPAQueryFactory queryFactory;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    BoardCategoryRepository boardCategoryRepository;

    @Autowired
    BoardRepository boardRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    ReplyRepository replyRepository;

    @Autowired
    PostLikeRepository postLikeRepository;

    @Autowired
    ViewRepository viewRepository;

    @Autowired
    ReplyLikeRepository replyLikeRepository;

    @BeforeEach
    void init(){
        // Member
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

        // BoardCategory
        final BoardCategory boardCategory = BoardCategory.builder()
                .name("BOARD")
                .build();

        boardCategoryRepository.save(boardCategory);

        // Board
        final Board board = Board.builder()
                .boardCategory(boardCategory)
                .name("FREE")
                .build();

        boardRepository.save(board);

        // Post
        String title = "테스트 제목";
        String content = "테스트 내용";
        PostState postState = PostState.ACTIVATE;

        final Post post = Post.builder()
                .member(member)
                .board(board)
                .title(title)
                .content(content)
                .secretFlag(false)
                .state(postState)
                .build();

        postRepository.save(post);

        // Reply
        String replyContent = "테스트 댓글 내용";
        ReplyState replyState = ReplyState.ACTIVATE;
        final Reply reply = Reply.builder()
                .member(member)
                .post(post)
                .content(replyContent)
                .state(replyState)
                .build();

        replyRepository.save(reply);

        // PostLike
        final PostLike postLike = PostLike.builder()
                .member(member)
                .post(post)
                .build();

        postLikeRepository.save(postLike);

        // View
        final View view = View.builder()
                .member(member)
                .post(post)
                .build();

        viewRepository.save(view);

        // CommentLike
        final ReplyLike replyLike = ReplyLike.builder()
                .member(member)
                .reply(reply)
                .build();

        replyLikeRepository.save(replyLike);
    }

    @Test
    @DisplayName("게시판 조회: name")
    public void BoardFindByName(){
        // given
        final String boardName = "FREE";

        // when
        final Board board = boardRepository.findByName(boardName).orElseThrow(BoardNotFoundException::new);

        // then
        assertThat(board.getName()).isEqualTo(boardName);
    }

    @Test
    @DisplayName("게시물 조회: id")
    public void PostFindById(){
        // given
        List<Post> postList = postRepository.findAll();
        final Long postId = postList.get(0).getId();

        // when
        final Post post = postRepository.findById(postId).orElseThrow(PostNotFoundException::new);

        // then
        assertThat(post.getId()).isEqualTo(postId);
    }

    @Test
    @DisplayName("게시물 추천 조회: member_id and post_id")
    public void PostLikeFindByMemberIdAndPostId(){
        // given
        List<Member> memberList = memberRepository.findAll();
        final Long memberId = memberList.get(0).getId();
        List<Post> postList = postRepository.findAll();
        final Long postId = postList.get(0).getId();

        // when
        final Member member = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
        final Post post = postRepository.findById(postId).orElseThrow(PostNotFoundException::new);

        final PostLike postLike = postLikeRepository.findByMemberAndPost(member, post).orElseThrow(PostLikeNotFoundException::new);

        // then
        assertThat(postLike.getPost().getId()).isEqualTo(postId);
        assertThat(postLike.getMember().getId()).isEqualTo(memberId);
    }

    @Test
    @DisplayName("댓글 조회: id")
    public void ReplyFindById(){
        // given
        List<Reply> replyList = replyRepository.findAll();
        final Long replyId = replyList.get(0).getId();

        // when
        final Reply reply = replyRepository.findById(replyId).orElseThrow(ReplyNotFoundException::new);

        // then
        assertThat(reply.getId()).isEqualTo(replyId);
    }

    @Test
    @DisplayName("댓글 추천 조회: member_id and reply_id")
    public void ReplyLikeFindByMemberIdAndReplyId(){
        // given
        List<Member> memberList = memberRepository.findAll();
        final Long memberId = memberList.get(0).getId();
        List<Reply> replyList = replyRepository.findAll();
        final Long replyId = replyList.get(0).getId();

        // when
        final Member member = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
        final Reply reply = replyRepository.findById(replyId).orElseThrow(ReplyNotFoundException::new);

        final ReplyLike replyLike = replyLikeRepository.findByMemberAndReply(member, reply).orElseThrow(ReplyLikeNotFoundException::new);

        // then
        assertThat(replyLike.getReply().getId()).isEqualTo(replyId);
        assertThat(replyLike.getMember().getId()).isEqualTo(memberId);
    }


}
