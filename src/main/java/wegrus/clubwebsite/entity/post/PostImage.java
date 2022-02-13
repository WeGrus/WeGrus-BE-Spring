package wegrus.clubwebsite.entity.post;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import wegrus.clubwebsite.vo.Image;

import javax.persistence.*;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "post_images")
public class PostImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_image_id", insertable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "url", column = @Column(name = "post_image_url")),
            @AttributeOverride(name = "type", column = @Column(name = "post_image_type")),
            @AttributeOverride(name = "name", column = @Column(name = "post_image_name")),
            @AttributeOverride(name = "uuid", column = @Column(name = "post_image_uuid"))
    })
    private Image image;

    @Builder
    public PostImage(Post post, Image image) {
        this.post = post;
        this.image = image;
    }

    public void updatePost(Post post) {
        this.post = post;
    }

}
