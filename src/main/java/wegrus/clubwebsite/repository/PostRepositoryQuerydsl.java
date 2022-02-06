package wegrus.clubwebsite.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import wegrus.clubwebsite.dto.post.BookmarkDto;
import wegrus.clubwebsite.dto.post.PostDto;
import wegrus.clubwebsite.dto.post.PostReplyDto;

public interface PostRepositoryQuerydsl {

    Page<PostDto> findPostDtoPageByMemberIdOrderByCreatedDateDesc(Long memberId, Pageable pageable);
    Page<BookmarkDto> findBookmarkedPostDtoPageByMemberIdOrderByCreatedDateDesc(Long memberId, Pageable pageable);
    Page<PostReplyDto> findPostReplyDtoPageByMemberIdOrderByCreatedDateDesc(Long memberId, Pageable pageable);
}
