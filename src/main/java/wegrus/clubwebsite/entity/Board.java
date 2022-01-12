package wegrus.clubwebsite.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Entity
@Table(name = "boards")
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_id", updatable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "board_category", nullable = false)
    private BoardCategory category;

    @Column(name = "board_type", nullable = false)
    private BoardType type;

    @Column(name = "board_title", nullable = false)
    private String title;

    @Column(name = "board_content", nullable = false)
    private String content;

    @CreatedDate
    @Column(name = "board_create_date", nullable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "board_update_date", nullable = false)
    private LocalDateTime updatedDate;

    @Column(name = "board_secret_flag", nullable = false)
    private boolean secretFlag;

    @Column(name = "board_state", nullable = false)
    private BoardState state;
}
