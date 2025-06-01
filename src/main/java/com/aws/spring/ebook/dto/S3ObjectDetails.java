package com.aws.spring.ebook.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class S3ObjectDetails {
    private String objectName;
    private String objectUrl;
    private String key;
    private String contentType;
    private long contentLength;
    private String lastModified;
}
