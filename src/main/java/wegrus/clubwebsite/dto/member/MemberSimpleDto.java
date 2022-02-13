package wegrus.clubwebsite.dto.member;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import wegrus.clubwebsite.entity.member.Member;
import wegrus.clubwebsite.entity.member.MemberGrade;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class MemberSimpleDto {

    private String name;
    private String studentId;
    private String department;
    private String grade;
    private String gender;
    private LocalDateTime createdDate;
    private String introduce;
    private String imageUrl;
    private List<String> roles = new ArrayList<>();
    private List<GroupDto> groups = new ArrayList<>();

    public MemberSimpleDto(Member member) {
        this.name = member.getName();
        this.studentId = member.getStudentId().substring(2, 4);
        this.department = member.getDepartment();
        this.grade = member.getGrade().getValue();
        this.gender = member.getGender().getValue();
        this.createdDate = member.getCreatedDate();
        this.introduce = member.getIntroduce();
        this.imageUrl = member.getImage().getUrl();
        member.getRoles()
                .forEach(r -> this.roles.add(r.getRole().getName()));
    }
}
