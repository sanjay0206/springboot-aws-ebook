package com.aws.spring.ebook.service;

import com.aws.spring.ebook.entity.Ebook;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface EbookService {

    List<Ebook> getAllEbooks();

    Ebook getEbook(String ebookId);

    Ebook createEbook(MultipartFile file, String title, String author, String genre) throws IOException;

    Ebook updateEbook(String ebookId, String title, String author, String genre, MultipartFile newFile) throws IOException;

    boolean deleteEbook(String ebookId);

    byte[] downloadEbook(String ebookId) throws IOException;

    String getObjectName(String ebookId);
}
