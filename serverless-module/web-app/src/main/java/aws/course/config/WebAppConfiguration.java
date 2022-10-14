package aws.course.config;

import aws.course.config.properties.SNSClientProperties;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
@RequiredArgsConstructor
public class WebAppConfiguration {

    private final SNSClientProperties snsClientProperties;

    @Bean
    public AmazonSNS snsClient() {
        return AmazonSNSClient
                .builder()
                .withRegion(snsClientProperties.getRegion())
                .build();
    }
}
