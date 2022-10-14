package aws.lambda.task2;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.GetQueueUrlRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FileUploadHandler implements RequestHandler<Map<String, Object>, APIGatewayProxyResponseEvent> {

    private static final String SNS_QUEUE_NAME = "lambda-uploads-notification-queue";
    private static final String TOPIC_ARN = "arn:aws:sns:us-east-1:119939467338:lambda-uploads-notification-topic";
    private static final Integer RECEIVE_MESSAGES_TIMEOUT = 3;
    private static final Integer MAX_NUMBER_OF_MESSAGES = 3;

    private AmazonSQS amazonSQS = AmazonSQSClientBuilder.standard().withRegion(Regions.US_EAST_1).build();
    private AmazonSNS amazonSNS = AmazonSNSClientBuilder.standard().withRegion(Regions.US_EAST_1).build();
    private LambdaLogger logger;
    private String queueUrl;

    @Override
    public APIGatewayProxyResponseEvent handleRequest(Map<String, Object> input, Context context) {
        Object detail = input.get("detail-type");
        String detailType = detail == null ? "API" : String.valueOf(detail);
        logger = context.getLogger();

        List<Message> messages = readMessages();
        if (!messages.isEmpty()) {
            sendNotification(messages);
            deleteMessages(messages);
        }

        logger.log("Handled Request for ARN = " + TOPIC_ARN
                + ";\nRequest Source = " + detailType
                + ";\nFunction Name = " + context.getFunctionName()
                + ";\nProcessed Messages count = " + messages.size()
                + ";\nRemaining Time in millis = " + context.getRemainingTimeInMillis());

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withIsBase64Encoded(false);
    }

    private List<Message> readMessages() {
        GetQueueUrlRequest getQueueRequest = new GetQueueUrlRequest().withQueueName(SNS_QUEUE_NAME);
        queueUrl = amazonSQS.getQueueUrl(getQueueRequest).getQueueUrl();

        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest()
                .withQueueUrl(queueUrl)
                .withMaxNumberOfMessages(MAX_NUMBER_OF_MESSAGES)
                .withWaitTimeSeconds(RECEIVE_MESSAGES_TIMEOUT);

        List<Message> sqsMessages = IntStream.of(0, MAX_NUMBER_OF_MESSAGES)
                .mapToObj(i -> amazonSQS.receiveMessage(receiveMessageRequest).getMessages())
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        logger.log("Amount of received messages: " + sqsMessages.size());
        return sqsMessages;
    }

    private void sendNotification(List<Message> messages) {
        String message = messages.stream()
                .map(Message::getBody)
                .collect(Collectors.joining("\n=========================\n"));
        logger.log(" Result message = \n" + message);

        PublishRequest request = new PublishRequest()
                .withTopicArn(TOPIC_ARN)
                .withSubject("Processed SQS Queue Messages")
                .withMessage(message);
        amazonSNS.publish(request);
    }

    private void deleteMessages(List<Message> messages) {
        messages.forEach(message -> {
            amazonSQS.deleteMessage(queueUrl, message.getReceiptHandle());
            logger.log("Message was deleted: id = " + message.getMessageId());
        });
    }
}
