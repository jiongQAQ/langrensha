package com.werewolf.engine.night;

import com.werewolf.domain.model.GameEvent;
import com.werewolf.domain.model.GameState;
import com.werewolf.domain.model.Player;
import com.werewolf.domain.role.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 夜晚阶段引擎测试
 *
 * @author Claude & User
 */
class NightPhaseEngineTest {

    private NightPhaseEngine engine;
    private GameState gameState;

    @BeforeEach
    void setUp() {
        engine = new NightPhaseEngine();
        gameState = createTestGameState();
    }

    @Test
    void testCompleteNightPhaseWithoutWitchAction() {
        // 狼人投票击杀3号
        Map<Long, Long> werewolfVotes = new HashMap<>();
        werewolfVotes.put(1L, 3L);
        werewolfVotes.put(2L, 3L);

        // 预言家查验1号
        Long seerCheckTarget = 1L;

        // 女巫不行动
        boolean witchUseAntidote = false;
        Long witchPoisonTarget = null;

        NightPhaseEngine.NightResult result = engine.executeNightPhase(
                gameState,
                werewolfVotes,
                seerCheckTarget,
                witchUseAntidote,
                witchPoisonTarget
        );

        // 验证狼人击杀结果
        assertNotNull(result.getWerewolfKillResult());
        assertTrue(result.getWerewolfKillResult().isSuccess());
        assertEquals(3L, result.getWerewolfKillResult().getTargetPlayerId());

        // 验证预言家查验结果
        assertNotNull(result.getSeerCheckResult());
        assertTrue(result.getSeerCheckResult().isSuccess());
        assertEquals(1L, result.getSeerCheckResult().getTargetPlayerId());
        assertTrue(result.getSeerCheckResult().isWerewolf());

        // 验证死亡名单
        assertNotNull(result.getDeaths());
        assertEquals(1, result.getDeaths().size());
        assertTrue(result.getDeaths().contains(3L));

        // 验证玩家状态
        Player player3 = gameState.getPlayerById(3L);
        assertFalse(player3.isAlive());
        assertEquals(Player.DeathReason.KILLED_BY_WEREWOLF, player3.getDeathReason());

        // 验证游戏状态的昨晚死亡列表
        assertEquals(1, gameState.getLastNightDeaths().size());
        assertTrue(gameState.getLastNightDeaths().contains(3L));
    }

    @Test
    void testCompleteNightPhaseWithWitchSave() {
        // 狼人投票击杀3号
        Map<Long, Long> werewolfVotes = new HashMap<>();
        werewolfVotes.put(1L, 3L);
        werewolfVotes.put(2L, 3L);

        // 预言家查验1号
        Long seerCheckTarget = 1L;

        // 女巫使用解药救活3号
        boolean witchUseAntidote = true;
        Long witchPoisonTarget = null;

        // 标记女巫已过首夜（允许救人）
        Player witch = gameState.getPlayerById(6L);
        ((Witch) witch.getRole()).markFirstNightPassed();

        NightPhaseEngine.NightResult result = engine.executeNightPhase(
                gameState,
                werewolfVotes,
                seerCheckTarget,
                witchUseAntidote,
                witchPoisonTarget
        );

        // 验证女巫行动结果
        assertNotNull(result.getWitchActionResult());
        assertTrue(result.getWitchActionResult().isSuccess());
        assertTrue(result.getWitchActionResult().isAntidoteUsed());
        assertEquals(3L, result.getWitchActionResult().getSavedPlayerId());

        // 验证死亡名单（应该为空，因为被救活）
        assertNotNull(result.getDeaths());
        assertEquals(0, result.getDeaths().size());

        // 验证玩家3存活
        Player player3 = gameState.getPlayerById(3L);
        assertTrue(player3.isAlive());
    }

    @Test
    void testCompleteNightPhaseWithWitchPoison() {
        // 狼人投票击杀3号
        Map<Long, Long> werewolfVotes = new HashMap<>();
        werewolfVotes.put(1L, 3L);
        werewolfVotes.put(2L, 3L);

        // 预言家查验2号
        Long seerCheckTarget = 2L;

        // 女巫使用毒药毒死1号狼人
        boolean witchUseAntidote = false;
        Long witchPoisonTarget = 1L;

        NightPhaseEngine.NightResult result = engine.executeNightPhase(
                gameState,
                werewolfVotes,
                seerCheckTarget,
                witchUseAntidote,
                witchPoisonTarget
        );

        // 验证女巫行动结果
        assertNotNull(result.getWitchActionResult());
        assertTrue(result.getWitchActionResult().isSuccess());
        assertTrue(result.getWitchActionResult().isPoisonUsed());
        assertEquals(1L, result.getWitchActionResult().getPoisonedPlayerId());

        // 验证死亡名单（3号被杀，1号被毒）
        assertNotNull(result.getDeaths());
        assertEquals(2, result.getDeaths().size());
        assertTrue(result.getDeaths().contains(3L));
        assertTrue(result.getDeaths().contains(1L));

        // 验证玩家状态
        Player player3 = gameState.getPlayerById(3L);
        assertFalse(player3.isAlive());
        assertEquals(Player.DeathReason.KILLED_BY_WEREWOLF, player3.getDeathReason());

        Player player1 = gameState.getPlayerById(1L);
        assertFalse(player1.isAlive());
        assertEquals(Player.DeathReason.POISONED, player1.getDeathReason());
    }

