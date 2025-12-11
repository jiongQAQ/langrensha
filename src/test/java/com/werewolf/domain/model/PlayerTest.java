package com.werewolf.domain.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.werewolf.domain.role.Werewolf;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Player序列化测试
 *
 * @author Claude & User
 */
class PlayerTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void testPlayerBasicInfo() {
        Player player = Player.builder()
                .id(1L)
                .name("玩家1")
                .type(Player.PlayerType.HUMAN)
                .role(new Werewolf())
                .alive(true)
                .seatNumber(1)
                .hasSpoken(false)
                .hasVoted(false)
                .joinTime(LocalDateTime.now())
                .build();

        assertEquals(1L, player.getId());
        assertEquals("玩家1", player.getName());
        assertTrue(player.isAlive());
        assertTrue(player.isHuman());
        assertFalse(player.isAI());
    }

    @Test
    void testPlayerDeath() {
        Player player = Player.builder()
                .id(1L)
                .name("玩家1")
                .alive(true)
                .build();

        assertTrue(player.isAlive());
        assertNull(player.getDeathTime());

        player.markDead(Player.DeathReason.KILLED_BY_WEREWOLF);

        assertFalse(player.isAlive());
        assertNotNull(player.getDeathTime());
        assertEquals(Player.DeathReason.KILLED_BY_WEREWOLF, player.getDeathReason());
    }

    @Test
    void testPlayerRevive() {
        Player player = Player.builder()
                .id(1L)
                .name("玩家1")
                .alive(false)
                .deathReason(Player.DeathReason.KILLED_BY_WEREWOLF)
                .build();

        assertFalse(player.isAlive());

        player.revive();

        assertTrue(player.isAlive());
        assertNull(player.getDeathTime());
        assertNull(player.getDeathReason());
    }

    @Test
    void testPlayerSerialization() throws Exception {
        Player player = Player.builder()
                .id(1L)
                .name("玩家1")
                .type(Player.PlayerType.AI)
                .alive(true)
                .seatNumber(1)
                .hasSpoken(false)
                .hasVoted(false)
                .joinTime(LocalDateTime.now())
                .build();

        // 序列化
        String json = objectMapper.writeValueAsString(player);
        assertNotNull(json);
        assertTrue(json.contains("玩家1"));

        // 反序列化
        Player deserializedPlayer = objectMapper.readValue(json, Player.class);
        assertNotNull(deserializedPlayer);
        assertEquals(player.getId(), deserializedPlayer.getId());
        assertEquals(player.getName(), deserializedPlayer.getName());
        assertEquals(player.getType(), deserializedPlayer.getType());
    }

    @Test
    void testPlayerResetStatus() {
        Player player = Player.builder()
                .id(1L)
                .hasSpoken(true)
                .hasVoted(true)
                .voteTargetId(2L)
                .build();

        assertTrue(player.isHasSpoken());
        assertTrue(player.isHasVoted());

        player.resetSpeechStatus();
        assertFalse(player.isHasSpoken());

        player.resetVoteStatus();
        assertFalse(player.isHasVoted());
        assertNull(player.getVoteTargetId());
    }
}
