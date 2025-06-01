package com.aws.spring.ebook.service.impl;

import com.aws.spring.ebook.service.S3BucketService;
import com.aws.spring.ebook.service.S3FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class S3BucketServiceImpl implements S3BucketService {

    private final S3Client s3Client;
    private final S3FileService s3FileService;

    @Value("${aws.region}")
    private String defaultAwsRegion;

    @Override
    public void createNewBucket(String bucketName, String region) {
        CreateBucketRequest.Builder createBucketRequestBuilder = CreateBucketRequest.builder()
                .bucket(bucketName);

        // For all regions EXCEPT us-east-1, we need to add the location constraint
        if (!defaultAwsRegion.equals(region)) {
            createBucketRequestBuilder.createBucketConfiguration(config -> config
                    .locationConstraint(region));
        }

        s3Client.createBucket(createBucketRequestBuilder.build());
    }

    @Override
    public void removeBucket(String bucketName) {
        ListObjectsV2Response objects = s3Client.listObjectsV2(ListObjectsV2Request.builder()
                .bucket(bucketName)
                .build());

        // Objects are present, delete them first
        if (!objects.contents().isEmpty()) {
            s3FileService.deleteAllObjectsFromS3(bucketName);
        }

        s3Client.deleteBucket(DeleteBucketRequest.builder()
                .bucket(bucketName)
                .build());
    }

    @Override
    public String fetchBucketRegion(String bucketName) {
        GetBucketLocationResponse response = s3Client.getBucketLocation(
                GetBucketLocationRequest.builder().bucket(bucketName).build()
        );
        String region = response.locationConstraintAsString();

        return (region == null || region.isEmpty()) ? defaultAwsRegion : region;
    }

    @Override
    public List<String> fetchAllBucketNames() {
        return s3Client.listBuckets().buckets()
                .stream()
                .map(Bucket::name)
                .collect(Collectors.toList());
    }

    @Override
    public boolean checkBucketExists(String bucketName) {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
            return true;
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                return false;
            }
            throw e;
        }
    }

    public String getBucketVersioningStatus(String bucketName) {
        GetBucketVersioningRequest versioningRequest = GetBucketVersioningRequest.builder()
                .bucket(bucketName)
                .build();

        GetBucketVersioningResponse response = s3Client.getBucketVersioning(versioningRequest);

        return response.statusAsString(); // It returns "Enabled" or "null"
    }

    public void updateBucketVersioning(String bucketName, boolean enableVersioning) {
        PutBucketVersioningRequest versioningRequest = PutBucketVersioningRequest.builder()
                .bucket(bucketName)
                .versioningConfiguration(versionConfig -> versionConfig
                        .status(BucketVersioningStatus.ENABLED))
                .build();

        s3Client.putBucketVersioning(versioningRequest);
    }
}
