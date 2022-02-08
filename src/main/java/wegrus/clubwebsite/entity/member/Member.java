package wegrus.clubwebsite.entity.member;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import wegrus.clubwebsite.dto.member.MemberInfoUpdateRequest;
import wegrus.clubwebsite.dto.member.MemberSignupRequest;
import wegrus.clubwebsite.entity.post.*;
import wegrus.clubwebsite.vo.Image;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static wegrus.clubwebsite.util.ImageUtil.MEMBER_BASIC_IMAGE_URL;

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
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<PostLike> postLikes = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<View> views = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<ReplyLike> replyLikes = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    private List<Bookmark> bookmarks = new ArrayList<>();

    @Column(name = "member_user_id", unique = true, nullable = false)
    private String userId;

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

    @Column(name = "member_gender")
    private Gender gender;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "url", column = @Column(name = "member_image_url")),
            @AttributeOverride(name = "type", column = @Column(name = "member_image_type")),
            @AttributeOverride(name = "name", column = @Column(name = "member_image_name")),
            @AttributeOverride(name = "uuid", column = @Column(name = "member_image_uuid"))
    })
    private Image image;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_academic_status", nullable = false)
    private MemberAcademicStatus academicStatus;

    @Column(name = "member_refresh_token")
    private String refreshToken;

    @Builder
    public Member(String userId, String email, String name, String department, MemberGrade grade, String phone, MemberAcademicStatus academicStatus, Gender gender) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.studentId = email.substring(0, 8);
        this.department = department;
        this.grade = grade;
        this.phone = phone;
        this.academicStatus = academicStatus;
        this.image = Image.builder().url(MEMBER_BASIC_IMAGE_URL).build();
        this.gender = gender;
    }

    public void update(MemberInfoUpdateRequest request) {
        this.name = request.getName();
        this.grade = request.getGrade();
        this.phone = request.getPhone();
        this.department = request.getDepartment();
        this.academicStatus = request.getAcademicStatus();
        this.introduce = request.getIntroduce();
    }

    public void updateImage(Image image) {
        this.image = image;
    }

    public void resign() {
        this.academicStatus = MemberAcademicStatus.ETC;
        this.grade = MemberGrade.ETC;
        this.phone = "";
        this.introduce = "";
        this.department = "";
        this.email = "";
        this.name = "";
        this.studentId = "";
        this.image = Image.builder().url(MEMBER_BASIC_IMAGE_URL).build();
    }

    public void rejoin(MemberSignupRequest request) {
        this.email = request.getEmail();
        this.studentId = request.getEmail().substring(0, 8);
        this.name = request.getName();
        this.department = request.getDepartment();
        this.phone = request.getPhone();
        this.academicStatus = request.getAcademicStatus();
        this.grade = request.getGrade();
        this.gender = request.getGender();
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
