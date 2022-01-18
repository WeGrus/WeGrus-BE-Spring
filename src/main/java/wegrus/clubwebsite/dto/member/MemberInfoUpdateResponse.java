package wegrus.clubwebsite.dto.member;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import wegrus.clubwebsite.entity.member.Member;
import wegrus.clubwebsite.entity.member.MemberAcademicStatus;
import wegrus.clubwebsite.entity.member.MemberGrade;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MemberInfoUpdateResponse {

    private String status;
    private String name;
    private String department;
    private String phone;
    private MemberAcademicStatus academicStatus;
    private MemberGrade grade;

    public MemberInfoUpdateResponse(String status, Member member) {
        this.status = status;
        this.name = member.getName();
        this.department = member.getDepartment();
        this.phone = member.getPhone();
        this.academicStatus = member.getAcademicStatus();
        this.grade = member.getGrade();
    }
}
