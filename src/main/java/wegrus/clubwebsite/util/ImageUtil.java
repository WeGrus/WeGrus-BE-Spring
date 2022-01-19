package wegrus.clubwebsite.util;

import com.google.common.base.Enums;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import wegrus.clubwebsite.exception.NotSupportedImageTypeException;
import wegrus.clubwebsite.vo.Image;
import wegrus.clubwebsite.vo.ImageType;

import java.util.UUID;

@Component
public class ImageUtil {

    public final static String MEMBER_BASIC_IMAGE_URL = "https://igrus-webservice-bucket.s3.ap-northeast-2.amazonaws.com/basic.jpeg";

    public Image convertMultipartFile(MultipartFile multipartFile) {
        String originalName = multipartFile.getOriginalFilename();
        String name = FilenameUtils.getBaseName(originalName);
        String type = FilenameUtils.getExtension(originalName).toUpperCase();

        if (!Enums.getIfPresent(ImageType.class, type).isPresent())
            throw new NotSupportedImageTypeException();

        return Image.builder()
                .type(ImageType.valueOf(type))
                .name(name)
                .uuid(UUID.randomUUID().toString())
                .build();
    }
}
