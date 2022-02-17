package wegrus.clubwebsite.dto.member;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import wegrus.clubwebsite.entity.member.Member;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MemberInfoUpdateResponse {

    private String status;
    private String name;
    private String department;
    private String phone;
    private String introduce;
    private String academicStatus;
    private String grade;

    public MemberInfoUpdateResponse(String status, Member member) {
        this.status = status;
        this.name = member.getName();
        this.department = member.getDepartment();
        this.phone = member.getPhone();
        this.academicStatus = member.getAcademicStatus().getValue();
        this.grade = member.getGrade().getValue();
        this.introduce = member.getIntroduce();
    }
}
