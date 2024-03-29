package com.github.storytime.service.http;

import com.github.storytime.config.SqsConfig;
import io.awspring.cloud.messaging.core.QueueMessagingTemplate;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import static com.github.storytime.service.util.STUtils.createSt;
import static com.github.storytime.service.util.STUtils.getTimeAndReset;
import static java.util.UUID.randomUUID;
import static org.apache.logging.log4j.LogManager.getLogger;
import static org.apache.logging.log4j.util.Strings.EMPTY;

@Service
public class SqsAsyncService {

    private static final Logger LOGGER = getLogger(SqsAsyncService.class);
    private final QueueMessagingTemplate queueMessagingTemplate;
    private final SqsConfig sqsConfig;

    public SqsAsyncService(final QueueMessagingTemplate queueMessagingTemplate,
                           final SqsConfig sqsConfig) {
        this.queueMessagingTemplate = queueMessagingTemplate;
        this.sqsConfig = sqsConfig;
    }

    public String publishFinishMessage() {
        var uuid = randomUUID().toString();
        final var st = createSt();
        try {
            queueMessagingTemplate.convertAndSend(sqsConfig.getShutdownQueue(), uuid);
            LOGGER.info("Pushed to SQS done: [{}], time: [{}] - done", uuid, getTimeAndReset(st));
            return uuid;
        } catch (Exception e) {
            LOGGER.error("Cannot push to SQL: [{}], time: [{}] - error", uuid, getTimeAndReset(st), e);
            return EMPTY;
        }
    }
}
