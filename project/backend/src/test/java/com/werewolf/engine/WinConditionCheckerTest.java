package com.werewolf.engine;

import com.werewolf.domain.model.GameState;
import com.werewolf.domain.model.Phase;
import com.werewolf.domain.model.Player;
import com.werewolf.domain.role.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * WinConditionChecker 单元测试
 */
@DisplayName("胜利条件检查器测试")
class WinConditionCheckerTest {

    private WinConditionChecker checker;
    private GameState gameState;
    private List<Player> players;

    @BeforeEach
    void setUp() {
        checker = new WinConditionChecker();
        players = new ArrayList<>();

        // 创建标准6人局：2狼2民1预1女
        players.add(createPlayer(1L, "狼人1", new Werewolf(), true));
        players.add(createPlayer(2L, "狼人2", new Werewolf(), true));
        players.add(createPlayer(3L, "预言家", new Seer(), true));
        players.add(createPlayer(4L, "女巫", new Witch(), true));
        players.add(createPlayer(5L, "平民1", new Villager(), true));
        players.add(createPlayer(6L, "平民2", new Villager(), true));

        gameState = GameState.builder()
                .gameId(1L)
                .currentPhase(Phase.DAY)
                .currentRound(1)
                .players(players)
                .build();
    }

    private Player createPlayer(Long id, String name, Role role, boolean alive) {
        Player player = Player.builder()
                .id(id)
                .name(name)
                .role(role)
                .alive(alive)
                .build();
        return player;
    }

    @Test
    @DisplayName("游戏进行中 - 所有玩家存活")
    void testGameInProgress_AllAlive() {
        WinConditionChecker.WinResult result = checker.checkWinCondition(gameState);

        assertNotNull(result);
        assertFalse(result.isGameEnded());
        assertNull(result.getWinningCamp());
        assertEquals("游戏进行中", result.getReason());
        assertEquals(2, result.getAliveWerewolves());
        assertEquals(1, result.getAliveSeers());
        assertEquals(1, result.getAliveWitches());
        assertEquals(2, result.getAliveVillagers());
        assertEquals(6, result.getTotalAlivePlayers());
    }

    @Test
    @DisplayName("好人胜利 - 所有狼人死亡")
    void testGoodWin_AllWerewolvesDead() {
        // 狼人全部死亡
        players.get(0).markDead(Player.DeathReason.VOTED_OUT);  // 狼人1
        players.get(1).markDead(Player.DeathReason.POISONED);   // 狼人2

        WinConditionChecker.WinResult result = checker.checkWinCondition(gameState);

        assertNotNull(result);
        assertTrue(result.isGameEnded());
        assertTrue(result.isGoodWin());
        assertFalse(result.isWerewolfWin());
        assertEquals(Camp.GOOD, result.getWinningCamp());
        assertEquals("所有狼人已被消灭", result.getReason());
        assertEquals(0, result.getAliveWerewolves());
        assertEquals(4, result.getTotalAlivePlayers());
    }

    @Test
    @DisplayName("狼人胜利 - 所有神职死亡")
    void testWerewolfWin_AllGodRolesDead() {
        // 预言家和女巫死亡
        players.get(2).markDead(Player.DeathReason.KILLED_BY_WEREWOLF);  // 预言家
        players.get(3).markDead(Player.DeathReason.KILLED_BY_WEREWOLF);  // 女巫

        WinConditionChecker.WinResult result = checker.checkWinCondition(gameState);

        assertNotNull(result);
        assertTrue(result.isGameEnded());
        assertFalse(result.isGoodWin());
        assertTrue(result.isWerewolfWin());
        assertEquals(Camp.WEREWOLF, result.getWinningCamp());
        assertEquals("所有神职（预言家、女巫）已死亡", result.getReason());
        assertEquals(0, result.getAliveSeers());
        assertEquals(0, result.getAliveWitches());
        assertEquals(2, result.getAliveWerewolves());
        assertEquals(2, result.getAliveVillagers());
    }

    @Test
    @DisplayName("狼人胜利 - 所有平民死亡")
    void testWerewolfWin_AllVillagersDead() {
        // 平民全部死亡
        players.get(4).markDead(Player.DeathReason.KILLED_BY_WEREWOLF);  // 平民1
        players.get(5).markDead(Player.DeathReason.KILLED_BY_WEREWOLF);  // 平民2

        WinConditionChecker.WinResult result = checker.checkWinCondition(gameState);

        assertNotNull(result);
        assertTrue(result.isGameEnded());
        assertTrue(result.isWerewolfWin());
        assertEquals(Camp.WEREWOLF, result.getWinningCamp());
        assertEquals("所有平民已死亡", result.getReason());
        assertEquals(0, result.getAliveVillagers());
        assertEquals(2, result.getAliveWerewolves());
        assertEquals(1, result.getAliveSeers());
        assertEquals(1, result.getAliveWitches());
    }

    @Test
    @DisplayName("边界条件 - 只剩1狼1神（所有平民死亡）")
    void testBoundary_OneWerewolfOneGod() {
        // 只剩狼人1和预言家，所有平民死亡
        players.get(1).markDead(Player.DeathReason.VOTED_OUT);          // 狼人2
        players.get(3).markDead(Player.DeathReason.KILLED_BY_WEREWOLF); // 女巫
        players.get(4).markDead(Player.DeathReason.KILLED_BY_WEREWOLF); // 平民1
        players.get(5).markDead(Player.DeathReason.KILLED_BY_WEREWOLF); // 平民2

        WinConditionChecker.WinResult result = checker.checkWinCondition(gameState);

        assertNotNull(result);
        assertTrue(result.isGameEnded());  // 所有平民死亡，游戏结束
        assertTrue(result.isWerewolfWin());  // 狼人胜利
        assertEquals("所有平民已死亡", result.getReason());
        assertEquals(1, result.getAliveWerewolves());
        assertEquals(1, result.getAliveSeers());
        assertEquals(0, result.getAliveVillagers());
    }

