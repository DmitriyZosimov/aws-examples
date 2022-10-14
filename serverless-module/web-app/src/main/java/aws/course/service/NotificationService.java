package aws.course.service;

public interface NotificationService {

    void sendMessageToQueue(String message);
}
