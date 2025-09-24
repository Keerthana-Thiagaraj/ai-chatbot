package com.example.ai_chatbot.service;

import com.example.ai_chatbot.model.ChatMessage;
import com.example.ai_chatbot.repository.ChatMessageRepository;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.ollama.OllamaChatModel;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ChatService {

    private final OllamaChatModel chatModel;
    private final ChatMessageRepository chatMessageRepository;

    public ChatService(ChatMessageRepository chatMessageRepository, OllamaChatModel chatModel) {
        this.chatModel = chatModel;
        this.chatMessageRepository = chatMessageRepository;
    }

    // For production use, keep the old constructor for backward compatibility
    public ChatService(ChatMessageRepository chatMessageRepository) {
        this(chatMessageRepository, OllamaChatModel.builder()
                .baseUrl("http://localhost:11434") // default Ollama server
                .modelName("llama2")               // or mistral, phi3, codellama etc.
                .build());
    }

    public Map<String, Object> chat(String userId, String userMessage) {
        chatMessageRepository.save(new ChatMessage(userId, "USER", userMessage, LocalDateTime.now()));
        String aiReply = String.valueOf(chatModel.chat(List.of(UserMessage.from(userMessage))).aiMessage());
        chatMessageRepository.save(new ChatMessage(userId, "AI", aiReply, LocalDateTime.now()));
        List<String> formattedHistory = new ArrayList<>();
        chatMessageRepository.findByUserIdOrderByTimestampAsc(userId)
            .forEach(msg -> formattedHistory.add(msg.getSender() + ": " + msg.getMessage()));
        return Map.of("reply", aiReply, "history", formattedHistory);
    }
}
