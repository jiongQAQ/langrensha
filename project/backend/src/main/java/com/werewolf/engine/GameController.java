package com.werewolf.engine;

import com.werewolf.domain.model.GameEvent;
import com.werewolf.domain.model.GameState;
import com.werewolf.domain.model.Phase;
import com.werewolf.domain.model.Player;
import com.werewolf.domain.role.*;
import com.werewolf.engine.day.DayPhaseEngine;
import com.werewolf.engine.night.NightPhaseEngine;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 游戏主控制器
 * 负责游戏的初始化、阶段切换、流程控制和胜利判定
 *
 * @author Claude & User
 */
@Slf4j
public class GameController {

    private final NightPhaseEngine nightPhaseEngine;
    private final DayPhaseEngine dayPhaseEngine;
    private final WinConditionChecker winConditionChecker;

    public GameController() {
        this.nightPhaseEngine = new NightPhaseEngine();
        this.dayPhaseEngine = new DayPhaseEngine();
        this.winConditionChecker = new WinConditionChecker();
    }

    /**
     * 初始化游戏（角色分配）
     * 按照标准6人局配置：2狼2民1预1女
     *
     * @param gameState 游戏状态
     */
    public void initializeGame(GameState gameState) {
        if (gameState == null) {
            throw new IllegalArgumentException("游戏状态不能为空");
        }

        List<Player> players = gameState.getPlayers();
        if (players == null || players.size() != 6) {
            throw new IllegalArgumentException("玩家数量必须是6人");
        }

        log.info("=== 开始初始化游戏 ===");
        log.info("游戏ID: {}, 房间ID: {}", gameState.getGameId(), gameState.getRoomId());

        // 创建角色列表：2狼2民1预1女
        List<Role> roles = new ArrayList<>();
        roles.add(new Werewolf());  // 狼人1
        roles.add(new Werewolf());  // 狼人2
        roles.add(new Seer());      // 预言家
        roles.add(new Witch());     // 女巫
        roles.add(new Villager());  // 平民1
        roles.add(new Villager());  // 平民2

        // 打乱角色顺序
        Collections.shuffle(roles);

        // 分配角色给玩家
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            Role role = roles.get(i);
            player.setRole(role);
            log.info("玩家[{}] 分配角色: {}", player.getName(), role.getRoleType());
        }

        // 设置游戏初始状态
        gameState.setStatus(GameState.GameStatus.RUNNING);
        gameState.setCurrentPhase(Phase.NIGHT);
        gameState.setCurrentRound(1);
        gameState.setStartTime(LocalDateTime.now());

        // 记录游戏开始事件
        GameEvent startEvent = GameEvent.builder()
                .type(GameEvent.EventType.GAME_START)
                .isPublic(true)
                .description("游戏开始！首夜降临，请各位玩家准备行动。")
                .timestamp(LocalDateTime.now())
                .build();
        gameState.addEvent(startEvent);

