package wegrus.clubwebsite.vo;

import lombok.*;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.Objects;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class Image {

    private String url;

    @Enumerated(EnumType.STRING)
    private ImageType type;

    private String name;

    private String uuid;

    public void setUrl(String url){
        this.url = url;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUuid());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        Image image = (Image) obj;
        return Objects.equals(getUuid(), image.getUuid());
    }
}
