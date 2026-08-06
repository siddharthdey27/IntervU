package com.preppilot.service;

import com.preppilot.entity.Resume;
import com.preppilot.repository.ResumeRepository;
import com.preppilot.service.impl.VectorStoreServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private final S3StorageService s3StorageService;
    private final PdfExtractionService pdfExtractionService;
    private final EmbeddingService embeddingService;
    private final VectorStoreServiceImpl vectorStore;
    private final ResumeRepository resumeRepository;

    /**
     * Full ingestion pipeline: upload PDF to S3 -> extract text -> chunk ->
     * embed each chunk -> persist resume + chunks (with embeddings) to Supabase Postgres.
     */
    public Resume ingestResume(UUID userId, MultipartFile file) throws IOException {
        byte[] bytes = file.getBytes();

        String s3Key = s3StorageService.uploadResume(userId, file.getOriginalFilename(), bytes);
        String rawText = pdfExtractionService.extractText(bytes);

        Resume resume = Resume.builder()
                .userId(userId)
                .fileName(file.getOriginalFilename())
                .s3Key(s3Key)
                .rawText(rawText)
                .build();
        resume = resumeRepository.save(resume);

        List<String> chunks = pdfExtractionService.chunk(rawText);
        for (int i = 0; i < chunks.size(); i++) {
            String chunkText = chunks.get(i);
            float[] embedding = embeddingService.embed(chunkText);
            vectorStore.insertResumeChunk(UUID.randomUUID(), resume.getId(), i, chunkText, embedding);
        }

        return resume;
    }

    public List<Resume> listForUser(UUID userId) {
        return resumeRepository.findByUserIdOrderByUploadedAtDesc(userId);
    }
}