    @Test
    @DisplayName("边界条件 - 只剩1狼2平民")
    void testBoundary_OneWerewolfTwoVillagers() {
        // 只剩狼人1和2个平民
        players.get(1).markDead(Player.DeathReason.VOTED_OUT);          // 狼人2
        players.get(2).markDead(Player.DeathReason.KILLED_BY_WEREWOLF); // 预言家
        players.get(3).markDead(Player.DeathReason.KILLED_BY_WEREWOLF); // 女巫

        WinConditionChecker.WinResult result = checker.checkWinCondition(gameState);

        assertNotNull(result);
        assertTrue(result.isGameEnded());
        assertTrue(result.isWerewolfWin());  // 神职全死，狼人胜利
        assertEquals("所有神职（预言家、女巫）已死亡", result.getReason());
    }

    @Test
    @DisplayName("边界条件 - 最后一轮同归于尽")
    void testBoundary_MutualDestruction() {
        // 假设最后只剩预言家和狼人1，狼人被放逐
        players.get(1).markDead(Player.DeathReason.VOTED_OUT);          // 狼人2
        players.get(3).markDead(Player.DeathReason.KILLED_BY_WEREWOLF); // 女巫
        players.get(4).markDead(Player.DeathReason.KILLED_BY_WEREWOLF); // 平民1
        players.get(5).markDead(Player.DeathReason.KILLED_BY_WEREWOLF); // 平民2
        players.get(0).markDead(Player.DeathReason.VOTED_OUT);          // 狼人1被放逐

        WinConditionChecker.WinResult result = checker.checkWinCondition(gameState);

        assertNotNull(result);
        assertTrue(result.isGameEnded());
        assertTrue(result.isGoodWin());  // 狼人全灭，好人胜利
        assertEquals(0, result.getAliveWerewolves());
        assertEquals(1, result.getTotalAlivePlayers());
    }

    @Test
    @DisplayName("异常情况 - 所有玩家死亡")
    void testAbnormal_AllPlayersDead() {
        // 所有玩家都死亡（异常情况）
        for (Player player : players) {
            player.markDead(Player.DeathReason.UNKNOWN);
        }

        WinConditionChecker.WinResult result = checker.checkWinCondition(gameState);

        assertNotNull(result);
        assertTrue(result.isGameEnded());
        assertNull(result.getWinningCamp());
        assertEquals("所有玩家死亡，游戏异常结束", result.getReason());
        assertEquals(0, result.getTotalAlivePlayers());
    }

    @Test
    @DisplayName("异常情况 - 游戏状态为null")
    void testAbnormal_NullGameState() {
        WinConditionChecker.WinResult result = checker.checkWinCondition(null);

        assertNull(result);
    }

    @Test
    @DisplayName("异常情况 - 玩家列表为null")
    void testAbnormal_NullPlayerList() {
        gameState.setPlayers(null);

        WinConditionChecker.WinResult result = checker.checkWinCondition(gameState);

        assertNull(result);
    }

    @Test
    @DisplayName("多种死亡原因 - 确保统计正确")
    void testMultipleDeathReasons() {
        // 不同死亡原因
        players.get(0).markDead(Player.DeathReason.VOTED_OUT);          // 狼人1被放逐
        players.get(1).markDead(Player.DeathReason.POISONED);           // 狼人2被毒死

        WinConditionChecker.WinResult result = checker.checkWinCondition(gameState);

        assertTrue(result.isGameEnded());
        assertTrue(result.isGoodWin());
        assertEquals(0, result.getAliveWerewolves());
    }

    @Test
    @DisplayName("连续检查 - 游戏状态变化")
    void testSequentialChecks() {
        // 第一次检查：游戏进行中
        WinConditionChecker.WinResult result1 = checker.checkWinCondition(gameState);
        assertFalse(result1.isGameEnded());

        // 狼人1死亡
        players.get(0).markDead(Player.DeathReason.VOTED_OUT);
        WinConditionChecker.WinResult result2 = checker.checkWinCondition(gameState);
        assertFalse(result2.isGameEnded());  // 还有1个狼人

        // 狼人2也死亡
        players.get(1).markDead(Player.DeathReason.VOTED_OUT);
        WinConditionChecker.WinResult result3 = checker.checkWinCondition(gameState);
        assertTrue(result3.isGameEnded());   // 狼人全灭
        assertTrue(result3.isGoodWin());
    }

    @Test
    @DisplayName("特殊场景 - 只剩神职和狼人")
    void testSpecial_OnlyGodsAndWerewolves() {
        // 所有平民死亡
        players.get(4).markDead(Player.DeathReason.KILLED_BY_WEREWOLF);
        players.get(5).markDead(Player.DeathReason.KILLED_BY_WEREWOLF);

        WinConditionChecker.WinResult result = checker.checkWinCondition(gameState);

        assertTrue(result.isGameEnded());
        assertTrue(result.isWerewolfWin());
        assertEquals("所有平民已死亡", result.getReason());
        assertEquals(0, result.getAliveVillagers());
        assertEquals(2, result.getAliveWerewolves());
        assertEquals(2, result.getAliveSeers() + result.getAliveWitches());
    }
}
