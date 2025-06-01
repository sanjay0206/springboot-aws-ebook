package com.aws.spring.ebook.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EbookSqsMessage {
    private String ebookId;
    private String title;
    private String pdfUrl;
}
