package com.werewolf.domain.role;

/**
 * 女巫角色
 *
 * @author Claude & User
 */
public class Witch extends BaseRole {

    private final AntidoteSkill antidoteSkill;
    private final PoisonSkill poisonSkill;
    private boolean firstNight = true;

    public Witch() {
        super(
            RoleType.WITCH,
            Camp.GOOD,
            "好人阵营神职，拥有解药（救人）和毒药（杀人）各一次。解药和毒药不能在同一夜使用，首夜不能自救。"
        );
        this.antidoteSkill = new AntidoteSkill();
        this.poisonSkill = new PoisonSkill();
        addSkill(antidoteSkill);
        addSkill(poisonSkill);
    }

    @Override
    public ActionResult performNightAction(Object context) {
        // 具体的药品使用逻辑将在游戏引擎中实现
        return ActionResult.builder()
                .success(true)
                .actionType(ActionResult.ActionType.ANTIDOTE)
                .message("女巫准备使用药品")
                .build();
    }

    /**
     * 获取解药技能
     *
     * @return 解药技能
     */
    public AntidoteSkill getAntidoteSkill() {
        return antidoteSkill;
    }

    /**
     * 获取毒药技能
     *
     * @return 毒药技能
     */
    public PoisonSkill getPoisonSkill() {
        return poisonSkill;
    }

    /**
     * 检查是否是首夜
     *
     * @return true表示是首夜
     */
    public boolean isFirstNight() {
        return firstNight;
    }

    /**
     * 标记首夜已过
     */
    public void markFirstNightPassed() {
        this.firstNight = false;
    }

    /**
     * 检查解药和毒药是否可以在同一夜使用
     *
     * @return false表示不能同时使用
     */
    public boolean canUseBothPotionsInSameNight() {
        return false;
    }
}
