package wegrus.clubwebsite.repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import wegrus.clubwebsite.dto.member.*;
import wegrus.clubwebsite.entity.group.Group;
import wegrus.clubwebsite.entity.group.GroupRoles;
import wegrus.clubwebsite.entity.member.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static wegrus.clubwebsite.entity.QRequest.request;
import static wegrus.clubwebsite.entity.group.QGroupMember.groupMember;
import static wegrus.clubwebsite.entity.member.QMember.member;
import static wegrus.clubwebsite.entity.member.QMemberRole.memberRole;
import static wegrus.clubwebsite.entity.member.QRole.role;

@RequiredArgsConstructor
public class MemberRepositoryQuerydslImpl implements MemberRepositoryQuerydsl {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<MemberDto> findMemberDtoById(Long id) {
        final Member member = queryFactory
                .selectFrom(QMember.member)
                .innerJoin(QMember.member.roles, memberRole).fetchJoin()
                .innerJoin(memberRole.role, role).fetchJoin()
                .where(QMember.member.id.eq(id))
                .fetchOne();

        if (member == null)
            return Optional.empty();

        final List<GroupDto> groupDtos = queryFactory
                .select(new QGroupDto(
                        groupMember.group.id,
                        groupMember.group.name,
                        groupMember.role,
                        groupMember.createdDate,
                        groupMember.member.id
                ))
                .from(groupMember)
                .where(groupMember.member.id.in(member.getId()).and(groupMember.role.ne(GroupRoles.APPLICANT)))
                .innerJoin(groupMember.group)
                .fetch();

        final MemberDto memberDto = new MemberDto(member);
        if (!groupDtos.isEmpty())
            memberDto.setGroups(groupDtos);

        return Optional.of(memberDto);
    }

    @Override
    public Optional<MemberSimpleDto> findMemberSimpleDtoById(Long id) {
        final Member member = queryFactory
                .selectFrom(QMember.member)
                .innerJoin(QMember.member.roles, memberRole).fetchJoin()
                .innerJoin(memberRole.role, role).fetchJoin()
                .where(QMember.member.id.eq(id))
                .fetchOne();

        if (member == null)
            return Optional.empty();

        final List<GroupDto> groupDtos = queryFactory
                .select(new QGroupDto(
                        groupMember.group.id,
                        groupMember.group.name,
                        groupMember.role,
                        groupMember.createdDate,
                        groupMember.member.id
                ))
                .from(groupMember)
                .where(groupMember.member.id.in(member.getId()).and(groupMember.role.ne(GroupRoles.APPLICANT)))
                .innerJoin(groupMember.group)
                .fetch();

        final MemberSimpleDto memberSimpleDto = new MemberSimpleDto(member);
        if (!groupDtos.isEmpty())
            memberSimpleDto.setGroups(groupDtos);

        return Optional.of(memberSimpleDto);
    }

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

        final long total = queryFactory
                .selectFrom(member)
                .fetchCount();

        final List<Long> memberIds = members.stream()
                .map(Member::getId)
                .collect(Collectors.toList());
        final Map<Long, List<GroupDto>> groupDtoMap = queryFactory
                .select(new QGroupDto(
                        groupMember.group.id,
                        groupMember.group.name,
                        groupMember.role,
                        groupMember.createdDate,
                        groupMember.member.id
                ))
                .from(groupMember)
                .where(groupMember.member.id.in(memberIds).and(groupMember.role.ne(GroupRoles.APPLICANT)))
                .innerJoin(groupMember.group)
                .fetch()
                .stream()
                .collect(Collectors.groupingBy(GroupDto::getMemberId));

        final List<MemberDto> memberDtos = members.stream()
                .map(MemberDto::new)
                .collect(Collectors.toList());
        memberDtos.forEach(m -> {
                    if (groupDtoMap.get(m.getId()) != null)
                        m.setGroups(groupDtoMap.get(m.getId()));
                }
        );

        return new PageImpl<>(memberDtos, pageable, total);
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

        final long total = queryFactory
                .selectFrom(member)
                .where(search(searchType, word))
                .fetchCount();

