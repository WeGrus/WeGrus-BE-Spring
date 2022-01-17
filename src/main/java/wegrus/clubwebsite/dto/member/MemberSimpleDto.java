package wegrus.clubwebsite.dto.member;

import lombok.Getter;
import lombok.NoArgsConstructor;
import wegrus.clubwebsite.entity.member.Member;
import wegrus.clubwebsite.entity.member.MemberGrade;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
public class MemberSimpleDto {

    private String name;
    private String studentId;
    private String department;
    private MemberGrade grade;
    private LocalDateTime createdDate;
    private String introduce;
    private String imageUrl;
    private List<String> roles = new ArrayList<>();

    public MemberSimpleDto(Member member) {
        this.name = member.getName();
        this.studentId = member.getStudentId().substring(2, 4);
        this.department = member.getDepartment();
        this.grade = member.getGrade();
        this.createdDate = member.getCreatedDate();
        this.introduce = member.getIntroduce();
        this.imageUrl = member.getImageUrl();
        member.getRoles()
                .forEach(r -> this.roles.add(r.getRole().getName()));
    }
}
