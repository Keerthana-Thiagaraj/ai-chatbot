package com.example.ai_chatbot.controller;

import com.example.ai_chatbot.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ResponseEntity<?> chat(@RequestBody Map<String, String> payload, Authentication authentication) {
        String message = payload.get("message");
        if (message == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing message"));
        }
        String userId = authentication.getName();
        Map<String, Object> response = chatService.chat(userId, message);
        return ResponseEntity.ok(response);
    }
}
