package com.preppilot.service;

import com.preppilot.dto.KnowledgeIngestRequest;
import com.preppilot.entity.KnowledgeDocument;
import com.preppilot.repository.KnowledgeDocumentRepository;
import com.preppilot.service.impl.VectorStoreServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KnowledgeService {

    private final KnowledgeDocumentRepository knowledgeDocumentRepository;
    private final PdfExtractionService textChunker;
    private final EmbeddingService embeddingService;
    private final VectorStoreServiceImpl vectorStore;

    @Transactional
    public KnowledgeDocument ingestDocument(KnowledgeIngestRequest req) {
        KnowledgeDocument doc = KnowledgeDocument.builder()
                .sourceType(req.sourceType())
                .companyName(req.companyName())
                .title(req.title())
                .content(req.content())
                .build();

        doc = knowledgeDocumentRepository.save(doc);

        List<String> chunks = textChunker.chunk(req.content());
        for (int i = 0; i < chunks.size(); i++) {
            String chunkText = chunks.get(i);
            float[] embedding = embeddingService.embed(chunkText);
            vectorStore.insertKnowledgeChunk(UUID.randomUUID(), doc.getId(), i, chunkText, embedding);
        }

        return doc;
    }

    public List<KnowledgeDocument> listAll() {
        return knowledgeDocumentRepository.findAll();
    }

    public List<KnowledgeDocument> listByCompany(String companyName) {
        return knowledgeDocumentRepository.findByCompanyName(companyName);
    }
}