        final List<Long> memberIds = members.stream()
                .map(Member::getId)
                .collect(Collectors.toList());
        final Map<Long, List<GroupDto>> groupDtoMap = queryFactory
                .select(new QGroupDto(
                        groupMember.group.id,
                        groupMember.group.name,
                        groupMember.role,
                        groupMember.createdDate,
                        groupMember.member.id
                ))
                .from(groupMember)
                .where(groupMember.member.id.in(memberIds).and(groupMember.role.ne(GroupRoles.APPLICANT)))
                .innerJoin(groupMember.group)
                .fetch()
                .stream()
                .collect(Collectors.groupingBy(GroupDto::getMemberId));

        final List<MemberDto> memberDtos = members.stream()
                .map(MemberDto::new)
                .collect(Collectors.toList());
        memberDtos.forEach(m -> {
                    if (groupDtoMap.get(m.getId()) != null)
                        m.setGroups(groupDtoMap.get(m.getId()));
                }
        );

        return new PageImpl<>(memberDtos, pageable, total);
    }

    @Override
    public Page<MemberDto> findMemberDtoPageByGender(Pageable pageable, Gender gender) {
        final List<Member> members = queryFactory
                .selectFrom(member)
                .innerJoin(member.roles, memberRole).fetchJoin()
                .innerJoin(memberRole.role, role).fetchJoin()
                .where(member.gender.eq(gender))
                .orderBy(memberSort(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        final long total = queryFactory
                .selectFrom(member)
                .where(member.gender.eq(gender))
                .fetchCount();

        final List<Long> memberIds = members.stream()
                .map(Member::getId)
                .collect(Collectors.toList());
        final Map<Long, List<GroupDto>> groupDtoMap = queryFactory
                .select(new QGroupDto(
                        groupMember.group.id,
                        groupMember.group.name,
                        groupMember.role,
                        groupMember.createdDate,
                        groupMember.member.id
                ))
                .from(groupMember)
                .where(groupMember.member.id.in(memberIds).and(groupMember.role.ne(GroupRoles.APPLICANT)))
                .innerJoin(groupMember.group)
                .fetch()
                .stream()
                .collect(Collectors.groupingBy(GroupDto::getMemberId));

        final List<MemberDto> memberDtos = members.stream()
                .map(MemberDto::new)
                .collect(Collectors.toList());
        memberDtos.forEach(m -> {
                    if (groupDtoMap.get(m.getId()) != null)
                        m.setGroups(groupDtoMap.get(m.getId()));
                }
        );

        return new PageImpl<>(memberDtos, pageable, total);
    }

    @Override
    public Page<MemberDto> findMemberDtoPageByAcademicStatus(Pageable pageable, MemberAcademicStatus academicStatus) {
        final List<Member> members = queryFactory
                .selectFrom(member)
                .innerJoin(member.roles, memberRole).fetchJoin()
                .innerJoin(memberRole.role, role).fetchJoin()
                .where(member.academicStatus.eq(academicStatus))
                .orderBy(memberSort(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        final long total = queryFactory
                .selectFrom(member)
                .where(member.academicStatus.eq(academicStatus))
                .fetchCount();

        final List<Long> memberIds = members.stream()
                .map(Member::getId)
                .collect(Collectors.toList());
        final Map<Long, List<GroupDto>> groupDtoMap = queryFactory
                .select(new QGroupDto(
                        groupMember.group.id,
                        groupMember.group.name,
                        groupMember.role,
                        groupMember.createdDate,
                        groupMember.member.id
                ))
                .from(groupMember)
                .where(groupMember.member.id.in(memberIds))
                .innerJoin(groupMember.group)
                .fetch()
                .stream()
                .collect(Collectors.groupingBy(GroupDto::getMemberId));

        final List<MemberDto> memberDtos = members.stream()
                .map(MemberDto::new)
                .collect(Collectors.toList());
        memberDtos.forEach(m -> {
                    if (groupDtoMap.get(m.getId()) != null)
                        m.setGroups(groupDtoMap.get(m.getId()));
                }
        );

        return new PageImpl<>(memberDtos, pageable, total);
    }

    @Override
    public Page<MemberDto> findMemberDtoPageByGrade(Pageable pageable, MemberGrade grade) {
        final List<Member> members = queryFactory
                .selectFrom(member)
                .innerJoin(member.roles, memberRole).fetchJoin()
                .innerJoin(memberRole.role, role).fetchJoin()
                .where(member.grade.eq(grade))
                .orderBy(memberSort(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        final long total = queryFactory
                .selectFrom(member)
                .where(member.grade.eq(grade))
                .fetchCount();

        final List<Long> memberIds = members.stream()
                .map(Member::getId)
                .collect(Collectors.toList());
        final Map<Long, List<GroupDto>> groupDtoMap = queryFactory
                .select(new QGroupDto(
                        groupMember.group.id,
                        groupMember.group.name,
                        groupMember.role,
                        groupMember.createdDate,
                        groupMember.member.id
                ))
                .from(groupMember)
                .where(groupMember.member.id.in(memberIds))
                .innerJoin(groupMember.group)
                .fetch()
                .stream()
                .collect(Collectors.groupingBy(GroupDto::getMemberId));

        final List<MemberDto> memberDtos = members.stream()
                .map(MemberDto::new)
                .collect(Collectors.toList());
        memberDtos.forEach(m -> {
                    if (groupDtoMap.get(m.getId()) != null)
                        m.setGroups(groupDtoMap.get(m.getId()));
                }
        );

        return new PageImpl<>(memberDtos, pageable, total);
    }

    @Override
    public Page<MemberDto> findMemberDtoPageByAuthority(Pageable pageable, MemberRoleSearchType authority) {
        final List<Member> members = queryFactory
                .selectFrom(member)
                .innerJoin(member.roles, memberRole).fetchJoin()
                .innerJoin(memberRole.role, role).fetchJoin()
                .where(
                        JPAExpressions
                                .selectFrom(memberRole)
                                .where(memberRole.member.eq(member).and(memberRole.role.name.eq(authority.getRoleName())))
                                .exists()
                )
                .orderBy(memberSort(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        final long total = queryFactory
                .selectFrom(member)
                .where(
                        JPAExpressions
                                .selectFrom(memberRole)
                                .where(memberRole.member.eq(member).and(memberRole.role.name.eq(authority.getRoleName())))
                                .exists()
                )
                .fetchCount();

        final List<Long> memberIds = members.stream()
                .map(Member::getId)
                .collect(Collectors.toList());
        final List<MemberRole> memberRoles = queryFactory
                .selectFrom(memberRole)
                .innerJoin(memberRole.member, member).fetchJoin()
                .innerJoin(memberRole.role, role).fetchJoin()
                .where(memberRole.member.id.in(memberIds))
                .fetch();
        final Map<Long, List<MemberRole>> memberRoleMap = memberRoles.stream()
                .collect(Collectors.groupingBy(m -> m.getMember().getId()));

        final Map<Long, List<GroupDto>> groupDtoMap = queryFactory
                .select(new QGroupDto(
                        groupMember.group.id,
                        groupMember.group.name,
                        groupMember.role,
                        groupMember.createdDate,
                        groupMember.member.id
                ))
                .from(groupMember)
                .where(groupMember.member.id.in(memberIds))
                .innerJoin(groupMember.group)
                .fetch()
                .stream()
                .collect(Collectors.groupingBy(GroupDto::getMemberId));

        final List<MemberDto> memberDtos = members.stream()
                .map(MemberDto::new)
                .collect(Collectors.toList());
        memberDtos.forEach(m -> {
                    if (groupDtoMap.get(m.getId()) != null)
                        m.setGroups(groupDtoMap.get(m.getId()));
                }
        );
        memberDtos.forEach(
                m -> m.setRoles(memberRoleMap.get(m.getId()).stream()
                        .map(mr -> mr.getRole().getName())
                        .collect(Collectors.toList()))
        );

        return new PageImpl<>(memberDtos, pageable, total);
    }

    @Override
    public Page<MemberDto> findMemberDtoPageByGroup(Pageable pageable, Long groupId) {
        final List<Member> members = queryFactory
                .selectFrom(member)
                .innerJoin(member.roles, memberRole).fetchJoin()
                .innerJoin(memberRole.role, role).fetchJoin()
                .where(
                        JPAExpressions
                                .selectFrom(groupMember)
                                .where(groupMember.group.id.eq(groupId).and(groupMember.member.eq(member)))
                                .exists()
                )
                .orderBy(memberSort(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        final long total = queryFactory
                .selectFrom(member)
                .where(
                        JPAExpressions
                                .selectFrom(groupMember)
                                .where(groupMember.group.id.eq(groupId).and(groupMember.member.eq(member)))
                                .exists()
                )
                .fetchCount();

        final List<Long> memberIds = members.stream()
                .map(Member::getId)
                .collect(Collectors.toList());
        final Map<Long, List<GroupDto>> groupDtoMap = queryFactory
                .select(new QGroupDto(
                        groupMember.group.id,
                        groupMember.group.name,
                        groupMember.role,
                        groupMember.createdDate,
                        groupMember.member.id
                ))
                .from(groupMember)
                .where(groupMember.member.id.in(memberIds))
                .innerJoin(groupMember.group)
                .fetch()
                .stream()
                .collect(Collectors.groupingBy(GroupDto::getMemberId));

        final List<MemberDto> memberDtos = members.stream()
                .map(MemberDto::new)
                .collect(Collectors.toList());
        memberDtos.forEach(m -> {
                    if (groupDtoMap.get(m.getId()) != null)
                        m.setGroups(groupDtoMap.get(m.getId()));
                }
        );

        return new PageImpl<>(memberDtos, pageable, total);
    }

    @Override
    public Page<MemberDto> findMemberDtoPageByGroupAndRole(Pageable pageable, Group group, List<GroupRoles> groupRoles) {
        final List<Member> members = queryFactory
                .selectFrom(member)
                .innerJoin(member.roles, memberRole).fetchJoin()
                .innerJoin(memberRole.role, role).fetchJoin()
                .where(
                        JPAExpressions
                                .selectFrom(groupMember)
                                .where(groupMember.group.id.eq(group.getId()).and(groupMember.member.eq(member)).and(groupMember.role.in(groupRoles)))
                                .exists()
                )
                .orderBy(memberSort(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        final long total = queryFactory
                .selectFrom(member)
                .where(
                        JPAExpressions
                                .selectFrom(groupMember)
                                .where(groupMember.group.id.eq(group.getId()).and(groupMember.member.eq(member)).and(groupMember.role.in(groupRoles)))
                                .exists()
                )
                .fetchCount();

        final List<Long> memberIds = members.stream()
                .map(Member::getId)
                .collect(Collectors.toList());
        final Map<Long, List<GroupDto>> groupDtoMap = queryFactory
                .select(new QGroupDto(
                        groupMember.group.id,
                        groupMember.group.name,
                        groupMember.role,
                        groupMember.createdDate,
                        groupMember.member.id
                ))
                .from(groupMember)
                .where(groupMember.member.id.in(memberIds))
                .innerJoin(groupMember.group)
                .fetch()
                .stream()
                .collect(Collectors.groupingBy(GroupDto::getMemberId));

        final List<MemberDto> memberDtos = members.stream()
                .map(MemberDto::new)
                .collect(Collectors.toList());
        memberDtos.forEach(m -> {
                    if (groupDtoMap.get(m.getId()) != null)
                        m.setGroups(groupDtoMap.get(m.getId()));
                }
        );

        return new PageImpl<>(memberDtos, pageable, total);

    }

    @Override
    public Page<MemberDto> findMemberDtoPageByWordContainingAtRequesterSearchType(Pageable pageable, RequesterSearchType type, String word) {
        final List<Long> ids = queryFactory
                .selectFrom(request)
                .innerJoin(request.member, member).fetchJoin()
                .where(searchRequester(type, word))
                .orderBy(request.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch()
                .stream()
                .map(r -> r.getMember().getId())
                .collect(Collectors.toList());

        final List<Member> members = queryFactory
                .selectFrom(member)
                .innerJoin(member.roles, memberRole).fetchJoin()
                .innerJoin(memberRole.role, role).fetchJoin()
                .where(member.id.in(ids))
                .fetch();

        final long total = queryFactory
                .selectFrom(request)
                .innerJoin(request.member, member).fetchJoin()
                .where(searchRequester(type, word))
                .fetchCount();

        final List<Long> memberIds = members.stream()
                .map(Member::getId)
                .collect(Collectors.toList());
        final Map<Long, List<GroupDto>> groupDtoMap = queryFactory
                .select(new QGroupDto(
                        groupMember.group.id,
                        groupMember.group.name,
                        groupMember.role,
                        groupMember.createdDate,
                        groupMember.member.id
                ))
                .from(groupMember)
                .where(groupMember.member.id.in(memberIds).and(groupMember.role.ne(GroupRoles.APPLICANT)))
                .innerJoin(groupMember.group)
                .fetch()
                .stream()
                .collect(Collectors.groupingBy(GroupDto::getMemberId));

        final List<MemberDto> memberDtos = members.stream()
                .map(MemberDto::new)
                .collect(Collectors.toList());
        memberDtos.forEach(m -> {
                    if (groupDtoMap.get(m.getId()) != null)
                        m.setGroups(groupDtoMap.get(m.getId()));
                }
        );

        return new PageImpl<>(memberDtos, pageable, total);
    }

    @Override
    public Page<MemberDto> findMemberDtoPageByWordContainingAtSearchTypeAndGroup(Pageable pageable, Group group, MemberSearchType searchType, String word) {
        final List<Long> ids = queryFactory
                .selectFrom(groupMember)
                .innerJoin(groupMember.member, member)
                .where(groupMember.group.eq(group))
                .fetch()
                .stream()
                .map(g -> g.getMember().getId())
                .collect(Collectors.toList());

        final List<Member> members = queryFactory
                .selectFrom(member)
                .innerJoin(member.roles, memberRole).fetchJoin()
                .innerJoin(memberRole.role, role).fetchJoin()
                .where(member.id.in(ids).and(search(searchType, word)))
                .orderBy(memberSort(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        final long total = queryFactory
                .selectFrom(member)
                .where(member.id.in(ids).and(search(searchType, word)))
                .fetchCount();

        final List<Long> memberIds = members.stream()
                .map(Member::getId)
                .collect(Collectors.toList());
        final Map<Long, List<GroupDto>> groupDtoMap = queryFactory
                .select(new QGroupDto(
                        groupMember.group.id,
                        groupMember.group.name,
                        groupMember.role,
                        groupMember.createdDate,
                        groupMember.member.id
                ))
                .from(groupMember)
                .where(groupMember.member.id.in(memberIds).and(groupMember.role.ne(GroupRoles.APPLICANT)))
                .innerJoin(groupMember.group)
                .fetch()
                .stream()
                .collect(Collectors.groupingBy(GroupDto::getMemberId));

        final List<MemberDto> memberDtos = members.stream()
                .map(MemberDto::new)
                .collect(Collectors.toList());
        memberDtos.forEach(m -> {
                    if (groupDtoMap.get(m.getId()) != null)
                        m.setGroups(groupDtoMap.get(m.getId()));
                }
        );

        return new PageImpl<>(memberDtos, pageable, total);
    }

    @Override
    public Page<MemberDto> findMemberDtoPageByGenderAndGroup(Pageable pageable, Group group, Gender gender) {
        final List<Long> ids = queryFactory
                .selectFrom(groupMember)
                .innerJoin(groupMember.member, member)
                .where(groupMember.group.eq(group))
                .fetch()
                .stream()
                .map(g -> g.getMember().getId())
                .collect(Collectors.toList());

        final List<Member> members = queryFactory
                .selectFrom(member)
                .innerJoin(member.roles, memberRole).fetchJoin()
                .innerJoin(memberRole.role, role).fetchJoin()
                .where(member.id.in(ids).and(member.gender.eq(gender)))
                .orderBy(memberSort(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        final long total = queryFactory
                .selectFrom(member)
                .where(member.id.in(ids).and(member.gender.eq(gender)))
                .fetchCount();

        final List<Long> memberIds = members.stream()
                .map(Member::getId)
                .collect(Collectors.toList());
        final Map<Long, List<GroupDto>> groupDtoMap = queryFactory
                .select(new QGroupDto(
                        groupMember.group.id,
                        groupMember.group.name,
                        groupMember.role,
                        groupMember.createdDate,
                        groupMember.member.id
                ))
                .from(groupMember)
                .where(groupMember.member.id.in(memberIds).and(groupMember.role.ne(GroupRoles.APPLICANT)))
                .innerJoin(groupMember.group)
                .fetch()
                .stream()
                .collect(Collectors.groupingBy(GroupDto::getMemberId));

        final List<MemberDto> memberDtos = members.stream()
                .map(MemberDto::new)
                .collect(Collectors.toList());
        memberDtos.forEach(m -> {
                    if (groupDtoMap.get(m.getId()) != null)
                        m.setGroups(groupDtoMap.get(m.getId()));
                }
        );

        return new PageImpl<>(memberDtos, pageable, total);
    }

    @Override
    public Page<MemberDto> findMemberDtoPageByAcademicStatusAndGroup(Pageable pageable, Group group, MemberAcademicStatus academicStatus) {
        final List<Long> ids = queryFactory
                .selectFrom(groupMember)
                .innerJoin(groupMember.member, member)
                .where(groupMember.group.eq(group))
                .fetch()
                .stream()
                .map(g -> g.getMember().getId())
                .collect(Collectors.toList());

        final List<Member> members = queryFactory
                .selectFrom(member)
                .innerJoin(member.roles, memberRole).fetchJoin()
                .innerJoin(memberRole.role, role).fetchJoin()
                .where(member.id.in(ids).and(member.academicStatus.eq(academicStatus)))
                .orderBy(memberSort(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        final long total = queryFactory
                .selectFrom(member)
                .where(member.id.in(ids).and(member.academicStatus.eq(academicStatus)))
                .fetchCount();

        final List<Long> memberIds = members.stream()
                .map(Member::getId)
                .collect(Collectors.toList());
        final Map<Long, List<GroupDto>> groupDtoMap = queryFactory
                .select(new QGroupDto(
                        groupMember.group.id,
                        groupMember.group.name,
                        groupMember.role,
                        groupMember.createdDate,
                        groupMember.member.id
                ))
                .from(groupMember)
                .where(groupMember.member.id.in(memberIds))
                .innerJoin(groupMember.group)
                .fetch()
                .stream()
                .collect(Collectors.groupingBy(GroupDto::getMemberId));

        final List<MemberDto> memberDtos = members.stream()
                .map(MemberDto::new)
                .collect(Collectors.toList());
        memberDtos.forEach(m -> {
                    if (groupDtoMap.get(m.getId()) != null)
                        m.setGroups(groupDtoMap.get(m.getId()));
                }
        );

        return new PageImpl<>(memberDtos, pageable, total);
    }

    @Override
    public Page<MemberDto> findMemberDtoPageByGradeAndGroup(Pageable pageable, Group group, MemberGrade grade) {
        final List<Long> ids = queryFactory
                .selectFrom(groupMember)
                .innerJoin(groupMember.member, member)
                .where(groupMember.group.eq(group))
                .fetch()
                .stream()
                .map(g -> g.getMember().getId())
                .collect(Collectors.toList());

        final List<Member> members = queryFactory
                .selectFrom(member)
                .innerJoin(member.roles, memberRole).fetchJoin()
                .innerJoin(memberRole.role, role).fetchJoin()
                .where(member.id.in(ids).and(member.grade.eq(grade)))
                .orderBy(memberSort(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        final long total = queryFactory
                .selectFrom(member)
                .where(member.id.in(ids).and(member.grade.eq(grade)))
                .fetchCount();

        final List<Long> memberIds = members.stream()
                .map(Member::getId)
                .collect(Collectors.toList());
        final Map<Long, List<GroupDto>> groupDtoMap = queryFactory
                .select(new QGroupDto(
                        groupMember.group.id,
                        groupMember.group.name,
                        groupMember.role,
                        groupMember.createdDate,
                        groupMember.member.id
                ))
                .from(groupMember)
                .where(groupMember.member.id.in(memberIds))
                .innerJoin(groupMember.group)
                .fetch()
                .stream()
                .collect(Collectors.groupingBy(GroupDto::getMemberId));

        final List<MemberDto> memberDtos = members.stream()
                .map(MemberDto::new)
                .collect(Collectors.toList());
        memberDtos.forEach(m -> {
                    if (groupDtoMap.get(m.getId()) != null)
                        m.setGroups(groupDtoMap.get(m.getId()));
                }
        );

        return new PageImpl<>(memberDtos, pageable, total);
    }

    private BooleanExpression searchRequester(RequesterSearchType type, String word) {
        switch (type.getField()) {
            case "member_name":
                return request.member.name.contains(word);
            case "member_student_id":
                return request.member.studentId.contains(word);
        }

        return null;
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
