package com.werewolf.domain.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.werewolf.domain.role.Werewolf;
import com.werewolf.domain.role.Seer;
import com.werewolf.domain.role.Villager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GameState序列化测试
 *
 * @author Claude & User
 */
class GameStateTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void testGameStateBasicInfo() {
        List<Player> players = createTestPlayers();

        GameState gameState = GameState.builder()
                .gameId(1L)
                .roomId(100L)
                .currentPhase(Phase.WAITING)
                .currentRound(0)
                .players(players)
                .events(new ArrayList<>())
                .startTime(LocalDateTime.now())
                .status(GameState.GameStatus.WAITING)
                .build();

        assertEquals(1L, gameState.getGameId());
        assertEquals(100L, gameState.getRoomId());
        assertEquals(Phase.WAITING, gameState.getCurrentPhase());
        assertEquals(6, gameState.getPlayers().size());
    }

    @Test
    void testGetAlivePlayers() {
        List<Player> players = createTestPlayers();
        players.get(0).setAlive(false);
        players.get(1).setAlive(false);

        GameState gameState = GameState.builder()
                .players(players)
                .build();

        List<Player> alivePlayers = gameState.getAlivePlayers();
        assertEquals(4, alivePlayers.size());
    }

    @Test
    void testGetDeadPlayers() {
        List<Player> players = createTestPlayers();
        players.get(0).setAlive(false);
        players.get(1).setAlive(false);

        GameState gameState = GameState.builder()
                .players(players)
                .build();

        List<Player> deadPlayers = gameState.getDeadPlayers();
        assertEquals(2, deadPlayers.size());
    }

    @Test
    void testGetPlayerById() {
        List<Player> players = createTestPlayers();

        GameState gameState = GameState.builder()
                .players(players)
                .build();

        Player player = gameState.getPlayerById(1L);
        assertNotNull(player);
        assertEquals("玩家1", player.getName());

        Player nonExistentPlayer = gameState.getPlayerById(999L);
        assertNull(nonExistentPlayer);
    }

    @Test
    void testGetPlayerBySeat() {
        List<Player> players = createTestPlayers();

        GameState gameState = GameState.builder()
                .players(players)
                .build();

        Player player = gameState.getPlayerBySeat(3);
        assertNotNull(player);
        assertEquals(3, player.getSeatNumber());
    }

    @Test
    void testAddEvent() {
        GameState gameState = GameState.builder()
                .gameId(1L)
                .currentRound(1)
                .currentPhase(Phase.NIGHT)
                .events(new ArrayList<>())
                .build();

        GameEvent event = GameEvent.createPublicEvent(
                GameEvent.EventType.NIGHT_START,
                "夜晚开始"
        );

        gameState.addEvent(event);

        assertEquals(1, gameState.getEvents().size());
        assertEquals(1L, gameState.getEvents().get(0).getGameId());
        assertEquals(1, gameState.getEvents().get(0).getRound());
        assertEquals(Phase.NIGHT, gameState.getEvents().get(0).getPhase());
    }

    @Test
    void testNextRound() {
        List<Player> players = createTestPlayers();
        players.forEach(player -> {
            player.setHasSpoken(true);
            player.setHasVoted(true);
        });

        GameState gameState = GameState.builder()
                .currentRound(1)
                .players(players)
                .lastNightDeaths(new ArrayList<>(List.of(1L, 2L)))
                .build();

        gameState.nextRound();

        assertEquals(2, gameState.getCurrentRound());
        assertTrue(gameState.getLastNightDeaths().isEmpty());
        players.forEach(player -> {
            assertFalse(player.isHasSpoken());
            assertFalse(player.isHasVoted());
        });
    }

    @Test
    void testChangePhase() {
        GameState gameState = GameState.builder()
                .currentPhase(Phase.WAITING)
                .build();

        gameState.changePhase(Phase.NIGHT);
        assertEquals(Phase.NIGHT, gameState.getCurrentPhase());
    }

    @Test
    void testFinishGame() {
        GameState gameState = GameState.builder()
                .status(GameState.GameStatus.RUNNING)
                .currentPhase(Phase.DAY)
                .build();

        assertFalse(gameState.isFinished());

        gameState.finishGame("好人阵营");

        assertTrue(gameState.isFinished());
        assertEquals(GameState.GameStatus.FINISHED, gameState.getStatus());
        assertEquals(Phase.FINISHED, gameState.getCurrentPhase());
        assertEquals("好人阵营", gameState.getWinningCamp());
        assertNotNull(gameState.getEndTime());
    }

    @Test
    void testGameStateSerialization() throws Exception {
        // 创建不包含role的简化玩家列表用于序列化测试
        List<Player> players = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            Player player = Player.builder()
                    .id((long) i)
                    .name("玩家" + i)
                    .type(Player.PlayerType.AI)
                    .role(null)  // 不设置role以避免序列化问题
                    .alive(true)
                    .seatNumber(i)
                    .hasSpoken(false)
                    .hasVoted(false)
                    .build();
            players.add(player);
        }

        GameState gameState = GameState.builder()
                .gameId(1L)
                .roomId(100L)
                .currentPhase(Phase.NIGHT)
                .currentRound(1)
                .players(players)
                .events(new ArrayList<>())
                .startTime(LocalDateTime.now())
                .status(GameState.GameStatus.RUNNING)
                .build();

        // 序列化
        String json = objectMapper.writeValueAsString(gameState);
        assertNotNull(json);

        // 反序列化
        GameState deserializedState = objectMapper.readValue(json, GameState.class);
        assertNotNull(deserializedState);
        assertEquals(gameState.getGameId(), deserializedState.getGameId());
        assertEquals(gameState.getRoomId(), deserializedState.getRoomId());
        assertEquals(gameState.getCurrentPhase(), deserializedState.getCurrentPhase());
        assertEquals(gameState.getPlayers().size(), deserializedState.getPlayers().size());
    }

    @Test
    void testGetEventsByPhase() {
        GameState gameState = GameState.builder()
                .gameId(1L)
                .currentRound(1)
                .currentPhase(Phase.NIGHT)
                .events(new ArrayList<>())
                .build();

        // 在NIGHT阶段添加事件
        GameEvent nightEvent1 = GameEvent.createPublicEvent(
                GameEvent.EventType.NIGHT_START,
                "夜晚开始"
        );
        gameState.addEvent(nightEvent1);

        GameEvent nightEvent2 = GameEvent.createPublicEvent(
                GameEvent.EventType.WEREWOLF_KILL,
                "狼人击杀"
        );
        gameState.addEvent(nightEvent2);

        // 切换到DAY阶段
        gameState.changePhase(Phase.DAY);

        // 在DAY阶段添加事件
        GameEvent dayEvent = GameEvent.createPublicEvent(
                GameEvent.EventType.DAY_START,
                "白天开始"
        );
        gameState.addEvent(dayEvent);

        List<GameEvent> nightEvents = gameState.getEventsByPhase(Phase.NIGHT);
        assertEquals(2, nightEvents.size());

        List<GameEvent> dayEvents = gameState.getEventsByPhase(Phase.DAY);
        assertEquals(1, dayEvents.size());
    }

    private List<Player> createTestPlayers() {
        List<Player> players = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            Player player = Player.builder()
                    .id((long) i)
                    .name("玩家" + i)
                    .type(Player.PlayerType.AI)
                    .role(i <= 2 ? new Werewolf() : i <= 4 ? new Villager() : new Seer())
                    .alive(true)
                    .seatNumber(i)
                    .hasSpoken(false)
                    .hasVoted(false)
                    .build();
            players.add(player);
        }
        return players;
    }
}
