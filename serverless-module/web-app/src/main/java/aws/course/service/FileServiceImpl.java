package aws.course.service;

import aws.course.config.properties.S3Properties;
import aws.course.controller.FileController;
import aws.course.dao.FileMetadataRepository;
import aws.course.model.FileMetadata;
import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.Upload;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.Instant;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Service
@RequiredArgsConstructor
@Transactional
public class FileServiceImpl implements FileService {

    private static final String IMAGE_UPLOAD_ACTION = "Image was uploaded";

    private final FileMetadataRepository dao;
    private final TransferManager transferManager;
    private final AmazonS3 amazonS3;
    private final S3Properties s3Properties;
    private final NotificationService notificationService;

    @Override
    public boolean upload(MultipartFile file) {
        boolean result = false;
        checkWhetherFileExists(file);
        try {
            Upload upload = transferManager.upload(s3Properties.getBucketName(), file.getOriginalFilename(),
                    file.getInputStream(), new ObjectMetadata());
            upload.waitForUploadResult();
            FileMetadata fileMetadata = saveMetadata(file);
            if (result = fileMetadata != null) {
                notificationService.sendMessageToQueue(createMessage(fileMetadata));
            }
        } catch (IOException | InterruptedException e) {
            delete(file.getOriginalFilename());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
        return result;
    }

    @Override
    public byte[] download(String name) {
        try {
            dao.findByName(name)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "The file " + name + " was not found"));
            return amazonS3.getObject(s3Properties.getBucketName(), name).getObjectContent().readAllBytes();
        } catch (IOException | AmazonClientException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @Override
    public void delete(String name) {
        try {
            dao.findByName(name)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "The file " + name + " was not found"));
            amazonS3.deleteObject(s3Properties.getBucketName(), name);
        } catch (AmazonClientException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    private void checkWhetherFileExists(MultipartFile file) {
        dao.findByName(file.getOriginalFilename())
                .ifPresent(metadata -> {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The file is already exist");
                });
    }

    private FileMetadata saveMetadata(MultipartFile file) {
        FileMetadata fileMetadata = FileMetadata.builder()
                .fileExtension(FilenameUtils.getExtension(file.getOriginalFilename()))
                .name(file.getOriginalFilename())
                .sizeInBytes(file.getSize())
                .lastUpdateDate(Instant.now())
                .build();
        return dao.saveAndFlush(fileMetadata);
    }

    private String createMessage(FileMetadata fileMetadata) {
        WebMvcLinkBuilder downloadLink = linkTo(methodOn(FileController.class).download(fileMetadata.getName()));
        return String.join(":::", IMAGE_UPLOAD_ACTION, fileMetadata.toString(),
                downloadLink.toString());
    }
}
