package com.werewolf.engine;

import com.werewolf.domain.model.GameState;
import com.werewolf.domain.model.Phase;
import com.werewolf.domain.model.Player;
import com.werewolf.domain.role.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * GameController 单元测试
 */
@DisplayName("游戏主控制器测试")
class GameControllerTest {

    private GameController gameController;
    private GameState gameState;
    private List<Player> players;

    @BeforeEach
    void setUp() {
        gameController = new GameController();

        // 创建6个玩家
        players = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            players.add(createPlayer((long) i, "玩家" + i, i));
        }

        // 创建游戏状态
        gameState = GameState.builder()
                .gameId(1L)
                .roomId(1L)
                .currentPhase(Phase.WAITING)
                .currentRound(0)
                .players(players)
                .events(new ArrayList<>())
                .status(GameState.GameStatus.WAITING)
                .lastNightDeaths(new ArrayList<>())
                .build();
    }

    private Player createPlayer(Long id, String name, int seatNumber) {
        return Player.builder()
                .id(id)
                .name(name)
                .seatNumber(seatNumber)
                .alive(true)
                .hasSpoken(false)
                .hasVoted(false)
                .type(Player.PlayerType.AI)
                .build();
    }

    @Test
    @DisplayName("初始化游戏 - 正常情况")
    void testInitializeGame_Success() {
        gameController.initializeGame(gameState);

        // 验证游戏状态
        assertEquals(GameState.GameStatus.RUNNING, gameState.getStatus());
        assertEquals(Phase.NIGHT, gameState.getCurrentPhase());
        assertEquals(1, gameState.getCurrentRound());
        assertNotNull(gameState.getStartTime());

        // 验证所有玩家都有角色
        for (Player player : players) {
            assertNotNull(player.getRole(), "玩家应该被分配角色");
        }

        // 验证事件记录
        assertFalse(gameState.getEvents().isEmpty());
    }

    @Test
    @DisplayName("初始化游戏 - 验证角色分配")
    void testInitializeGame_RoleDistribution() {
        gameController.initializeGame(gameState);

        // 统计各角色数量
        long werewolfCount = players.stream()
                .filter(p -> p.getRole().getRoleType() == RoleType.WEREWOLF)
                .count();
        long seerCount = players.stream()
                .filter(p -> p.getRole().getRoleType() == RoleType.SEER)
                .count();
        long witchCount = players.stream()
                .filter(p -> p.getRole().getRoleType() == RoleType.WITCH)
                .count();
        long villagerCount = players.stream()
                .filter(p -> p.getRole().getRoleType() == RoleType.VILLAGER)
                .count();

        // 验证角色数量符合6人局配置：2狼2民1预1女
        assertEquals(2, werewolfCount, "应该有2个狼人");
        assertEquals(1, seerCount, "应该有1个预言家");
        assertEquals(1, witchCount, "应该有1个女巫");
        assertEquals(2, villagerCount, "应该有2个平民");
    }

    @Test
    @DisplayName("初始化游戏 - 空游戏状态抛出异常")
    void testInitializeGame_NullGameState() {
        assertThrows(IllegalArgumentException.class, () -> {
            gameController.initializeGame(null);
        });
    }

    @Test
    @DisplayName("初始化游戏 - 错误的玩家数量抛出异常")
    void testInitializeGame_WrongPlayerCount() {
        // 只有4个玩家
        List<Player> wrongPlayers = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            wrongPlayers.add(createPlayer((long) i, "玩家" + i, i));
        }
        gameState.setPlayers(wrongPlayers);

        assertThrows(IllegalArgumentException.class, () -> {
            gameController.initializeGame(gameState);
        });
    }

    @Test
    @DisplayName("执行回合 - 正常执行一个完整回合")
    void testExecuteRound_CompleteRound() {
        // 先初始化游戏
        gameController.initializeGame(gameState);

        // 创建回合行动数据
        GameController.RoundActions actions = createBasicRoundActions();

        // 执行回合
        GameController.RoundResult result = gameController.executeRound(gameState, actions);

        // 验证回合结果
        assertNotNull(result);
        assertEquals(1, result.getRound());
        assertNotNull(result.getNightResult());
        assertNotNull(result.getDayResult());

        // 如果游戏未结束，验证回合递增
        if (!result.isGameEnded()) {
            assertEquals(2, gameState.getCurrentRound());
        }
    }

    @Test
    @DisplayName("执行回合 - 好人胜利场景")
    void testExecuteRound_GoodWins() {
        // 初始化游戏
        gameController.initializeGame(gameState);

        // 找到所有狼人并标记为死亡
        players.stream()
                .filter(p -> p.getRole().getRoleType() == RoleType.WEREWOLF)
                .forEach(p -> p.markDead(Player.DeathReason.VOTED_OUT));

        // 创建回合行动数据
        GameController.RoundActions actions = createBasicRoundActions();

        // 执行回合（狼人已全死，应该触发好人胜利）
        GameController.RoundResult result = gameController.executeRound(gameState, actions);

        // 验证游戏结束且好人获胜
        assertTrue(result.isGameEnded());
        assertNotNull(result.getWinResult());
        assertTrue(result.getWinResult().isGoodWin());
        assertEquals(GameState.GameStatus.FINISHED, gameState.getStatus());
    }

    @Test
    @DisplayName("执行回合 - 狼人胜利场景（所有神职死亡）")
    void testExecuteRound_WerewolfWins_AllGodsDead() {
        // 初始化游戏
        gameController.initializeGame(gameState);

        // 找到所有神职并标记为死亡
        players.stream()
                .filter(p -> p.getRole().getRoleType() == RoleType.SEER ||
                             p.getRole().getRoleType() == RoleType.WITCH)
                .forEach(p -> p.markDead(Player.DeathReason.KILLED_BY_WEREWOLF));

        // 创建回合行动数据
        GameController.RoundActions actions = createBasicRoundActions();

        // 执行回合
        GameController.RoundResult result = gameController.executeRound(gameState, actions);

        // 验证游戏结束且狼人获胜
        assertTrue(result.isGameEnded());
        assertNotNull(result.getWinResult());
        assertTrue(result.getWinResult().isWerewolfWin());
    }

    @Test
    @DisplayName("执行回合 - 狼人胜利场景（所有平民死亡）")
    void testExecuteRound_WerewolfWins_AllVillagersDead() {
        // 初始化游戏
        gameController.initializeGame(gameState);

        // 找到所有平民并标记为死亡
        players.stream()
                .filter(p -> p.getRole().getRoleType() == RoleType.VILLAGER)
                .forEach(p -> p.markDead(Player.DeathReason.KILLED_BY_WEREWOLF));

        // 创建回合行动数据
        GameController.RoundActions actions = createBasicRoundActions();

        // 执行回合
        GameController.RoundResult result = gameController.executeRound(gameState, actions);

        // 验证游戏结束且狼人获胜
        assertTrue(result.isGameEnded());
        assertNotNull(result.getWinResult());
        assertTrue(result.getWinResult().isWerewolfWin());
    }

    @Test
    @DisplayName("执行回合 - 游戏已结束抛出异常")
    void testExecuteRound_GameAlreadyFinished() {
        gameController.initializeGame(gameState);
        gameState.finishGame(Camp.GOOD.name());

        GameController.RoundActions actions = createBasicRoundActions();

        assertThrows(IllegalStateException.class, () -> {
            gameController.executeRound(gameState, actions);
        });
    }

    @Test
    @DisplayName("完整游戏流程测试 - 从初始化到结束")
    void testFullGameFlow() {
        // 1. 初始化游戏
        gameController.initializeGame(gameState);
        assertEquals(GameState.GameStatus.RUNNING, gameState.getStatus());
        assertEquals(1, gameState.getCurrentRound());

        // 2. 执行第一回合
        GameController.RoundActions round1Actions = createBasicRoundActions();
        GameController.RoundResult round1Result = gameController.executeRound(gameState, round1Actions);

        assertNotNull(round1Result);
        assertEquals(1, round1Result.getRound());

        // 如果游戏未结束，继续执行
        if (!round1Result.isGameEnded()) {
            assertEquals(2, gameState.getCurrentRound());

            // 3. 执行第二回合
            GameController.RoundActions round2Actions = createBasicRoundActions();
            GameController.RoundResult round2Result = gameController.executeRound(gameState, round2Actions);

            assertNotNull(round2Result);
            assertEquals(2, round2Result.getRound());
        }

        // 验证游戏事件记录
        assertFalse(gameState.getEvents().isEmpty());
    }

    @Test
    @DisplayName("多回合执行直到游戏结束")
    void testMultipleRounds_UntilGameEnd() {
        gameController.initializeGame(gameState);

        int maxRounds = 10;  // 最多执行10回合
        int roundCount = 0;

        while (roundCount < maxRounds && !gameState.isFinished()) {
            GameController.RoundActions actions = createBasicRoundActions();

            // 为了加速游戏结束，每回合随机杀一个玩家
            List<Player> alivePlayers = gameState.getAlivePlayers();
            if (!alivePlayers.isEmpty()) {
                Player victim = alivePlayers.get(0);
                victim.markDead(Player.DeathReason.KILLED_BY_WEREWOLF);
            }

            GameController.RoundResult result = gameController.executeRound(gameState, actions);

            if (result.isGameEnded()) {
                // 验证游戏正确结束
                assertTrue(gameState.isFinished());
                assertNotNull(result.getWinResult());
                assertNotNull(gameState.getWinningCamp());
                break;
            }

            roundCount++;
        }

        // 验证游戏最终结束
        assertTrue(roundCount < maxRounds, "游戏应该在10回合内结束");
    }

    @Test
    @DisplayName("阶段切换测试")
    void testPhaseTransitions() {
        gameController.initializeGame(gameState);

        // 初始化后应该是夜晚
        assertEquals(Phase.NIGHT, gameState.getCurrentPhase());

        // 执行回合
        GameController.RoundActions actions = createBasicRoundActions();
        gameController.executeRound(gameState, actions);

        // 回合执行后，如果游戏未结束，阶段应该回到夜晚（准备下一回合）
        if (!gameState.isFinished()) {
            // 下一回合开始前是夜晚
            assertTrue(gameState.getCurrentPhase() == Phase.DAY ||
                       gameState.getCurrentPhase() == Phase.NIGHT);
        }
    }

    /**
     * 创建基础的回合行动数据
     */
    private GameController.RoundActions createBasicRoundActions() {
        // 找到各角色
        Player werewolf1 = findPlayerByRole(RoleType.WEREWOLF, 0);
        Player werewolf2 = findPlayerByRole(RoleType.WEREWOLF, 1);
        Player seer = findPlayerByRole(RoleType.SEER, 0);
        Player villager1 = findPlayerByRole(RoleType.VILLAGER, 0);

        // 创建狼人投票（如果狼人存活）
        Map<Long, Long> werewolfVotes = new HashMap<>();
        if (werewolf1 != null && werewolf1.isAlive() && villager1 != null) {
            werewolfVotes.put(werewolf1.getId(), villager1.getId());
        }
        if (werewolf2 != null && werewolf2.isAlive() && villager1 != null) {
            werewolfVotes.put(werewolf2.getId(), villager1.getId());
        }

        // 预言家查验目标（如果存活）
        Long seerTarget = null;
        if (seer != null && seer.isAlive() && werewolf1 != null) {
            seerTarget = werewolf1.getId();
        }

        // 创建投票（所有存活玩家投给第一个狼人）
        Map<Long, Long> votes = new HashMap<>();
        List<Player> alivePlayers = gameState.getAlivePlayers();
        if (!alivePlayers.isEmpty() && werewolf1 != null && werewolf1.isAlive()) {
            for (Player player : alivePlayers) {
                if (!player.getId().equals(werewolf1.getId())) {
                    votes.put(player.getId(), werewolf1.getId());
                }
            }
        }

        // 创建发言
        Map<Long, String> speeches = new HashMap<>();
        for (Player player : alivePlayers) {
            speeches.put(player.getId(), "我的发言-" + player.getName());
        }

        return GameController.RoundActions.builder()
                .werewolfVotes(werewolfVotes)
                .seerCheckTarget(seerTarget)
                .witchUseAntidote(false)
                .witchPoisonTarget(null)
                .lastWords(new HashMap<>())
                .speeches(speeches)
                .votes(votes)
                .build();
    }

    /**
     * 根据角色类型查找玩家
     *
     * @param roleType 角色类型
     * @param index 索引（如果有多个相同角色）
     * @return 玩家对象
     */
    private Player findPlayerByRole(RoleType roleType, int index) {
        List<Player> matchingPlayers = players.stream()
                .filter(p -> p.getRole() != null && p.getRole().getRoleType() == roleType)
                .toList();

        if (matchingPlayers.size() > index) {
            return matchingPlayers.get(index);
        }
        return null;
    }
}