        log.info("游戏初始化完成，当前阶段: {}, 回合: {}", gameState.getCurrentPhase(), gameState.getCurrentRound());
    }

    /**
     * 执行完整的游戏回合（夜晚 + 白天）
     *
     * @param gameState 游戏状态
     * @param roundActions 回合行动数据
     * @return 回合结果
     */
    public RoundResult executeRound(GameState gameState, RoundActions roundActions) {
        if (gameState == null) {
            throw new IllegalArgumentException("游戏状态不能为空");
        }

        if (gameState.isFinished()) {
            throw new IllegalStateException("游戏已结束");
        }

        log.info("=== 开始执行回合 {} ===", gameState.getCurrentRound());

        RoundResult result = new RoundResult();
        result.setRound(gameState.getCurrentRound());

        // 阶段1: 执行夜晚流程
        log.info("--- 执行夜晚阶段 ---");
        gameState.changePhase(Phase.NIGHT);
        NightPhaseEngine.NightResult nightResult = executeNightPhase(gameState, roundActions);
        result.setNightResult(nightResult);

        // 检查夜晚后的胜利条件
        WinConditionChecker.WinResult winCheck1 = winConditionChecker.checkWinCondition(gameState);
        if (winCheck1 != null && winCheck1.isGameEnded()) {
            log.info("游戏在夜晚阶段后结束: {}", winCheck1.getReason());
            endGame(gameState, winCheck1);
            result.setWinResult(winCheck1);
            result.setGameEnded(true);
            return result;
        }

        // 阶段2: 执行白天流程
        log.info("--- 执行白天阶段 ---");
        gameState.changePhase(Phase.DAY);
        DayPhaseEngine.DayPhaseResult dayResult = executeDayPhase(gameState, nightResult, roundActions);
        result.setDayResult(dayResult);

        // 检查白天后的胜利条件
        WinConditionChecker.WinResult winCheck2 = winConditionChecker.checkWinCondition(gameState);
        if (winCheck2 != null && winCheck2.isGameEnded()) {
            log.info("游戏在白天阶段后结束: {}", winCheck2.getReason());
            endGame(gameState, winCheck2);
            result.setWinResult(winCheck2);
            result.setGameEnded(true);
            return result;
        }

        // 回合结束，进入下一回合
        gameState.nextRound();
        log.info("回合 {} 完成，进入回合 {}", result.getRound(), gameState.getCurrentRound());

        result.setGameEnded(false);
        return result;
    }

    /**
     * 执行夜晚阶段
     *
     * @param gameState 游戏状态
     * @param roundActions 回合行动数据
     * @return 夜晚结果
     */
    private NightPhaseEngine.NightResult executeNightPhase(GameState gameState, RoundActions roundActions) {
        // 记录夜晚开始事件
        GameEvent nightStartEvent = GameEvent.builder()
                .type(GameEvent.EventType.NIGHT_START)
                .isPublic(true)
                .description(String.format("第%d夜降临，天黑请闭眼", gameState.getCurrentRound()))
                .timestamp(LocalDateTime.now())
                .build();
        gameState.addEvent(nightStartEvent);

        // 执行夜晚流程
        NightPhaseEngine.NightResult nightResult = nightPhaseEngine.executeNightPhase(
                gameState,
                roundActions.getWerewolfVotes(),
                roundActions.getSeerCheckTarget(),
                roundActions.isWitchUseAntidote(),
                roundActions.getWitchPoisonTarget()
        );

        // 更新昨晚死亡列表
        gameState.setLastNightDeaths(nightResult.getDeaths());

        return nightResult;
    }

    /**
     * 执行白天阶段
     *
     * @param gameState 游戏状态
     * @param nightResult 夜晚结果
     * @param roundActions 回合行动数据
     * @return 白天结果
     */
    private DayPhaseEngine.DayPhaseResult executeDayPhase(
            GameState gameState,
            NightPhaseEngine.NightResult nightResult,
            RoundActions roundActions) {

        // 记录白天开始事件
        GameEvent dayStartEvent = GameEvent.builder()
                .type(GameEvent.EventType.DAY_START)
                .isPublic(true)
                .description(String.format("第%d天到来，天亮请睁眼", gameState.getCurrentRound()))
                .timestamp(LocalDateTime.now())
                .build();
        gameState.addEvent(dayStartEvent);

        // 执行白天流程
        DayPhaseEngine.DayPhaseResult dayResult = dayPhaseEngine.executeDayPhase(
                gameState,
                nightResult.getDeaths(),
                roundActions.getLastWords(),
                roundActions.getSpeeches(),
                roundActions.getVotes()
        );

        return dayResult;
    }

    /**
     * 结束游戏
     *
     * @param gameState 游戏状态
     * @param winResult 胜利结果
     */
    private void endGame(GameState gameState, WinConditionChecker.WinResult winResult) {
        String winningCamp = winResult.getWinningCamp() != null ?
                winResult.getWinningCamp().name() : "NONE";
        gameState.finishGame(winningCamp);

        // 记录游戏结束事件
        GameEvent endEvent = GameEvent.builder()
                .type(GameEvent.EventType.GAME_END)
                .isPublic(true)
                .description(String.format("游戏结束！%s", winResult.getReason()))
                .timestamp(LocalDateTime.now())
                .build();
        gameState.addEvent(endEvent);

        log.info("=== 游戏结束 ===");
        log.info("胜利阵营: {}", winningCamp);
        log.info("游戏时长: {} - {}", gameState.getStartTime(), gameState.getEndTime());
        log.info("总回合数: {}", gameState.getCurrentRound());
    }

    /**
     * 回合行动数据
     * 封装一个回合内所有玩家的行动
     */
    @Data
    @Builder
    public static class RoundActions {
        /**
         * 狼人投票（狼人ID -> 目标ID）
         */
        private Map<Long, Long> werewolfVotes;

        /**
         * 预言家查验目标
         */
        private Long seerCheckTarget;

        /**
         * 女巫是否使用解药
         */
        private boolean witchUseAntidote;

        /**
         * 女巫毒药目标
         */
        private Long witchPoisonTarget;

        /**
         * 遗言（玩家ID -> 遗言内容）
         */
        private Map<Long, String> lastWords;

        /**
         * 白天发言（玩家ID -> 发言内容）
         */
        private Map<Long, String> speeches;

        /**
         * 投票（投票者ID -> 目标ID）
         */
        private Map<Long, Long> votes;
    }

    /**
     * 回合结果
     * 封装一个回合的执行结果
     */
    @Data
    public static class RoundResult {
        /**
         * 回合数
         */
        private int round;

        /**
         * 夜晚结果
         */
        private NightPhaseEngine.NightResult nightResult;

        /**
         * 白天结果
         */
        private DayPhaseEngine.DayPhaseResult dayResult;

        /**
         * 游戏是否结束
         */
        private boolean gameEnded;

        /**
         * 胜利结果（如果游戏结束）
         */
        private WinConditionChecker.WinResult winResult;
    }
}
