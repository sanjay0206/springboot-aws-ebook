package com.aws.spring.ebook.service.impl;


import com.aws.spring.ebook.dto.EbookSqsMessage;
import com.aws.spring.ebook.dto.S3ObjectDetails;
import com.aws.spring.ebook.entity.Ebook;
import com.aws.spring.ebook.exception.EBookNotFoundException;
import com.aws.spring.ebook.respository.EbookRepository;
import com.aws.spring.ebook.service.EbookService;
import com.aws.spring.ebook.service.S3BucketService;
import com.aws.spring.ebook.service.S3FileService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EbookServiceImpl implements EbookService {

    private final EbookRepository ebookRepository;
    private final S3FileService s3FileService;
    private final SqsClient sqsClient;
    private final SnsClient snsClient;

    @Value("${aws.sqsQueueUrl}")
    private String sqsQueueUrl;

    @Value("${aws.snsTopicArn}")
    private String snsTopicArn;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Override
    public List<Ebook> getAllEbooks() {
        return ebookRepository.getAllItems();
    }

    @Override
    public Ebook getEbook(String ebookId) {
        return ebookRepository.getItem(ebookId);
    }

    @Override
    public Ebook createEbook(MultipartFile file, String title, String author, String genre) throws IOException {
        String ebookId = UUID.randomUUID().toString();
        S3ObjectDetails s3ObjectDetails = s3FileService.uploadFileToS3(bucketName, file, ebookId);

        Ebook ebook = new Ebook(ebookId, title, author, genre, s3ObjectDetails.getObjectUrl());
        ebookRepository.createItem(ebook);

        // Send message to SQS for background processing
//        sendEbookToProcessingQueue(ebook);

        // Notify users about the new eBook using SNS
//        notifyUsersOfNewEbook(ebook);

        return ebook;
    }

    private void sendEbookToProcessingQueue(Ebook ebook) {
        EbookSqsMessage message = new EbookSqsMessage(ebook.getEbookId(), ebook.getTitle(), ebook.getUrl());
        String messageBody = convertToJson(message);

        SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                .queueUrl(sqsQueueUrl)
                .messageBody(messageBody)
                .build();

        SendMessageResponse response = sqsClient.sendMessage(sendMessageRequest);
        log.info("Message sent successfully: {}", response.messageId());
    }

    private void notifyUsersOfNewEbook(Ebook ebook) {
        String message = "New eBook Available: '" + ebook.getTitle() + "' by " + ebook.getAuthor() +
                ". Check it out at: " + ebook.getUrl();

        PublishRequest publishRequest = PublishRequest.builder()
                .topicArn(snsTopicArn)
                .message(message)
                .build();

        PublishResponse response = snsClient.publish(publishRequest);
        log.info("Notification sent successfully: {}", response.messageId());
    }

    private String convertToJson(Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting to JSON", e);
        }
    }

    @Override
    public Ebook updateEbook(String ebookId, String title, String author, String genre, MultipartFile newFile) throws IOException {
        Ebook existingEbook = ebookRepository.getItem(ebookId);
        if (existingEbook == null) {
            throw new EBookNotFoundException("Ebook", "ebookId", ebookId);
        }

        if (title != null && !title.isBlank()) existingEbook.setTitle(title);
        if (author != null && !author.isBlank()) existingEbook.setAuthor(author);
        if (genre != null && !genre.isBlank()) existingEbook.setGenre(genre);
        if (newFile != null) {
            S3ObjectDetails s3ObjectDetails = s3FileService.uploadFileToS3(bucketName, newFile, ebookId);
        }

        ebookRepository.updateItem(existingEbook);
        return existingEbook;
    }

    @Override
    public boolean deleteEbook(String ebookId) {
        Ebook ebook = ebookRepository.getItem(ebookId);
        if (ebook == null) {
            throw new EBookNotFoundException("Ebook", "ebookId", ebookId);
        }

        String key = getObjectName(ebookId);
        s3FileService.deleteFileFromS3(bucketName, key);
        ebookRepository.deleteItem(ebookId);

        return true;
    }

    @Override
    public byte[] downloadEbook(String ebookId) throws IOException {
        Ebook ebook = ebookRepository.getItem(ebookId);
        if (ebook == null) {
            throw new EBookNotFoundException("Ebook", "ebookId", ebookId);
        }

        String key = getObjectName(ebookId);
        try (InputStream inputStream = s3FileService.downloadFileFromS3(bucketName, key)) {
            return inputStream.readAllBytes();
        }
    }

    @Override
    public String getObjectName(String ebookId) {
        Ebook ebook = ebookRepository.getItem(ebookId);
        if (ebook == null) {
            throw new EBookNotFoundException("Ebook", "ebookId", ebookId);
        }

        String fileUrl = ebook.getUrl();
        if (fileUrl == null || !fileUrl.contains("/")) {
            throw new IllegalArgumentException("Invalid S3 URL");
        }

        return fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
    }
}