    @Test
    void testNightPhaseWithoutSeer() {
        // 预言家死亡
        Player seer = gameState.getPlayerById(5L);
        seer.setAlive(false);

        // 狼人投票击杀3号
        Map<Long, Long> werewolfVotes = new HashMap<>();
        werewolfVotes.put(1L, 3L);
        werewolfVotes.put(2L, 3L);

        NightPhaseEngine.NightResult result = engine.executeNightPhase(
                gameState,
                werewolfVotes,
                null,  // 预言家已死，无查验
                false,
                null
        );

        // 验证预言家查验结果应该为null
        assertNull(result.getSeerCheckResult());

        // 验证死亡名单
        assertEquals(1, result.getDeaths().size());
        assertTrue(result.getDeaths().contains(3L));
    }

    @Test
    void testNightPhaseWithoutWitch() {
        // 女巫死亡
        Player witch = gameState.getPlayerById(6L);
        witch.setAlive(false);

        // 狼人投票击杀3号
        Map<Long, Long> werewolfVotes = new HashMap<>();
        werewolfVotes.put(1L, 3L);
        werewolfVotes.put(2L, 3L);

        NightPhaseEngine.NightResult result = engine.executeNightPhase(
                gameState,
                werewolfVotes,
                1L,
                false,
                null
        );

        // 验证女巫行动结果应该为null
        assertNull(result.getWitchActionResult());

        // 验证死亡名单
        assertEquals(1, result.getDeaths().size());
        assertTrue(result.getDeaths().contains(3L));
    }

    @Test
    void testGetAliveWerewolves() {
        List<Player> werewolves = engine.getAliveWerewolves(gameState);

        assertEquals(2, werewolves.size());
        assertTrue(werewolves.stream().allMatch(p -> p.getRole() instanceof Werewolf));
        assertTrue(werewolves.stream().allMatch(Player::isAlive));
    }

    @Test
    void testGetAliveWerewolvesAfterOneDies() {
        // 狼人1死亡
        Player werewolf1 = gameState.getPlayerById(1L);
        werewolf1.setAlive(false);

        List<Player> werewolves = engine.getAliveWerewolves(gameState);

        assertEquals(1, werewolves.size());
        assertEquals(2L, werewolves.get(0).getId());
    }

    @Test
    void testNightPhaseEventsRecorded() {
        // 执行完整夜晚流程
        Map<Long, Long> werewolfVotes = new HashMap<>();
        werewolfVotes.put(1L, 3L);
        werewolfVotes.put(2L, 3L);

        // 标记女巫已过首夜
        Player witch = gameState.getPlayerById(6L);
        ((Witch) witch.getRole()).markFirstNightPassed();

        engine.executeNightPhase(
                gameState,
                werewolfVotes,
                1L,
                true,  // 女巫使用解药
                null
        );

        // 验证事件记录
        List<GameEvent> events = gameState.getEvents();
        assertTrue(events.size() > 0);

        // 验证包含狼人击杀事件
        boolean hasKillEvent = events.stream()
                .anyMatch(e -> e.getType() == GameEvent.EventType.WEREWOLF_KILL);
        assertTrue(hasKillEvent);

        // 验证包含预言家查验事件
        boolean hasCheckEvent = events.stream()
                .anyMatch(e -> e.getType() == GameEvent.EventType.SEER_CHECK);
        assertTrue(hasCheckEvent);

        // 验证包含女巫解药事件
        boolean hasAntidoteEvent = events.stream()
                .anyMatch(e -> e.getType() == GameEvent.EventType.WITCH_ANTIDOTE);
        assertTrue(hasAntidoteEvent);

        // 验证包含夜晚结束事件
        boolean hasNightEndEvent = events.stream()
                .anyMatch(e -> e.getType() == GameEvent.EventType.NIGHT_END);
        assertTrue(hasNightEndEvent);
    }

    private GameState createTestGameState() {
        List<Player> players = new ArrayList<>();

        // 1号和2号是狼人
        players.add(Player.builder()
                .id(1L)
                .name("狼人1")
                .role(new Werewolf())
                .alive(true)
                .seatNumber(1)
                .build());

        players.add(Player.builder()
                .id(2L)
                .name("狼人2")
                .role(new Werewolf())
                .alive(true)
                .seatNumber(2)
                .build());

        // 3号和4号是平民
        players.add(Player.builder()
                .id(3L)
                .name("平民1")
                .role(new Villager())
                .alive(true)
                .seatNumber(3)
                .build());

        players.add(Player.builder()
                .id(4L)
                .name("平民2")
                .role(new Villager())
                .alive(true)
                .seatNumber(4)
                .build());

        // 5号是预言家
        players.add(Player.builder()
                .id(5L)
                .name("预言家")
                .role(new Seer())
                .alive(true)
                .seatNumber(5)
                .build());

        // 6号是女巫
        players.add(Player.builder()
                .id(6L)
                .name("女巫")
                .role(new Witch())
                .alive(true)
                .seatNumber(6)
                .build());

        return GameState.builder()
                .gameId(1L)
                .currentRound(1)
                .players(players)
                .events(new ArrayList<>())
                .lastNightDeaths(new ArrayList<>())
                .build();
    }
}
