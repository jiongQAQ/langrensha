package com.werewolf.domain.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GameEvent序列化测试
 *
 * @author Claude & User
 */
class GameEventTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void testGameEventBasicInfo() {
        GameEvent event = GameEvent.builder()
                .id(1L)
                .gameId(100L)
                .type(GameEvent.EventType.GAME_START)
                .round(1)
                .phase(Phase.NIGHT)
                .description("游戏开始")
                .timestamp(LocalDateTime.now())
                .isPublic(true)
                .build();

        assertEquals(1L, event.getId());
        assertEquals(100L, event.getGameId());
        assertEquals(GameEvent.EventType.GAME_START, event.getType());
        assertEquals("游戏开始", event.getDescription());
        assertTrue(event.isPublic());
    }

    @Test
    void testCreatePublicEvent() {
        GameEvent event = GameEvent.createPublicEvent(
                GameEvent.EventType.DAY_START,
                "白天开始"
        );

        assertNotNull(event);
        assertEquals(GameEvent.EventType.DAY_START, event.getType());
        assertEquals("白天开始", event.getDescription());
        assertTrue(event.isPublic());
        assertNotNull(event.getTimestamp());
    }

    @Test
    void testCreatePrivateEvent() {
        GameEvent event = GameEvent.createPrivateEvent(
                GameEvent.EventType.SEER_CHECK,
                1L,
                "预言家查验了2号玩家"
        );

        assertNotNull(event);
        assertEquals(GameEvent.EventType.SEER_CHECK, event.getType());
        assertEquals(1L, event.getPlayerId());
        assertEquals("预言家查验了2号玩家", event.getDescription());
        assertFalse(event.isPublic());
        assertNotNull(event.getTimestamp());
    }

    @Test
    void testGameEventSerialization() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("targetId", 2L);
        data.put("result", "狼人");

        GameEvent event = GameEvent.builder()
                .id(1L)
                .gameId(100L)
                .type(GameEvent.EventType.SEER_CHECK)
                .round(1)
                .phase(Phase.NIGHT)
                .playerId(1L)
                .targetPlayerId(2L)
                .description("预言家查验")
                .data(data)
                .timestamp(LocalDateTime.now())
                .isPublic(false)
                .build();

        // 序列化
        String json = objectMapper.writeValueAsString(event);
        assertNotNull(json);
        assertTrue(json.contains("预言家查验"));

        // 反序列化
        GameEvent deserializedEvent = objectMapper.readValue(json, GameEvent.class);
        assertNotNull(deserializedEvent);
        assertEquals(event.getId(), deserializedEvent.getId());
        assertEquals(event.getType(), deserializedEvent.getType());
        assertEquals(event.getPlayerId(), deserializedEvent.getPlayerId());
        assertEquals(event.getTargetPlayerId(), deserializedEvent.getTargetPlayerId());
    }
}
