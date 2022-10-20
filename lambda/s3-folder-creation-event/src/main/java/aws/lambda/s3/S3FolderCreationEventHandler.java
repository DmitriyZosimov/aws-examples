package aws.lambda.s3;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudfront.AmazonCloudFront;
import com.amazonaws.services.cloudfront.AmazonCloudFrontClientBuilder;
import com.amazonaws.services.cloudfront.model.*;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import java.time.Instant;

public class S3FolderCreationEventHandler implements RequestHandler<S3Event, APIGatewayProxyResponseEvent> {

    private static final String BUCKET_NAME = "my-app-dmitriy-zosimov";

    private AmazonCloudFront cloudFront;
    private Distribution disObject;
    private AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(Regions.EU_CENTRAL_1).build();
    private LambdaLogger logger;

    @Override
    public APIGatewayProxyResponseEvent handleRequest(S3Event input, Context context) {
        this.cloudFront = AmazonCloudFrontClientBuilder.standard().build();
        GetDistributionRequest disRequest = new GetDistributionRequest().withId("E6A8EL4723FIL");

        GetDistributionResult response = cloudFront.getDistribution(disRequest);
        this.disObject = response.getDistribution();

        logger = context.getLogger();

        // get first prefix
        String folder = input.getRecords().get(0).getS3().getObject().getKey().split("/")[0];
        logger.log("First prefix (folder) is: " + folder + "\n");

        if (!s3.doesObjectExist(BUCKET_NAME, folder + "/index.html")
            || !s3.doesObjectExist(BUCKET_NAME, folder + "/test.html")
            || !s3.doesObjectExist(BUCKET_NAME, folder + "/error.html")) {
            logger.log("The bucket was not contain required index, test, error html files");
            return returnBadRequestReturn();
        }

        logger.log("The folder is correct and contain all required files!\n");

        DistributionConfig config = disObject.getDistributionConfig();
        config.getOrigins().getItems().stream()
                .filter(origin -> origin.getDomainName().endsWith(".amazonaws.com"))
                .forEach(origin -> {
                    origin.setOriginPath("/" + folder);
                });

        logger.log("The new configuration of distribution was prepared!\n");
        UpdateDistributionRequest updateDistributionRequest = new UpdateDistributionRequest()
                .withDistributionConfig(config)
                .withId(disObject.getId())
                .withIfMatch(response.getETag());

        cloudFront.updateDistribution(updateDistributionRequest);

        executeInvalidation();

        return new APIGatewayProxyResponseEvent().withStatusCode(200).withBody("Distribution was updated!");
    }

    private APIGatewayProxyResponseEvent returnBadRequestReturn() {
        return new APIGatewayProxyResponseEvent().withStatusCode(400)
                .withBody("The bucket was not contain required index, test, error html files");
    }

    private void executeInvalidation() {
        logger.log("The invalidation process is running...");
        InvalidationBatch invalidationBatch = new InvalidationBatch().withCallerReference(Instant.now().toString())
                .withPaths(new Paths().withItems("/*", "/test*", "/error*", "/404*").withQuantity(4));

        CreateInvalidationRequest invalidationRequest = new CreateInvalidationRequest()
                .withDistributionId(disObject.getId())
                .withInvalidationBatch(invalidationBatch);
        cloudFront.createInvalidation(invalidationRequest);
        logger.log("The invalidation process finished");
    }
}
