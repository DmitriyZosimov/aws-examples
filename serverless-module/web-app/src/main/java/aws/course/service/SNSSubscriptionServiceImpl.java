package aws.course.service;

import aws.course.config.properties.SNSClientProperties;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.AmazonSNSException;
import com.amazonaws.services.sns.model.ListSubscriptionsByTopicResult;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sns.model.Subscription;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SNSSubscriptionServiceImpl implements SNSSubscriptionService {

    private final AmazonSNS snsClient;
    private final SNSClientProperties snsClientProperties;


    @Override
    public void subscribeEmail(String email) {
        try {
            SubscribeRequest request = new SubscribeRequest()
                    .withProtocol(SNS_PROTOCOL)
                    .withEndpoint(email)
                    .withTopicArn(snsClientProperties.getTopicArn());
            snsClient.subscribe(request);
        } catch (AmazonSNSException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @Override
    public void unsubscribeEmail(String email) {
        try {
            ListSubscriptionsByTopicResult listRequest = snsClient.listSubscriptionsByTopic(snsClientProperties.getTopicArn());
            List<Subscription> subscriptions = listRequest.getSubscriptions();
            subscriptions.stream()
                    .filter(sub -> email.equals(sub.getEndpoint()))
                    .findAny()
                    .ifPresent(sub -> snsClient.unsubscribe(sub.getSubscriptionArn()));
        } catch (AmazonSNSException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }
}
