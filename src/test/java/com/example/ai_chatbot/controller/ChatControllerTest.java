package com.example.ai_chatbot.controller;

import com.example.ai_chatbot.service.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class ChatControllerTest {
    @Mock
    private ChatService chatService;

    @InjectMocks
    private ChatController chatController;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    private static Stream<Object[]> validMessages() {
        return Stream.of(
            new Object[]{"Hi", "testUser", "Hello, testUser!"},
            new Object[]{"", "user", "Empty!"},
            new Object[]{"   ", "user", "Whitespace!"},
            new Object[]{"!@#$%^&*", "user", "Special!"},
            new Object[]{"a".repeat(1000), "user", "Long!"},
            new Object[]{"Hello", "!@#user", "Hello, !@#user!"}
        );
    }

    @ParameterizedTest
    @MethodSource("validMessages")
    void testChatValidMessages(String message, String username, String expectedResponse) {
        Map<String, String> payload = Map.of("message", message);
        when(authentication.getName()).thenReturn(username);
        when(chatService.chat(username, message)).thenReturn(Map.of("response", expectedResponse));
        ResponseEntity<?> response = chatController.chat(payload, authentication);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().toString().contains(expectedResponse));
    }

    @Test
    void testChatMissingMessage() {
        when(authentication.getName()).thenReturn("user");
        assertEquals(400, chatController.chat(new HashMap<>(), authentication).getStatusCodeValue());
    }

    @Test
    void testChatNullMessageInPayload() {
        Map<String, String> payload = new HashMap<>();
        payload.put("message", null);
        when(authentication.getName()).thenReturn("user");
        assertEquals(400, chatController.chat(payload, authentication).getStatusCodeValue());
    }

    @Test
    void testChatEmptyPayload() {
        when(authentication.getName()).thenReturn("user");
        assertEquals(400, chatController.chat(new HashMap<>(), authentication).getStatusCodeValue());
    }

    @Test
    void testChatNullAuthentication() {
        assertEquals(401, chatController.chat(Map.of("message", "Hi"), null).getStatusCodeValue());
    }

    @Test
    void testChatMissingAuthenticationName() {
        when(authentication.getName()).thenReturn(null);
        assertEquals(401, chatController.chat(Map.of("message", "Hi"), authentication).getStatusCodeValue());
    }
}
