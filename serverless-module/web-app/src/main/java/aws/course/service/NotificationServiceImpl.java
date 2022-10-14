package aws.course.service;

import aws.course.config.properties.SQSProperties;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.AmazonSQSException;
import com.amazonaws.services.sqs.model.GetQueueUrlRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final SQSProperties sqsProperties;
    private final AmazonSQS amazonSQS;

    @Override
    public void sendMessageToQueue(String message) {
        GetQueueUrlRequest queueUrlRequest = new GetQueueUrlRequest().withQueueName(sqsProperties.getSqsQueueName());
        try {
            SendMessageRequest request = new SendMessageRequest()
                    .withQueueUrl(amazonSQS.getQueueUrl(queueUrlRequest).getQueueUrl())
                    .withMessageBody(message)
                    .withDelaySeconds(5);
            amazonSQS.sendMessage(request);
        } catch (AmazonSQSException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);

        }
    }
}
