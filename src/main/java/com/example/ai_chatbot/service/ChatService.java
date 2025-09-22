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

    public ChatService(ChatMessageRepository chatMessageRepository) {
        this.chatModel = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434") // default Ollama server
                .modelName("llama2")               // or mistral, phi3, codellama etc.
                .build();
        this.chatMessageRepository = chatMessageRepository;
    }

    public Map<String, Object> chat(String userId, String userMessage) {
        // Save user message
        chatMessageRepository.save(new ChatMessage(userId, "USER", userMessage, LocalDateTime.now()));
        String aiReply = String.valueOf(chatModel.chat(List.of(UserMessage.from(userMessage))).aiMessage());
        // Save AI reply
        chatMessageRepository.save(new ChatMessage(userId, "AI", aiReply, LocalDateTime.now()));
        // Retrieve full history
        List<ChatMessage> history = chatMessageRepository.findByUserIdOrderByTimestampAsc(userId);
        List<String> formattedHistory = new ArrayList<>();
        for (ChatMessage msg : history) {
            formattedHistory.add(msg.getSender() + ": " + msg.getMessage());
        }
        Map<String, Object> result = new HashMap<>();
        result.put("reply", aiReply);
        result.put("history", formattedHistory);
        return result;
    }
}
