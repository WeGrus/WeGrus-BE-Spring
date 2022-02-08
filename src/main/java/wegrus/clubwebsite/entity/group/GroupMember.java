package wegrus.clubwebsite.entity.group;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import wegrus.clubwebsite.entity.member.Member;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "group_members")
public class GroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_member_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    @Enumerated(EnumType.STRING)
    @Column(name = "group_member_role")
    private GroupRoles role;

    @Column(name = "group_member_create_date")
    @CreatedDate
    private LocalDateTime createdDate;

    public GroupMember(Member member, Group group) {
        this.member = member;
        this.group = group;
        this.role = GroupRoles.APPLICANT;
    }

    public void updateRole(GroupRoles role){
        this.role = role;
    }
}
