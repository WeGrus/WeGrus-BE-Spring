package wegrus.clubwebsite.util;

import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import wegrus.clubwebsite.vo.File;

import java.util.UUID;

@Component
public class FileUtil {
    public File convertMultipartFile(MultipartFile multipartFile) {
        String originalName = multipartFile.getOriginalFilename();
        String name = FilenameUtils.getBaseName(originalName);
        String type = FilenameUtils.getExtension(originalName);

        return File.builder()
                .type(type)
                .name(name)
                .uuid(UUID.randomUUID().toString())
                .build();
    }
}
