package wegrus.clubwebsite.dto.member;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import wegrus.clubwebsite.entity.member.Member;
import wegrus.clubwebsite.entity.member.MemberAcademicStatus;
import wegrus.clubwebsite.entity.member.MemberGrade;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MemberDto {

    private String email;
    private String name;
    private String studentId;
    private String department;
    private MemberGrade grade;
    private String phone;
    private LocalDateTime createdDate;
    private String introduce;
    private String imageUrl;
    private MemberAcademicStatus academicStatus;
    private List<String> roles = new ArrayList<>();

    public MemberDto(Member member) {
        this.email = member.getEmail();
        this.name = member.getName();
        this.studentId = member.getStudentId();
        this.department = member.getDepartment();
        this.grade = member.getGrade();
        this.phone = member.getPhone();
        this.createdDate = member.getCreatedDate();
        this.introduce = member.getIntroduce();
        this.imageUrl = member.getImageUrl();
        this.academicStatus = member.getAcademicStatus();
        member.getRoles().stream()
                .map(r -> this.roles.add(r.getRole().getName()))
                .collect(Collectors.toList());
    }
}
