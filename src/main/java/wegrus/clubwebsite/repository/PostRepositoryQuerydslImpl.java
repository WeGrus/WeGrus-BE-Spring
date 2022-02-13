package wegrus.clubwebsite.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import wegrus.clubwebsite.dto.post.*;
import wegrus.clubwebsite.entity.post.Bookmark;
import wegrus.clubwebsite.entity.post.Reply;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static wegrus.clubwebsite.entity.member.QMember.member;
import static wegrus.clubwebsite.entity.post.QBoard.board;
import static wegrus.clubwebsite.entity.post.QBoardCategory.boardCategory;
import static wegrus.clubwebsite.entity.post.QBookmark.bookmark;
import static wegrus.clubwebsite.entity.post.QPost.post;
import static wegrus.clubwebsite.entity.post.QReply.reply;

@RequiredArgsConstructor
public class PostRepositoryQuerydslImpl implements PostRepositoryQuerydsl {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<PostDto> findPostDtoPageByMemberIdOrderByCreatedDateDesc(Long memberId, Pageable pageable) {
        final List<PostDto> postDtos = queryFactory
                .select(new QPostDto(
                        post.id,
                        post.member.id,
                        post.member.studentId.substring(2, 4).append(post.member.name),
                        post.member.image,
                        post.board.name,
                        post.board.boardCategory.name,
                        post.type.stringValue(),
                        post.title,
                        post.content,
                        post.createdDate,
                        post.updatedDate,
                        post.postLikeNum,
                        post.postReplyNum,
                        post.views.size(),
                        post.bookmarks.size(),
                        post.secretFlag
                ))
                .from(post)
                .innerJoin(post.member, member)
                .innerJoin(post.board, board)
                .innerJoin(post.board.boardCategory, boardCategory)
                .where(post.member.id.eq(memberId))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(post.createdDate.desc())
                .fetch();

        final long total = queryFactory
                .selectFrom(post)
                .where(post.member.id.eq(memberId))
                .fetchCount();

        return new PageImpl<>(postDtos, pageable, total);
    }

    @Override
    public Page<BookmarkDto> findBookmarkedPostDtoPageByMemberIdOrderByCreatedDateDesc(Long memberId, Pageable pageable) {
        final List<Bookmark> bookmarks = queryFactory
                .selectFrom(bookmark)
                .innerJoin(bookmark.post, post)
                .where(bookmark.member.id.eq(memberId))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(bookmark.id.desc())
                .fetch();
        final List<Long> postIds = bookmarks.stream()
                .map(b -> b.getPost().getId())
                .collect(Collectors.toList());

        final long total = queryFactory
                .selectFrom(bookmark)
                .where(bookmark.member.id.eq(memberId))
                .fetchCount();

        final List<PostDto> postDtos = queryFactory
                .select(new QPostDto(
                        post.id,
                        post.member.id,
                        post.member.studentId.substring(2, 4).append(post.member.name),
                        post.member.image,
                        post.board.name,
                        post.board.boardCategory.name,
                        post.type.stringValue(),
                        post.title,
                        post.content,
                        post.createdDate,
                        post.updatedDate,
                        post.postLikeNum,
                        post.postReplyNum,
                        post.views.size(),
                        post.bookmarks.size(),
                        post.secretFlag
                ))
                .from(post)
                .innerJoin(post.member, member)
                .innerJoin(post.board, board)
                .innerJoin(post.board.boardCategory, boardCategory)
                .where(post.id.in(postIds))
                .fetch();
        final Map<Long, PostDto> postDtoMap = postDtos.stream()
                .collect(Collectors.toMap(PostDto::getPostId, p -> p));

        final List<BookmarkDto> bookmarkDtos = bookmarks.stream()
                .map(b -> new BookmarkDto(b.getId(), postDtoMap.get(b.getPost().getId())))
                .collect(Collectors.toList());
        return new PageImpl<>(bookmarkDtos, pageable, total);
    }

    @Override
    public Page<PostReplyDto> findPostReplyDtoPageByMemberIdOrderByCreatedDateDesc(Long memberId, Pageable pageable) {
        final List<Reply> replies = queryFactory
                .selectFrom(reply)
                .innerJoin(reply.member, member)
                .innerJoin(reply.post, post)
                .where(reply.member.id.eq(memberId))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(reply.createdDate.desc())
                .fetch();
        final List<Long> postIds = replies.stream()
                .map(r -> r.getPost().getId())
                .collect(Collectors.toList());

        final long total = queryFactory
                .selectFrom(reply)
                .where(reply.member.id.eq(memberId))
                .fetchCount();

        final List<PostDto> postDtos = queryFactory
                .select(new QPostDto(
                        post.id,
                        post.member.id,
                        post.member.studentId.substring(2, 4).append(post.member.name),
                        post.member.image,
                        post.board.name,
                        post.board.boardCategory.name,
                        post.type.stringValue(),
                        post.title,
                        post.content,
                        post.createdDate,
                        post.updatedDate,
                        post.postLikeNum,
                        post.postReplyNum,
                        post.views.size(),
                        post.bookmarks.size(),
                        post.secretFlag
                ))
                .from(post)
                .innerJoin(post.member, member)
                .innerJoin(post.board, board)
                .innerJoin(post.board.boardCategory, boardCategory)
                .where(post.id.in(postIds))
                .fetch();
        final Map<Long, PostDto> postDtoMap = postDtos.stream()
                .collect(Collectors.toMap(PostDto::getPostId, p -> p));

        final List<PostReplyDto> replyDtos = replies.stream()
                .map(r -> new PostReplyDto(r, postDtoMap.get(r.getPost().getId())))
                .collect(Collectors.toList());
        return new PageImpl<>(replyDtos, pageable, total);
    }
}
