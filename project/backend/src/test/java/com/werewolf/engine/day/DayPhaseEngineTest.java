package com.werewolf.engine.day;

import com.werewolf.domain.model.GameState;
import com.werewolf.domain.model.Phase;
import com.werewolf.domain.model.Player;
import com.werewolf.domain.role.Werewolf;
import com.werewolf.domain.role.Seer;
import com.werewolf.domain.role.Villager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DayPhaseEngine 单元测试
 */
@DisplayName("白天阶段引擎测试")
class DayPhaseEngineTest {

    private DayPhaseEngine dayEngine;
    private GameState gameState;
    private List<Player> players;

    @BeforeEach
    void setUp() {
        dayEngine = new DayPhaseEngine();

        // 创建测试玩家
        players = new ArrayList<>();
        players.add(createPlayer(1L, "狼人1", new Werewolf(), 1, true));
        players.add(createPlayer(2L, "狼人2", new Werewolf(), 2, true));
        players.add(createPlayer(3L, "预言家", new Seer(), 3, true));
        players.add(createPlayer(4L, "平民1", new Villager(), 4, true));
        players.add(createPlayer(5L, "平民2", new Villager(), 5, true));
        players.add(createPlayer(6L, "平民3", new Villager(), 6, true));

        // 创建游戏状态
        gameState = GameState.builder()
                .gameId(1L)
                .roomId(1L)
                .currentPhase(Phase.DAY)
                .currentRound(1)
                .players(players)
                .events(new ArrayList<>())
                .build();
    }

    private Player createPlayer(Long id, String name, com.werewolf.domain.role.Role role,
                               int seatNumber, boolean alive) {
        return Player.builder()
                .id(id)
                .name(name)
                .role(role)
                .seatNumber(seatNumber)
                .alive(alive)
                .hasSpoken(false)
                .hasVoted(false)
                .type(Player.PlayerType.AI)
                .build();
    }

    @Test
    @DisplayName("完整白天流程 - 正常放逐")
    void testFullDayPhase_NormalExile() {
        // 昨晚死亡：平民1
        List<Long> lastNightDeaths = Arrays.asList(4L);
        Player deadPlayer = gameState.getPlayerById(4L);
        deadPlayer.markDead(Player.DeathReason.KILLED_BY_WEREWOLF);

        // 遗言
        Map<Long, String> lastWords = new HashMap<>();
        lastWords.put(4L, "我是好人，请帮我报仇");

        // 发言
        Map<Long, String> speeches = new HashMap<>();
        speeches.put(1L, "我觉得3号很可疑");
        speeches.put(2L, "我同意1号的看法");
        speeches.put(3L, "我昨晚查验了1号，他是狼人");
        speeches.put(5L, "我相信预言家");
        speeches.put(6L, "我也投1号");

        // 投票：1号狼人获得3票
        Map<Long, Long> votes = new HashMap<>();
        votes.put(2L, 3L);  // 狼人2投预言家
        votes.put(3L, 1L);  // 预言家投狼人1
        votes.put(5L, 1L);  // 平民2投狼人1
        votes.put(6L, 1L);  // 平民3投狼人1

        // 执行白天流程
        DayPhaseEngine.DayPhaseResult result = dayEngine.executeDayPhase(
                gameState, lastNightDeaths, lastWords, speeches, votes);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getLastNightDeaths().size());
        assertTrue(result.getLastNightDeaths().contains(4L));

        // 验证遗言记录
        assertNotNull(result.getLastWordsRecords());
        assertTrue(result.getLastWordsRecords().containsKey(4L));

        // 验证发言记录
        assertNotNull(result.getSpeechRecords());
        assertEquals(5, result.getSpeechRecords().size());

        // 验证投票结果
        assertNotNull(result.getVoteResult());
        assertFalse(result.getVoteResult().isTie());
        assertEquals(1L, result.getExiledPlayerId());

        // 验证玩家状态
        Player exiledPlayer = gameState.getPlayerById(1L);
        assertFalse(exiledPlayer.isAlive());
        assertEquals(Player.DeathReason.VOTED_OUT, exiledPlayer.getDeathReason());

