package com.aws.spring.ebook.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ebook {
    private String ebookId;
    private String title;
    private String author;
    private String genre;
    private String url;
}
