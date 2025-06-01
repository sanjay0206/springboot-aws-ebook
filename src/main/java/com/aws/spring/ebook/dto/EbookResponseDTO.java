package com.aws.spring.ebook.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EbookResponseDTO {
    private String ebookId;
    private String title;
    private String author;
    private String genre;
    private String pdfUrl;
}
