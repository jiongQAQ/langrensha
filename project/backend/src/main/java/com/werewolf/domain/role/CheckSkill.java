package com.werewolf.domain.role;

/**
 * 预言家查验技能
 *
 * @author Claude & User
 */
public class CheckSkill extends BaseSkill {

    public CheckSkill() {
        super("查验", "每晚选择一名玩家查验身份（返回狼人或好人）", -1);
    }
}
