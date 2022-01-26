package wegrus.clubwebsite.member;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import wegrus.clubwebsite.entity.member.*;
import wegrus.clubwebsite.repository.MemberRepository;
import wegrus.clubwebsite.repository.MemberRoleRepository;
import wegrus.clubwebsite.repository.RoleRepository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class MemberRoleRepositoryTest {

    @Autowired
    private MemberRoleRepository memberRoleRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void init() {
        final List<String> roles = Arrays.stream(MemberRoles.values())
                .map(Enum::name)
                .collect(Collectors.toList());

        final String sql = "INSERT INTO ROLES (`role_name`) VALUES(?)";
        final BatchPreparedStatementSetter pss = new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ps.setString(1, roles.get(i));
            }

            @Override
            public int getBatchSize() {
                return roles.size();
            }
        };
        jdbcTemplate.batchUpdate(sql, pss);
    }

    @Test
    @DisplayName("회원 PK로 해당 회원의 모든 권한 조회하기")
    void findAllByMemberId() throws Exception {
        // given
        final Member member = Member.builder()
                .name("김만두")
                .email("12161542@inha.edu")
                .department("컴퓨터공학과")
                .academicStatus(MemberAcademicStatus.ATTENDING)
                .phone("010-1234-5672")
                .userId("kakao_124124512541")
                .grade(MemberGrade.FRESHMAN)
                .build();
        memberRepository.save(member);
        final Optional<Role> guestRole = roleRepository.findByName(MemberRoles.ROLE_GUEST.name());
        final Optional<Role> guestMember = roleRepository.findByName(MemberRoles.ROLE_MEMBER.name());
        memberRoleRepository.save(new MemberRole(member, guestMember.get()));
        memberRoleRepository.save(new MemberRole(member, guestRole.get()));

        // when
        final List<MemberRole> memberRoles = memberRoleRepository.findAllByMemberId(member.getId());

        // then
        assertThat(memberRoles.size()).isEqualTo(2);
    }
    
    @Test
    @DisplayName("Batch delete로 MemberRole 엔티티 여러 개 한 번에 삭제하기")
    void deleteAllByIdInBatch() throws Exception {
        // given
        final Member member = Member.builder()
                .name("김만두")
                .email("12161542@inha.edu")
                .department("컴퓨터공학과")
                .academicStatus(MemberAcademicStatus.ATTENDING)
                .phone("010-1234-5672")
                .userId("kakao_124124512541")
                .grade(MemberGrade.FRESHMAN)
                .build();
        memberRepository.save(member);
        final Optional<Role> guestRole = roleRepository.findByName(MemberRoles.ROLE_GUEST.name());
        final Optional<Role> guestMember = roleRepository.findByName(MemberRoles.ROLE_MEMBER.name());
        memberRoleRepository.save(new MemberRole(member, guestMember.get()));
        memberRoleRepository.save(new MemberRole(member, guestRole.get()));

        final List<Long> ids = memberRoleRepository.findAllByMemberId(member.getId()).stream()
                .map(MemberRole::getId)
                .collect(Collectors.toList());

        // when
        memberRoleRepository.deleteAllByIdInBatch(ids);
        
        // then
        assertThat(memberRoleRepository.findAllByMemberId(member.getId()).size()).isEqualTo(0);
    }
}
