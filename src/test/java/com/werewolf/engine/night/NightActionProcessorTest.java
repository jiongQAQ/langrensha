package com.werewolf.engine.night;

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
 * 夜晚行动处理器测试
 *
 * @author Claude & User
 */
class NightActionProcessorTest {

    private NightActionProcessor processor;
    private GameState gameState;

    @BeforeEach
    void setUp() {
        processor = new NightActionProcessor();
        gameState = createTestGameState();
    }

    @Test
    void testWerewolfKillSuccess() {
        // 狼人1和2都投票给玩家3
        Map<Long, Long> votes = new HashMap<>();
        votes.put(1L, 3L);
        votes.put(2L, 3L);

        NightActionProcessor.WerewolfKillResult result =
                processor.processWerewolfKill(gameState, votes);

        assertTrue(result.isSuccess());
        assertEquals(3L, result.getTargetPlayerId());
        assertNotNull(result.getTargetPlayerName());
        assertEquals(2, result.getVoteCount().get(3L));
    }

    @Test
    void testWerewolfKillNoVotes() {
        Map<Long, Long> votes = new HashMap<>();

        NightActionProcessor.WerewolfKillResult result =
                processor.processWerewolfKill(gameState, votes);

        assertFalse(result.isSuccess());
        assertNull(result.getTargetPlayerId());
    }

    @Test
    void testWerewolfKillInvalidTarget() {
        // 投票给已死亡玩家
        Player player3 = gameState.getPlayerById(3L);
        player3.setAlive(false);

        Map<Long, Long> votes = new HashMap<>();
        votes.put(1L, 3L);
        votes.put(2L, 3L);

        NightActionProcessor.WerewolfKillResult result =
                processor.processWerewolfKill(gameState, votes);

        assertFalse(result.isSuccess());
    }

    @Test
    void testSeerCheckWerewolf() {
        // 预言家5号查验狼人1号
        NightActionProcessor.SeerCheckResult result =
                processor.processSeerCheck(gameState, 5L, 1L);

        assertTrue(result.isSuccess());
        assertEquals(5L, result.getSeerPlayerId());
        assertEquals(1L, result.getTargetPlayerId());
        assertTrue(result.isWerewolf());
        assertEquals("狼人", result.getResult());
    }

    @Test
    void testSeerCheckVillager() {
        // 预言家5号查验平民3号
        NightActionProcessor.SeerCheckResult result =
                processor.processSeerCheck(gameState, 5L, 3L);

        assertTrue(result.isSuccess());
        assertEquals(5L, result.getSeerPlayerId());
        assertEquals(3L, result.getTargetPlayerId());
        assertFalse(result.isWerewolf());
        assertEquals("好人", result.getResult());
    }

    @Test
    void testSeerCannotCheckSelf() {
        // 预言家不能查验自己
        NightActionProcessor.SeerCheckResult result =
                processor.processSeerCheck(gameState, 5L, 5L);

        assertFalse(result.isSuccess());
        assertEquals("不能查验自己", result.getMessage());
    }

    @Test
    void testWitchUseAntidote() {
        // 女巫6号使用解药救活3号
        NightActionProcessor.WitchActionResult result =
                processor.processWitchAction(gameState, 6L, 3L, true, null);

        assertTrue(result.isSuccess());
        assertTrue(result.isAntidoteUsed());
        assertEquals(3L, result.getSavedPlayerId());
        assertFalse(result.isPoisonUsed());
        assertNull(result.getPoisonedPlayerId());
    }

    @Test
    void testWitchUsePoison() {
        // 女巫6号使用毒药毒死1号
        NightActionProcessor.WitchActionResult result =
                processor.processWitchAction(gameState, 6L, null, false, 1L);

        assertTrue(result.isSuccess());
        assertFalse(result.isAntidoteUsed());
        assertTrue(result.isPoisonUsed());
        assertEquals(1L, result.getPoisonedPlayerId());
    }

    @Test
    void testWitchCannotUseBothPotions() {
        // 女巫不能同时使用解药和毒药
        NightActionProcessor.WitchActionResult result =
                processor.processWitchAction(gameState, 6L, 3L, true, 1L);

        assertFalse(result.isSuccess());
        assertEquals("解药和毒药不能同时使用", result.getMessage());
    }

    @Test
    void testWitchFirstNightCannotSaveSelf() {
        // 女巫首夜不能自救
        Player witch = gameState.getPlayerById(6L);
        Witch witchRole = (Witch) witch.getRole();
        assertTrue(witchRole.isFirstNight());

        NightActionProcessor.WitchActionResult result =
                processor.processWitchAction(gameState, 6L, 6L, true, null);

        assertFalse(result.isSuccess());
        assertEquals("首夜不能自救", result.getMessage());
    }

    @Test
    void testWitchAntidoteUsedTwice() {
        // 女巫解药只能用一次
        Player witch = gameState.getPlayerById(6L);
        Witch witchRole = (Witch) witch.getRole();

        // 第一次使用成功
        NightActionProcessor.WitchActionResult result1 =
                processor.processWitchAction(gameState, 6L, 3L, true, null);
        assertTrue(result1.isSuccess());

        // 第二次使用失败
        NightActionProcessor.WitchActionResult result2 =
                processor.processWitchAction(gameState, 6L, 3L, true, null);
        assertFalse(result2.isSuccess());
        assertEquals("解药已用完", result2.getMessage());
    }

    @Test
    void testCalculateDeathsWerewolfKillOnly() {
        // 只有狼人击杀，女巫不行动
        NightActionProcessor.WerewolfKillResult killResult =
                NightActionProcessor.WerewolfKillResult.builder()
                        .success(true)
                        .targetPlayerId(3L)
                        .build();

        NightActionProcessor.WitchActionResult witchResult =
                NightActionProcessor.WitchActionResult.builder()
                        .success(false)
                        .antidoteUsed(false)
                        .poisonUsed(false)
                        .build();

        List<Long> deaths = processor.calculateDeaths(killResult, witchResult);

        assertEquals(1, deaths.size());
        assertTrue(deaths.contains(3L));
    }

    @Test
    void testCalculateDeathsWitchSaved() {
        // 狼人击杀，女巫救活
        NightActionProcessor.WerewolfKillResult killResult =
                NightActionProcessor.WerewolfKillResult.builder()
                        .success(true)
                        .targetPlayerId(3L)
                        .build();

        NightActionProcessor.WitchActionResult witchResult =
                NightActionProcessor.WitchActionResult.builder()
                        .success(true)
                        .antidoteUsed(true)
                        .savedPlayerId(3L)
                        .poisonUsed(false)
                        .build();

        List<Long> deaths = processor.calculateDeaths(killResult, witchResult);

        assertEquals(0, deaths.size());
    }

    @Test
    void testCalculateDeathsWitchPoisonOnly() {
        // 狼人击杀，女巫救活，同时毒死另一人
        NightActionProcessor.WerewolfKillResult killResult =
                NightActionProcessor.WerewolfKillResult.builder()
                        .success(true)
                        .targetPlayerId(3L)
                        .build();

        NightActionProcessor.WitchActionResult witchResult =
                NightActionProcessor.WitchActionResult.builder()
                        .success(true)
                        .antidoteUsed(false)
                        .savedPlayerId(null)
                        .poisonUsed(true)
                        .poisonedPlayerId(1L)
                        .build();

        List<Long> deaths = processor.calculateDeaths(killResult, witchResult);

        assertEquals(2, deaths.size());
        assertTrue(deaths.contains(3L));
        assertTrue(deaths.contains(1L));
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
