package aws.course.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "lambda")
@Component
public class LambdaProperties {

    private String region;
    private String functionARN;
}
