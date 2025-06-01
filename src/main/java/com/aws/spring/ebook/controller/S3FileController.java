package com.aws.spring.ebook.controller;

import com.aws.spring.ebook.dto.S3ObjectDetails;
import com.aws.spring.ebook.service.S3FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "S3 File Controller", description = "APIs for uploading, downloading, and managing files in AWS S3")
public class S3FileController {

    private final S3FileService s3FileService;

    @Value("${aws.s3.bucket-name}")
    private String defaultBucketName;

    @Operation(summary = "List all files in an S3 bucket")
    @GetMapping("/list")
    public ResponseEntity<List<String>> listAllObjects(@RequestParam(required = false) String bucketName) {
        bucketName = (bucketName == null || bucketName.isEmpty()) ? defaultBucketName : bucketName;
        List<String> objects = s3FileService.listAllObjectsFromS3(bucketName);
        return ResponseEntity.ok(objects);
    }

    @Operation(summary = "Download a file from an S3 bucket")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File downloaded successfully"),
            @ApiResponse(responseCode = "404", description = "File not found")
    })
    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadFile(@RequestParam(required = false) String bucketName,
                                               @RequestParam String key) throws IOException {
        bucketName = (bucketName == null || bucketName.isEmpty()) ? defaultBucketName : bucketName;

        byte[] ebookContent;
        try (InputStream inputStream = s3FileService.downloadFileFromS3(bucketName, key)) {
            ebookContent = inputStream.readAllBytes();
        }

        String contentType = URLConnection.guessContentTypeFromName(key);
        contentType = contentType == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : contentType;

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + key + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(ebookContent);
    }

    @Operation(summary = "Upload a file to S3")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "File uploaded successfully")
    })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<S3ObjectDetails> uploadFile(
            @RequestParam(required = false) String bucketName,
            @RequestParam String ebookId,
            @RequestPart("file") MultipartFile file) throws IOException {
        bucketName = (bucketName == null || bucketName.isEmpty()) ? defaultBucketName : bucketName;

        S3ObjectDetails uploadedFileDetails = s3FileService.uploadFileToS3(bucketName, file, ebookId);
        return ResponseEntity.status(HttpStatus.CREATED).body(uploadedFileDetails);
    }

    @Operation(summary = "Delete a specific file from S3")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "File deleted successfully")
    })
    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteFile(@RequestParam(required = false) String bucketName,
                                           @RequestParam String key) {
        bucketName = (bucketName == null || bucketName.isEmpty()) ? defaultBucketName : bucketName;

        s3FileService.deleteFileFromS3(bucketName, key);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete all files from an S3 bucket")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "All files deleted successfully")
    })
    @DeleteMapping("/delete-all")
    public ResponseEntity<Void> deleteAllObjects(@RequestParam(required = false) String bucketName) {
        bucketName = (bucketName == null || bucketName.isEmpty()) ? defaultBucketName : bucketName;

        s3FileService.deleteAllObjectsFromS3(bucketName);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Generate a presigned URL for a file")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Presigned URL generated successfully")
    })
    @GetMapping("/presigned-url")
    public ResponseEntity<String> getPresignedUrl(
            @RequestParam String bucketName,
            @RequestParam String key,
            @RequestParam(defaultValue = "15") int expiryMinutes) {
        bucketName = (bucketName == null || bucketName.isEmpty()) ? defaultBucketName : bucketName;

        String url = s3FileService.generatePresignedUrl(bucketName, key, expiryMinutes);
        return ResponseEntity.ok(url);
    }
}
