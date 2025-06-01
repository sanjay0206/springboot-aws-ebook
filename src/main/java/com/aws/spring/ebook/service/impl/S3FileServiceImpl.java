package com.aws.spring.ebook.service.impl;

import com.aws.spring.ebook.dto.S3ObjectDetails;
import com.aws.spring.ebook.service.S3FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class S3FileServiceImpl implements S3FileService {
    private final S3Presigner s3Presigner = S3Presigner.create();
    private final S3Client s3Client;
    @Value("${aws.s3.bucket-name}")

    private String bucketName;

    @Override
    public List<String> listAllObjectsFromS3(String bucketName) {
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .build();

        ListObjectsV2Response response = s3Client.listObjectsV2(request);

        // Extract and return object keys (file names)
        return response.contents()
                .stream()
                .map(S3Object::key)
                .collect(Collectors.toList());
    }

    @Override
    public InputStream downloadFileFromS3(String bucketName, String key) {

        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        return s3Client.getObject(request);
    }


    @Override
    public S3ObjectDetails uploadFileToS3(String bucketName,
                                          MultipartFile file,
                                          String ebookId) throws IOException {
        // Generate the key for the file in S3
        String key =  ebookId + "_" + file.getOriginalFilename();

        // Upload the file to S3
        s3Client.putObject(PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build(),
                RequestBody.fromInputStream(file.getInputStream(), file.getInputStream().available()));

        // Generate the URL for the uploaded file
        String objectUrl = "https://" + bucketName + ".s3.amazonaws.com/" + key;

        // Retrieve the metadata of the uploaded file
        HeadObjectResponse metadata = s3Client.headObject(HeadObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build());

        // Extract metadata details
        String contentType = metadata.contentType();
        long contentLength = metadata.contentLength();
        String lastModified = metadata.lastModified().toString();
        String objectName = file.getOriginalFilename();

        // Return the details in an S3ObjectDetails object
        return new S3ObjectDetails(objectName, objectUrl, key, contentType, contentLength, lastModified);
    }


    @Override
    public void deleteFileFromS3(String bucketName, String key) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build());
    }

    @Override
    public void deleteAllObjectsFromS3(String bucketName) {
        ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .build();

        ListObjectsV2Response listObjectsResponse = s3Client.listObjectsV2(listObjectsRequest);

        // Delete each object in the bucket
        listObjectsResponse.contents().forEach(s3Object -> {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Object.key())
                    .build();
            s3Client.deleteObject(deleteRequest);
        });
    }

    @Override
    public String generatePresignedUrl(String bucketName, String key, int expirationInMinutes) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(expirationInMinutes))
                .getObjectRequest(getObjectRequest)
                .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);

        return presignedRequest.url().toString();
    }
}