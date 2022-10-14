package aws.course.controller;

import aws.course.service.SNSSubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sns/subscription")
@RequiredArgsConstructor
public class SNSSubscriptionController {

    private SNSSubscriptionService service;

    @PostMapping("/{email}")
    public void subscribeEmail(@PathVariable String email) {
        this.service.subscribeEmail(email);
    }

    @DeleteMapping("/{email}")
    public void unsubscribeEmail(@PathVariable String email) {
        this.service.unsubscribeEmail(email);
    }
}
