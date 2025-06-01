package com.aws.spring.ebook.controller;

import com.aws.spring.ebook.service.S3BucketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/buckets")
@Tag(name = "S3 Bucket Controller", description = "APIs for managing AWS S3 Buckets")
public class S3BucketController {

    private final S3BucketService s3BucketService;

    public S3BucketController(S3BucketService s3BucketService) {
        this.s3BucketService = s3BucketService;
    }

    @Operation(summary = "Create a new bucket")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bucket created successfully")
    })
    @PostMapping
    public ResponseEntity<String> createNewBucket(
            @RequestParam String bucketName,
            @RequestParam String region) {
        s3BucketService.createNewBucket(bucketName, region);
        return ResponseEntity.ok("Bucket created successfully: " + bucketName);
    }

    @Operation(summary = "Delete an existing bucket")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bucket deleted successfully")
    })
    @DeleteMapping("/{bucketName}")
    public ResponseEntity<String> removeBucket(@PathVariable String bucketName) {
        s3BucketService.removeBucket(bucketName);
        return ResponseEntity.ok("Bucket deleted successfully: " + bucketName);
    }

    @Operation(summary = "Get the region of a specific bucket")
    @ApiResponse(responseCode = "200", description = "Region fetched successfully")
    @GetMapping("/{bucketName}/region")
    public ResponseEntity<String> fetchBucketRegion(@PathVariable String bucketName) {
        String region = s3BucketService.fetchBucketRegion(bucketName);
        return ResponseEntity.ok("Bucket region: " + region);
    }

    @Operation(summary = "List all bucket names")
    @ApiResponse(responseCode = "200", description = "Buckets fetched successfully")
    @GetMapping
    public ResponseEntity<List<String>> fetchAllBucketNames() {
        List<String> buckets = s3BucketService.fetchAllBucketNames();
        return ResponseEntity.ok(buckets);
    }

    @Operation(summary = "Check if a bucket exists")
    @ApiResponse(responseCode = "200", description = "Bucket existence checked")
    @GetMapping("/{bucketName}/exists")
    public ResponseEntity<String> checkBucketExists(@PathVariable String bucketName) {
        boolean exists = s3BucketService.checkBucketExists(bucketName);
        return ResponseEntity.ok(exists ? "Bucket exists: " + bucketName : "Bucket does not exist: " + bucketName);
    }

    @Operation(summary = "Get the versioning status of a bucket")
    @ApiResponse(responseCode = "200", description = "Versioning status fetched")
    @GetMapping("/{bucketName}/versioning")
    public ResponseEntity<String> fetchBucketVersioningStatus(@PathVariable String bucketName) {
        String status = s3BucketService.getBucketVersioningStatus(bucketName);
        return ResponseEntity.ok("Bucket versioning status: " + status);
    }

    @Operation(summary = "Update the versioning setting of a bucket")
    @ApiResponse(responseCode = "200", description = "Versioning updated successfully")
    @PutMapping("/{bucketName}/update")
    public ResponseEntity<String> updateBucket(
            @PathVariable String bucketName,
            @RequestParam boolean enableVersioning) {
        s3BucketService.updateBucketVersioning(bucketName, enableVersioning);
        return ResponseEntity.ok("Bucket versioning " + (enableVersioning ? "enabled" : "disabled") + " for bucket: " + bucketName);
    }
}
