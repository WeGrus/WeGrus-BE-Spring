package wegrus.clubwebsite.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import wegrus.clubwebsite.dto.member.RequestDto;
import wegrus.clubwebsite.entity.Request;
import wegrus.clubwebsite.entity.member.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static wegrus.clubwebsite.entity.QRequest.request;
import static wegrus.clubwebsite.entity.member.QMember.member;
import static wegrus.clubwebsite.entity.member.QMemberRole.memberRole;
import static wegrus.clubwebsite.entity.member.QRole.role;

@RequiredArgsConstructor
public class RequestRepositoryQuerydslImpl implements RequestRepositoryQuerydsl {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<RequestDto> findRequestDtoPageByRole(MemberRoles memberRoles, Pageable pageable) {
        final List<Request> requests = queryFactory
                .selectFrom(request)
                .innerJoin(request.role, role).fetchJoin()
                .where(request.role.name.eq(memberRoles.name()))
                .orderBy(request.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        final long total = queryFactory
                .selectFrom(request)
                .where(request.role.name.eq(memberRoles.name()))
                .fetchCount();

        final List<Long> memberIds = requests.stream()
                .map(r -> r.getMember().getId())
                .collect(Collectors.toList());

        final List<Member> members = queryFactory
                .selectFrom(member)
                .where(member.id.in(memberIds))
                .innerJoin(member.roles, memberRole).fetchJoin()
                .innerJoin(memberRole.role, role).fetchJoin()
                .fetch();
        final Map<Long, List<Member>> memberMap = members.stream()
                .collect(Collectors.groupingBy(Member::getId));

        final List<RequestDto> requestDtos = requests.stream()
                .map(r -> new RequestDto(r.getId(), r.getRole().getName(), r.getCreatedDate(), memberMap.get(r.getMember().getId()).get(0)))
                .collect(Collectors.toList());

        return new PageImpl<>(requestDtos, pageable, total);
    }
}
