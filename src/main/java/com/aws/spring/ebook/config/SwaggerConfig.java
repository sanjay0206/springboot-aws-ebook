package com.aws.spring.ebook.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi ebookApi() {
        return GroupedOpenApi.builder()
                .group("Ebook Management API")
                .pathsToMatch("/api/ebooks/**")
                .build();
    }

    @Bean
    public GroupedOpenApi s3BucketApi() {
        return GroupedOpenApi.builder()
                .group("S3 Bucket API")
                .pathsToMatch("/api/buckets/**")
                .build();
    }

    @Bean
    public GroupedOpenApi s3FileApi() {
        return GroupedOpenApi.builder()
                .group("S3 File API")
                .pathsToMatch("/api/files/**")
                .build();
    }
}