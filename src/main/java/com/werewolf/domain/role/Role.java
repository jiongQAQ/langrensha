package com.werewolf.domain.role;

import java.util.List;

/**
 * 角色接口
 * 定义所有角色的共同行为
 *
 * @author Claude & User
 */
public interface Role {

    /**
     * 获取角色类型
     *
     * @return 角色类型
     */
    RoleType getRoleType();

    /**
     * 获取角色名称
     *
     * @return 角色名称
     */
    String getRoleName();

    /**
     * 获取角色所属阵营
     *
     * @return 阵营
     */
    Camp getCamp();

    /**
     * 获取角色描述
     *
     * @return 角色描述
     */
    String getDescription();

    /**
     * 获取角色拥有的技能列表
     *
     * @return 技能列表
     */
    List<Skill> getSkills();

    /**
     * 检查角色是否有夜晚行动
     *
     * @return true表示有夜晚行动
     */
    boolean hasNightAction();

    /**
     * 检查角色是否是神职（预言家、女巫）
     *
     * @return true表示是神职
     */
    boolean isDivine();

    /**
     * 执行夜晚行动
     * 子类需要实现具体的夜晚行动逻辑
     *
     * @param context 游戏上下文（包含当前游戏状态、其他玩家信息等）
     * @return 行动结果
     */
    ActionResult performNightAction(Object context);
}
