package aws.course.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    boolean upload(MultipartFile file);

    byte[] download(String name);

    void delete(String name);
}
