package aws.lambda.s3;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class S3GetObject implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private AmazonS3 amazonS3;

    public S3GetObject() {
        this.amazonS3 = AmazonS3ClientBuilder.standard()
                .withRegion(Regions.US_EAST_1)
                .build();
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        LambdaLogger logger = context.getLogger();
        String bucket = input.getPathParameters().get("bucket");
        String object = input.getPathParameters().get("object");
        if (!amazonS3.doesObjectExist(bucket, object)) {
            return buildError(bucket, object);
        }
        S3Object s3Object = amazonS3.getObject(bucket, object);

        APIGatewayProxyResponseEvent response = buildAPIGatewayProxyResponseEvent();

        byte[] result;
        try(S3ObjectInputStream inputStream = s3Object.getObjectContent()) {
            result = inputStream.readAllBytes();
            response.setBody(new String(result, StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.log("Response: " + response.toString());
        return response;
    }

    private APIGatewayProxyResponseEvent buildAPIGatewayProxyResponseEvent() {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        response.setIsBase64Encoded(false);
        response.setStatusCode(200);
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/octet-stream");
        headers.put("Content-Disposition", "attachment");
        response.setHeaders(headers);
        return response;
    }

    private APIGatewayProxyResponseEvent buildError(String bucket, String object) {
        return new APIGatewayProxyResponseEvent()
                .withIsBase64Encoded(false)
                .withStatusCode(404)
                .withBody("The bucket " + bucket + " and/or the object " + object + " does not exist");
    }
}
