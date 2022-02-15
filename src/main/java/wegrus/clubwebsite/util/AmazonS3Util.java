package wegrus.clubwebsite.util;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import wegrus.clubwebsite.exception.MultipartfileConvertException;
import wegrus.clubwebsite.vo.Image;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class AmazonS3Util {

    private final AmazonS3Client amazonS3Client;
    private final ImageUtil imageUtil;
    private final FileUtil fileUtil;

    @Value("${cloud.aws.s3.bucket}")
    public String bucket;

    public void createDirectory(String dirName) {
        amazonS3Client.putObject(bucket, dirName + "/", new ByteArrayInputStream(new byte[0]), new ObjectMetadata());
    }

    public wegrus.clubwebsite.vo.File uploadFile(MultipartFile multipartFile, String dirName) throws IOException {
        final wegrus.clubwebsite.vo.File file = fileUtil.convertMultipartFile(multipartFile);
        final String fileName = dirName + "/" + file.getUuid() + "_" + file.getName() + "." + file.getType();

        final File uploadFile = convert(multipartFile).orElseThrow(MultipartfileConvertException::new);
        final String url = uploadFile(uploadFile, fileName);
        file.setUrl(url);
        return file;
    }

    public Image uploadImage(MultipartFile multipartFile, String dirName) throws IOException {
        final Image image = imageUtil.convertMultipartFile(multipartFile);
        final String fileName = dirName + "/" + image.getUuid() + "_" + image.getName() + "." + image.getType();

        final File uploadFile = convert(multipartFile).orElseThrow(MultipartfileConvertException::new);
        final String url = uploadFile(uploadFile, fileName);

        image.setUrl(url);
        return image;
    }

    public void deleteImage(Image image, String dirName) {
        final String filename = dirName + "/" + image.getUuid() + "_" + image.getName() + "." + image.getType().toString();
        deleteS3(filename);
    }

    public void deleteFile(wegrus.clubwebsite.vo.File file, String dirName) {
        final String filename = dirName + "/" + file.getUuid() + "_" + file.getName() + "." + file.getType();
        deleteS3(filename);
    }

    public String uploadFile(File uploadFile, String fileName) {
        final String uploadImageUrl = putS3(uploadFile, fileName);
        removeNewFile(uploadFile);
        return uploadImageUrl;
    }

    public Image updateImage(Image image, String prevDirName, String dirName) {
        final String oldSource = prevDirName + "/" + image.getUuid() + "_" + image.getName() + "." + image.getType().toString();
        final String newSource = dirName + "/" + image.getUuid() + "_" + image.getName() + "." + image.getType().toString();

        moveS3(oldSource, newSource);
        deleteS3(oldSource);

        image.setUrl(amazonS3Client.getUrl(bucket, newSource).toString());
        return image;
    }

    /**
     * S3에 <b>File</b> 업로드
     */
    private String putS3(File uploadFile, String fileName) {
        amazonS3Client.putObject(new PutObjectRequest(bucket, fileName, uploadFile).withCannedAcl(CannedAccessControlList.PublicRead));
        return amazonS3Client.getUrl(bucket, fileName).toString();
    }

    /**
     * S3에서 <b>File</b> 삭제
     */
    private void deleteS3(String filename) {
        amazonS3Client.deleteObject(bucket, filename);
    }

    /**
     * S3에 저장된 <b>File</b> 복사
     */
    private void moveS3(String oldSource, String newSource) {
        amazonS3Client.copyObject(bucket, oldSource, bucket, newSource);
    }

    /**
     * Local에 저장된 <b>File</b> 삭제
     */
    private void removeNewFile(File targetFile) {
        targetFile.delete();
    }

    /**
     * S3에 전달하기 위해 Local에서 <b>MultipartFile -> File</b> 변환
     */
    private Optional<File> convert(MultipartFile file) throws IOException {
        final File convertFile = new File(System.getProperty("user.dir") + "\\upload\\" + file.getOriginalFilename());
        if (convertFile.createNewFile()) {
            try (FileOutputStream fos = new FileOutputStream(convertFile)) {
                fos.write(file.getBytes());
            }
            return Optional.of(convertFile);
        }
        return Optional.empty();
    }
}
