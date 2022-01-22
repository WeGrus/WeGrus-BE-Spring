package wegrus.clubwebsite.member;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import wegrus.clubwebsite.entity.member.Member;
import wegrus.clubwebsite.entity.member.MemberAcademicStatus;
import wegrus.clubwebsite.entity.member.MemberGrade;
import wegrus.clubwebsite.exception.MemberNotFoundException;
import wegrus.clubwebsite.repository.MemberRepository;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void init() {
        for(int i = 0; i < 5; i ++) {
            final Member member = Member.builder()
                    .name("김만두" + i)
                    .email("1216154" + i + "@inha.edu")
                    .department("컴퓨터공학과")
                    .academicStatus(i % 2 == 1 ? MemberAcademicStatus.ATTENDING : MemberAcademicStatus.ABSENCE)
                    .phone("010-1234-567" + i)
                    .userId("1234567" + i)
                    .grade(MemberGrade.FRESHMAN)
                    .build();

            memberRepository.save(member);
        }
    }

    @Test
    @DisplayName("회원 조회: 카카오 회원 번호 or 이메일")
    void findByKakaoIdOrEmail() throws Exception {
        // given
        final String realUserId = "1234568L";
        final String fakeEmail = "에베베";
        final String fakeUserId = "1L";
        final String realEmail = "12161541@inha.edu";

        // when
        final Member findMemberByKakaoId = memberRepository.findByUserIdOrEmail(realUserId, fakeEmail).orElseThrow(MemberNotFoundException::new);
        final Member findMemberByEmail = memberRepository.findByUserIdOrEmail(fakeUserId, realEmail).orElseThrow(MemberNotFoundException::new);

        // then
        assertThat(findMemberByEmail).isEqualTo(findMemberByKakaoId);
    }

    @Test
    @DisplayName("회원 조회: 카카오 회원 번호")
    void findByKakaoId() throws Exception {
        // given
        final String userId = "1234568L";

        // when
        final Member findMemberByKakaoId = memberRepository.findByUserId(userId).orElseThrow(MemberNotFoundException::new);

        // then
        assertThat(findMemberByKakaoId.getUserId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("회원 조회: 이메일")
    void findByEmail() throws Exception {
        // given
        final String email = "12161541@inha.edu";

        // when
        final Member findMemberByEmail = memberRepository.findByEmail(email).orElseThrow(MemberNotFoundException::new);

        // then
        assertThat(findMemberByEmail.getEmail()).isEqualTo(email);
    }
}
