package com.aws.spring.ebook.service.impl;

import com.aws.spring.ebook.dto.EbookSqsMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SqsConsumerService {

    private final SqsClient sqsClient;

    @Value("${aws.sqsQueueUrl}")
    private String sqsQueueUrl;

    // Poll every 5 seconds (adjust timing as needed)
    @Scheduled(fixedRate = 5000)
    public void pollSqsQueue() {
        ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder()
                .queueUrl(sqsQueueUrl)
                .maxNumberOfMessages(10) // Fetch up to 10 messages at once
                .waitTimeSeconds(20) // Long polling (adjust based on your needs)
                .build();

        ReceiveMessageResponse response = sqsClient.receiveMessage(receiveMessageRequest);
        List<Message> messages = response.messages();
        log.info("Received {} messages from SQS queue", messages.size());

        for (Message message : messages) {
            processMessage(message);
            deleteMessageFromQueue(message);
        }
    }

    private void processMessage(Message message) {
        try {
            log.info("Processing message: {}", message.body());

            ObjectMapper objectMapper = new ObjectMapper();
            EbookSqsMessage ebookSqsMessage = objectMapper.readValue(message.body(), EbookSqsMessage.class);

            log.info("Processed Ebook - ID: {}, Title: {}", ebookSqsMessage.getEbookId(), ebookSqsMessage.getTitle());
        } catch (Exception e) {
            log.error("Error processing message: {}", message.body(), e);
        }
    }

    private void deleteMessageFromQueue(Message message) {
        DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder()
                .queueUrl(sqsQueueUrl)
                .receiptHandle(message.receiptHandle())
                .build();
        sqsClient.deleteMessage(deleteMessageRequest);

        log.info("Deleted message with ID: {}", message.messageId());
    }
}
