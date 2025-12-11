package com.werewolf.domain.role;

/**
 * 女巫解药技能
 *
 * @author Claude & User
 */
public class AntidoteSkill extends BaseSkill {

    public AntidoteSkill() {
        super("解药", "可救活当晚被狼人杀死的玩家（全局仅1次）", 1);
    }
}
