package aws.course.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "sqs")
@Component
public class SQSProperties {

    private String sqsQueueName;
    private String region;
}
