package com.aws.spring.ebook.respository;

import com.aws.spring.ebook.entity.Ebook;

import java.util.List;

public interface EbookRepository {
    List<Ebook> getAllItems();

    Ebook getItem(String ebookId);

    void createItem(Ebook ebook);

    void updateItem(Ebook ebook);

    void deleteItem(String ebookId);
}