        // 验证事件记录
        assertFalse(gameState.getEvents().isEmpty());
    }

    @Test
    @DisplayName("白天流程 - 平票无人出局")
    void testDayPhase_TieVote() {
        List<Long> lastNightDeaths = new ArrayList<>();
        Map<Long, String> speeches = createBasicSpeeches();

        // 平票：每个玩家得1票
        Map<Long, Long> votes = new HashMap<>();
        votes.put(1L, 2L);
        votes.put(2L, 3L);
        votes.put(3L, 4L);
        votes.put(4L, 5L);
        votes.put(5L, 6L);
        votes.put(6L, 1L);

        DayPhaseEngine.DayPhaseResult result = dayEngine.executeDayPhase(
                gameState, lastNightDeaths, new HashMap<>(), speeches, votes);

        // 验证平票结果
        assertTrue(result.getVoteResult().isTie());
        assertNull(result.getExiledPlayerId());

        // 验证所有玩家仍然存活
        assertTrue(players.stream().allMatch(Player::isAlive));
    }

    @Test
    @DisplayName("白天流程 - 平安夜无人死亡")
    void testDayPhase_PeacefulNight() {
        List<Long> lastNightDeaths = new ArrayList<>();  // 空列表表示平安夜
        Map<Long, String> speeches = createBasicSpeeches();
        Map<Long, Long> votes = createBasicVotes();

        DayPhaseEngine.DayPhaseResult result = dayEngine.executeDayPhase(
                gameState, lastNightDeaths, new HashMap<>(), speeches, votes);

        assertNotNull(result);
        assertTrue(result.getLastNightDeaths().isEmpty());

        // 验证有"平安夜"的事件记录
        long peacefulNightEvents = gameState.getEvents().stream()
                .filter(e -> e.getDescription().contains("平安夜"))
                .count();
        assertTrue(peacefulNightEvents > 0);
    }

    @Test
    @DisplayName("白天流程 - 多人死亡的遗言环节")
    void testDayPhase_MultipleDeaths() {
        // 昨晚多人死亡：平民1、平民2
        List<Long> lastNightDeaths = Arrays.asList(4L, 5L);
        players.get(3).markDead(Player.DeathReason.KILLED_BY_WEREWOLF);
        players.get(4).markDead(Player.DeathReason.POISONED);

        Map<Long, String> lastWords = new HashMap<>();
        lastWords.put(4L, "我是好人");
        lastWords.put(5L, "我也是好人");

        Map<Long, String> speeches = new HashMap<>();
        speeches.put(1L, "RIP");
        speeches.put(2L, "RIP");
        speeches.put(3L, "RIP");
        speeches.put(6L, "RIP");

        Map<Long, Long> votes = new HashMap<>();
        votes.put(1L, 3L);
        votes.put(2L, 3L);
        votes.put(3L, 1L);
        votes.put(6L, 1L);

        DayPhaseEngine.DayPhaseResult result = dayEngine.executeDayPhase(
                gameState, lastNightDeaths, lastWords, speeches, votes);

        // 验证遗言记录
        assertEquals(2, result.getLastWordsRecords().size());
        assertTrue(result.getLastWordsRecords().containsKey(4L));
        assertTrue(result.getLastWordsRecords().containsKey(5L));

        // 验证死亡名单
        assertEquals(2, result.getLastNightDeaths().size());
    }

    @Test
    @DisplayName("白天流程 - 部分玩家沉默")
    void testDayPhase_SilentPlayers() {
        List<Long> lastNightDeaths = new ArrayList<>();

        // 只有部分玩家发言
        Map<Long, String> speeches = new HashMap<>();
        speeches.put(1L, "我是好人");
        // 2号沉默
        speeches.put(3L, "我是预言家");
        // 4号、5号、6号沉默

        Map<Long, Long> votes = createBasicVotes();

        DayPhaseEngine.DayPhaseResult result = dayEngine.executeDayPhase(
                gameState, lastNightDeaths, new HashMap<>(), speeches, votes);

        // 验证发言记录包含所有玩家（沉默的会被标记为[沉默]）
        assertEquals(6, result.getSpeechRecords().size());
    }

    @Test
    @DisplayName("白天流程 - 有玩家弃票")
    void testDayPhase_AbstainVote() {
        List<Long> lastNightDeaths = new ArrayList<>();
        Map<Long, String> speeches = createBasicSpeeches();

        Map<Long, Long> votes = new HashMap<>();
        votes.put(1L, 2L);
        votes.put(2L, null);  // 弃票
        votes.put(3L, 2L);
        votes.put(4L, null);  // 弃票
        votes.put(5L, 2L);
        votes.put(6L, 2L);

        DayPhaseEngine.DayPhaseResult result = dayEngine.executeDayPhase(
                gameState, lastNightDeaths, new HashMap<>(), speeches, votes);

        // 2号应该被放逐（获得4票）
        assertEquals(2L, result.getExiledPlayerId());
        assertFalse(gameState.getPlayerById(2L).isAlive());
    }

    @Test
    @DisplayName("白天流程 - 验证玩家状态更新")
    void testDayPhase_PlayerStatusUpdate() {
        List<Long> lastNightDeaths = new ArrayList<>();
        Map<Long, String> speeches = createBasicSpeeches();
        Map<Long, Long> votes = createBasicVotes();

        DayPhaseEngine.DayPhaseResult result = dayEngine.executeDayPhase(
                gameState, lastNightDeaths, new HashMap<>(), speeches, votes);

        // 验证所有玩家的发言状态
        for (Player player : players) {
            if (player.isAlive()) {
                assertTrue(player.isHasSpoken());
                assertTrue(player.isHasVoted());
            }
        }
    }

    private Map<Long, String> createBasicSpeeches() {
        Map<Long, String> speeches = new HashMap<>();
        for (Player player : players) {
            if (player.isAlive()) {
                speeches.put(player.getId(), "发言-" + player.getName());
            }
        }
        return speeches;
    }

    private Map<Long, Long> createBasicVotes() {
        Map<Long, Long> votes = new HashMap<>();
        // 所有人投1号
        for (Player player : players) {
            if (player.isAlive() && !player.getId().equals(1L)) {
                votes.put(player.getId(), 1L);
            }
        }
        return votes;
    }
}
