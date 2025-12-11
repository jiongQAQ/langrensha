package com.werewolf.domain.role;

/**
 * 女巫毒药技能
 *
 * @author Claude & User
 */
public class PoisonSkill extends BaseSkill {

    public PoisonSkill() {
        super("毒药", "可毒死任意一名玩家（全局仅1次）", 1);
    }
}
