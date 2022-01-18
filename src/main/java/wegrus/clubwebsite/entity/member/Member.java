package wegrus.clubwebsite.entity.member;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import wegrus.clubwebsite.dto.member.MemberInfoUpdateRequest;
import wegrus.clubwebsite.entity.board.Board;
import wegrus.clubwebsite.entity.board.CommentLike;
import wegrus.clubwebsite.entity.board.PostLike;
import wegrus.clubwebsite.entity.board.View;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "members")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id", updatable = false)
    private Long id;

    @OneToMany(mappedBy = "member")
    private List<Board> boards = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<PostLike> postLikes = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<View> views = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<CommentLike> commentLikes = new ArrayList<>();

    @Column(name = "member_kakao_id", unique = true, nullable = false)
    private Long kakaoId;

    @Column(name = "member_email", unique = true, nullable = false)
    private String email;

    @OneToMany(mappedBy = "member")
    private Set<MemberRole> roles = new LinkedHashSet<>();

    @Column(name = "member_name", nullable = false)
    private String name;

    @Column(name = "member_student_id", unique = true, nullable = false)
    private String studentId;

    @Column(name = "member_department", nullable = false)
    private String department;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_grade", nullable = false)
    private MemberGrade grade;

    @Column(name = "member_phone", nullable = false)
    private String phone;

    @CreatedDate
    @Column(name = "member_create_date", nullable = false)
    private LocalDateTime createdDate;

    @Lob
    @Column(name = "member_introduce")
    private String introduce = "";

    @Column(name = "member_image_url")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_academic_status", nullable = false)
    private MemberAcademicStatus academicStatus;

    @Builder
    public Member(Long kakaoId, String email, String name, String department, MemberGrade grade, String phone, MemberAcademicStatus academicStatus) {
        this.kakaoId = kakaoId;
        this.email = email;
        this.name = name;
        this.studentId = email.substring(0, 8);
        this.department = department;
        this.grade = grade;
        this.phone = phone;
        this.academicStatus = academicStatus;
    }

    public void update(MemberInfoUpdateRequest request) {
        this.name = request.getName();
        this.grade = request.getGrade();
        this.phone = request.getPhone();
        this.department = request.getDepartment();
        this.academicStatus = request.getAcademicStatus();
    }
}
