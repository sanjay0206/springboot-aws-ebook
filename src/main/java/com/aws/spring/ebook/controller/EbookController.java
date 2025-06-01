package com.aws.spring.ebook.controller;

import com.aws.spring.ebook.entity.Ebook;
import com.aws.spring.ebook.service.EbookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLConnection;
import java.util.List;

@RestController
@RequestMapping("/api/ebooks")
@RequiredArgsConstructor
@Tag(name = "Ebook Management", description = "API for managing eBooks")
public class EbookController {

    private final EbookService ebookService;

    @Operation(summary = "Get all eBooks", description = "Retrieve a list of all eBooks")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved eBooks"),
            @ApiResponse(responseCode = "404", description = "No eBooks found", content = @Content)
    })
    @GetMapping
    public ResponseEntity<List<Ebook>> getAllEbooks() {
        List<Ebook> ebooks = ebookService.getAllEbooks();
        return ebooks.isEmpty() ? ResponseEntity.status(HttpStatus.NOT_FOUND).body(null) : ResponseEntity.ok(ebooks);
    }

    @Operation(summary = "Get eBook by ID", description = "Retrieve an eBook by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved eBook"),
            @ApiResponse(responseCode = "404", description = "eBook not found", content = @Content)
    })
    @GetMapping("/{ebookId}")
    public ResponseEntity<Ebook> getEbook(@Parameter(description = "ID of the eBook to be retrieved") @PathVariable String ebookId) {
        Ebook ebook = ebookService.getEbook(ebookId);
        return ebook == null ? ResponseEntity.status(HttpStatus.NOT_FOUND).body(null) : ResponseEntity.ok(ebook);
    }

    @Operation(summary = "Download eBook", description = "Download the eBook file by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully downloaded eBook"),
            @ApiResponse(responseCode = "404", description = "eBook not found", content = @Content)
    })
    @GetMapping("/download/{ebookId}")
    public ResponseEntity<byte[]> downloadEbook(@Parameter(description = "ID of the eBook to download") @PathVariable String ebookId) throws IOException {
        byte[] ebookContent = ebookService.downloadEbook(ebookId);
        String fileName = ebookService.getObjectName(ebookId);
        String contentType = URLConnection.guessContentTypeFromName(fileName);
        contentType = contentType == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : contentType;

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(ebookContent);
    }

    @Operation(summary = "Create a new eBook", description = "Upload a new eBook with metadata")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully created eBook")
    })
    @PostMapping
    public ResponseEntity<Ebook> createEbook(@RequestParam MultipartFile file,
                                             @RequestParam String title,
                                             @RequestParam String author,
                                             @RequestParam String genre) throws IOException {
        Ebook ebook = ebookService.createEbook(file, title, author, genre);
        return ResponseEntity.status(HttpStatus.CREATED).body(ebook);
    }

    @Operation(summary = "Update an eBook", description = "Update an existing eBook")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated eBook"),
            @ApiResponse(responseCode = "404", description = "eBook not found", content = @Content)
    })
    @PutMapping("/{ebookId}")
    public ResponseEntity<Ebook> updateEbook(@PathVariable String ebookId,
                                             @RequestParam(required = false) String title,
                                             @RequestParam(required = false) String author,
                                             @RequestParam(required = false) String genre,
                                             @RequestPart(required = false) MultipartFile newFile) throws IOException {
        Ebook ebook = ebookService.updateEbook(ebookId, title, author, genre, newFile);
        return ebook == null ? ResponseEntity.status(HttpStatus.NOT_FOUND).body(null) : ResponseEntity.ok(ebook);
    }

    @Operation(summary = "Delete eBook", description = "Delete an eBook by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successfully deleted eBook"),
            @ApiResponse(responseCode = "404", description = "eBook not found", content = @Content)
    })
    @DeleteMapping("/{ebookId}")
    public ResponseEntity<Void> deleteEbook(@PathVariable String ebookId) {
        return ebookService.deleteEbook(ebookId) ? ResponseEntity.noContent().build() : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}
