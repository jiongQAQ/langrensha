package com.werewolf.domain.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 游戏事件
 * 记录游戏中发生的所有事件
 *
 * @author Claude & User
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameEvent {

    /**
     * 事件ID
     */
    private Long id;

    /**
     * 游戏ID
     */
    private Long gameId;

    /**
     * 事件类型
     */
    private EventType type;

    /**
     * 回合数
     */
    private int round;

    /**
     * 游戏阶段
     */
    private Phase phase;

    /**
     * 相关玩家ID
     */
    private Long playerId;

    /**
     * 目标玩家ID（如果有）
     */
    private Long targetPlayerId;

    /**
     * 事件描述
     */
    private String description;

    /**
     * 事件详细数据（JSON格式）
     */
    private Map<String, Object> data;

    /**
     * 事件发生时间
     */
    private LocalDateTime timestamp;

    /**
     * 是否公开（是否对所有玩家可见）
     */
    private boolean isPublic;

    /**
     * 事件类型枚举
     */
    public enum EventType {
        /**
         * 游戏开始
         */
        GAME_START,

        /**
         * 角色分配
         */
        ROLE_ASSIGNED,

        /**
         * 夜晚开始
         */
        NIGHT_START,

        /**
         * 狼人击杀
         */
        WEREWOLF_KILL,

        /**
         * 预言家查验
         */
        SEER_CHECK,

        /**
         * 女巫使用解药
         */
        WITCH_ANTIDOTE,

        /**
         * 女巫使用毒药
         */
        WITCH_POISON,

        /**
         * 夜晚结束
         */
        NIGHT_END,

        /**
         * 白天开始
         */
        DAY_START,

        /**
         * 公布死讯
         */
        DEATH_ANNOUNCEMENT,

        /**
         * 玩家发言
         */
        PLAYER_SPEECH,

        /**
         * 投票开始
         */
        VOTE_START,

        /**
         * 玩家投票
         */
        PLAYER_VOTE,

        /**
         * 投票结果
         */
        VOTE_RESULT,

        /**
         * 玩家被放逐
         */
        PLAYER_EXILED,

        /**
         * 遗言
         */
        LAST_WORDS,

        /**
         * 游戏结束
         */
        GAME_END,

        /**
         * 系统消息
         */
        SYSTEM_MESSAGE
    }

    /**
     * 创建公开事件
     *
     * @param type 事件类型
     * @param description 描述
     * @return 游戏事件
     */
    public static GameEvent createPublicEvent(EventType type, String description) {
        return GameEvent.builder()
                .type(type)
                .description(description)
                .timestamp(LocalDateTime.now())
                .isPublic(true)
                .build();
    }

    /**
     * 创建私有事件
     *
     * @param type 事件类型
     * @param playerId 玩家ID
     * @param description 描述
     * @return 游戏事件
     */
    public static GameEvent createPrivateEvent(EventType type, Long playerId, String description) {
        return GameEvent.builder()
                .type(type)
                .playerId(playerId)
                .description(description)
                .timestamp(LocalDateTime.now())
                .isPublic(false)
                .build();
    }
}
