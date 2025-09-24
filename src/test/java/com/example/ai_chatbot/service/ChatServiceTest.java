package com.example.ai_chatbot.service;

import com.example.ai_chatbot.model.ChatMessage;
import com.example.ai_chatbot.repository.ChatMessageRepository;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.data.message.AiMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class ChatServiceTest {
    @Mock
    private ChatMessageRepository chatMessageRepository;
    @Mock
    private OllamaChatModel ollamaChatModel;
    private ChatService chatService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        chatService = new ChatService(chatMessageRepository, ollamaChatModel);
        ChatResponse chatResponseMock = mock(ChatResponse.class);
        when(chatResponseMock.aiMessage()).thenReturn(new AiMessage("AI reply"));
        when(ollamaChatModel.chat(anyList())).thenReturn(chatResponseMock);
    }

    private static Stream<Object[]> validReplyCases() {
        return Stream.of(
            new Object[]{"user1", "Hello"},
            new Object[]{null, "Test"},
            new Object[]{"user5", "a".repeat(1000)},
            new Object[]{"user6", "!@#$%^&*()"},
            new Object[]{"", "Hello"},
            new Object[]{"!@#user", "Hello"}
        );
    }

    @ParameterizedTest
    @MethodSource("validReplyCases")
    void testChatReturnsReply(String userId, String userMessage) {
        List<ChatMessage> history = List.of(new ChatMessage(userId, "USER", userMessage, LocalDateTime.now()));
        when(chatMessageRepository.findByUserIdOrderByTimestampAsc(userId)).thenReturn(history);
        Map<String, Object> result = chatService.chat(userId, userMessage);
        assertNotNull(result.get("reply"));
    }

    private static Stream<Object[]> exceptionCases() {
        return Stream.of(
            new Object[]{"user2", ""},
            new Object[]{"user3", null},
            new Object[]{"user4", "   "}
        );
    }

    @ParameterizedTest
    @MethodSource("exceptionCases")
    void testChatThrowsException(String userId, String userMessage) {
        assertThrows(IllegalArgumentException.class, () -> chatService.chat(userId, userMessage));
    }

    @Test
    void testChatNormalFlow() {
        String userId = "user1";
        String userMessage = "Hello";
        List<ChatMessage> history = Arrays.asList(
                new ChatMessage(userId, "USER", userMessage, LocalDateTime.now()),
                new ChatMessage(userId, "AI", "AI reply", LocalDateTime.now())
        );
        when(chatMessageRepository.findByUserIdOrderByTimestampAsc(userId)).thenReturn(history);
        Map<String, Object> result = chatService.chat(userId, userMessage);
        assertInstanceOf(String.class, result.get("reply"));
        assertTrue(((List<?>) result.get("history")).size() >= 2);
        verify(chatMessageRepository, times(2)).save(any(ChatMessage.class));
    }
}
