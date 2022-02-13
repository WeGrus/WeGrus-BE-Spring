package wegrus.clubwebsite.dto.member;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import wegrus.clubwebsite.entity.member.Gender;
import wegrus.clubwebsite.entity.member.Member;
import wegrus.clubwebsite.entity.member.MemberAcademicStatus;
import wegrus.clubwebsite.entity.member.MemberGrade;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class MemberDto {

    private Long id;
    private String email;
    private String name;
    private String studentId;
    private String department;
    private String grade;
    private String gender;
    private String phone;
    private LocalDateTime createdDate;
    private String introduce;
    private String imageUrl;
    private String academicStatus;
    private List<String> roles = new ArrayList<>();
    private List<GroupDto> groups = new ArrayList<>();

    public MemberDto(Member member) {
        this.id = member.getId();
        this.email = member.getEmail();
        this.name = member.getName();
        this.studentId = member.getStudentId();
        this.department = member.getDepartment();
        this.gender = member.getGender().getValue();
        this.grade = member.getGrade().getValue();
        this.phone = member.getPhone();
        this.createdDate = member.getCreatedDate();
        this.introduce = member.getIntroduce();
        this.imageUrl = member.getImage().getUrl();
        this.academicStatus = member.getAcademicStatus().getValue();
        member.getRoles()
                .forEach(r -> this.roles.add(r.getRole().getName()));
    }
}
