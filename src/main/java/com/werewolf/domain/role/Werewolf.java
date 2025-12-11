package com.werewolf.domain.role;

/**
 * 狼人角色
 *
 * @author Claude & User
 */
public class Werewolf extends BaseRole {

    private final KillSkill killSkill;

    public Werewolf() {
        super(
            RoleType.WEREWOLF,
            Camp.WEREWOLF,
            "狼人阵营成员，每晚可以与队友协商击杀一名玩家。白天需要伪装成好人身份。"
        );
        this.killSkill = new KillSkill();
        addSkill(killSkill);
    }

    @Override
    public ActionResult performNightAction(Object context) {
        // 具体的击杀逻辑将在游戏引擎中实现
        // 这里只是标记角色有夜晚行动
        return ActionResult.builder()
                .success(true)
                .actionType(ActionResult.ActionType.KILL)
                .message("狼人准备击杀目标")
                .build();
    }

    /**
     * 获取击杀技能
     *
     * @return 击杀技能
     */
    public KillSkill getKillSkill() {
        return killSkill;
    }
}
