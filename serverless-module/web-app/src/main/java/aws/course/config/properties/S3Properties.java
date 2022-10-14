package aws.course.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "s3")
@Component
public class S3Properties {

    private String bucketName;
    private String region;
}
