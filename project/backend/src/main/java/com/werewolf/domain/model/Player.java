package com.werewolf.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.werewolf.domain.role.Role;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 玩家实体
 *
 * @author Claude & User
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Player {

    /**
     * 玩家ID
     */
    private Long id;

    /**
     * 玩家名称
     */
    private String name;

    /**
     * 玩家类型
     */
    private PlayerType type;

    /**
     * 角色
     */
    private Role role;

    /**
     * 是否存活
     */
    private boolean alive;

    /**
     * 座位号（1-6）
     */
    private int seatNumber;

    /**
     * 是否已发言
     */
    private boolean hasSpoken;

    /**
     * 是否已投票
     */
    private boolean hasVoted;

    /**
     * 投票目标玩家ID
     */
    private Long voteTargetId;

    /**
     * 加入时间
     */
    private LocalDateTime joinTime;

    /**
     * 死亡时间
     */
    private LocalDateTime deathTime;

    /**
     * 死亡原因
     */
    private DeathReason deathReason;

    /**
     * 玩家类型枚举
     */
    public enum PlayerType {
        /**
         * 人类玩家
         */
        HUMAN,

        /**
         * AI玩家
         */
        AI
    }

    /**
     * 死亡原因枚举
     */
    public enum DeathReason {
        /**
         * 被狼人击杀
         */
        KILLED_BY_WEREWOLF,

        /**
         * 被女巫毒死
         */
        POISONED,

        /**
         * 被投票放逐
         */
        VOTED_OUT,

        /**
         * 未知
         */
        UNKNOWN
    }

    /**
     * 检查玩家是否是AI
     *
     * @return true表示是AI
     */
    public boolean isAI() {
        return type == PlayerType.AI;
    }

    /**
     * 检查玩家是否是人类
     *
     * @return true表示是人类
     */
    public boolean isHuman() {
        return type == PlayerType.HUMAN;
    }

    /**
     * 标记玩家死亡
     *
     * @param reason 死亡原因
     */
    public void markDead(DeathReason reason) {
        this.alive = false;
        this.deathTime = LocalDateTime.now();
        this.deathReason = reason;
    }

    /**
     * 复活玩家（女巫解药）
     */
    public void revive() {
        this.alive = true;
        this.deathTime = null;
        this.deathReason = null;
    }

    /**
     * 重置发言状态
     */
    public void resetSpeechStatus() {
        this.hasSpoken = false;
    }

    /**
     * 重置投票状态
     */
    public void resetVoteStatus() {
        this.hasVoted = false;
        this.voteTargetId = null;
    }
}
