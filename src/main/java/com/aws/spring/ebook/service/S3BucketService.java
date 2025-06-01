package com.aws.spring.ebook.service;

import java.util.List;

public interface S3BucketService {

    void createNewBucket(String bucketName, String region);

    void removeBucket(String bucketName);

    String fetchBucketRegion(String bucketName);

    List<String> fetchAllBucketNames();

    boolean checkBucketExists(String bucketName);

    String getBucketVersioningStatus(String bucketName);

    void updateBucketVersioning(String bucketName, boolean enableVersioning);
}
