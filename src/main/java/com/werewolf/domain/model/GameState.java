package com.werewolf.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 游戏状态
 * 表示完整的游戏状态信息
 *
 * @author Claude & User
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GameState {

    /**
     * 游戏ID
     */
    private Long gameId;

    /**
     * 房间ID
     */
    private Long roomId;

    /**
     * 当前阶段
     */
    private Phase currentPhase;

    /**
     * 当前回合数（从1开始）
     */
    private int currentRound;

    /**
     * 玩家列表
     */
    private List<Player> players;

    /**
     * 游戏事件历史
     */
    @Builder.Default
    private List<GameEvent> events = new ArrayList<>();

    /**
     * 游戏开始时间
     */
    private LocalDateTime startTime;

    /**
     * 游戏结束时间
     */
    private LocalDateTime endTime;

    /**
     * 游戏状态
     */
    private GameStatus status;

    /**
     * 胜利阵营
     */
    private String winningCamp;

    /**
     * 昨晚死亡的玩家ID列表
     */
    @Builder.Default
    private List<Long> lastNightDeaths = new ArrayList<>();

    /**
     * 当前发言顺序索引
     */
    private int currentSpeakerIndex;

    /**
     * 额外数据（用于存储临时状态）
     */
    private Map<String, Object> metadata;

    /**
     * 游戏状态枚举
     */
    public enum GameStatus {
        /**
         * 等待中
         */
        WAITING,

        /**
         * 进行中
         */
        RUNNING,

        /**
         * 已暂停
         */
        PAUSED,

        /**
         * 已结束
         */
        FINISHED
    }

    /**
     * 获取存活玩家列表
     *
     * @return 存活玩家列表
     */
    public List<Player> getAlivePlayers() {
        return players.stream()
                .filter(Player::isAlive)
                .collect(Collectors.toList());
    }

    /**
     * 获取死亡玩家列表
     *
     * @return 死亡玩家列表
     */
    public List<Player> getDeadPlayers() {
        return players.stream()
                .filter(player -> !player.isAlive())
                .collect(Collectors.toList());
    }

    /**
     * 根据ID获取玩家
     *
     * @param playerId 玩家ID
     * @return 玩家对象
     */
    public Player getPlayerById(Long playerId) {
        return players.stream()
                .filter(player -> player.getId().equals(playerId))
                .findFirst()
                .orElse(null);
    }

    /**
     * 根据座位号获取玩家
     *
     * @param seatNumber 座位号
     * @return 玩家对象
     */
    public Player getPlayerBySeat(int seatNumber) {
        return players.stream()
                .filter(player -> player.getSeatNumber() == seatNumber)
                .findFirst()
                .orElse(null);
    }

    /**
     * 添加游戏事件
     *
     * @param event 游戏事件
     */
    public void addEvent(GameEvent event) {
        event.setGameId(this.gameId);
        event.setRound(this.currentRound);
        event.setPhase(this.currentPhase);
        this.events.add(event);
    }

    /**
     * 进入下一回合
     */
    public void nextRound() {
        this.currentRound++;
        this.lastNightDeaths.clear();
        // 重置所有玩家的发言和投票状态
        players.forEach(player -> {
            player.resetSpeechStatus();
            player.resetVoteStatus();
        });
    }

    /**
     * 切换到指定阶段
     *
     * @param phase 目标阶段
     */
    public void changePhase(Phase phase) {
        this.currentPhase = phase;
    }

    /**
     * 检查游戏是否已结束
     *
     * @return true表示已结束
     */
    public boolean isFinished() {
        return status == GameStatus.FINISHED;
    }

    /**
     * 结束游戏
     *
     * @param winningCamp 胜利阵营
     */
    public void finishGame(String winningCamp) {
        this.status = GameStatus.FINISHED;
        this.currentPhase = Phase.FINISHED;
        this.endTime = LocalDateTime.now();
        this.winningCamp = winningCamp;
    }

    /**
     * 获取指定阶段的事件列表
     *
     * @param phase 阶段
     * @return 事件列表
     */
    public List<GameEvent> getEventsByPhase(Phase phase) {
        return events.stream()
                .filter(event -> event.getPhase() == phase)
                .collect(Collectors.toList());
    }

    /**
     * 获取指定回合的事件列表
     *
     * @param round 回合数
     * @return 事件列表
     */
    public List<GameEvent> getEventsByRound(int round) {
        return events.stream()
                .filter(event -> event.getRound() == round)
                .collect(Collectors.toList());
    }
}
