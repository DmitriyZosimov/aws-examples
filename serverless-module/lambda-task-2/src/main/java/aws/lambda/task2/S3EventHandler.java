package aws.lambda.task2;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.lambda.runtime.events.S3Event;

public class S3EventHandler implements RequestHandler<S3Event, APIGatewayProxyResponseEvent> {

    @Override
    public APIGatewayProxyResponseEvent handleRequest(S3Event input, Context context) {
        LambdaLogger log = context.getLogger();
        input.getRecords().stream()
                .map(record -> record.getS3().getObject().getKey())
                .forEach(key -> log.log("The file uploaded " + key));

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withBody("")
                .withIsBase64Encoded(false);
    }
}
