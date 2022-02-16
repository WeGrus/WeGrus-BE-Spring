package wegrus.clubwebsite.vo;

import lombok.*;

import javax.persistence.Embeddable;
import java.util.Objects;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class File {

    private String url;

    private String type;

    private String name;

    private String uuid;

    public void setUrl(String url) {
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

        File file = (File) obj;
        return Objects.equals(getUuid(), file.getUuid());
    }
}
