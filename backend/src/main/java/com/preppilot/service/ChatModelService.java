package com.preppilot.service;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChatModelService {

    @Value("${app.ai.openai-api-key}")
    private String openAiApiKey;

    @Value("${app.ai.chat-model}")
    private String chatModelName;

    private ChatLanguageModel chatModel;

    @PostConstruct
    void init() {
        this.chatModel = OpenAiChatModel.builder()
                .apiKey(openAiApiKey)
                .modelName(chatModelName)
                .temperature(0.7)
                .build();
    }

    /**
     * @param systemPrompt   instructions + RAG context for the interviewer persona
     * @param conversation   prior turns as alternating user/AI text (user first)
     */
    public String generateReply(String systemPrompt, List<String[]> conversation) {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(SystemMessage.from(systemPrompt));

        for (String[] turn : conversation) {
            String sender = turn[0];
            String content = turn[1];
            if ("USER".equals(sender)) {
                messages.add(UserMessage.from(content));
            } else {
                messages.add(AiMessage.from(content));
            }
        }

        return chatModel.generate(messages).content().text();
    }
}
