package wegrus.clubwebsite.repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import wegrus.clubwebsite.dto.member.MemberDto;
import wegrus.clubwebsite.dto.member.MemberSearchType;
import wegrus.clubwebsite.entity.member.Member;

import java.util.List;
import java.util.stream.Collectors;

import static wegrus.clubwebsite.entity.member.QMember.member;
import static wegrus.clubwebsite.entity.member.QMemberRole.memberRole;
import static wegrus.clubwebsite.entity.member.QRole.role;

@RequiredArgsConstructor
public class MemberRepositoryQuerydslImpl implements MemberRepositoryQuerydsl {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<MemberDto> findMemberDtoPage(Pageable pageable) {
        final List<Member> members = queryFactory
                .selectFrom(member)
                .innerJoin(member.roles, memberRole).fetchJoin()
                .innerJoin(memberRole.role, role).fetchJoin()
                .orderBy(memberSort(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        final List<MemberDto> memberDtos = members.stream()
                .map(MemberDto::new)
                .collect(Collectors.toList());

        return new PageImpl<>(memberDtos, pageable, memberDtos.size());
    }

    @Override
    public Page<MemberDto> findMemberDtoPageByWordContainingAtSearchType(Pageable pageable, MemberSearchType searchType, String word) {
        final List<Member> members = queryFactory
                .selectFrom(member)
                .innerJoin(member.roles, memberRole).fetchJoin()
                .innerJoin(memberRole.role, role).fetchJoin()
                .where(search(searchType, word))
                .orderBy(memberSort(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        final List<MemberDto> memberDtos = members.stream()
                .map(MemberDto::new)
                .collect(Collectors.toList());

        return new PageImpl<>(memberDtos, pageable, memberDtos.size());
    }

    private BooleanExpression search(MemberSearchType searchType, String word) {
        switch (searchType.getField()) {
            case "member_name":
                return member.name.contains(word);
            case "member_student_id":
                return member.studentId.contains(word);
            case "member_department":
                return member.department.contains(word);
            case "member_phone":
                return member.phone.contains(word);
        }
        return null;
    }

    private OrderSpecifier<?> memberSort(Pageable pageable) {
        for (Sort.Order order : pageable.getSort()) {
            final Order direction = order.getDirection().isAscending() ? Order.ASC : Order.DESC;
            switch (order.getProperty()) {
                case "member_id":
                    return new OrderSpecifier<>(direction, member.id);
                case "member_name":
                    return new OrderSpecifier<>(direction, member.name);
                case "member_student_id":
                    return new OrderSpecifier<>(direction, member.studentId);
                case "member_department":
                    return new OrderSpecifier<>(direction, member.department);
                case "member_phone":
                    return new OrderSpecifier<>(direction, member.phone);
                case "member_gender":
                    return new OrderSpecifier<>(direction, member.gender);
                case "member_academic_status":
                    return new OrderSpecifier<>(direction, member.academicStatus);
                case "member_grade":
                    return new OrderSpecifier<>(direction, member.grade);
            }
        }

        return null;
    }

}
