package com.werewolf.domain.role;

import lombok.Data;
import lombok.Builder;

/**
 * 行动结果
 * 封装角色夜晚行动的结果
 *
 * @author Claude & User
 */
@Data
@Builder
public class ActionResult {

    /**
     * 行动是否成功
     */
    private boolean success;

    /**
     * 目标玩家ID（如果有）
     */
    private Long targetPlayerId;

    /**
     * 行动类型（如：击杀、查验、解药、毒药）
     */
    private ActionType actionType;

    /**
     * 行动结果描述
     */
    private String message;

    /**
     * 额外数据（如查验结果等）
     */
    private Object data;

    /**
     * 行动类型枚举
     */
    public enum ActionType {
        /**
         * 狼人击杀
         */
        KILL,

        /**
         * 预言家查验
         */
        CHECK,

        /**
         * 女巫解药
         */
        ANTIDOTE,

        /**
         * 女巫毒药
         */
        POISON,

        /**
         * 无行动
         */
        NONE
    }
}
