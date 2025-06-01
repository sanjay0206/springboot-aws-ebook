package com.aws.spring.ebook.respository.impl;

import com.aws.spring.ebook.entity.Ebook;
import com.aws.spring.ebook.respository.EbookRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class EbookRepositoryImpl implements EbookRepository {

    private final DynamoDbClient dynamoDbClient;

    @Value("${aws.dynamodb.table-name}")
    private String tableName;

    public EbookRepositoryImpl(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    @Override
    public List<Ebook> getAllItems() {
        // Scan the entire DynamoDB table to retrieve all ebooks
        ScanRequest scanRequest = ScanRequest.builder()
                .tableName(tableName)
                .build();

        ScanResponse scanResponse = dynamoDbClient.scan(scanRequest);

        // Convert the scan result into a list of Ebook objects
        return scanResponse.items().stream()
                .map(item -> new Ebook(
                        item.get("id").s(),
                        item.get("title").s(),
                        item.get("author").s(),
                        item.get("genre").s(),
                        item.get("pdfUrl").s()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public Ebook getItem(String ebookId) {
        Map<String, AttributeValue> key = Map.of("id", AttributeValue.builder().s(ebookId).build());

        GetItemResponse response = dynamoDbClient.getItem(GetItemRequest.builder()
                .tableName(tableName)
                .key(key)
                .build());

        if (!response.hasItem()) {
            return null;
        }

        Map<String, AttributeValue> item = response.item();
        return new Ebook(
                item.get("id").s(),
                item.get("title").s(),
                item.get("author").s(),
                item.get("genre").s(),
                item.get("pdfUrl").s()
        );
    }

    @Override
    public void createItem(Ebook ebook) {
        Map<String, AttributeValue> item = Map.of(
                "id", AttributeValue.builder().s(ebook.getEbookId()).build(),
                "title", AttributeValue.builder().s(ebook.getTitle()).build(),
                "author", AttributeValue.builder().s(ebook.getAuthor()).build(),
                "genre", AttributeValue.builder().s(ebook.getGenre()).build(),
                "pdfUrl", AttributeValue.builder().s(ebook.getUrl()).build()
        );

        dynamoDbClient.putItem(PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .build());
    }

    @Override
    public void updateItem(Ebook ebook) {
        Map<String, AttributeValue> key = Map.of("id", AttributeValue.builder().s(ebook.getEbookId()).build());

        Map<String, AttributeValueUpdate> updates = Map.of(
                "title", AttributeValueUpdate.builder()
                        .value(AttributeValue.builder().s(ebook.getTitle()).build())
                        .action(AttributeAction.PUT)
                        .build(),
                "author", AttributeValueUpdate.builder()
                        .value(AttributeValue.builder().s(ebook.getAuthor()).build())
                        .action(AttributeAction.PUT)
                        .build(),
                "genre", AttributeValueUpdate.builder()
                        .value(AttributeValue.builder().s(ebook.getGenre()).build())
                        .action(AttributeAction.PUT)
                        .build(),
                "pdfUrl", AttributeValueUpdate.builder()
                        .value(AttributeValue.builder().s(ebook.getUrl()).build())
                        .action(AttributeAction.PUT)
                        .build()
        );

        dynamoDbClient.updateItem(UpdateItemRequest.builder()
                .tableName(tableName)
                .key(key)
                .attributeUpdates(updates)
                .build());
    }

    @Override
    public void deleteItem(String ebookId) {
        Map<String, AttributeValue> key = Map.of("id", AttributeValue.builder().s(ebookId).build());

        dynamoDbClient.deleteItem(DeleteItemRequest.builder()
                .tableName(tableName)
                .key(key)
                .build());
    }
}
