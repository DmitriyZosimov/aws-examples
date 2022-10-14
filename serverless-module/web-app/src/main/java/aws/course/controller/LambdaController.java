package aws.course.controller;

import aws.course.config.properties.LambdaProperties;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.model.InvokeRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/lambda")
@RequiredArgsConstructor
public class LambdaController {

    private final AWSLambda awsLambda;
    private final LambdaProperties lambdaProperties;

    @PutMapping("/action")
    public ResponseEntity lambdaAction() {
        try {
            InvokeRequest invokeRequest = new InvokeRequest()
                    .withFunctionName(lambdaProperties.getFunctionARN())
                    .withPayload("{\"detail-type\":\"Web Application\"}");
            System.out.println("Invoke function: " + lambdaProperties.getFunctionARN());
            awsLambda.invoke(invokeRequest);
        } catch (Exception e) {
            System.out.println("Something went wrong\n");
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
        return ResponseEntity.ok().build();
    }

}
