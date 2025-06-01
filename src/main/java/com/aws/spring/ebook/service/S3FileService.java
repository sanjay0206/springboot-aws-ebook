package com.aws.spring.ebook.service;

import com.aws.spring.ebook.dto.S3ObjectDetails;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface S3FileService {

    List<String> listAllObjectsFromS3(String bucketName);

    InputStream downloadFileFromS3(String bucketName, String key);

    S3ObjectDetails uploadFileToS3(String bucketName, MultipartFile file, String ebookId) throws IOException;

    void deleteFileFromS3(String bucketName, String key);

    void deleteAllObjectsFromS3(String bucketName);

    String generatePresignedUrl(String bucketName, String key, int expirationInMinutes);
}
