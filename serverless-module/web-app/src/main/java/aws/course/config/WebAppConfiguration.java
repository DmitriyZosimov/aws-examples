package aws.course.config;

import aws.course.config.properties.LambdaProperties;
import aws.course.config.properties.S3Properties;
import aws.course.config.properties.SNSClientProperties;
import aws.course.config.properties.SQSProperties;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
@RequiredArgsConstructor
public class WebAppConfiguration {

    private final SNSClientProperties snsClientProperties;
    private final SQSProperties sqsProperties;
    private final S3Properties s3Properties;
    private final LambdaProperties lambdaProperties;

    @Bean
    public AmazonSNS snsClient() {
        return AmazonSNSClient
                .builder()
                .withRegion(snsClientProperties.getRegion())
                .build();
    }

    @Bean
    public AmazonSQS sqsClient() {
        return AmazonSQSClientBuilder
                .standard()
                .withRegion(sqsProperties.getRegion())
                .build();
    }

    @Bean
    public AmazonS3 s3Client() {
        return AmazonS3ClientBuilder
                .standard()
                .withRegion(s3Properties.getRegion())
                .build();
    }

    @Bean
    public TransferManager transferManager(AmazonS3 s3Client) {
        return TransferManagerBuilder.standard()
                .withS3Client(s3Client)
                .build();
    }

    @Bean
    public AWSLambda awsLambda() {
        return AWSLambdaClientBuilder.standard()
                .withRegion(lambdaProperties.getRegion())
                .build();
    }


}
