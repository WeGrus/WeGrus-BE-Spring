package wegrus.clubwebsite.entity.post;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import wegrus.clubwebsite.vo.File;

import javax.persistence.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "post_files")
public class PostFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_file_id", insertable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "url", column = @Column(name = "post_file_url")),
            @AttributeOverride(name = "type", column = @Column(name = "post_file_type")),
            @AttributeOverride(name = "name", column = @Column(name = "post_file_name")),
            @AttributeOverride(name = "uuid", column = @Column(name = "post_file_uuid"))
    })
    private File file;

    @Builder
    public PostFile(Post post, File file) {
        this.post = post;
        this.file = file;
    }
}
