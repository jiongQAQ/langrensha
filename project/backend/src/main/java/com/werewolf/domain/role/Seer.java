package com.werewolf.domain.role;

/**
 * 预言家角色
 *
 * @author Claude & User
 */
public class Seer extends BaseRole {

    private final CheckSkill checkSkill;

    public Seer() {
        super(
            RoleType.SEER,
            Camp.GOOD,
            "好人阵营神职，每晚可以查验一名玩家的身份（狼人或好人）。需谨慎透露验人信息。"
        );
        this.checkSkill = new CheckSkill();
        addSkill(checkSkill);
    }

    @Override
    public ActionResult performNightAction(Object context) {
        // 具体的查验逻辑将在游戏引擎中实现
        return ActionResult.builder()
                .success(true)
                .actionType(ActionResult.ActionType.CHECK)
                .message("预言家准备查验目标")
                .build();
    }

    /**
     * 获取查验技能
     *
     * @return 查验技能
     */
    public CheckSkill getCheckSkill() {
        return checkSkill;
    }
}
