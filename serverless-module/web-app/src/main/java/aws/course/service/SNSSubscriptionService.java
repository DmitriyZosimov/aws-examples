package aws.course.service;

/**
 * SNS Subscription service subscribe or unsubscribe email to/from SNS Topic
 */
public interface SNSSubscriptionService {

    String SNS_PROTOCOL = "email";

    /**
     * Subscribe to SNS Topic
     * @param email email address that will be subscribed
     */
    void subscribeEmail(String email);

    /**
     * Unsubscribe from SNS Topic
     * @param email email address that will be unsubscribed
     */
    void unsubscribeEmail(String email);
}
