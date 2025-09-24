package com.example.ai_chatbot.repository;

import com.example.ai_chatbot.model.ChatMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ChatMessageRepositoryTest {
    @Autowired ChatMessageRepository repository;

    @Test
    void testSaveAndFind() {
        ChatMessage msg = new ChatMessage("u1", "USER", "Test", LocalDateTime.now());
        ChatMessage saved = repository.save(msg);
        assertNotNull(saved.getId());
        assertEquals("Test", saved.getMessage());
    }

    @Test
    void testSaveNullFields() {
        ChatMessage msg = new ChatMessage(null, null, null, null);
        ChatMessage saved = repository.save(msg);
        assertNotNull(saved.getId());
    }

    @Test
    void testFindByUserId() {
        String userId = "u2";
        repository.save(new ChatMessage(userId, "USER", "Hello", LocalDateTime.now()));
        List<ChatMessage> found = repository.findByUserIdOrderByTimestampAsc(userId);
        assertFalse(found.isEmpty());
        assertEquals(userId, found.get(0).getUserId());
    }
}
