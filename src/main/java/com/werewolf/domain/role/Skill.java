package com.werewolf.domain.role;

/**
 * 技能接口
 * 定义角色可以执行的技能行为
 *
 * @author Claude & User
 */
public interface Skill {

    /**
     * 获取技能名称
     *
     * @return 技能名称
     */
    String getName();

    /**
     * 获取技能描述
     *
     * @return 技能描述
     */
    String getDescription();

    /**
     * 获取技能使用限制（-1表示无限制）
     *
     * @return 可使用次数
     */
    int getUsageLimit();

    /**
     * 获取剩余使用次数
     *
     * @return 剩余次数
     */
    int getRemainingUses();

    /**
     * 检查技能是否可用
     *
     * @return true表示可用
     */
    boolean isAvailable();

    /**
     * 使用技能
     */
    void use();

    /**
     * 重置技能使用次数（新游戏开始时）
     */
    void reset();
}
